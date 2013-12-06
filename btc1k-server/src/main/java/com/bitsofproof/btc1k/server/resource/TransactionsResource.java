package com.bitsofproof.btc1k.server.resource;

import com.bitsofproof.btc1k.server.vault.Vault;

import javax.ws.rs.POST;

public class TransactionsResource
{

	private final Vault vault;

	public TransactionsResource (Vault vault)
	{
		this.vault = vault;
	}

	@POST
	public void createTransaction(NewTransaction newTransaction)
	{
		//vault.createTransaction (newTransaction.getAddress ())
	}

}
