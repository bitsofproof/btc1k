package com.bitsofproof.btc1k.server.vault;

import com.bitsofproof.supernode.api.Address;
import com.bitsofproof.supernode.api.Transaction;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PendingTransaction implements Comparable<PendingTransaction>
{
	private Address targetAddress;

	private UUID id;

	private Transaction transaction;

	private String title;

	private BigDecimal amount;

	private DateTime createdAt;

	private List<String> signedBy = new ArrayList<> ();

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

	public List<String> getSignedBy ()
	{
		return signedBy;
	}

	public void setSignedBy (List<String> signedBy)
	{
		this.signedBy = signedBy;
	}

	@Override
	public int compareTo (PendingTransaction o)
	{
		return createdAt.compareTo (o.createdAt);
	}
}
