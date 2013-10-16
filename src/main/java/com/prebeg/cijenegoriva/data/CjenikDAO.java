package com.prebeg.cijenegoriva.data;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.prebeg.cijenegoriva.data.scraper.ScraperEngine;
import com.prebeg.cijenegoriva.data.scraper.ScraperService;
import com.prebeg.cijenegoriva.model.Cjenik;

@Component
public class CjenikDAO {

	
	@Resource 
	CachedCjenik cachedCjenik;
	
	
	@Resource(name = "scraperService")
	ScraperService scraperService; 
	
	@Resource
	CjenikFilterEngine filterEngine;
	
	public Cjenik getCjenik (String kategorijaFilter, String distributeriFilter, String autocestaFilter)  {
		
	
		Cjenik cjenik = cachedCjenik.getCjenik();
		
		
		// in bootstrap time, maybe xml isn't there
		
		if (cjenik == null) {
			scraperService.runScrapers();
			cjenik = cachedCjenik.getCjenik();
			
		}
	
		
		filterEngine.filter(cjenik, kategorijaFilter, distributeriFilter, autocestaFilter);
		
		cjenik.sort();
		
		return cjenik;
		
	}
}
