package com.shdwfghtr.entity;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.shdwfghtr.explore.Asset;
import com.shdwfghtr.explore.Tile;
import com.shdwfghtr.explore.World;

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
        effect = Asset.getParticles().obtain("gnats", true);
        ParticleEmitter[] emitters = effect.getEmitters().toArray(ParticleEmitter.class);
        for(ParticleEmitter emitter : emitters)
            emitter.setContinuous(true);
    }

    @Override
    public void loadAnimation(World world) {
        super.loadAnimation(world);
        getAnimation().setFrameDuration(0.08f);
        getAnimation().setPlayMode(Animation.PlayMode.LOOP_RANDOM);
        Asset.Particles.colorEffect(effect, world.palette[2]);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if(effect == null) effect = Asset.getParticles().obtain("gnats", true);
        if(!Asset.CAMERA.getBox().overlaps(World.CURRENT.getSector(getCenter()).getBox()))
            setDelete(true);            
        effect.setPosition(getCenterX(), getCenterY());

        if(d.isZero(0.01f))
            d.set(Asset.RANDOM.nextFloat(), Asset.RANDOM.nextFloat()).setLength(speed);
        else if(getCenter().dst2(Player.CURRENT.getCenterX(), Player.CURRENT.getCenterY()) < MIN_DST2_TO_PLAYER)
			d.set(Player.CURRENT.getCenterX() - getCenterX(), Player.CURRENT.getCenterY() - getCenterY()).setLength(3 * speed);
		else
            d.setAngle(Asset.RANDOM.nextInt(20) + (d.angle() - 10)).setLength(speed);
    }
    
    @Override
    public void collideWith(Entity e) {
        super.collideWith(e);
        if(e instanceof Player && !e.hurt) {
            Asset.getMusicHandler().playSound("gnat", 0.4f, 1, (getCenterX() - e.getCenterX()) / 16f);
            ((Player) e).health -= 2;
            e.takeDamage(0);
        }
    }

    @Override
    public void takeDamage(float amount) {
        if(!hurt) Asset.getMusicHandler().playSound("enemy_damage");
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
        effect = Asset.getParticles().obtain("gnats", true);
        this.health = 1;
        this.d.set(speed, 0);
        ParticleEmitter[] emitters = effect.getEmitters().toArray(ParticleEmitter.class);
        for(ParticleEmitter emitter : emitters)
            emitter.setContinuous(true);
    }
}
