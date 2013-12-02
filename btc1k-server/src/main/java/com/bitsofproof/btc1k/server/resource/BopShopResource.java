package com.bitsofproof.btc1k.server.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path ("/tickets")
@Produces (MediaType.APPLICATION_JSON)
@Consumes (MediaType.APPLICATION_JSON)
public class BopShopResource
{
	private static final Logger log = LoggerFactory.getLogger (BopShopResource.class);

	@POST
	public void callback (BopShopCallback callback)
	{
		log.info (callback.toString ());
	}
}
