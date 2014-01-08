package com.prebeg.cijenegoriva.data.scraper;

import java.util.List;
import java.util.ArrayList;

import javax.annotation.Resource;
import javax.annotation.PostConstruct;

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

  List<Scraper> scrapers;

  private WebClient getWebClient() {
		WebClient wc = null;
    wc = new WebClient(BrowserVersion.CHROME);
		wc.getOptions().setJavaScriptEnabled(false);
		wc.getOptions().setCssEnabled(false);
    return wc;
  }

  @PostConstruct
	public void postconstruct() {
    scrapers = new ArrayList<Scraper>();
    scrapers.add(croduxScraper);
    scrapers.add(tifonScraper);
    scrapers.add(inaScraper);
    scrapers.add(petrolScraper);
    scrapers.add(lukoilScraper);
	}
	
	public Cjenik scrape() {
		return runScrapers();	
	}
	
	public Cjenik runScrapers() {
		
		Cjenik cjenik = new Cjenik();
		List<Gorivo> newGoriva = null;

    for (Scraper scraper : scrapers)
    {
      WebClient wc = getWebClient();
		  newGoriva = scraper.scrape(wc);
			if (newGoriva != null)
				cjenik.addGoriva(newGoriva);
	    wc.closeAllWindows();
    }

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
