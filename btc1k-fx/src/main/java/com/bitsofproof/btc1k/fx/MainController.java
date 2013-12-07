package com.bitsofproof.btc1k.fx;

import com.atlassian.fugue.Either;
import com.bitsofproof.btc1k.server.resource.NewTransaction;
import com.bitsofproof.btc1k.server.vault.PendingTransaction;
import com.bitsofproof.supernode.api.Address;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.converter.BigDecimalStringConverter;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
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
		Callback<WebResource, List<PendingTransaction>> callback = new Callback<WebResource, List<PendingTransaction>> ()
		{
			@Override
			public List<PendingTransaction> call (WebResource webResource)
			{
				return webResource.path ("/transactions/all")
						.accept (MediaType.APPLICATION_JSON)
						.get(new GenericType<List<PendingTransaction>> () {});
			}
		};
		RestClient.RestTask<List<PendingTransaction>> task = App.instance.restClient.submitRestCall ("Fetching transaction list", callback);
		task.setOnSucceeded (new EventHandler<WorkerStateEvent> () {
			@Override
			public void handle (WorkerStateEvent workerStateEvent)
			{
				List<PendingTransaction> transactionList = (List<PendingTransaction>) workerStateEvent.getSource ().getValue ();
				doRefreshTransactionList (transactionList);
			}
		});
		task.start ();

	}

	private void doRefreshTransactionList (List<PendingTransaction> transactionList)
	{
		transactionListPane.getChildren ().clear ();
		for (PendingTransaction pendingTransaction : transactionList)
		{
			PendingTransactionEntry entry = new PendingTransactionEntry (pendingTransaction);
			transactionListPane.getChildren ().add(entry);
		}
	}

	public void sendTransaction ()
	{
		final NewTransaction nt = new NewTransaction (address.get ().right ().get (),
		                                              btc.get ().right ().get ().movePointRight (8));

		Callback<WebResource, ClientResponse> callback = new Callback<WebResource, ClientResponse> ()
		{
			@Override
			public ClientResponse call (WebResource webResource)
			{
				return webResource.path ("/transactions")
				                  .type (MediaType.APPLICATION_JSON)
				                  .post (ClientResponse.class, nt);
			}
		};

		final RestClient.RestTask<ClientResponse> task = App.instance.restClient.submitRestCall ("Sending new transaction", callback);
		// TODO restclient is singlethreaded. Should be better to simply call refreshTransactionList, which will schedule the refresh task in the rest executor?
		task.setOnSucceeded (new EventHandler<WorkerStateEvent> ()
		{
			public void handle (WorkerStateEvent workerStateEvent)
			{
				ClientResponse response = task.getValue ();
				refreshTransactionList ();
			}
		});
		task.start ();
	}
}
