package com.bitsofproof.btc1k.server.vault;

import com.bitsofproof.supernode.api.Address;
import com.bitsofproof.supernode.api.Transaction;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.UUID;

public class PendingTransaction implements Comparable<PendingTransaction>
{
	private Address targetAddress;

	private UUID id;

	private Transaction transaction;

	private String title;

	private BigDecimal amount;

	private DateTime createdAt;

	public PendingTransaction ()
	{
	}

	public PendingTransaction (Transaction transaction, BigDecimal amount, Address targetAddress, String title)
	{
		this.id = UUID.randomUUID ();
		this.transaction = transaction;
		this.title = title;
		this.amount = amount;
		this.targetAddress = targetAddress;
		this.createdAt = DateTime.now ();
	}

	public Transaction getTransaction ()
	{
		return transaction;
	}

	public void setTransaction (Transaction transaction)
	{
		this.transaction = transaction;
	}

	public String getTitle ()
	{
		return title;
	}

	public void setTitle (String title)
	{
		this.title = title;
	}

	public DateTime getCreatedAt ()
	{
		return createdAt;
	}

	public UUID getId ()
	{
		return id;
	}

	public BigDecimal getAmount ()
	{
		return amount;
	}

	public Address getTargetAddress ()
	{
		return targetAddress;
	}

	@Override
	public int compareTo (PendingTransaction o)
	{
		return createdAt.compareTo (o.createdAt);
	}
}
