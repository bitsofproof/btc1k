package com.bitsofproof.btc1k.fx;

import com.atlassian.fugue.Either;
import com.bitsofproof.btc1k.fx.rest.RestTask;
import com.bitsofproof.btc1k.server.resource.NewTransaction;
import com.bitsofproof.btc1k.server.vault.PendingTransaction;
import com.bitsofproof.supernode.api.Address;
import com.bitsofproof.supernode.common.ValidationException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import javafx.util.converter.BigDecimalStringConverter;

import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

import static com.bitsofproof.btc1k.fx.EitherConverters.adapter;
import static com.bitsofproof.btc1k.fx.EitherConverters.addressConverter;
import static com.bitsofproof.btc1k.fx.EitherConverters.validateTextField;

public class MainController
{
	@FXML
	TitledPane newTransactionPane;

	@FXML
	TextField targetAddress;

	ObjectProperty<Either<String, Address>> address = new SimpleObjectProperty<> (this, "address");

	@FXML
	TextField amount;

	ObjectProperty<Either<String, BigDecimal>> btc = new SimpleObjectProperty<> (this, "btc");

	@FXML
	Button sendButton;

	@FXML
	Label messageLabel;

	@FXML
	VBox transactionListPane;

	public void initialize ()
	{
		refreshTransactionList ();

		messageLabel.textProperty ().bind (Bindings.selectString (App.instance.restClient.currentTaskProperty (), "title"));

		newTransactionPane.setExpanded (false);

		validateTextField (targetAddress, address, addressConverter ());
		validateTextField (amount, btc, adapter (new BigDecimalStringConverter ()));

		sendButton.disableProperty ().bind (address.isNull ().or (btc.isNull ()));
		refreshTransactionList ();
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
				               .get (new GenericType<List<PendingTransaction>> () {});
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
		for (PendingTransaction pendingTransaction : transactionList)
		{
			transactionListPane.getChildren ().add (createEntry (pendingTransaction));
		}
	}

	private PendingTransactionEntry createEntry (final PendingTransaction pt)
	{
		final PendingTransactionEntry entry = new PendingTransactionEntry (pt);
		entry.signHandler (new EventHandler<ActionEvent> ()
		{
			public void handle (ActionEvent actionEvent)
			{
				signTransaction (pt);
			}
		});
		entry.rejectHandler (new EventHandler<ActionEvent> ()
		{
			public void handle (ActionEvent actionEvent)
			{
				rejectTransaction (pt);
			}
		});

		return entry;
	}

	private void txAdded (URI uri)
	{
		App.instance.restClient.submitRestCall (new RestTask<PendingTransaction> (uri)
		{
			@Override
			protected PendingTransaction call (WebResource resource)
			{
				return resource
						.accept (MediaType.APPLICATION_JSON)
						.get (PendingTransaction.class);
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
			App.instance.vault.sign (value.getTransaction (), passphrase);
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
		catch (ValidationException e)
		{
			throw new RuntimeException (e);
			// TODO display error dialog
		}
	}

	public void sendTransaction ()
	{
		final NewTransaction nt = new NewTransaction (address.get ().right ().get (),
		                                              btc.get ().right ().get ());

		App.instance.restClient.submitRestCall (new RestTask<ClientResponse> ()
		{
			@Override
			protected ClientResponse call (WebResource resource)
			{
				return resource.path ("/transactions")
				               .type (MediaType.APPLICATION_JSON)
				               .accept (MediaType.APPLICATION_JSON)
				               .post (ClientResponse.class, nt);
			}

			@Override
			protected void succeeded ()
			{
				super.succeeded ();
				ClientResponse response = getValue ();
				if (response.getStatus () == 201)
				{
					txAdded (response.getLocation ());
				}
				else
				{
					throw new RuntimeException (response.getEntity (ClientResponse.class).toString ());
				}
			}
		});
	}


}
