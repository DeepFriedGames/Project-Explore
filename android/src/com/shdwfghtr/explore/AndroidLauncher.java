package com.shdwfghtr.explore;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.shdwfghtr.explore.GdxGame;

public class AndroidLauncher extends AndroidApplication implements ActionResolver {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new GdxGame(this), config);
	}

	@Override
	public void signIn() {

	}

	@Override
	public void signOut() {

	}

	@Override
	public void rateGame() {

	}

	@Override
	public void submitScore(long score) {

	}

	@Override
	public void unlockAchievement(String id) {

	}

	@Override
	public void showScores() {

	}

	@Override
	public void showAchievements() {

	}

	@Override
	public boolean isSignedIn() {
		return false;
	}
}
