package com.bitsofproof.btc1k.server.vault;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.joda.time.DateTime;

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
import com.bitsofproof.supernode.wallet.AddressListAccountManager;
import com.bitsofproof.supernode.wallet.BaseAccountManager;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

public class Vault
{
	public class AM extends AddressListAccountManager
	{
		@Override
		protected void spendNonAddressOutput (int ix, TransactionOutput source, ScriptFormat.Writer sw, Transaction transaction) throws ValidationException
		{
			for ( int i = 0; i < publicKeys.size (); ++i )
			{
				sw.writeData (new byte[0]);
			}
			sw.writeData (new byte[0]);
			sw.writeData (getCustomerScript ());
		}

		@Override
		public Address getNextAddress () throws ValidationException
		{
			return getVaultAddress ();
		}
	}

	private final TreeMap<String, ECPublicKey> publicKeys = new TreeMap<> ();

	private final AM accountManager;

	private final Map<UUID, PendingTransaction> pendingTransactions = Maps.newConcurrentMap ();

	public void addKey (String name, ECPublicKey key)
	{
		publicKeys.put (name, key);
	}

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

	public Vault () throws BCSAPIException, ValidationException
	{
		this.accountManager = new AM ();
		accountManager.addAddress (getVaultAddress ());
		accountManager.setCreated (new DateTime (2013, 12, 7, 0, 0).getMillis ());
	}

	public PendingTransaction createTransaction (Address targetAddress, BigDecimal btcAmount) throws ValidationException
	{
		Transaction tx = accountManager.pay (targetAddress, btcAmount.longValue (), true);
		PendingTransaction pendingTransaction = new PendingTransaction (tx, btcAmount, targetAddress, "");

		pendingTransactions.put (pendingTransaction.getId (), pendingTransaction);
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
			byte[] vaultInputScript = getVaultAddress ().getAddressScript ();

			for ( TransactionInput input : transaction.getInputs () )
			{
				List<Token> tokens = ScriptFormat.parse (input.getScript ());

				byte[] sig =
						key.sign (BaseAccountManager.hashTransaction (transaction, i++, ScriptFormat.SIGHASH_ALL, vaultInputScript));
				byte[] sigPlusType = new byte[sig.length + 1];
				System.arraycopy (sig, 0, sigPlusType, 0, sig.length);
				sigPlusType[sigPlusType.length - 1] = (byte) (ScriptFormat.SIGHASH_ALL & 0xff);
				tokens.get (slot).op = ScriptFormat.Opcode.values ()[sigPlusType.length];
				tokens.get (slot).data = sigPlusType;

				ScriptFormat.Writer writer = new ScriptFormat.Writer ();
				for ( Token t : tokens )
				{
					writer.writeToken (t);
				}
				input.setScript (writer.toByteArray ());
			}
		}
		catch ( UnsupportedEncodingException e )
		{
		}
	}

	public PendingTransaction getPendingTransaction (UUID id)
	{
		return pendingTransactions.get (id);
	}

	public List<PendingTransaction> getAllPendingTransactions ()
	{
		return Ordering.natural ().sortedCopy (pendingTransactions.values ());
	}

	public long getBalance ()
	{
		return accountManager.getBalance ();
	}

	public AM getAccountManager ()
	{
		return accountManager;
	}

	public void updateTransaction (BCSAPI api, PendingTransaction transaction) throws BCSAPIException
	{
		pendingTransactions.put (transaction.getId (), transaction);
		api.sendTransaction (transaction.getTransaction ());
		pendingTransactions.remove (transaction.getId ());
	}

	public PendingTransaction deletePendingTransaction (UUID id)
	{
		return pendingTransactions.remove (id);
	}

	public List<NamedKey> getKeys ()
	{
		List<NamedKey> keys = new ArrayList<NamedKey> ();
		for ( Map.Entry<String, ECPublicKey> e : publicKeys.entrySet () )
		{
			keys.add (new NamedKey (e.getKey (), ByteUtils.toHexString (e.getValue ().getPublic ())));
		}
		return keys;
	}
}
