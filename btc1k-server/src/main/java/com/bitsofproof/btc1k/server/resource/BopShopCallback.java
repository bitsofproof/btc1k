package com.bitsofproof.btc1k.server.resource;

import java.util.Map;

public class BopShopCallback
{
	private String paymentRequestId;
	private Long paidAmount;
	private Long requestedAmount;
	private Long confirmations;
	private String address;
	private Long requiredConfirmations;
	private String transactionHash;
	private BopShopCallbackReason reason;
	private Map<String, Object> callbackParameters;

	public String getPaymentRequestId ()
	{
		return paymentRequestId;
	}

	public void setPaymentRequestId (String paymentRequestId)
	{
		this.paymentRequestId = paymentRequestId;
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
}
