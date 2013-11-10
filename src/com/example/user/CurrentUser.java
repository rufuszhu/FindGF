package com.example.user;

import com.google.android.maps.GeoPoint;

public class CurrentUser {

	private String name;
	private String partner_email;
	private String city;
	private String country;
	private GeoPoint location;
	private String gender;
	private String email;
	private CurrentUser partner;
	
	public CurrentUser(String name, String email, String partner_email, String country, String city, String gender){
		this.name =name;
		this.city = city;
		this.country = country;
		this.partner_email = partner_email;
		this.gender = gender;
		this.email = email;
		this.location = null;
		this.partner = null;
	}
	public CurrentUser getPartner() {
		return partner;
	}

	public void setPartner(CurrentUser partner) {
		this.partner = partner;
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setPartner_email(String partner_email) {
		this.partner_email = partner_email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPartner_email() {
		return partner_email;
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
