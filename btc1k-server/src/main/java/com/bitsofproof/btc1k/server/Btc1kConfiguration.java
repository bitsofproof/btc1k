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

import com.bitsofproof.btc1k.server.vault.Vault;
import com.bitsofproof.supernode.api.BCSAPIException;
import io.dropwizard.Configuration;

import com.bitsofproof.dropwizard.supernode.SupernodeConfiguration;
import com.bitsofproof.dropwizard.supernode.activemq.SupernodeConfigurationImpl;
import com.bitsofproof.supernode.common.ByteUtils;
import com.bitsofproof.supernode.common.ECPublicKey;
import com.bitsofproof.supernode.common.ExtendedKey;
import com.bitsofproof.supernode.common.ValidationException;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class Btc1kConfiguration extends Configuration
{
	public static class VaultConfiguration
	{
		private Map<String, String> keys;

		public Map<String, String> getKeys ()
		{
			return keys;
		}

		public void setKeys (Map<String, String> keys)
		{
			this.keys = keys;
		}

		public Vault createVault() throws ValidationException, BCSAPIException
		{
			Vault vault = new Vault ();
			for (Map.Entry<String, String> keyEntry : keys.entrySet ())
			{
				vault.addKey (keyEntry.getKey (), new ECPublicKey (ByteUtils.fromHex (keyEntry.getValue ()), true));
			}
			return vault;
		}
	}

	@JsonProperty
	SupernodeConfigurationImpl supernode;

	@JsonProperty("vault")
	VaultConfiguration vaultFactory;

	private String masterKey;

	private Integer customerId;

	private String passphrase;

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

	public VaultConfiguration getVaultFactory ()
	{
		return vaultFactory;
	}

	public SupernodeConfiguration getSupernode ()
	{
		return supernode;
	}

}
