package com.example.user;

import android.location.Location;


public class CurrentUser {

	private String name;
	private String partner_email;
	private Double latitude, longitude;
	private String gender;
	private String email, photo,update_at;
	
	public CurrentUser(String name, String email, String partner_email, String gender, String photo, Double lat, Double lon, String update_at){
		this.name =name;
		this.partner_email = partner_email;
		this.gender = gender;
		this.email = email;
		this.latitude = 0.0;
		this.longitude = 0.0;
		this.photo = photo;
		this.latitude = lat;
		this.longitude = lon;
		this.update_at = update_at;
	}
	public Location getLocation(){
		Location lo = new Location("a location");
		lo.setLatitude(latitude);
		lo.setLongitude(longitude);
		return lo;
	}
	public Double getLatitude() {
		return latitude;
	}
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	public Double getLongitude() {
		return longitude;
	}
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
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

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getPhoto() {
		return photo;
	}
	public void setPhoto(String photo) {
		this.photo = photo;
	}
	public String getUpdate_at() {
		return update_at;
	}
	public void setUpdate_at(String update_at) {
		this.update_at = update_at;
	}

	
}
