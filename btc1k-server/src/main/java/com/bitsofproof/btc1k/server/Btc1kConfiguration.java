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

import java.util.Map;

import com.bitsofproof.btc1k.server.vault.Vault;
import com.bitsofproof.dropwizard.supernode.SupernodeConfiguration;
import com.bitsofproof.dropwizard.supernode.activemq.JMSConnectedSupernode;
import com.bitsofproof.supernode.api.BCSAPIException;
import com.bitsofproof.supernode.common.ExtendedKey;
import com.bitsofproof.supernode.common.ValidationException;
import com.fasterxml.jackson.annotation.JsonProperty;

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

		public Vault createVault () throws ValidationException, BCSAPIException
		{
			Vault vault = new Vault (keys);
			return vault;
		}
	}

	@JsonProperty
	JMSConnectedSupernode externalServer;

	@JsonProperty ("vault")
	VaultConfiguration vaultFactory;

	private String bopShopKey;

	private Integer customerId;

	private String passphrase;

	public ExtendedKey getBopShopKey () throws ValidationException
	{
		return ExtendedKey.parse (bopShopKey);
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
		return externalServer;
	}

}
