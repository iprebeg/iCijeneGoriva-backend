package com.prebeg.cijenegoriva.data.scraper;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.prebeg.cijenegoriva.model.Cjenik;
import com.prebeg.cijenegoriva.model.Gorivo;

@Component("scraperEngine")
public class ScraperEngine {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(ScraperEngine.class);
	
	@Resource
	INAScraper inaScraper;
	
	@Resource
	PetrolScraper petrolScraper;
	
	@Resource
	PetrolScraperLegacy petrolScraperLegacy;

	@Resource
	TifonScraper tifonScraper;
	
	@Resource
	CroBenzScraper crobenzScraper;
	
	@Resource
	LukoilScraper lukoilScraper;
	
	@Resource
	OMVScraper omvScraper;
	
	@Resource
	EuroPetrolScraper euroPetrolScraper;
		
  @Resource
  CroduxScraper croduxScraper; 

	private WebClient wc = null;
	
	public ScraperEngine () {
		wc = new WebClient(BrowserVersion.FIREFOX_3_6);
		wc.setJavaScriptEnabled(false);
		wc.setCssEnabled(false);
	}
	
	public Cjenik scrape() {
		return runScrapers();	
	}
	
	public Cjenik runScrapers() {
		
		Cjenik cjenik = new Cjenik();
		List<Gorivo> newGoriva = null;

			/*
			newGoriva = omvScraper.scrape(wc);
			if (newGoriva != null)
				cjenik.addGoriva(newGoriva);

			*/

		  newGoriva = croduxScraper.scrape(wc);
			if (newGoriva != null)
				cjenik.addGoriva(newGoriva);
		
		
			newGoriva = tifonScraper.scrape(wc);
			if (newGoriva != null)
				cjenik.addGoriva(newGoriva);
			
			newGoriva = inaScraper.scrape(wc);
			if (newGoriva != null)
				cjenik.addGoriva(newGoriva);
			
			newGoriva = petrolScraper.scrape(wc);
			if (newGoriva != null)
				cjenik.addGoriva(newGoriva);			
			
			newGoriva = lukoilScraper.scrape(wc);
			if (newGoriva != null)
				cjenik.addGoriva(newGoriva);
			
			/*
			newGoriva = crobenzScraper.scrape(wc);
			if (newGoriva != null)
				cjenik.addGoriva(newGoriva);
			*/
			
			/*
			newGoriva = euroPetrolScraper.scrape(wc);
			if (newGoriva != null)
				cjenik.addGoriva(newGoriva);
			*/

		for (Gorivo gorivo : cjenik.getGoriva()) {
			System.out.println("Distributer:" + gorivo.getDistributer() + ";Naziv:" + gorivo.getNaziv() + ";Datum:" + gorivo.getDatum() + ";Cijena:" + gorivo.getCijena() + ";Kategorija:" + gorivo.getKategorija() + ";Autocesta:" + gorivo.getAutocesta());
		}
		
		return cjenik;
	}

	public PetrolScraperLegacy getPetrolScraperLegacy() {
		return petrolScraperLegacy;
	}

	public void setPetrolScraperLegacy(PetrolScraperLegacy petrolScraperLegacy) {
		this.petrolScraperLegacy = petrolScraperLegacy;
	}

	public void setEuroPetrolScraper(EuroPetrolScraper euroPetrolScraper) {
		this.euroPetrolScraper = euroPetrolScraper;
	}
	
	public void setInaScraper(INAScraper inaScraper) {
		this.inaScraper = inaScraper;
	}


	public void setPetrolScraper(PetrolScraper petrolScraper) {
		this.petrolScraper = petrolScraper;
	}


	public void setTifonScraper(TifonScraper tifonScraper) {
		this.tifonScraper = tifonScraper;
	}

	public void setCrobenzScraper(CroBenzScraper crobenzScraper) {
		this.crobenzScraper = crobenzScraper;
	}


	public void setLukoilScraper(LukoilScraper lukoilScraper) {
		this.lukoilScraper = lukoilScraper;
	}


	public void setOmvScraper(OMVScraper omvScraper) {
		this.omvScraper = omvScraper;
	}
}
