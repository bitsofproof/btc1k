package com.bitsofproof.btc1k.server.vault;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bitsofproof.btc1k.server.resource.NamedKey;
import com.bitsofproof.supernode.api.Address;
import com.bitsofproof.supernode.api.BCSAPI;
import com.bitsofproof.supernode.api.BCSAPIException;
import com.bitsofproof.supernode.api.Transaction;
import com.bitsofproof.supernode.api.TransactionInput;
import com.bitsofproof.supernode.api.TransactionOutput;
import com.bitsofproof.supernode.common.ECKeyPair;
import com.bitsofproof.supernode.common.ECPublicKey;
import com.bitsofproof.supernode.common.Hash;
import com.bitsofproof.supernode.common.Key;
import com.bitsofproof.supernode.common.ScriptFormat;
import com.bitsofproof.supernode.common.ScriptFormat.Token;
import com.bitsofproof.supernode.common.ValidationException;
import com.bitsofproof.supernode.wallet.AccountListener;
import com.bitsofproof.supernode.wallet.AccountManager;
import com.bitsofproof.supernode.wallet.AddressListAccountManager;
import com.bitsofproof.supernode.wallet.BaseAccountManager;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

public class Vault
{
	private static final Logger log = LoggerFactory.getLogger (Vault.class);

	public class AM extends AddressListAccountManager
	{
		@Override
		protected void spendNonAddressOutput (int ix, TransactionOutput source, ScriptFormat.Writer sw, Transaction transaction) throws ValidationException
		{
			sw.writeData (new byte[0]);
			for ( int i = 0; i < publicKeys.size (); ++i )
			{
				sw.writeData (new byte[0]);
			}
			sw.writeData (getCustomerScript ());
		}

		@Override
		public Address getNextAddress () throws ValidationException
		{
			return getVaultAddress ();
		}

		@Override
		public boolean updateWithTransaction (Transaction t)
		{
			if ( super.updateWithTransaction (t) )
			{
				log.info ("Vault updated with transaction " + t.getHash ());
				return true;
			}
			return false;
		}

	}

	private final TreeMap<String, ECPublicKey> publicKeys = new TreeMap<> ();

	private final AM accountManager;

	private final Map<UUID, PendingTransaction> pendingTransactions = Maps.newConcurrentMap ();

	private byte[] getCustomerScript () throws ValidationException
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
		return new Address (Address.Type.P2SH, Hash.keyHash (getCustomerScript ()));
	}

	public Vault (Map<String, String> keys) throws BCSAPIException, ValidationException
	{
		for ( Map.Entry<String, String> keyEntry : keys.entrySet () )
		{
			publicKeys.put (keyEntry.getKey (), new ECPublicKey (ByteUtils.fromHexString (keyEntry.getValue ()), true));
		}
		log.info ("Vault address: " + getVaultAddress ());
		this.accountManager = new AM ();
		accountManager.addAddress (getVaultAddress ());
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
		Transaction tx = accountManager.pay (targetAddress, toSatoshi (btcAmount), true);
		PendingTransaction pendingTransaction = new PendingTransaction (tx, btcAmount, targetAddress, "");

		pendingTransactions.put (pendingTransaction.getId (), pendingTransaction);
		log.info ("Created transaction to pay " + btcAmount + " BTC to " + targetAddress);
		return pendingTransaction;
	}

	public void sign (Transaction transaction, String passphrase) throws ValidationException
	{
		try
		{
			ECKeyPair key = new ECKeyPair (new BigInteger (1, Hash.sha256 (passphrase.getBytes ("UTF-8"))), true);
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

			System.out.println (transaction.toWireDump ());
			log.info ("Signing with " + name);
			int nsignatures = 0;
			for ( TransactionInput input : transaction.getInputs () )
			{
				List<Token> tokens = ScriptFormat.parse (input.getScript ());

				byte[] sig =
						key.sign (BaseAccountManager.hashTransaction (transaction, i++, ScriptFormat.SIGHASH_ALL, getCustomerScript ()));
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
			if ( nsignatures >= 2 )
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
		catch ( UnsupportedEncodingException e )
		{
		}
	}

	List<String> getSignedBy (Transaction transaction) throws ValidationException
	{
		List<String> names = new ArrayList<> ();
		for ( TransactionInput input : transaction.getInputs () )
		{
			List<Token> tokens = ScriptFormat.parse (input.getScript ());
			Iterator<String> it = publicKeys.keySet ().iterator ();
			for ( int i = 1; i < tokens.size () - 1; ++i )
			{
				if ( tokens.get (i).data != null )
				{
					String name = it.next ();
					if ( tokens.get (i).op != ScriptFormat.Opcode.OP_FALSE )
					{
						names.add (name);
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
