package com.bitsofproof.btc1k.fx.rest;

import com.bitsofproof.dropwizard.supernode.jackson.SupernodeModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.LoggingFilter;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.jackson.JacksonMessageBodyProvider;
import io.dropwizard.jersey.jackson.JsonProcessingExceptionMapper;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;

import javax.validation.Validation;
import javax.validation.Validator;
import java.net.URI;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class RestClient
{
	private static final ThreadFactory THREAD_FACTORY = new ThreadFactory ()
	{
		public Thread newThread (final Runnable run)
		{
			return AccessController.doPrivileged (new PrivilegedAction<Thread> ()
			{
				@Override
				public Thread run ()
				{
					final Thread th = new Thread (run);
					th.setPriority (Thread.MIN_PRIORITY);
					th.setDaemon (true);
					return th;
				}
			});
		}
	};

	/**
	 * Single thread executor for REST requests.
	 */
	static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor (THREAD_FACTORY);

	private final Client client;

	private final URI baseURI;

	private ObjectProperty<Task<?>> currentTask = new SimpleObjectProperty<> (this, "currentTask");

	public RestClient (URI baseURI)
	{
		ObjectMapper mapper = Jackson.newObjectMapper ()
		                             .registerModule (new SupernodeModule ());

		Validator validator = Validation.buildDefaultValidatorFactory ().getValidator ();

		ClientConfig cc = new DefaultClientConfig (JsonProcessingExceptionMapper.class);
		cc.getProperties ().put (ClientConfig.PROPERTY_CONNECT_TIMEOUT, 5000);
		cc.getProperties ().put (ClientConfig.PROPERTY_READ_TIMEOUT, 5000);
		cc.getSingletons ().add (new JacksonMessageBodyProvider (mapper, validator));

		Client client = Client.create (cc);
		client.addFilter (new LoggingFilter ());

		this.client = client;
		this.baseURI = baseURI;
	}

	public WebResource getBaseResource ()
	{
		return client.resource (baseURI);
	}

	public Task<?> getCurrentTask ()
	{
		return currentTask.get ();
	}

	public ObjectProperty<Task<?>> currentTaskProperty ()
	{
		return currentTask;
	}

	public <T> void submitRestCall (final RestTask<T> task)
	{
		task.setClient (client);
		if (!task.hasUri ())
		{
			task.setResource (getBaseResource ());
		}

		EXECUTOR.submit (task);
	}

}
