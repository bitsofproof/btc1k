package com.bitsofproof.btc1k.server.resource;

import com.bitsofproof.supernode.common.ByteUtils;
import com.bitsofproof.supernode.common.ECPublicKey;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NamedKey
{
	@JsonProperty
	private String name;
	@JsonProperty
	private String key;

	public NamedKey ()
	{
	}

	public NamedKey (String name, String key)
	{
		this.name = name;
		this.key = key;
	}

	public String getName ()
	{
		return name;
	}

	@JsonIgnore
	public ECPublicKey getPublicKey ()
	{
		return new ECPublicKey (ByteUtils.fromHex (key), true);
	}

	public String getKey ()
	{
		return key;
	}

	public void setName (String name)
	{
		this.name = name;
	}

	public void setKey (String key)
	{
		this.key = key;
	}

}
