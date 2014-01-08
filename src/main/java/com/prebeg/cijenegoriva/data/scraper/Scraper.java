package com.prebeg.cijenegoriva.data.scraper;

import com.gargoylesoftware.htmlunit.WebClient;
import com.prebeg.cijenegoriva.model.Gorivo;
import java.util.List;

public interface Scraper {

	public List<Gorivo> scrape(WebClient wc);

}
