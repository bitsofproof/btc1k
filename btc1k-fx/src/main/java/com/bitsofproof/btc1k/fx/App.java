package com.bitsofproof.btc1k.fx;

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
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneBuilder;
import javafx.stage.Stage;
import javafx.stage.StageBuilder;

import javax.validation.Validation;
import javax.validation.Validator;
import java.net.URI;

public class App extends Application
{
	private static final URI SERVER_URI = URI.create ("http://localhost:8280/btc1k");

	public static App instance;

	public RestClient restClient;

	@Override
	public void start (Stage primaryStage) throws Exception
	{
		instance = this;

		restClient = new RestClient (SERVER_URI);

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

	public static void main (String[] args)
	{
		launch (args);
	}

}
