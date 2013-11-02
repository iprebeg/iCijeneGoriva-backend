package com.prebeg.cijenegoriva.data.scraper;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.prebeg.cijenegoriva.data.CachedCjenik;
import com.prebeg.cijenegoriva.model.Cjenik;
import com.prebeg.cijenegoriva.notification.NotificationService;

@Service("scraperService")
public class ScraperService {
	
	private static final Logger logger = LoggerFactory.getLogger(ScraperService.class);
	
	@Resource
	NotificationService notificationService;
	
	@Resource
	ScraperEngine scraperEngine;
	
	@Resource 
	CachedCjenik cachedCjenik;
	
	@PostConstruct
	public void afterPropertiesSet() {
		this.runScrapers();
	}

	public void runScrapers() {
		
		logger.info("running scraper task");
		
		Cjenik cjenik = scraperEngine.scrape();
		
		if (cjenik != null) {
			cachedCjenik.saveCjenik(cjenik);
		}
	
		if (notificationService != null)
			notificationService.sendEmail("[iProxy] CijeneGoriva scraping results", cjenik.toString());
	}

	public void setScraperEngine(ScraperEngine scraperEngine) {
		this.scraperEngine = scraperEngine;
	}
	
	public void setCachedCjenik(CachedCjenik cachedCjenik) {
		this.cachedCjenik = cachedCjenik;
	}
}
