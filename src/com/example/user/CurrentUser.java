package com.example.user;

import com.google.android.maps.GeoPoint;

public class CurrentUser {

	private String name;
	private String partner_name;
	private String city;
	private String country;
	private GeoPoint location;
	private String gender;
	
	public CurrentUser(String name, String partner_name, String country, String city, String gender){
		this.name =name;
		this.city = city;
		this.country = country;
		this.partner_name = partner_name;
		this.gender = gender;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPartner_name() {
		return partner_name;
	}

	public void setPartner_name(String partner_name) {
		this.partner_name = partner_name;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public GeoPoint getLocation() {
		return location;
	}

	public void setLocation(GeoPoint location) {
		this.location = location;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}
	
	


	
	
	
}
