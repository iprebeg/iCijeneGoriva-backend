package com.prebeg.cijenegoriva.model;

public class Gorivo implements Comparable<Gorivo> {
	String naziv;
	String kategorija;
	String distributer;
	String cijena;
	String datum;
	
	public String getDatum() {
		return datum;
	}
	public void setDatum(String datum) {
		this.datum = datum;
	}
	String autocesta;
	
	public String getAutocesta() {
		return autocesta;
	}
	public void setAutocesta(String autocesta) {
		this.autocesta = autocesta;
	}
	public String getNaziv() {
		return naziv;
	}
	public void setNaziv(String naziv) {
		this.naziv = naziv;
	}
	public String getKategorija() {
		return kategorija;
	}
	public void setKategorija(String kategorija) {
		this.kategorija = kategorija;
	}
	public String getDistributer() {
		return distributer;
	}
	public void setDistributer(String distributer) {
		this.distributer = distributer;
	}
	public String getCijena() {
		return cijena;
	}
	public void setCijena(String cijena) {
		this.cijena = cijena;
	}
	
	public int compareTo(Gorivo otherGorivo) { 
		
	    float thisCijena = Float.valueOf(this.cijena.replace(",", "."));
	    float otherCijena = Float.valueOf(otherGorivo.getCijena().replace(",", "."));
	    float res = thisCijena - otherCijena;
	    
	    if (res < 0)
	    	return -1;
	    else if (res > 0)
	    	return 1;
	    else return 0;
	}
}
