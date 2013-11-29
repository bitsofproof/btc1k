package com.bitsofproof.btc1k.server;

import com.bitsofproof.dropwizard.supernode.SupernodeConfiguration;
import com.bitsofproof.dropwizard.supernode.activemq.SupernodeConfigurationImpl;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

public class Btc1kConfiguration extends Configuration
{
	@JsonProperty
	SupernodeConfigurationImpl supernode;

	public SupernodeConfiguration getSupernode ()
	{
		return supernode;
	}
}
