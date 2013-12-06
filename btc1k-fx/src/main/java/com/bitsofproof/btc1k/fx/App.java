package com.bitsofproof.btc1k.fx;

import com.bitsofproof.dropwizard.supernode.jackson.SupernodeModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.LoggingFilter;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.jackson.JacksonMessageBodyProvider;
import io.dropwizard.jersey.jackson.JsonProcessingExceptionMapper;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneBuilder;
import javafx.stage.Stage;
import javafx.stage.StageBuilder;

import javax.validation.Validation;
import javax.validation.Validator;
import java.io.InputStream;

public class App extends Application
{
	public static App instance;

	private Client jerseyClient;

	@Override
	public void start (Stage primaryStage) throws Exception
	{
		instance = this;

		jerseyClient = setupJerseyClient ();

		InputStream stream = App.class.getResource ("main.fxml").openStream ();

		FXMLLoader loader = new FXMLLoader (App.class.getResource ("main.fxml"));
		Parent root = (Parent) loader.load ();

		Scene scene = SceneBuilder.create ()
				.root (root)
				.stylesheets ("com/bitsofproof/btc1k/fx/main.css")
				.build ();

		StageBuilder.create()
				.title("BTC-1K")
				.scene (scene)
				.minWidth (600)
				.minHeight (400)
				.applyTo (primaryStage);

		primaryStage.show ();

	}

	private Client setupJerseyClient ()
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

		return client;
	}

	public static void main (String[] args)
	{
		launch (args);
	}

}
