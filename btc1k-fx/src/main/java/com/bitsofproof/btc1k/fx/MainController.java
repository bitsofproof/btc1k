package com.bitsofproof.btc1k.fx;

import com.atlassian.fugue.Either;
import com.bitsofproof.supernode.api.Address;
import com.bitsofproof.supernode.common.ValidationException;
import com.google.common.base.Strings;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.util.StringConverter;
import javafx.util.converter.BigDecimalStringConverter;

import java.math.BigDecimal;
import java.util.logging.Logger;

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

	public void initialize ()
	{
		newTransactionPane.setExpanded (false);

		EitherConverters.validateTextField (targetAddress, address, EitherConverters.addressConverter ());
		EitherConverters.validateTextField (amount, btc, EitherConverters.adapter (new BigDecimalStringConverter ()));


	}

	public void sendTransaction ()
	{

	}


}
