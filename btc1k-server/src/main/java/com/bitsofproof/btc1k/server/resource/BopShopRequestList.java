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
