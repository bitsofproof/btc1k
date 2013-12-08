package com.bitsofproof.btc1k.fx.rest;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import javafx.concurrent.Task;

import javax.ws.rs.core.MediaType;
import java.net.URI;

public abstract class RestTask<T> extends Task<T>
{
	protected URI uri;
	protected Client client;
	protected WebResource resource;

	public RestTask ()
	{
	}

	public RestTask (URI uri)
	{
		this.uri = uri;
	}

	public void setClient (Client client)
	{
		this.client = client;
	}

	public boolean hasUri()
	{
		return uri != null;
	}

	public void setResource (WebResource resource)
	{
		this.resource = resource;
	}

	@Override
	protected final T call () throws Exception
	{
		WebResource res = resource;
		if (res == null)
		{
			res = client.resource (uri);
		}

		return call(res);
	}

	protected abstract T call (WebResource resource);

	@Override
	protected void scheduled ()
	{
		updateMessage (getTitle ());
	}

	@Override
	protected void running ()
	{
		updateMessage (getTitle ());
	}

	@Override
	protected void succeeded ()
	{
		updateMessage ("Success: " + getTitle ());
	}

	@Override
	protected void cancelled ()
	{
		updateMessage ("Cancelled: " + getTitle ());
	}

	@Override
	protected void failed ()
	{
		updateMessage ("Failed: " + getTitle ());
	}
}
