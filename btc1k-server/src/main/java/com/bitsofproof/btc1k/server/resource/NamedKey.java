package com.bitsofproof.btc1k.server.resource;

import com.bitsofproof.supernode.common.ByteUtils;
import com.bitsofproof.supernode.common.ECPublicKey;

public class NamedKey
{
	private String name;
	private String key;

	public String getName ()
	{
		return name;
	}

	public ECPublicKey getPublicKey ()
	{
		return new ECPublicKey (ByteUtils.fromHex (key), true);
	}

	public String getKey ()
	{
		return key;
	}

	public NamedKey (String name, String key)
	{
		this.name = name;
		this.key = key;
	}

	public void setName (String name)
	{
		this.name = name;
	}

	public void setKey (String key)
	{
		this.key = key;
	}

	public NamedKey ()
	{
	}
}
