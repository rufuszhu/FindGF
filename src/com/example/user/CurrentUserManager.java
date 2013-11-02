package com.example.user;

public class CurrentUserManager {
	private static CurrentUser currentUser;

	public static CurrentUser getCurrentUser() {
		return currentUser;
	}

	public static void setCurrentUser(CurrentUser currentUser) {
		CurrentUserManager.currentUser = currentUser;
	}

	
	

}
