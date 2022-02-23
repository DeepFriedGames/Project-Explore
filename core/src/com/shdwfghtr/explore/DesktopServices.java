package com.shdwfghtr.explore;

public class DesktopServices implements ActionResolver{

	@Override
	public void signIn() {
	System.out.println("DesktopGoogleServies: signIn()");
	}

	@Override
	public void signOut() {
	System.out.println("DesktopGoogleServies: signOut()");
	}

	@Override
	public void rateGame() {
	System.out.println("DesktopGoogleServices: rateGame()");
	}

	@Override
	public void submitScore(long score) {
	System.out.println("DesktopGoogleServies: submitScore(" + score + ")");
	}

	@Override
	public void showScores() {
	System.out.println("DesktopGoogleServies: showScores()");
	}

	@Override
	public boolean isSignedIn() {
	System.out.println("DesktopGoogleServies: isSignedIn()");
	return false;
	}

	@Override
	public void unlockAchievement(String id) {
		System.out.println("DesktopGoogleServies: unlockAchievement(" + id + ")");
		
	}

	@Override
	public void showAchievements() {
		System.out.println("DesktopGoogleServies: showAchievements()");
		
	}
}
