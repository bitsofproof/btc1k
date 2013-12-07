package com.bitsofproof.btc1k.server.resource;

import com.bitsofproof.supernode.api.Address;
import javafx.scene.control.TextField;

import java.math.BigDecimal;

public class NewTransaction
{
	private Address address;

	private BigDecimal amount;

	public NewTransaction ()
	{
	}

	public NewTransaction (Address targetAddress, BigDecimal amount)
	{
		this.address = targetAddress;
		this.amount = amount;
	}

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
