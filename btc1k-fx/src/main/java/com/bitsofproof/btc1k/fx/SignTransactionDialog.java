package com.bitsofproof.btc1k.fx;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class SignTransactionDialog extends GridPane
{

	private final Stage stage;

	public static String requestPassphrase()
	{
		try
		{
			Stage dialogStage = new Stage ();
			SignTransactionDialog dialog = new SignTransactionDialog (dialogStage);

			dialogStage.initModality (Modality.APPLICATION_MODAL);
			FXMLLoader loader = new FXMLLoader (SignTransactionDialog.class.getResource ("SignTransactionDialog.fxml"));
			loader.setController (dialog);
			loader.setRoot (dialog);
			loader.load ();

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
	PasswordField passphrase;

	@FXML
	Button signButton;

	public SignTransactionDialog (Stage stage)
	{
		this.stage = stage;
		signButton.disableProperty ().bind(passphrase.textProperty ().isNotNull ());
	}

	public void onSign()
	{
		stage.close ();
	}
}
