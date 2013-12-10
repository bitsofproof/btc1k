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
package com.bitsofproof.btc1k.server;

import com.bitsofproof.btc1k.server.commands.TimeoutPaymentRequestCommand;
import com.bitsofproof.btc1k.server.resource.BopShopResource;
import com.bitsofproof.btc1k.server.vault.Vault;

import com.bitsofproof.dropwizard.supernode.SupernodeBundle;
import com.bitsofproof.dropwizard.supernode.SupernodeConfiguration;
import com.bitsofproof.supernode.api.BCSAPI;
import com.bitsofproof.supernode.api.BCSAPIException;
import com.bitsofproof.supernode.api.Transaction;
import com.bitsofproof.supernode.api.TransactionOutput;
import com.bitsofproof.supernode.common.ValidationException;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.security.Security;

public class Btc1kApplication extends Application<Btc1kConfiguration>
{
	private static final Logger log = LoggerFactory.getLogger (Btc1kApplication.class);

	private final SupernodeBundle<Btc1kConfiguration> supernodeBundle = new SupernodeBundle<Btc1kConfiguration> ()
	{
		@Override
		protected SupernodeConfiguration getSupernodeConfiguration (Btc1kConfiguration configuration)
		{
			return configuration.getSupernode ();
		}
	};

	private Vault vault;

	public static void main (String[] args) throws Exception
	{
		Security.addProvider (new BouncyCastleProvider ());
		new Btc1kApplication ().run (args);
	}

	@Override
	public void initialize (Bootstrap<Btc1kConfiguration> bootstrap)
	{
		bootstrap.addBundle (supernodeBundle);
		bootstrap.addCommand (new TimeoutPaymentRequestCommand ());
	}

	@Override
	public void run (Btc1kConfiguration configuration, Environment environment) throws Exception
	{
		BCSAPI api = supernodeBundle.getBCSAPI ();
		vault = configuration.getVaultFactory ().createVault ();
		vault.getAccountManager ().sync (api);
		api.registerTransactionListener (vault.getAccountManager ());
		System.out.println ("Vault " + vault.getVaultAddress ());
		environment.jersey ().register (new BopShopResource (
				supernodeBundle.getBCSAPI (),
				vault,
				configuration.getMasterKey (),
				configuration.getCustomerId (),
				configuration.getPassphrase ()
				)/* .processCleared () */);

		if ( supernodeBundle.getBox () != null )
		{
			fundVaultForTesting ();
		}
	}

	private void fundVaultForTesting () throws ValidationException, BCSAPIException, InterruptedException
	{
		Helper h = new Helper (supernodeBundle.getBox (), vault);
		h.fundVault (BigDecimal.valueOf (20));

		Thread.sleep (1000);
		log.info ("Start synchronizing account manager. This might take a while");
		// vault.syncHistory (api);

		log.info ("Querying account manager transactions {}", vault.getBalance ());
		for ( Transaction tx : vault.getAccountManager ().getTransactions () )
		{
			tx.computeHash ();
			log.info ("    tx {}", tx.getHash ());
			for ( TransactionOutput output : tx.getOutputs () )
			{
				log.info ("        {}", output.getValue ());
			}
		}
	}
}
