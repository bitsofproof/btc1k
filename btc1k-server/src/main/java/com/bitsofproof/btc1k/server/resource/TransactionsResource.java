package com.bitsofproof.btc1k.server.resource;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.bitsofproof.btc1k.server.vault.PendingTransaction;
import com.bitsofproof.btc1k.server.vault.Vault;
import com.bitsofproof.supernode.api.BCSAPIException;
import com.bitsofproof.supernode.common.ValidationException;

public class TransactionsResource
{

	private final Vault vault;

	public TransactionsResource (Vault vault)
	{
		this.vault = vault;
	}

	@POST
	public Response createTransaction (NewTransaction newTransaction, @Context UriInfo uriInfo) throws ValidationException
	{
		PendingTransaction tx = vault.createTransaction (newTransaction.getAddress (), newTransaction.getAmount ());

		URI targetURI = uriInfo.getAbsolutePathBuilder ()
				// .path (BopShopResource.class, "transactions")
				.path (TransactionsResource.class, "showTransaction")
				.build (tx.getId ());

		return Response.created (targetURI).build ();
	}

	@Path ("/{txId}")
	@GET
	public PendingTransaction showTransaction (@PathParam ("txId") UUID id)
	{
		return vault.getPendingTransaction (id);
	}

	@Path ("/{txId}")
	@PUT
	public Response updateTransaction (@PathParam ("txId") UUID id, PendingTransaction transaction)
	{
		if ( !id.equals (transaction.getId ()) )
		{
			return Response.notModified ().build ();
		}
		try
		{
			vault.updateTransaction (transaction);
		}
		catch ( BCSAPIException e )
		{
			return Response.serverError ().build ();
		}

		return Response.ok ().build ();
	}

	@Path ("/all")
	@GET
	public List<PendingTransaction> allPendingTransactions ()
	{
		return vault.getAllPendingTransactions ();
	}
}
