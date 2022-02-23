package com.shdwfghtr.explore;

interface ActionResolver {
	void signIn();
	void signOut();
	void rateGame();
	void submitScore(long score);
	void unlockAchievement(String id);
	void showScores();
	void showAchievements();
	boolean isSignedIn();
}
