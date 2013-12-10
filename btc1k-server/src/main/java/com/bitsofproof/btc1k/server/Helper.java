/*
 * Copyright 2013 bits of proof zrt.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bitsofproof.btc1k.server;

import java.math.BigDecimal;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bitsofproof.btc1k.server.vault.Vault;
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
import com.bitsofproof.supernode.wallet.KeyListAccountManager;

public class Helper
{
	private static final Logger log = LoggerFactory.getLogger (Helper.class);

	public static final long MINIMUM_FEE = 10000;

	private final Vault vault;

	private final APIServerInABox box;

	private final ECKeyPair miner;

	private int blockHeight = 1;

	private String lastHash;

	public Helper (APIServerInABox box, Vault vault)
	{
		this.box = box;
		this.vault = vault;
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

		vault.getAccountManager ().addAccountListener (new AccountListener ()
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
		Transaction payment = source.pay (vault.getVaultAddress (), satoshi, true);
		api.sendTransaction (payment);
		b = box.createBlock (lastHash, Transaction.createCoinbase (miner.getAddress (), satoshi, blockHeight++));
		b.getTransactions ().add (payment);
		box.mineBlock (b);
		lastHash = b.getHash ();
		api.sendBlock (b);

		paymentDone.acquireUninterruptibly ();

		log.info ("Vault funded");
	}

}
