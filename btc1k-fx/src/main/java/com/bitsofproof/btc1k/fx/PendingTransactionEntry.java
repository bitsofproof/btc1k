package com.bitsofproof.btc1k.fx;

import com.bitsofproof.btc1k.server.vault.PendingTransaction;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class PendingTransactionEntry extends HBox
{

	private ObjectProperty<PendingTransaction> pendingTransaction = new SimpleObjectProperty<> ();

	@FXML
	private Label amountLabel;

	@FXML
	private Label addressLabel;

	@FXML
	private Label createdAtLabel;

	@FXML
	private Button signButton;

	public PendingTransactionEntry (PendingTransaction pendingTransaction)
	{
		pendingTransaction.getTransaction ().computeHash ();
		this.pendingTransaction.set (pendingTransaction);

		FXMLLoader loader = new FXMLLoader (this.getClass ().getResource ("PendingTransactionEntry.fxml"));
		loader.setController (this);
		loader.setRoot (this);

		try
		{
			loader.load ();
		}
		catch (IOException e)
		{
			throw new RuntimeException (e);
		}
	}

	public void signHandler (EventHandler<ActionEvent> handler)
	{
		signButton.setOnAction (handler);
	}

	public void initialize()
	{
		pendingTransaction.addListener (new ChangeListener<PendingTransaction> () {
			@Override
			public void changed (ObservableValue<? extends PendingTransaction> observableValue, PendingTransaction pendingTransaction,
			                     PendingTransaction pendingTransaction2)
			{
				updateValues();
			}
		});

		updateValues ();
	}

	private void updateValues ()
	{
		PendingTransaction pt = pendingTransaction.get ();
		amountLabel.setText (pt.getAmount ().toPlainString ());
		addressLabel.setText (pt.getTargetAddress ().toString ());
		createdAtLabel.setText (pt.getCreatedAt ().toString ());
	}

}
