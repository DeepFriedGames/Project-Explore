package com.shdwfghtr.asset;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Disposable;

public class AssetManagerService implements Disposable {
    private final AssetManager assetManager;
    
    public AssetManagerService() {
        this.assetManager = new AssetManager();
        LoadAssets();
    }

    public float getProgress() {
        return assetManager.getProgress();
    }

    public AssetManager getAssetManager(){
        return assetManager;
    }
    
    void LoadAssets() {
        //here we load a butt ton of assets, which I would LOVE to do systematically, but java
        //doesn't support that, so brute force it is.  Just don't forget to add new assets here.
        this.assetManager.load("atlas/BG.atlas", TextureAtlas.class);
        this.assetManager.load("atlas/Entity.atlas", TextureAtlas.class);
        this.assetManager.load("atlas/Environment.atlas", TextureAtlas.class);
        this.assetManager.load("atlas/Explosions.atlas", TextureAtlas.class);
        this.assetManager.load("atlas/particle.atlas", TextureAtlas.class);
        this.assetManager.load("atlas/UI.atlas", TextureAtlas.class);

        this.assetManager.load("audio/ammo.wav", Sound.class);
        this.assetManager.load("audio/armor.wav", Sound.class);
        this.assetManager.load("audio/bomb.wav", Sound.class);
        this.assetManager.load("audio/boss_damage.wav", Sound.class);
        this.assetManager.load("audio/boss_explosion.wav", Sound.class);
        this.assetManager.load("audio/bullet.wav", Sound.class);
        this.assetManager.load("audio/damage1.wav", Sound.class);
        this.assetManager.load("audio/disruption.wav", Sound.class);
        this.assetManager.load("audio/door_close.wav", Sound.class);
        this.assetManager.load("audio/door_open.wav", Sound.class);
        this.assetManager.load("audio/enemy.wav", Sound.class);
        this.assetManager.load("audio/enemy_damage.wav", Sound.class);
        this.assetManager.load("audio/explosion.wav", Sound.class);
        this.assetManager.load("audio/footstep.wav", Sound.class);
        this.assetManager.load("audio/gnat.wav", Sound.class);
        this.assetManager.load("audio/item.wav", Sound.class);
        this.assetManager.load("audio/jump.wav", Sound.class);
        this.assetManager.load("audio/missile.wav", Sound.class);
        this.assetManager.load("audio/oxygen.wav", Sound.class);
        this.assetManager.load("audio/phase_shot.wav", Sound.class);
        this.assetManager.load("audio/player_damage.wav", Sound.class);
        this.assetManager.load("audio/power_up.wav", Sound.class);
        this.assetManager.load("audio/razor_jump.wav", Sound.class);
        this.assetManager.load("audio/select.wav", Sound.class);
        this.assetManager.load("audio/select_disabled.wav", Sound.class);
        this.assetManager.load("audio/switch.wav", Sound.class);
        this.assetManager.load("audio/wide_shot.wav", Sound.class);

        this.assetManager.load("audio/Bergamot.ogg", Music.class);
        this.assetManager.load("audio/Bergamot_Intro.ogg", Music.class);
        this.assetManager.load("audio/Intro.ogg", Music.class);
        this.assetManager.load("audio/Item.ogg", Music.class);
        this.assetManager.load("audio/World0.ogg", Music.class);
        this.assetManager.load("audio/World1.ogg", Music.class);
        this.assetManager.load("audio/World2.ogg", Music.class);
        this.assetManager.load("audio/World3.ogg", Music.class);
        this.assetManager.load("audio/World4.ogg", Music.class);

        this.assetManager.load("skin/font-export.fnt", BitmapFont.class);
        this.assetManager.load("skin/font-title-export.fnt", BitmapFont.class);
    }

    public <R> R GetResource(String fileName, Class<R> type) {
        return assetManager.get(fileName, type);
    }

    @Override
    public void dispose() {
        assetManager.dispose();
    }

    public boolean update() {
        return assetManager.update();
    }
}
