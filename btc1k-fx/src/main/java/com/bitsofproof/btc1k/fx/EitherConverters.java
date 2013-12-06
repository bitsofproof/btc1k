package com.bitsofproof.btc1k.fx;

import com.atlassian.fugue.Either;
import com.bitsofproof.supernode.api.Address;
import com.bitsofproof.supernode.common.ValidationException;
import com.google.common.base.Strings;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;
import javafx.scene.control.TooltipBuilder;
import javafx.util.StringConverter;

public class EitherConverters
{
	public static <T> void validateTextField (final TextField field,
	                                          ObjectProperty<Either<String, T>> property,
	                                          StringConverter<Either<String, T>> converter)
	{
		Bindings.bindBidirectional (field.textProperty (), property, converter);
		property.addListener (new ChangeListener<Either<String, T>> ()
		{
			@Override
			public void changed (ObservableValue<? extends Either<String, T>> observableValue,
			                     Either<String, T> old,
			                     Either<String, T> current)
			{
				if (current != null && current.isLeft ())
				{
					field.getStyleClass ().add ("validation_error");
					field.setTooltip (TooltipBuilder.create ()
					                                .text (current.left ().get ())
					                                .build ());
				}
				else
				{
					field.getStyleClass ().remove ("validation_error");
					field.setTooltip (null);
				}
			}
		});
	}

	public static <T> StringConverter<Either<String, T>> adapter (final StringConverter<T> converter)
	{
		return new StringConverter<Either<String, T>> ()
		{
			@Override
			public String toString (Either<String, T> t)
			{
				if (t == null || t.isLeft ())
					return null;

				return converter.toString (t.right ().get ());
			}

			@Override
			public Either<String, T> fromString (String s)
			{
				if (Strings.isNullOrEmpty (s))
					return null;

				try
				{
					return Either.right (converter.fromString (s));
				}
				catch (Exception e)
				{
					return Either.left (e.getMessage ());
				}
			}
		};
	}

	public static StringConverter<Either<String, Address>> addressConverter ()
	{
		return new StringConverter<Either<String, Address>> ()
		{
			@Override
			public String toString (Either<String, Address> address)
			{
				if (address == null || address.isLeft ())
					return null;

				return address.right ().toString ();
			}

			@Override
			public Either<String, Address> fromString (String s)
			{
				if (Strings.isNullOrEmpty (s))
					return null;

				try
				{
					return Either.right (Address.fromSatoshiStyle (s));
				}
				catch (ValidationException e)
				{
					return Either.left ("Invalid address");
				}
				catch (Exception e)
				{
					return Either.left (e.getMessage ());
				}
			}
		};
	}

}
