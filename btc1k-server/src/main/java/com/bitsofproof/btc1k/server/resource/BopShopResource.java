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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bitsofproof.btc1k.server.vault.Vault;
import com.bitsofproof.supernode.api.Address;
import com.bitsofproof.supernode.api.BCSAPI;
import com.bitsofproof.supernode.api.BCSAPIException;
import com.bitsofproof.supernode.api.Transaction;
import com.bitsofproof.supernode.api.TransactionInput;
import com.bitsofproof.supernode.api.TransactionOutput;
import com.bitsofproof.supernode.common.ExtendedKey;
import com.bitsofproof.supernode.common.Key;
import com.bitsofproof.supernode.common.ScriptFormat;
import com.bitsofproof.supernode.common.ValidationException;
import com.bitsofproof.supernode.wallet.BaseAccountManager;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.core.util.Base64;

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

	public BopShopResource (BCSAPI api, Vault vault, ExtendedKey master, int customerId, String password) throws ValidationException
	{
		this.master = master;
		this.vault = vault;
		this.api = api;
		client = HttpClientBuilder.create ().build ();
		this.customerId = customerId;
		this.password = password;

		log.info ("Vault address is " + vault.getVaultAddress ());
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

			processParsedRequest (request);
		}
		catch ( BCSAPIException | ValidationException | IOException e )
		{
			log.error ("Error processing ticket payment", e);
		}
	}

	private void processParsedRequest (BopShopPaymentRequest request) throws ValidationException, BCSAPIException
	{
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
		vaultOutput.setScript (vault.getVaultAddress ().getAddressScript ());
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
		log.info ("Ticket issued with " + transaction.getHash () + " for " + request.getId () + " address " + request.getTitle ());
		api.sendTransaction (transaction);
	}

	public BopShopResource processCleared () throws JsonParseException, JsonMappingException, ClientProtocolException, UnsupportedEncodingException,
			IOException, ValidationException, BCSAPIException
	{
		HttpGet get = new HttpGet ("https://api.bitsofproof.com/mbs/1/paymentRequest?state=CLEARED");
		ObjectMapper mapper = new ObjectMapper ();
		BopShopRequestList list = mapper.readValue (executeGet (get), BopShopRequestList.class);
		if ( list.get_embedded () != null )
		{
			Object item = list.get_embedded ().get ("item");
			if ( item instanceof List )
			{
				for ( Map<String, Object> o : (ArrayList<Map<String, Object>>) item )
				{
					processOneCleared (o);
				}
			}
			else
			{
				processOneCleared ((Map<String, Object>) item);
			}
		}
		return this;
	}

	private void processOneCleared (Map<String, Object> one) throws ClientProtocolException, UnsupportedEncodingException, IOException, ValidationException,
			BCSAPIException
	{
		String href = ((String) ((Map<String, Object>) (((Map<String, Object>) one.get ("_links")).get ("self"))).get ("href"));
		HttpGet get = new HttpGet (href);
		String req = executeGet (get);
		ObjectMapper mapper = new ObjectMapper ();
		processParsedRequest (mapper.readValue (req, BopShopPaymentRequest.class));
	}

	public BopShopPaymentRequest retrieveRequest (String id) throws IOException
	{
		HttpGet get = new HttpGet ("https://api.bitsofproof.com/mbs/1/paymentRequest/" + id);
		String req = executeGet (get);
		ObjectMapper mapper = new ObjectMapper ();
		return mapper.readValue (req, BopShopPaymentRequest.class);
	}

	private String executeGet (HttpGet get) throws IOException, ClientProtocolException, UnsupportedEncodingException
	{
		String authorizationString = "Basic " + new String (Base64.encode ((customerId + ":" + password).getBytes ()), "US-ASCII");
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
		return req;
	}

	@Path ("/transactions")
	public TransactionsResource tansactions ()
	{
		return new TransactionsResource (vault, api);
	}
}
