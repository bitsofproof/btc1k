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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;

import com.bitsofproof.supernode.api.Address;
import com.bitsofproof.supernode.api.Transaction;

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
