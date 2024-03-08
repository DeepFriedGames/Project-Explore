package com.shdwfghtr.asset;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;


public class OptionsService {
    private static final Preferences options = Gdx.app.getPreferences("options");

    public static boolean IsFullScreen(){
        return options.getBoolean("fullscreen", false);
    }

    public static float GetVolume(){
        return options.getFloat("volume");
    }

    public static void SetVolume(float value){
        options.putFloat("volume", value);
        options.flush();
    }

    public static boolean AreParticlesEnabled() {
        return options.getBoolean("particles", true);
    }

    public static void toggleParticlesEnabled() {
        options.putBoolean("particles", !options.getBoolean("particles"));
        options.flush();
    }

    public static void toggleFullscreen() {
        options.putBoolean("fullscreen", !options.getBoolean("fullscreen"));
        options.flush();
    }
}
