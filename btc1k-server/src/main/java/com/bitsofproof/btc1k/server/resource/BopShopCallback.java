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

import java.util.List;
import java.util.Map;

public class BopShopCallback
{
	private String paymentRequestId;
	private String title;
	private Long paidAmount;
	private Long requestedAmount;
	private Long confirmations;
	private String address;
	private Long requiredConfirmations;
	private String transactionHash;
	private BopShopCallbackReason reason;
	private Integer child;
	private Map<String, Object> callbackParameters;
	private List<BopShopEvent> events;

	public String getPaymentRequestId ()
	{
		return paymentRequestId;
	}

	public void setPaymentRequestId (String paymentRequestId)
	{
		this.paymentRequestId = paymentRequestId;
	}

	public String getTitle ()
	{
		return title;
	}

	public void setTitle (String title)
	{
		this.title = title;
	}

	public List<BopShopEvent> getEvents ()
	{
		return events;
	}

	public void setEvents (List<BopShopEvent> events)
	{
		this.events = events;
	}

	public Long getPaidAmount ()
	{
		return paidAmount;
	}

	public void setPaidAmount (Long paidAmount)
	{
		this.paidAmount = paidAmount;
	}

	public Long getRequestedAmount ()
	{
		return requestedAmount;
	}

	public void setRequestedAmount (Long requestedAmount)
	{
		this.requestedAmount = requestedAmount;
	}

	public Long getConfirmations ()
	{
		return confirmations;
	}

	public void setConfirmations (Long confirmations)
	{
		this.confirmations = confirmations;
	}

	public String getAddress ()
	{
		return address;
	}

	public void setAddress (String address)
	{
		this.address = address;
	}

	public Long getRequiredConfirmations ()
	{
		return requiredConfirmations;
	}

	public void setRequiredConfirmations (Long requiredConfirmations)
	{
		this.requiredConfirmations = requiredConfirmations;
	}

	public String getTransactionHash ()
	{
		return transactionHash;
	}

	public void setTransactionHash (String transactionHash)
	{
		this.transactionHash = transactionHash;
	}

	public BopShopCallbackReason getReason ()
	{
		return reason;
	}

	public void setReason (BopShopCallbackReason reason)
	{
		this.reason = reason;
	}

	public Map<String, Object> getCallbackParameters ()
	{
		return callbackParameters;
	}

	public void setCallbackParameters (Map<String, Object> callbackParameters)
	{
		this.callbackParameters = callbackParameters;
	}

	@Override
	public String toString ()
	{
		return paymentRequestId;
	}

	public Integer getChild ()
	{
		return child;
	}

	public void setChild (Integer child)
	{
		this.child = child;
	}
}
