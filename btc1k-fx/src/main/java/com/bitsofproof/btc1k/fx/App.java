package com.bitsofproof.btc1k.fx;

import java.net.URI;
import java.security.Security;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneBuilder;
import javafx.stage.Stage;
import javafx.stage.StageBuilder;

import javax.ws.rs.core.MediaType;

import com.bitsofproof.btc1k.fx.rest.RestClient;
import com.bitsofproof.btc1k.server.resource.NamedKey;
import com.bitsofproof.btc1k.server.vault.Vault;
import com.sun.jersey.api.client.GenericType;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class App extends Application
{
	private static final URI SERVER_URI = URI.create ("http://api.bitsofproof.com:8280/btc1k");

	public static App instance;

	public RestClient restClient;

	public Vault vault;

	@Override
	public void start (Stage primaryStage) throws Exception
	{
		instance = this;

		restClient = new RestClient (SERVER_URI);
		vault = new Vault (fetchKeys ());

		FXMLLoader loader = new FXMLLoader (App.class.getResource ("main.fxml"));
		Parent root = (Parent) loader.load ();

		Scene scene = SceneBuilder.create ()
				.root (root)
				.stylesheets ("com/bitsofproof/btc1k/fx/main.css")
				.build ();

		StageBuilder.create ()
				.title ("BTC-1K")
				.scene (scene)
				.minWidth (600)
				.minHeight (600)
				.applyTo (primaryStage);

		primaryStage.show ();

	}

	public Map<String, String> fetchKeys ()
	{
		List<NamedKey> keys = restClient.getBaseResource ()
				.path ("/transactions/keys")
				.accept (MediaType.APPLICATION_JSON)
				.get (new GenericType<List<NamedKey>> ()
				{
				});
		Map<String, String> keymap = new HashMap<> ();
		for ( NamedKey key : keys )
		{
			keymap.put (key.getName (), key.getKey ());
		}
		return keymap;
	}

	public static void main (String[] args)
	{
		Security.addProvider (new BouncyCastleProvider ());
		launch (args);
	}

}
