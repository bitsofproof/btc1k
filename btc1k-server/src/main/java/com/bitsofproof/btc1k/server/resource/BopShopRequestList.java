package com.bitsofproof.btc1k.server.resource;

import java.util.Map;

public class BopShopRequestList
{
	private Map<String, Object> _embedded;
	private Map<String, Object> _links;
	private Integer entriesPerPage;
	private String fromDate;
	private Integer page;
	private String toDate;

	public Map<String, Object> get_embedded ()
	{
		return _embedded;
	}

	public void set_embedded (Map<String, Object> _embedded)
	{
		this._embedded = _embedded;
	}

	public Map<String, Object> get_links ()
	{
		return _links;
	}

	public void set_links (Map<String, Object> _links)
	{
		this._links = _links;
	}

	public Integer getEntriesPerPage ()
	{
		return entriesPerPage;
	}

	public void setEntriesPerPage (Integer entriesPerPage)
	{
		this.entriesPerPage = entriesPerPage;
	}

	public String getFromDate ()
	{
		return fromDate;
	}

	public void setFromDate (String fromDate)
	{
		this.fromDate = fromDate;
	}

	public Integer getPage ()
	{
		return page;
	}

	public void setPage (Integer page)
	{
		this.page = page;
	}

	public String getToDate ()
	{
		return toDate;
	}

	public void setToDate (String toDate)
	{
		this.toDate = toDate;
	}

}
