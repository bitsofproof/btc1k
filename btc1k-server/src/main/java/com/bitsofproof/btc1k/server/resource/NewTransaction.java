package com.bitsofproof.btc1k.server.resource;

import com.bitsofproof.supernode.api.Address;

import java.math.BigDecimal;

public class NewTransaction
{
	private Address address;

	private BigDecimal amount;

	public Address getAddress ()
	{
		return address;
	}

	public void setAddress (Address address)
	{
		this.address = address;
	}

	public BigDecimal getAmount ()
	{
		return amount;
	}

	public void setAmount (BigDecimal amount)
	{
		this.amount = amount;
	}
}
