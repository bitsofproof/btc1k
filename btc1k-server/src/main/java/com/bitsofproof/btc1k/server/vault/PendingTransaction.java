package com.bitsofproof.btc1k.server.vault;

import com.bitsofproof.supernode.api.Transaction;
import org.joda.time.DateTime;

import java.util.UUID;

public class PendingTransaction implements Comparable<PendingTransaction>
{
	private final UUID id;

	private Transaction transaction;

	private String title;

	private final DateTime createdAt;

	public PendingTransaction (Transaction transaction, String title)
	{
		this.id = UUID.randomUUID ();
		this.transaction = transaction;
		this.title = title;
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

	@Override
	public int compareTo (PendingTransaction o)
	{
		return createdAt.compareTo (o.createdAt);
	}
}
