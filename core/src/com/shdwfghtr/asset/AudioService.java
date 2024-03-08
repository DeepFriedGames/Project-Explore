package com.shdwfghtr.asset;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class AudioService {
    private final AssetManager assetManager;
    private Music music;
    private Music queue;
    private float fadeTime = 3;
    private float MAX_VOLUME = OptionsService.GetVolume();
    private float volume = MAX_VOLUME;
    private MusicState currentState, previousState;
    
    public AudioService(AssetManagerService assetService) {
        this.assetManager = assetService.getAssetManager();
    }

    public void queueMusic(String name) {
        Music m = null;
        if(assetManager.isLoaded("audio/" + name + ".ogg", Music.class))
            m = assetManager.get("audio/" + name + ".ogg", Music.class);
        else System.out.println("audio/" + name + ".ogg not loaded!");
        queueMusic(m);
    }

    public void queueMusic(Music m) {
        queue = m;
    }

    public void setMusic(String name, boolean loop) {
        Music m = null;
        if(assetManager.isLoaded("audio/" + name + ".ogg", Music.class))
            m = assetManager.get("audio/" + name + ".ogg", Music.class);
        else System.out.println("audio/" + name + ".ogg not loaded!");
        setMusic(m, loop);
    }

    public void setMusic(Music m, boolean loop) {
        if(m == null) {
            setState(MusicState.EJECTED);
            System.out.println("music is null");
        } else if(m == music) {
            System.out.println(m + " is already playing!");
        } else {
            if(music != null) music.stop();
            if(loop) setState(MusicState.LOOPING);
            else setState(MusicState.PLAYING);
            music = m;
            music.setVolume(volume);
            music.play();
        }
    }

    public void playSound(String name) {
        if(assetManager.isLoaded("audio/" + name + ".wav", Sound.class)) {
            Sound s = assetManager.get("audio/" + name + ".wav", Sound.class);
            s.play(MAX_VOLUME);
        } else System.out.println("audio/" + name + ".wav not loaded!");
    }

    public void playSound(String name, float vol) {
        if(assetManager.isLoaded("audio/" + name + ".wav", Sound.class)) {
            Sound s = assetManager.get("audio/" + name + ".wav", Sound.class);
            s.play(vol * MAX_VOLUME);
        } else System.out.println("audio/" + name + ".wav not loaded!");
    }

    public void playSound(String name, float vol, float pitch, float pan) {
        if(assetManager.isLoaded("audio/" + name + ".wav", Sound.class)) {
            Sound s = assetManager.get("audio/" + name + ".wav", Sound.class);
            s.play(vol * MAX_VOLUME, pitch, pan);
        } else System.out.println("audio/" + name + ".wav not loaded!");
    }

    public long playSound(String name, float vol, float pitch, float pan, boolean loop) {
        if(assetManager.isLoaded("audio/" + name + ".wav", Sound.class)) {
            Sound s = assetManager.get("audio/" + name + ".wav", Sound.class);
            long id = s.play(vol * MAX_VOLUME, pitch, pan);
            s.setLooping(id, loop);
            return id;
        } else System.out.println("audio/" + name + ".wav not loaded!");
        return 0;
    }

    public void update(float delta) {
        if(music != null) {
            switch(currentState) {
                case EJECTED:
                    break;
                case LOOPING:
                    if(!music.isLooping()) {
                        music.setLooping(true);
                    }
                    if(!music.isPlaying())
                        music.play();
                    break;
                case STOPPED:
                    if(music.isLooping() || music.isPlaying())
                        music.stop();
                    break;
                case PAUSED:
                    if(music.isLooping() || music.isPlaying())
                        music.pause();
                    break;
                case PLAYING:
                    if(!music.isPlaying())
                        music.play();
                    break;
                case FADEOUT:
                    if(music.isLooping() || music.isPlaying()) {
                        if(volume > 0.00f)
                            volume -= delta / fadeTime;
                        else
                            volume = 0.0f;
                        music.setVolume(Math.abs(volume));
                    }
                    break;
                case FADEIN:
                    if(music.isLooping() || music.isPlaying()) {
                        if(volume < MAX_VOLUME)
                            volume += delta / fadeTime;
                        else {
                            volume = MAX_VOLUME;
                            setState(previousState);
                        }
                        music.setVolume(Math.abs(volume));
                    }
                    break;
            }
        } else if(queue != null) {
            music = queue;
            queue = null;
        }
    }

    private void setState(MusicState state) {
        previousState = currentState;
        currentState = state;
    }

    public void fadeIn(float duration) {
        fadeTime = duration;
        setState(MusicState.FADEIN);
    }

    public void fadeOut(float duration) {
        fadeTime = duration;
        setState(MusicState.FADEOUT);
    }

    public void setVolume(float amount) {
        volume = amount * MAX_VOLUME;
        music.setVolume(volume);
    }

    public void setMaxVolume(float amount) {
        MAX_VOLUME = amount;
        volume = MAX_VOLUME;
        music.setVolume(MAX_VOLUME);
        OptionsService.SetVolume(MAX_VOLUME);
    }

    public float getMaxVolume() { return MAX_VOLUME; }

    public enum MusicState {
        EJECTED, PAUSED, FADEIN, FADEOUT, PLAYING, STOPPED, LOOPING
    }

}
