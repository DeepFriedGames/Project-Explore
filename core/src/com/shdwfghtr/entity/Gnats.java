package com.shdwfghtr.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.shdwfghtr.asset.PaletteService;
import com.shdwfghtr.explore.GdxGame;
import com.shdwfghtr.explore.World;
import com.shdwfghtr.screens.GameScreen;

/**
 * Created by Stuart on 7/15/2015.
 * Gnats are a type of @Enemy which latch onto the player and slowly drain their oxygen
 */
public class Gnats extends Enemy {
	private static final float MIN_DST2_TO_PLAYER = 6400; //(5 * Tile.WIDTH)^2
    private transient ParticleEffectPool.PooledEffect effect;
    
    public Gnats(float x, float y) {
        super("enemy_gnats", x, y);
        this.power = 0;
        this.health = 1;
        this.speed = 0.5f;
        this.d.set(speed, 0);
        effect = GdxGame.particleService.obtain("gnats", true);
        ParticleEmitter[] emitters = effect.getEmitters().toArray(ParticleEmitter.class);
        for(ParticleEmitter emitter : emitters)
            emitter.setContinuous(true);
    }

    @Override
    public void loadAnimation(World world) {
        super.loadAnimation(world);
        getAnimation().setFrameDuration(0.08f);
        getAnimation().setPlayMode(Animation.PlayMode.LOOP_RANDOM);
        PaletteService.colorEffect(effect, world.palette[2]);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if(effect == null) effect = GdxGame.particleService.obtain("gnats", true);
        if(!GdxGame.getCamera().getBox().overlaps(World.CURRENT.getSector(getCenter()).getBox()))
            setDelete(true);            
        effect.setPosition(getCenterX(), getCenterY());

        if(d.isZero(0.01f))
            d.set(MathUtils.random(), MathUtils.random()).setLength(speed);
        else if(getCenter().dst2(Player.CURRENT.getCenterX(), Player.CURRENT.getCenterY()) < MIN_DST2_TO_PLAYER)
			d.set(Player.CURRENT.getCenterX() - getCenterX(), Player.CURRENT.getCenterY() - getCenterY()).setLength(3 * speed);
		else
            d.setAngleDeg(MathUtils.random(20) + (d.angleDeg() - 10)).setLength(speed);
    }
    
    @Override
    public void collideWith(Entity e) {
        super.collideWith(e);
        if(e instanceof Player && !e.hurt) {
            GdxGame.audioService.playSound("gnat", 0.4f, 1, (getCenterX() - e.getCenterX()) / 16f);
            ((Player) e).health -= 2;
            e.takeDamage(0);
        }
    }

    @Override
    public void takeDamage(float amount) {
        if(!hurt) GdxGame.audioService.playSound("enemy_damage");
        super.takeDamage(amount);
    }
        
    @Override
    public void setDelete(boolean b) {
        super.setDelete(b);
        if(b) {
            setPosition(0, 0);
            effect.setPosition(0, 0);
            ParticleEmitter[] emitters = effect.getEmitters().toArray(ParticleEmitter.class);
            for(ParticleEmitter emitter : emitters)
                emitter.setContinuous(false);
        }
    }

    @Override
    public void respawn() {
        super.respawn();
        effect = GdxGame.particleService.obtain("gnats", true);
        this.health = 1;
        this.d.set(speed, 0);
        ParticleEmitter[] emitters = effect.getEmitters().toArray(ParticleEmitter.class);
        for(ParticleEmitter emitter : emitters)
            emitter.setContinuous(true);
    }
}
