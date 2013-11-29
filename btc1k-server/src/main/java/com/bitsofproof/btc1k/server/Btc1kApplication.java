package com.bitsofproof.btc1k.server;

import com.bitsofproof.btc1k.server.resource.SomeResource;
import com.bitsofproof.dropwizard.supernode.SupernodeBundle;
import com.bitsofproof.dropwizard.supernode.SupernodeConfiguration;
import com.bitsofproof.supernode.api.BCSAPI;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;

public class Btc1kApplication extends Application<Btc1kConfiguration>
{
	private SupernodeBundle<Btc1kConfiguration> supernodeBundle = new SupernodeBundle<Btc1kConfiguration> ()
	{
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
	}

	@Override
	public void run (Btc1kConfiguration configuration, Environment environment) throws Exception
	{
		BCSAPI api = supernodeBundle.getBCSAPI ();

		environment.jersey ().register (new SomeResource ());
	}
}
