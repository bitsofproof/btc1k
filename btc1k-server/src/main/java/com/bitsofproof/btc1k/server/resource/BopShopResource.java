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

import com.bitsofproof.btc1k.server.vault.Vault;
import com.bitsofproof.supernode.api.*;
import com.bitsofproof.supernode.common.*;
import com.bitsofproof.supernode.wallet.BaseAccountManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Path ("/btc1k")
@Produces (MediaType.APPLICATION_JSON)
@Consumes (MediaType.APPLICATION_JSON)
public class BopShopResource
{
	private static final Logger log = LoggerFactory.getLogger (BopShopResource.class);

	private final ExtendedKey master;

	private final BCSAPI api;
	private static final long FEE = 10000;
	private static final long mB = 100000;
	private final HttpClient client;
	private final int customerId;
	private final String password;

	private final Vault vault;

	public BopShopResource (BCSAPI api, Vault vault, ExtendedKey master, int customerId, String password, String request)
			throws ValidationException
	{
		this.master = master;
		this.vault = vault;
		this.api = api;
		client = HttpClientBuilder.create ().build ();
		this.customerId = customerId;
		this.password = password;
		log.info ("Vault address is " + vault.getTwoOfThreeAddress ());
		if ( request != null )
		{
			processRequest (request);
		}
	}

	@Path ("/payment")
	@POST
	public void callback (BopShopCallback callback)
	{
		if ( callback.getReason () == BopShopCallbackReason.SETTLED )
		{
			processRequest (callback.getPaymentRequestId ());
		}
	}

	public void processRequest (String requestId)
	{
		try
		{
			BopShopPaymentRequest request = retrieveRequest (requestId);

			Key key = master.getKey (request.getChild ());

			Address incomingAddress = Address.fromSatoshiStyle (request.getAddress ());
			Transaction transaction = new Transaction ();
			transaction.setInputs (new ArrayList<TransactionInput> ());
			Set<String> txSet = new HashSet<> ();
			long amount = 0;
			for ( BopShopEvent event : request.getEvents () )
			{
				if ( event.getEventType () == BopShopEventType.TRANSACTION )
				{
					if ( !txSet.contains (event.getTxHash ()) )
					{
						txSet.add (event.getTxHash ());
						TransactionInput input = new TransactionInput ();
						input.setSourceHash (event.getTxHash ());
						input.setIx (event.getIx ());
						transaction.getInputs ().add (input);
						amount += event.getAmount ();
					}
				}
			}

			transaction.setOutputs (new ArrayList<TransactionOutput> ());
			TransactionOutput vaultOutput = new TransactionOutput ();
			vaultOutput.setScript (vault.getTwoOfThreeAddress ().getAddressScript ());
			vaultOutput.setValue (amount - FEE - mB);
			transaction.getOutputs ().add (vaultOutput);
			TransactionOutput ticket = new TransactionOutput ();
			ticket.setScript (Address.fromSatoshiStyle (request.getTitle ()).getAddressScript ());
			ticket.setValue (mB);
			transaction.getOutputs ().add (ticket);

			int i = 0;
			for ( TransactionInput input : transaction.getInputs () )
			{
				ScriptFormat.Writer sw = new ScriptFormat.Writer ();
				byte[] sig =
						key.sign (BaseAccountManager.hashTransaction (transaction, i++, ScriptFormat.SIGHASH_ALL, incomingAddress.getAddressScript ()));
				byte[] sigPlusType = new byte[sig.length + 1];
				System.arraycopy (sig, 0, sigPlusType, 0, sig.length);
				sigPlusType[sigPlusType.length - 1] = (byte) (ScriptFormat.SIGHASH_ALL & 0xff);
				sw.writeData (sigPlusType);
				sw.writeData (key.getPublic ());
				input.setScript (sw.toByteArray ());
			}
			transaction.computeHash ();
			log.info ("Ticket issued with " + transaction.getHash ());
			api.sendTransaction (transaction);
		}
		catch ( BCSAPIException | ValidationException | IOException e )
		{
			log.error ("Error processing ticket payment", e);
		}
	}

	public BopShopPaymentRequest retrieveRequest (String id) throws IOException
	{
		HttpGet get = new HttpGet ("https://api.bitsofproof.com/mbs/1/paymentRequest/" + id);
		String authorizationString = "Basic " + org.apache.shiro.codec.Base64.encodeToString ((customerId + ":" + password).getBytes ());
		get.setHeader ("Authorization", authorizationString);
		HttpResponse response = client.execute (get);
		BufferedReader in = new BufferedReader (new InputStreamReader (response.getEntity ().getContent (), "UTF-8"));
		StringWriter writer = new StringWriter ();
		String line;
		while ( (line = in.readLine ()) != null )
		{
			writer.write (line);
		}
		String req = writer.toString ();
		ObjectMapper mapper = new ObjectMapper ();
		return mapper.readValue (req, BopShopPaymentRequest.class);
	}

	@Path("/transactions")
	public TransactionsResource tansactions()
	{
		return new TransactionsResource (vault);
	}
}
