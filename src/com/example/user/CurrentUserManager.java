package com.example.user;

import android.location.Location;

import com.google.android.maps.GeoPoint;

public class CurrentUserManager {
	private static CurrentUser currentUser;
	private static CurrentUser partner;

	public static CurrentUser getCurrentUser() {
		return currentUser;
	}

	public static void setCurrentUser(CurrentUser currentUser) {
		CurrentUserManager.currentUser = currentUser;
	}

	public static CurrentUser getPartner() {
		return partner;
	}

	public static void setPartner(CurrentUser partner) {
		CurrentUserManager.partner = partner;
	}
	public static void clearPartner()
	{
		CurrentUserManager.partner = null;
	}
	public static void clearCurrentUser()
	{
		CurrentUserManager.currentUser = null;
	}
}
