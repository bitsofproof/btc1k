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
package com.bitsofproof.btc1k.server.resource;

import java.math.BigDecimal;

import com.bitsofproof.supernode.api.Address;

public class NewTransaction
{
	private Address address;

	private BigDecimal amount;

	public NewTransaction ()
	{
	}

	public NewTransaction (Address targetAddress, BigDecimal amount)
	{
		this.address = targetAddress;
		this.amount = amount;
	}

	public Address getAddress ()
	{
		return address;
	}

	public void setAddress (Address address)
	{
		this.address = address;
	}

	public BigDecimal getAmount ()
	{
		return amount;
	}

	public void setAmount (BigDecimal amount)
	{
		this.amount = amount;
	}
}
