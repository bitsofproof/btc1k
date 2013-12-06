package com.bitsofproof.btc1k.fx;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class ObjectTextField<T> extends TextField
{
	private ObjectProperty<T> objectValue = new SimpleObjectProperty<> (this, "objectValue");

	/**
	 *
	 * @param converter
	 * @param predicate If the value of the text field
	 */
	public ObjectTextField (final StringConverter<T> converter, final Callback<String, String> predicate)
	{
		super();

		Bindings.bindBidirectional (textProperty (), objectValue, converter);
	}

	public T getObjectValue ()
	{
		return objectValue.get ();
	}

	public ObjectProperty<T> objectValueProperty ()
	{
		return objectValue;
	}

	public void setObjectValue (T objectValue)
	{
		this.objectValue.set (objectValue);
	}
}
