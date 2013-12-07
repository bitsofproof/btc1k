package com.bitsofproof.btc1k.server.vault;

import com.bitsofproof.supernode.api.*;
import com.bitsofproof.supernode.common.*;
import com.bitsofproof.supernode.wallet.AddressListAccountManager;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import org.fusesource.hawtdispatch.OrderedEventAggregator;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Vault implements TransactionListener
{
	public class AM extends AddressListAccountManager
	{
		@Override
		protected void spendNonAddressOutput (int ix, TransactionOutput source, ScriptFormat.Writer sw, Transaction transaction) throws ValidationException
		{
			sw.writeData (new byte[0]);
			sw.writeData (new byte[0]);
			sw.writeData (new byte[0]);
			sw.writeData (new byte[0]);
			sw.writeData (customerScript);
		}

		@Override
		public Address getNextAddress () throws ValidationException
		{
			return ownAddress;
		}
	}

	private final AM accountManager;

	private Map<UUID, PendingTransaction> pendingTransactions = Maps.newConcurrentMap ();

	private final byte[] customerScript;

	private final Address ownAddress;

	private final Key[] p2shKeys;

	public static Vault create (Key... keys) throws ValidationException
	{
		Preconditions.checkArgument (keys != null && keys.length == 3);

		byte[] script = getCustomerScript (keys);
		Address address = new Address (Network.PRODUCTION, Address.Type.P2SH, Hash.keyHash (script));

		Vault vault = new Vault (getCustomerScript (keys), address, keys);
		return vault;
	}

	private static byte[] getCustomerScript (Key[] keys) throws ValidationException
	{
		ScriptFormat.Writer writer = new ScriptFormat.Writer ();
		writer.writeToken (new ScriptFormat.Token (ScriptFormat.Opcode.OP_2));
		for (Key key : keys)
		{
			writer.writeData (key.getPublic ());
		}
		writer.writeToken (new ScriptFormat.Token (ScriptFormat.Opcode.OP_3));
		writer.writeToken (new ScriptFormat.Token (ScriptFormat.Opcode.OP_CHECKMULTISIG));
		return writer.toByteArray ();
	}

	Vault (byte[] customerScript, Address ownAddress, Key... keys)
	{
		this.customerScript = customerScript;
		this.ownAddress = ownAddress;
		this.p2shKeys = keys;
		this.accountManager = new AM ();
		accountManager.addAddress (ownAddress);
	}


	public Key[] getP2SHKeys ()
	{
		return p2shKeys;
	}

	public Address getTwoOfThreeAddress ()
	{
		return ownAddress;
	}

	public PendingTransaction createTransaction (Address targetAddress, BigDecimal btcAmount) throws ValidationException
	{
		Transaction tx = accountManager.pay (targetAddress, btcAmount.longValue (), true);
		PendingTransaction pendingTransaction = new PendingTransaction (tx, "");

		pendingTransactions.put (pendingTransaction.getId (), pendingTransaction);
		return pendingTransaction;
	}

	public PendingTransaction getPendingTransaction(UUID id)
	{
		return pendingTransactions.get (id);
	}

	public List<PendingTransaction> getAllPendingTransactions()
	{
		return Ordering.natural ().sortedCopy (pendingTransactions.values ());
	}

	@Override
	public void process (Transaction t)
	{
		accountManager.process (t);
	}

	public long getBalance()
	{
		return accountManager.getBalance ();
	}

	public AM getAccountManager ()
	{
		return accountManager;
	}
}
