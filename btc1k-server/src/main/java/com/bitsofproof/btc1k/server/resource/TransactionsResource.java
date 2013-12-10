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
package com.bitsofproof.btc1k.server.resource;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.DELETE;
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
import com.bitsofproof.supernode.api.BCSAPI;
import com.bitsofproof.supernode.api.BCSAPIException;
import com.bitsofproof.supernode.common.ValidationException;

public class TransactionsResource
{

	private final Vault vault;
	private final BCSAPI api;

	public TransactionsResource (Vault vault, BCSAPI api)
	{
		this.vault = vault;
		this.api = api;
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
	@DELETE
	public PendingTransaction deleteTransaction (@PathParam ("txId") UUID id)
	{
		return vault.deletePendingTransaction (id);
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
			vault.updateTransaction (api, transaction);
		}
		catch ( BCSAPIException | ValidationException e )
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

	@Path ("/keys")
	@GET
	public List<NamedKey> getKeys ()
	{
		return vault.getKeys ();
	}

}
