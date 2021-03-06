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

import io.dropwizard.Application;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bitsofproof.btc1k.server.commands.TimeoutPaymentRequestCommand;
import com.bitsofproof.btc1k.server.resource.BopShopResource;
import com.bitsofproof.btc1k.server.vault.Vault;
import com.bitsofproof.dropwizard.supernode.SupernodeBundle;
import com.bitsofproof.dropwizard.supernode.SupernodeConfiguration;
import com.bitsofproof.supernode.account.ConfirmationManager;
import com.bitsofproof.supernode.api.BCSAPI;

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
		Vault vault = configuration.getVaultFactory ().createVault ();
		environment.lifecycle ().manage (new Btc1kService (api, vault));
		environment.jersey ().register (new BopShopResource (
				supernodeBundle.getBCSAPI (),
				vault,
				configuration.getBopShopKey (),
				configuration.getCustomerId (),
				configuration.getPassphrase ()
				).processCleared ());
	}

	private static class Btc1kService implements Managed
	{
		private ConfirmationManager confirmationManager = new ConfirmationManager ();
		private BCSAPI api;
		private Vault vault;

		private Btc1kService (BCSAPI api, Vault vault)
		{
			this.api = api;
			this.vault = vault;
		}

		@Override
		public void start () throws Exception
		{
			confirmationManager.init (api, 100);
			api.registerTrunkListener (confirmationManager);
			vault.getAccountManager ().sync (api);
			confirmationManager.addAccount (vault.getAccountManager ());
			api.registerRejectListener (vault.getAccountManager ());
			api.registerTransactionListener (vault.getAccountManager ());
			System.out.println ("Vault " + vault.getVaultAddress ());
		}

		@Override
		public void stop () throws Exception
		{
		}
	}
}
