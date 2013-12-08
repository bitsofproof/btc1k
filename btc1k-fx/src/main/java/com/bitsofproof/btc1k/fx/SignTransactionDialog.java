package com.bitsofproof.btc1k.fx;

import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageBuilder;

import java.io.IOException;

public class SignTransactionDialog extends GridPane
{

	private final Stage stage;

	public static String requestPassphrase()
	{
		try
		{
			Stage dialogStage = new Stage ();
			dialogStage.setTitle ("BTC-1K");
			dialogStage.initModality (Modality.APPLICATION_MODAL);

			SignTransactionDialog dialog = new SignTransactionDialog (dialogStage);
			FXMLLoader loader = new FXMLLoader (SignTransactionDialog.class.getResource ("SignTransactionDialog.fxml"));
			loader.setController (dialog);
			loader.setRoot (dialog);
			loader.load ();

			AwesomeDude.setIcon (dialog.logoLabel, AwesomeIcon.KEY, "60");

			dialogStage.setScene (new Scene (dialog));
			dialogStage.showAndWait ();

			return dialog.passphrase.getText ();
		}
		catch (IOException e)
		{
			throw new RuntimeException (e);
		}
	}

	@FXML
	TextField passphrase;

	@FXML
	Button signButton;

	@FXML
	Label logoLabel;

	public SignTransactionDialog (Stage stage)
	{
		this.stage = stage;
	}

	public void initialize()
	{
		signButton.disableProperty ().bind(passphrase.textProperty ().isNull ().or(passphrase.textProperty ().isEqualTo ("")));
	}

	public void onSign()
	{
		stage.close ();
	}
}
