package com.bitsofproof.btc1k.server.commands;

import com.bitsofproof.btc1k.server.Btc1kConfiguration;
import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;

public class TimeoutPaymentRequestCommand extends ConfiguredCommand<Btc1kConfiguration>
{

	public TimeoutPaymentRequestCommand ()
	{
		super ("timeout-payment-request", "Description");
	}

	@Override
	protected void run (Bootstrap<Btc1kConfiguration> bootstrap, Namespace namespace, Btc1kConfiguration configuration) throws Exception
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}
}
