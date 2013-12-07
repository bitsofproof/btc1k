/*
 * Copyright 2013 bits of proof zrt.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bitsofproof.btc1k.server;

import io.dropwizard.Configuration;

import com.bitsofproof.dropwizard.supernode.SupernodeConfiguration;
import com.bitsofproof.dropwizard.supernode.activemq.SupernodeConfigurationImpl;
import com.bitsofproof.supernode.common.ByteUtils;
import com.bitsofproof.supernode.common.ECPublicKey;
import com.bitsofproof.supernode.common.ExtendedKey;
import com.bitsofproof.supernode.common.ValidationException;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Btc1kConfiguration extends Configuration
{
	@JsonProperty
	SupernodeConfigurationImpl supernode;

	private String key1, key2, key3;
	private String name1, name2, name3;

	private String masterKey;

	private Integer customerId;

	private String passphrase;

	public SupernodeConfiguration getSupernode ()
	{
		return supernode;
	}

	public ECPublicKey getKey1 ()
	{
		return new ECPublicKey (ByteUtils.fromHex (key1), true);
	}

	public ECPublicKey getKey2 ()
	{
		return new ECPublicKey (ByteUtils.fromHex (key2), true);
	}

	public ECPublicKey getKey3 ()
	{
		return new ECPublicKey (ByteUtils.fromHex (key3), true);
	}

	public ExtendedKey getMasterKey () throws ValidationException
	{
		return ExtendedKey.parse (masterKey);
	}

	public Integer getCustomerId ()
	{
		return customerId;
	}

	public String getPassphrase ()
	{
		return passphrase;
	}

	public String getName1 ()
	{
		return name1;
	}

	public String getName2 ()
	{
		return name2;
	}

	public String getName3 ()
	{
		return name3;
	}

}
