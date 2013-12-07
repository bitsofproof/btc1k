package com.bitsofproof.btc1k.fx;

import com.bitsofproof.dropwizard.supernode.jackson.SupernodeModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
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
import javafx.util.Callback;

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

	private ObjectProperty<RestTask> currentTask = new SimpleObjectProperty<> (this, "currentTask");

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

	public <T> RestTask<T> submitRestCall (String title, Callback<WebResource, T> callback)
	{

		return new RestTask (title, callback);
	}

	public RestTask getCurrentTask ()
	{
		return currentTask.get ();
	}

	public ObjectProperty<RestTask> currentTaskProperty ()
	{
		return currentTask;
	}

	public class RestTask<T> extends Task<T>
	{
		private final Callback<WebResource, T> callback;

		public RestTask (String title, Callback<WebResource, T> callback)
		{
			this.callback = callback;
			updateTitle (title);
		}

		// called from the GUI thread
		public void start()
		{
			EXECUTOR.submit (this);
		}

		@Override
		protected void running ()
		{
			currentTask.set (this);
		}

		@Override
		protected void succeeded ()
		{
			currentTask.set (null);
		}

		@Override
		protected void cancelled ()
		{
			currentTask.set (null);
		}

		@Override
		protected void failed ()
		{
			currentTask.set (null);
		}

		@Override
		protected T call () throws Exception
		{
			return callback.call (getBaseResource ());
		}
	}

}
