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
package com.bitsofproof.btc1k.server.vault;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bitsofproof.btc1k.server.resource.NamedKey;
import com.bitsofproof.supernode.account.AccountListener;
import com.bitsofproof.supernode.account.AccountManager;
import com.bitsofproof.supernode.account.BaseTransactionFactory;
import com.bitsofproof.supernode.account.PaymentOptions;
import com.bitsofproof.supernode.account.TransactionSource;
import com.bitsofproof.supernode.api.Address;
import com.bitsofproof.supernode.api.BCSAPI;
import com.bitsofproof.supernode.api.BCSAPIException;
import com.bitsofproof.supernode.api.Transaction;
import com.bitsofproof.supernode.api.TransactionInput;
import com.bitsofproof.supernode.api.TransactionListener;
import com.bitsofproof.supernode.api.TransactionOutput;
import com.bitsofproof.supernode.common.ECKeyPair;
import com.bitsofproof.supernode.common.ECPublicKey;
import com.bitsofproof.supernode.common.ExtendedKey;
import com.bitsofproof.supernode.common.Hash;
import com.bitsofproof.supernode.common.Key;
import com.bitsofproof.supernode.common.ScriptFormat;
import com.bitsofproof.supernode.common.ScriptFormat.Token;
import com.bitsofproof.supernode.common.ValidationException;
import com.bitsofproof.supernode.misc.BIP39;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

public class Vault
{
	private static final Logger log = LoggerFactory.getLogger (Vault.class);

	public class AM extends BaseTransactionFactory
	{
		@Override
		protected TransactionSource createTransactionSource (TransactionOutput output)
		{
			return new TransactionSource (output, this)
			{
				@Override
				protected byte[] spend (int ix, Transaction transaction) throws ValidationException
				{
					ScriptFormat.Writer sw = new ScriptFormat.Writer ();
					sw.writeData (new byte[0]);
					for ( int i = 0; i < publicKeys.size (); ++i )
					{
						sw.writeData (new byte[0]);
					}
					sw.writeData (getVaultScript ());
					return sw.toByteArray ();
				}
			};
		}

		@Override
		public boolean updateWithTransaction (Transaction t)
		{
			if ( super.updateWithTransaction (t) )
			{
				log.info ("Vault updated with transaction " + t.getHash () + " balance " + getBalance ());
				return true;
			}
			return false;
		}

		@Override
		public Key getKeyForAddress (Address address)
		{
			return null;
		}

		@Override
		public void sync (BCSAPI api) throws BCSAPIException
		{
			reset ();
			api.scanUTXOForAddresses (getAddresses (), new TransactionListener ()
			{
				@Override
				public boolean process (Transaction t)
				{
					return updateWithTransaction (t);
				}
			});
		}

		@Override
		public void syncHistory (BCSAPI api) throws BCSAPIException
		{
			reset ();
			api.scanTransactionsForAddresses (getAddresses (), getCreated (), new TransactionListener ()
			{
				@Override
				public boolean process (Transaction t)
				{
					return updateWithTransaction (t);
				}
			});
		}

		@Override
		public boolean isOwnAddress (Address address)
		{
			try
			{
				return address.equals (getVaultAddress ());
			}
			catch ( ValidationException e )
			{
				return false;
			}
		}

		@Override
		public Set<Address> getAddresses ()
		{
			Set<Address> as = new HashSet<Address> ();
			try
			{
				as.add (getVaultAddress ());
			}
			catch ( ValidationException e )
			{
			}
			return as;
		}

		@Override
		public Address getNextChangeAddress () throws ValidationException
		{
			return getVaultAddress ();
		}

		@Override
		public Address getNextReceiverAddress () throws ValidationException
		{
			return getVaultAddress ();
		}

	}

	private final TreeMap<String, ECPublicKey> publicKeys = new TreeMap<> ();

	private final AM accountManager;

	private final Map<UUID, PendingTransaction> pendingTransactions = Maps.newConcurrentMap ();

	private byte[] getVaultScript () throws ValidationException
	{
		ScriptFormat.Writer writer = new ScriptFormat.Writer ();
		writer.writeToken (new ScriptFormat.Token (ScriptFormat.Opcode.OP_2));
		for ( Key key : publicKeys.values () )
		{
			writer.writeData (key.getPublic ());
		}
		writer.writeToken (new ScriptFormat.Token (ScriptFormat.Opcode.OP_3));
		writer.writeToken (new ScriptFormat.Token (ScriptFormat.Opcode.OP_CHECKMULTISIG));
		return writer.toByteArray ();
	}

	public Address getVaultAddress () throws ValidationException
	{
		return new Address (Address.Type.P2SH, Hash.keyHash (getVaultScript ()));
	}

	public Vault (Map<String, String> keys) throws BCSAPIException, ValidationException
	{
		for ( Map.Entry<String, String> keyEntry : keys.entrySet () )
		{
			publicKeys.put (keyEntry.getKey (), new ECPublicKey (ByteUtils.fromHexString (keyEntry.getValue ()), true));
		}
		log.info ("Vault address: " + getVaultAddress ());
		this.accountManager = new AM ();
		accountManager.setCreated (new DateTime (2013, 12, 1, 0, 0).getMillis ());
		accountManager.addAccountListener (new AccountListener ()
		{
			@Override
			public void accountChanged (AccountManager account, Transaction t)
			{
				log.info ("New account balance " + fromSatoshi (account.getBalance ()) + " " +
						fromSatoshi (account.getConfirmed ()) + " confrirmed " +
						fromSatoshi (account.getChange ()) + " change " +
						fromSatoshi (account.getReceiving ()) + " receiving");
			}
		});
	}

	private static long toSatoshi (BigDecimal btc)
	{
		return btc.movePointRight (8).longValue ();
	}

	private BigDecimal fromSatoshi (long amount)
	{
		return BigDecimal.valueOf (amount).movePointLeft (8);
	}

	public PendingTransaction createTransaction (Address targetAddress, BigDecimal btcAmount) throws ValidationException
	{
		log.info ("Create transaction to pay " + btcAmount + " BTC to " + targetAddress + " vault has " + fromSatoshi (accountManager.getBalance ()));
		Transaction tx = accountManager.pay (targetAddress, toSatoshi (btcAmount), PaymentOptions.receiverPaysFee);
		PendingTransaction pendingTransaction = new PendingTransaction (tx, btcAmount, targetAddress, "");
		return pendingTransaction;
	}

	public static class RandomKey
	{
		private final String mnemonic;
		private final String publicKey;

		public RandomKey (String mnemonic, String publicKey)
		{
			this.mnemonic = mnemonic;
			this.publicKey = publicKey;
		}

		public String getMnemonic ()
		{
			return mnemonic;
		}

		public String getPublicKey ()
		{
			return publicKey;
		}
	}

	private final SecureRandom random = new SecureRandom ();

	public RandomKey generateRandomKey () throws ValidationException
	{
		byte[] entropy = new byte[16];
		random.nextBytes (entropy);

		return new RandomKey (BIP39.encode (entropy, ""),
				ByteUtils.toHexString (ExtendedKey.create (entropy).getKey (0).getPublic ()));
	}

	public void sign (PendingTransaction pendingTransaction, String mnemonic) throws ValidationException
	{
		ECKeyPair key = (ECKeyPair) ExtendedKey.create (BIP39.decode (mnemonic, "")).getKey (0);
		String name = null;
		for ( Map.Entry<String, ECPublicKey> e : publicKeys.entrySet () )
		{
			if ( Arrays.equals (e.getValue ().getPublic (), key.getPublic ()) )
			{
				name = e.getKey ();
			}
		}
		if ( name == null )
		{
			throw new ValidationException ("Not a known key");
		}

		int slot = publicKeys.headMap (name).size () + 1;
		int i = 0;

		Transaction transaction = pendingTransaction.getTransaction ();
		long amount = 0;
		for ( TransactionOutput out : transaction.getOutputs () )
		{
			if ( !out.getOutputAddress ().equals (pendingTransaction.getTargetAddress ()) &&
					!out.getOutputAddress ().equals (getVaultAddress ()) )
			{
				throw new ValidationException ("Transaction address does not match the initiated");
			}
			if ( out.getOutputAddress ().equals (pendingTransaction.getTargetAddress ()) )
			{
				amount += out.getValue ();
			}
		}
		if ( BigDecimal.valueOf (amount).movePointLeft (8).compareTo (pendingTransaction.getAmount ()) != 0 )
		{
			throw new ValidationException ("Transaction amount not match the initiated");
		}

		log.info ("Signing with " + name);
		int nsignatures = 0;
		for ( TransactionInput input : transaction.getInputs () )
		{
			List<Token> tokens = ScriptFormat.parse (input.getScript ());

			byte[] sig =
					key.sign (transaction.hashTransaction (i++, ScriptFormat.SIGHASH_ALL, getVaultScript ()));
			byte[] sigPlusType = new byte[sig.length + 1];
			System.arraycopy (sig, 0, sigPlusType, 0, sig.length);
			sigPlusType[sigPlusType.length - 1] = (byte) (ScriptFormat.SIGHASH_ALL & 0xff);
			tokens.get (slot).op = ScriptFormat.Opcode.values ()[sigPlusType.length];
			tokens.get (slot).data = sigPlusType;

			ScriptFormat.Writer writer = new ScriptFormat.Writer ();
			int pos = 0;
			for ( Token t : tokens )
			{
				writer.writeToken (t);
				if ( pos > 0 && pos < 4 && t.op != ScriptFormat.Opcode.OP_FALSE )
				{
					++nsignatures;
				}
				++pos;
			}
			input.setScript (writer.toByteArray ());
		}
		if ( nsignatures >= 2 * transaction.getInputs ().size () )
		{
			for ( TransactionInput input : transaction.getInputs () )
			{
				List<Token> tokens = ScriptFormat.parse (input.getScript ());
				Iterator<Token> ti = tokens.iterator ();
				ti.next ();
				while ( ti.hasNext () )
				{
					Token t = ti.next ();
					if ( t.op == ScriptFormat.Opcode.OP_FALSE )
					{
						ti.remove ();
					}
				}
				ScriptFormat.Writer writer = new ScriptFormat.Writer ();
				for ( Token t : tokens )
				{
					writer.writeToken (t);
				}
				input.setScript (writer.toByteArray ());
			}
		}
		transaction.computeHash ();
		log.info ("Transaction hash after sign " + transaction.getHash ());
	}

	List<String> getSignedBy (Transaction transaction) throws ValidationException
	{
		List<String> names = new ArrayList<> ();
		for ( TransactionInput input : transaction.getInputs () )
		{
			List<Token> tokens = ScriptFormat.parse (input.getScript ());
			Iterator<Map.Entry<String, ECPublicKey>> it = publicKeys.entrySet ().iterator ();
			for ( int i = 1; i < tokens.size () - 1; ++i )
			{
				if ( tokens.get (i).data != null )
				{
					Map.Entry<String, ECPublicKey> e = it.next ();
					String name = e.getKey ();
					Key k = e.getValue ();
					if ( tokens.get (i).op != ScriptFormat.Opcode.OP_FALSE )
					{
						byte[] digest = transaction.hashTransaction (0, ScriptFormat.SIGHASH_ALL, getVaultScript ());
						if ( k.verify (digest, tokens.get (i).data) )
						{
							names.add (name);
						}
					}
				}
			}
			break;
		}
		return names;
	}

	public PendingTransaction getPendingTransaction (UUID id)
	{
		return pendingTransactions.get (id);
	}

	public List<PendingTransaction> getAllPendingTransactions ()
	{
		return Ordering.natural ().reverse ().sortedCopy (pendingTransactions.values ());
	}

	public long getBalance ()
	{
		return accountManager.getBalance ();
	}

	public AM getAccountManager ()
	{
		return accountManager;
	}

	public void updateTransaction (BCSAPI api, PendingTransaction transaction) throws BCSAPIException, ValidationException
	{
		transaction.setSignedBy (getSignedBy (transaction.getTransaction ()));
		if ( transaction.getSignedBy ().size () > 0 )
		{
			pendingTransactions.put (transaction.getId (), transaction);
			try
			{
				transaction.getTransaction ().computeHash ();
				log.info ("Updated " + transaction.getId () + " to " + transaction.getTransaction ().getHash ());
				if ( getSignedBy (transaction.getTransaction ()).size () >= 2 )
				{
					api.sendTransaction (transaction.getTransaction ());
					log.info ("Successfully sent " + transaction.getTransaction ().getHash ());
					log.info ("transaction: " + transaction.getTransaction ().toWireDump ());
					pendingTransactions.remove (transaction.getId ());
				}
			}
			catch ( BCSAPIException e )
			{
				log.info ("Transaction rejected " + transaction.getTransaction ().getHash ());
				throw e;
			}
		}
	}

	public PendingTransaction deletePendingTransaction (UUID id)
	{
		return pendingTransactions.remove (id);
	}

	public List<NamedKey> getKeys ()
	{
		List<NamedKey> keys = new ArrayList<> ();
		for ( Map.Entry<String, ECPublicKey> e : publicKeys.entrySet () )
		{
			keys.add (new NamedKey (e.getKey (), ByteUtils.toHexString (e.getValue ().getPublic ())));
		}
		return keys;
	}
}
