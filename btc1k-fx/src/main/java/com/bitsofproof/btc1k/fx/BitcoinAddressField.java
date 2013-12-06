package com.bitsofproof.btc1k.fx;

import com.atlassian.fugue.Either;
import com.bitsofproof.supernode.api.Address;
import com.bitsofproof.supernode.common.ValidationException;
import com.google.common.base.Strings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.util.StringConverter;

public class BitcoinAddressField extends ObjectTextField<Either<String, Address>>
{
	private static final class AddressConverter extends StringConverter<Either<String, Address>>
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
				return Either.left (e.getMessage ());
			}
		}
	}

	public BitcoinAddressField ()
	{
		super(new AddressConverter (), null);

		setPromptText ("Bitcoin address");
	}
}
