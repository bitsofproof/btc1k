package com.bitsofproof.btc1k.server.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;

@Path ("/binary")
public class SomeResource
{
	@GET
	@Produces (MediaType.APPLICATION_OCTET_STREAM)
	public Response binaryData ()
	{
		ByteArrayInputStream baos = new ByteArrayInputStream (new byte[]{1, 2});

		return Response.ok (baos).build ();
	}
}
