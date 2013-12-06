package com.bitsofproof.btc1k.server;

import com.bitsofproof.supernode.api.*;
import com.bitsofproof.supernode.common.ECKeyPair;
import com.bitsofproof.supernode.common.Hash;
import com.bitsofproof.supernode.common.ValidationException;
import com.bitsofproof.supernode.testbox.APIServerInABox;
import com.bitsofproof.supernode.wallet.AccountListener;
import com.bitsofproof.supernode.wallet.AccountManager;
import com.bitsofproof.supernode.wallet.AddressListAccountManager;
import com.bitsofproof.supernode.wallet.KeyListAccountManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Helper
{
	private static final Logger log = LoggerFactory.getLogger(Helper.class);

	public static final long MINIMUM_FEE = 10000;

	private final Address vaultAddress;

	private final APIServerInABox box;

	private final ECKeyPair miner;

	private int blockHeight = 1;

	private String lastHash;

	public Helper (APIServerInABox box, Address address)
	{
		this.box = box;
		this.vaultAddress = address;
		this.miner = ECKeyPair.createNew (true);
	}

	public void fundVault(BigDecimal coin) throws ValidationException, BCSAPIException
	{
		log.info ("Founding vault with {}", coin);

		BCSAPI api = box.getAPI ();

		final long satoshi = coin.movePointRight (8).longValue ();

		final Transaction mockTransaction =
				Transaction.createCoinbase (miner.getAddress (), satoshi + MINIMUM_FEE, blockHeight++);
		if ( lastHash == null )
		{
			lastHash = api.getBlockHeader (Hash.ZERO_HASH_STRING).getHash ();
		}

		final Semaphore blockMined = new Semaphore (0);
		TrunkListener tl;
		api.registerTrunkListener (tl = new TrunkListener ()
		{
			@Override
			public void trunkUpdate (List<Block> removed, List<Block> added)
			{
				blockMined.release ();
			}
		});
		log.info("Mining a block");
		synchronized ( lastHash )
		{
			Block b = box.createBlock (lastHash, mockTransaction);
			box.mineBlock (b);
			lastHash = b.getHash ();

			api.sendBlock (b);
		}
		blockMined.acquireUninterruptibly ();

		final Semaphore paymentDone = new Semaphore (0);
		KeyListAccountManager source = new KeyListAccountManager ();
		source.addKey (miner);
		source.sync (api);

		AddressListAccountManager target = new AddressListAccountManager ();
		target.addAddress (vaultAddress);
		api.registerTransactionListener (target);
		target.addAccountListener (new AccountListener ()
		{
			@Override
			public void accountChanged (AccountManager account, Transaction t)
			{
				long c = account.getConfirmed ();
				if ( c == satoshi )
				{
					paymentDone.release ();
				}
			}
		});
		log.info("Sending fund payment");
		Transaction payment = source.pay (vaultAddress, satoshi, true);
		api.sendTransaction (payment);
		synchronized ( lastHash )
		{
			Block b = box.createBlock (lastHash, Transaction.createCoinbase (miner.getAddress (), satoshi, blockHeight++));
			b.getTransactions ().add (payment);
			box.mineBlock (b);
			lastHash = b.getHash ();

			api.sendBlock (b);
		}
		blockMined.acquireUninterruptibly ();
		paymentDone.acquireUninterruptibly ();
		api.removeTransactionListener (source);
		api.removeTrunkListener (tl);

		log.info("Vault funded");
		//Thread.sleep (1000);
	}

}
