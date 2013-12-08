package com.bitsofproof.btc1k.server;

import java.math.BigDecimal;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bitsofproof.supernode.api.Address;
import com.bitsofproof.supernode.api.BCSAPI;
import com.bitsofproof.supernode.api.BCSAPIException;
import com.bitsofproof.supernode.api.Block;
import com.bitsofproof.supernode.api.Transaction;
import com.bitsofproof.supernode.common.ECKeyPair;
import com.bitsofproof.supernode.common.Hash;
import com.bitsofproof.supernode.common.ValidationException;
import com.bitsofproof.supernode.testbox.APIServerInABox;
import com.bitsofproof.supernode.wallet.AccountListener;
import com.bitsofproof.supernode.wallet.AccountManager;
import com.bitsofproof.supernode.wallet.AddressListAccountManager;
import com.bitsofproof.supernode.wallet.KeyListAccountManager;

public class Helper
{
	private static final Logger log = LoggerFactory.getLogger (Helper.class);

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

	public void fundVault (BigDecimal coin) throws ValidationException, BCSAPIException
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

		final Semaphore miningDone = new Semaphore (0);
		KeyListAccountManager source = new KeyListAccountManager ();
		source.addKey (miner);
		api.registerTransactionListener (source);
		source.addAccountListener (new AccountListener ()
		{
			@Override
			public void accountChanged (AccountManager account, Transaction t)
			{
				miningDone.release ();
			}
		});

		log.info ("Mining a block");
		Block b = box.createBlock (lastHash, mockTransaction);
		box.mineBlock (b);
		lastHash = b.getHash ();
		api.sendBlock (b);
		miningDone.acquireUninterruptibly ();
		api.removeTransactionListener (source);

		final Semaphore paymentDone = new Semaphore (0);

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
		log.info ("Sending fund payment");
		Transaction payment = source.pay (vaultAddress, satoshi, true);
		api.sendTransaction (payment);
		b = box.createBlock (lastHash, Transaction.createCoinbase (miner.getAddress (), satoshi, blockHeight++));
		b.getTransactions ().add (payment);
		box.mineBlock (b);
		lastHash = b.getHash ();
		api.sendBlock (b);

		paymentDone.acquireUninterruptibly ();
		api.removeTransactionListener (target);

		log.info ("Vault funded");
	}

}
