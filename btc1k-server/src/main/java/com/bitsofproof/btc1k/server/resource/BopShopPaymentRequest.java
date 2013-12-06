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

public class BopShopPaymentRequest
{
	private Map<String, Object> _links;
	private String address;
	private Long amount;
	private Long billingAmount;
	private Long billingCurrency;
	private String callback;
	private Map<String, Object> callbackParameters;
	private Integer child;
	private Integer confirmations;
	private String createdAt;
	private List<BopShopEvent> events;
	private String id;
	private Long paid;
	private String provisionAddress;
	private String provisionAmount;
	private String publicKey;
	private String requestCurrency;
	private Integer requiredConfirmations;
	private Integer sequenceNr;
	private BopShopPaymentRequestState state;
	private String timeout;
	private String title;

	public String getAddress ()
	{
		return address;
	}

	public void setAddress (String address)
	{
		this.address = address;
	}

	public Long getAmount ()
	{
		return amount;
	}

	public void setAmount (Long amount)
	{
		this.amount = amount;
	}

	public Long getBillingAmount ()
	{
		return billingAmount;
	}

	public void setBillingAmount (Long billingAmount)
	{
		this.billingAmount = billingAmount;
	}

	public Long getBillingCurrency ()
	{
		return billingCurrency;
	}

	public void setBillingCurrency (Long billingCurrency)
	{
		this.billingCurrency = billingCurrency;
	}

	public String getCallback ()
	{
		return callback;
	}

	public void setCallback (String callback)
	{
		this.callback = callback;
	}

	public Map<String, Object> getCallbackParameters ()
	{
		return callbackParameters;
	}

	public Map<String, Object> get_links ()
	{
		return _links;
	}

	public void set_links (Map<String, Object> _links)
	{
		this._links = _links;
	}

	public void setCallbackParameters (Map<String, Object> callbackParameters)
	{
		this.callbackParameters = callbackParameters;
	}

	public Integer getChild ()
	{
		return child;
	}

	public void setChild (Integer child)
	{
		this.child = child;
	}

	public Integer getConfirmations ()
	{
		return confirmations;
	}

	public void setConfirmations (Integer confirmations)
	{
		this.confirmations = confirmations;
	}

	public String getCreatedAt ()
	{
		return createdAt;
	}

	public void setCreatedAt (String createdAt)
	{
		this.createdAt = createdAt;
	}

	public List<BopShopEvent> getEvents ()
	{
		return events;
	}

	public void setEvents (List<BopShopEvent> events)
	{
		this.events = events;
	}

	public String getId ()
	{
		return id;
	}

	public void setId (String id)
	{
		this.id = id;
	}

	public Long getPaid ()
	{
		return paid;
	}

	public void setPaid (Long paid)
	{
		this.paid = paid;
	}

	public String getProvisionAddress ()
	{
		return provisionAddress;
	}

	public void setProvisionAddress (String provisionAddress)
	{
		this.provisionAddress = provisionAddress;
	}

	public String getProvisionAmount ()
	{
		return provisionAmount;
	}

	public void setProvisionAmount (String provisionAmount)
	{
		this.provisionAmount = provisionAmount;
	}

	public String getPublicKey ()
	{
		return publicKey;
	}

	public void setPublicKey (String publicKey)
	{
		this.publicKey = publicKey;
	}

	public String getRequestCurrency ()
	{
		return requestCurrency;
	}

	public void setRequestCurrency (String requestCurrency)
	{
		this.requestCurrency = requestCurrency;
	}

	public Integer getRequiredConfirmations ()
	{
		return requiredConfirmations;
	}

	public void setRequiredConfirmations (Integer requiredConfirmations)
	{
		this.requiredConfirmations = requiredConfirmations;
	}

	public Integer getSequenceNr ()
	{
		return sequenceNr;
	}

	public void setSequenceNr (Integer sequenceNr)
	{
		this.sequenceNr = sequenceNr;
	}

	public BopShopPaymentRequestState getState ()
	{
		return state;
	}

	public void setState (BopShopPaymentRequestState state)
	{
		this.state = state;
	}

	public String getTimeout ()
	{
		return timeout;
	}

	public void setTimeout (String timeout)
	{
		this.timeout = timeout;
	}

	public String getTitle ()
	{
		return title;
	}

	public void setTitle (String title)
	{
		this.title = title;
	}

}
