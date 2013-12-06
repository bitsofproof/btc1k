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


public class BopShopEvent
{
	private Long amount;
	private String blockHash;
	private Integer blockHeight;
	private String createdAt;
	private Integer deliveryAttempts;
	private Boolean doubleSpend;
	private BopShopEventType eventType;
	private Integer ix;
	private String lastDeliveryAttempt;
	private String scheduledDeliveryAttempt;
	private String script;
	private BopShopEventState state;
	private String txHash;
	private Integer keyNumber;
	private String claimingTxHash;

	public BopShopEventType getEventType ()
	{
		return eventType;
	}

	public void setEventType (BopShopEventType eventType)
	{
		this.eventType = eventType;
	}

	public BopShopEventState getState ()
	{
		return state;
	}

	public void setState (BopShopEventState state)
	{
		this.state = state;
	}

	public String getCreatedAt ()
	{
		return createdAt;
	}

	public void setCreatedAt (String createdAt)
	{
		this.createdAt = createdAt;
	}

	public Integer getDeliveryAttempts ()
	{
		return deliveryAttempts;
	}

	public void setDeliveryAttempts (Integer deliveryAttempts)
	{
		this.deliveryAttempts = deliveryAttempts;
	}

	public String getLastDeliveryAttempt ()
	{
		return lastDeliveryAttempt;
	}

	public void setLastDeliveryAttempt (String lastDeliveryAttempt)
	{
		this.lastDeliveryAttempt = lastDeliveryAttempt;
	}

	public String getBlockHash ()
	{
		return blockHash;
	}

	public void setBlockHash (String blockHash)
	{
		this.blockHash = blockHash;
	}

	public Integer getBlockHeight ()
	{
		return blockHeight;
	}

	public void setBlockHeight (Integer blockHeight)
	{
		this.blockHeight = blockHeight;
	}

	public String getScheduledDeliveryAttempt ()
	{
		return scheduledDeliveryAttempt;
	}

	public void setScheduledDeliveryAttempt (String scheduledDeliveryAttempt)
	{
		this.scheduledDeliveryAttempt = scheduledDeliveryAttempt;
	}

	public String getTxHash ()
	{
		return txHash;
	}

	public void setTxHash (String txHash)
	{
		this.txHash = txHash;
	}

	public Integer getIx ()
	{
		return ix;
	}

	public void setIx (Integer ix)
	{
		this.ix = ix;
	}

	public String getScript ()
	{
		return script;
	}

	public void setScript (String script)
	{
		this.script = script;
	}

	public Long getAmount ()
	{
		return amount;
	}

	public void setAmount (Long amount)
	{
		this.amount = amount;
	}

	public Boolean getDoubleSpend ()
	{
		return doubleSpend;
	}

	public void setDoubleSpend (Boolean doubleSpend)
	{
		this.doubleSpend = doubleSpend;
	}

	public Integer getKeyNumber ()
	{
		return keyNumber;
	}

	public void setKeyNumber (Integer keyNumber)
	{
		this.keyNumber = keyNumber;
	}

	public String getClaimingTxHash ()
	{
		return claimingTxHash;
	}

	public void setClaimingTxHash (String claimingTxHash)
	{
		this.claimingTxHash = claimingTxHash;
	}

}
