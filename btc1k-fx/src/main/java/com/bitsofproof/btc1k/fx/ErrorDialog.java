package com.bitsofproof.btc1k.fx;

import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.SceneBuilder;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ErrorDialog extends GridPane
{
	private final Stage stage;

	@FXML
	private Label messageLabel;

	@FXML
	private Label detailsLabel;

	@FXML
	private Label logoLabel;

	public static void showError (Exception e)
	{
		try
		{
			Stage dialogStage = new Stage ();
			dialogStage.setTitle ("BTC-1K");
			dialogStage.initModality (Modality.APPLICATION_MODAL);

			ErrorDialog dialog = new ErrorDialog (dialogStage);

			FXMLLoader loader = new FXMLLoader (SignTransactionDialog.class.getResource ("ErrorDialog.fxml"));
			loader.setController (dialog);
			loader.setRoot (dialog);
			loader.load ();


			dialog.messageLabel.setText (e.getMessage ());

			AwesomeDude.setIcon (dialog.logoLabel, AwesomeIcon.WARNING, "60");

			dialogStage.setScene (new Scene (dialog));
			dialogStage.showAndWait ();
		}
		catch (Exception e1)
		{
			throw new IllegalStateException (e1);
		}
	}

	public ErrorDialog (Stage dialogStage)
	{
		this.stage = dialogStage;
	}

	public void ok ()
	{
		stage.close ();
	}
}
