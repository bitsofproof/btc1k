package com.bitsofproof.btc1k.fx;

import com.atlassian.fugue.Either;
import com.bitsofproof.btc1k.fx.rest.RestTask;
import com.bitsofproof.btc1k.server.resource.NewTransaction;
import com.bitsofproof.btc1k.server.vault.PendingTransaction;
import com.bitsofproof.btc1k.server.vault.Vault;
import com.bitsofproof.supernode.api.Address;
import com.bitsofproof.supernode.common.ValidationException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.converter.BigDecimalStringConverter;

import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.util.List;

import static com.bitsofproof.btc1k.fx.components.EitherConverters.*;

public class MainController
{
	@FXML
	Button refreshButton;

	@FXML
	TabPane toolsPane;

	@FXML
	TextField targetAddress;

	ObjectProperty<Either<String, Address>> address = new SimpleObjectProperty<> (this, "address");

	@FXML
	TextField amount;

	ObjectProperty<Either<String, BigDecimal>> btc = new SimpleObjectProperty<> (this, "btc");

	@FXML
	Button sendButton;

	@FXML
	TextField mnemonicField;

	@FXML
	TextField publicKeyField;

	@FXML
	Label messageLabel;

	@FXML
	VBox transactionListPane;

	public void initialize ()
	{
		messageLabel.textProperty ().bind (Bindings.selectString (App.instance.restClient.currentTaskProperty (), "title"));
		AwesomeDude.setIcon (refreshButton, AwesomeIcon.REFRESH);
		refreshButton.setTooltip (new Tooltip ("Refresh transaction list"));

		toolsPane.getSelectionModel ().selectedItemProperty ().addListener (new ChangeListener<Tab> ()
		{
			@Override
			public void changed (ObservableValue<? extends Tab> v, Tab oldTab, Tab currentTab)
			{
				if ( "keygenTab".equals (oldTab.getId ()) )
				{
					clearKeyData ();
				}
			}
		});

		validateTextField (targetAddress, address, addressConverter ());
		validateTextField (amount, btc, adapter (new BigDecimalStringConverter ()));

		sendButton.disableProperty ().bind (new BooleanBinding ()
		{
			{
				bind (address, btc);
			}

			@Override
			protected boolean computeValue ()
			{
				return (address.get () == null || address.get ().isLeft () || btc.get () == null || btc.get ().isLeft ());
			}
		});

		refreshTransactionList ();
	}

	private void clearKeyData ()
	{
		mnemonicField.clear ();
		publicKeyField.clear ();
	}

	public void generateKeyData ()
	{
		try
		{
			Vault.RandomKey key = App.instance.vault.generateRandomKey ();
			mnemonicField.setText (key.getMnemonic ());
			publicKeyField.setText (key.getPublicKey ());
		}
		catch ( ValidationException e )
		{
			e.printStackTrace (); // To change body of catch statement use File | Settings | File Templates.
		}
	}

	public void refreshTransactionList ()
	{
		App.instance.restClient.submitRestCall (new RestTask<List<PendingTransaction>> ()
		{
			@Override
			protected List<PendingTransaction> call (WebResource resource)
			{
				return resource.path ("/transactions/all")
						.accept (MediaType.APPLICATION_JSON)
						.get (new GenericType<List<PendingTransaction>> () {
						});
			}

			@Override
			protected void succeeded ()
			{
				super.succeeded ();
				doRefreshTransactionList (getValue ());
			}
		});

	}

	private void doRefreshTransactionList (List<PendingTransaction> transactionList)
	{
		transactionListPane.getChildren ().clear ();
		for ( PendingTransaction pendingTransaction : transactionList )
		{
			transactionListPane.getChildren ().add (createEntry (pendingTransaction));
		}
	}

	private PendingTransactionEntry createEntry (final PendingTransaction pt)
	{
		final PendingTransactionEntry entry = new PendingTransactionEntry (pt);
		entry.signHandler (new EventHandler<ActionEvent> ()
		{
			@Override
			public void handle (ActionEvent actionEvent)
			{
				signTransaction (pt);
			}
		});
		entry.rejectHandler (new EventHandler<ActionEvent> ()
		{
			@Override
			public void handle (ActionEvent actionEvent)
			{
				rejectTransaction (pt);
			}
		});

		return entry;
	}

	private void rejectTransaction (final PendingTransaction tx)
	{
		App.instance.restClient.submitRestCall (new RestTask<ClientResponse> ()
		{
			@Override
			protected ClientResponse call (WebResource resource)
			{
				return resource.path ("/transactions/" + tx.getId ())
						.accept (MediaType.APPLICATION_JSON)
						.delete (ClientResponse.class);
			}

			@Override
			protected void succeeded ()
			{
				super.succeeded ();
				refreshTransactionList ();
			}

			@Override
			protected void cancelled ()
			{
				super.cancelled ();
				refreshTransactionList ();
			}

			@Override
			protected void failed ()
			{
				super.failed ();
				refreshTransactionList ();
			}
		});
	}

	private void signTransaction (final PendingTransaction value)
	{
		String passphrase = SignTransactionDialog.requestPassphrase ();
		try
		{
			App.instance.vault.sign (value, passphrase);
			App.instance.restClient.submitRestCall (new RestTask<ClientResponse> ()
			{
				@Override
				protected ClientResponse call (WebResource resource)
				{
					return resource.path ("/transactions/" + value.getId ())
							.type (MediaType.APPLICATION_JSON)
							.accept (MediaType.APPLICATION_JSON)
							.put (ClientResponse.class, value);
				}

				@Override
				protected void succeeded ()
				{
					super.succeeded ();
					refreshTransactionList ();
				}

				@Override
				protected void cancelled ()
				{
					super.cancelled ();
					refreshTransactionList ();
				}

				@Override
				protected void failed ()
				{
					super.failed ();
					refreshTransactionList ();
				}
			});
		}
		catch ( ValidationException e )
		{
			ErrorDialog.showError (e);
		}
	}

	public void sendTransaction ()
	{
		final NewTransaction nt = new NewTransaction (address.get ().right ().get (),
				btc.get ().right ().get ());

		App.instance.restClient.submitRestCall (new RestTask<PendingTransaction> ()
		{
			@Override
			protected PendingTransaction call (WebResource resource)
			{
				return resource.path ("/transactions")
						.type (MediaType.APPLICATION_JSON)
						.accept (MediaType.APPLICATION_JSON)
						.post (PendingTransaction.class, nt);
			}

			@Override
			protected void succeeded ()
			{
				super.succeeded ();
				transactionListPane.getChildren ().add (0, createEntry (getValue ()));
				signTransaction (getValue ());
			}
		});
	}

}
