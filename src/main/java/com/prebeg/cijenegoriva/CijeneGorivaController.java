package com.prebeg.cijenegoriva;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.prebeg.cijenegoriva.data.CjenikDAO;
import com.prebeg.cijenegoriva.data.scraper.ScraperEngine;
import com.prebeg.cijenegoriva.model.Cjenik;


/**
 * Handles requests for the application home page.
 */
@Controller
public class CijeneGorivaController {

	private static final Logger logger = LoggerFactory.getLogger(CijeneGorivaController.class);
	
	@Resource
	ScraperEngine scraperEngine; 
	
	@Resource
	CjenikDAO cjenikDAO;

	/**
	 * Simply selects the home view to render by returning its name.
	 */
	
	@RequestMapping(value="/cjenik", method=RequestMethod.GET)
	@ResponseBody
	public Cjenik get(@RequestParam("kategorija") String kategorija, @RequestParam("distributeri") String distributeri, 
			@RequestParam("Autocesta") String autocesta) {
		logger.info("Welcome to CijeneGoriva!");
		logger.info("Scraping goriva for category:" + kategorija);
		Cjenik cjenik = cjenikDAO.getCjenik(kategorija, distributeri, autocesta); 
		return cjenik;
	}
}

