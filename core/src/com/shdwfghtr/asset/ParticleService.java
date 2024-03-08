package com.shdwfghtr.asset;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;


import java.util.HashMap;

public class ParticleService implements Disposable {
    private final HashMap<String, ParticleEffectPool> POOLS = new HashMap<>();
    private final Array<ParticleEffectPool.PooledEffect> ADDITIVE_EFFECTS = new Array<>();
    private final Array<ParticleEffectPool.PooledEffect> NORMAL_EFFECTS = new Array<>();
    final TextureAtlas atlas = new TextureAtlas("atlas/particle.atlas");

    public ParticleService() {
        //TODO initialize with String[] fileNames
        addParticleEffect(Gdx.files.internal("particle/boss_explosion.p"));
        addParticleEffect(Gdx.files.internal("particle/break.p"));
        addParticleEffect(Gdx.files.internal("particle/bullet.p"));
        addParticleEffect(Gdx.files.internal("particle/damage.p"));
        addParticleEffect(Gdx.files.internal("particle/disrupt.p"));
        addParticleEffect(Gdx.files.internal("particle/dust.p"));
        addParticleEffect(Gdx.files.internal("particle/explosion.p"));
        addParticleEffect(Gdx.files.internal("particle/flame.p"));
        addParticleEffect(Gdx.files.internal("particle/glibber.p"));
        addParticleEffect(Gdx.files.internal("particle/gnats.p"));
        addParticleEffect(Gdx.files.internal("particle/lava.p"));
        addParticleEffect(Gdx.files.internal("particle/midasium.p"));
        addParticleEffect(Gdx.files.internal("particle/oxygen.p"));
        addParticleEffect(Gdx.files.internal("particle/slick.p"));
        addParticleEffect(Gdx.files.internal("particle/spark.p"));
        addParticleEffect(Gdx.files.internal("particle/stars.p"));
        addParticleEffect(Gdx.files.internal("particle/step.p"));
    }
    
    private void addParticleEffect(FileHandle handle) {
        ParticleEffect effect = new ParticleEffect();
        effect.load(handle, atlas);
        POOLS.put(handle.nameWithoutExtension(), new ParticleEffectPool(effect, 1, 5));
    }

    public ParticleEffectPool.PooledEffect obtain(String key, Boolean add) {
        ParticleEffectPool.PooledEffect effect = POOLS.get(key).obtain();
        if(add) add(effect);
        return effect;
    }

    public void add(ParticleEffectPool.PooledEffect effect) {
        if(OptionsService.AreParticlesEnabled()) {
            if(effect.getEmitters().first().isAdditive()) {
                if (!ADDITIVE_EFFECTS.contains(effect, true)) {
                    ADDITIVE_EFFECTS.add(effect);
                    effect.setEmittersCleanUpBlendFunction(false);
                }
            } else {
                if (!NORMAL_EFFECTS.contains(effect, true))  NORMAL_EFFECTS.add(effect);
            }
            effect.start();
        }
    }

    public void remove(ParticleEffectPool.PooledEffect effect) {
        if(NORMAL_EFFECTS.contains(effect, true)) {
            NORMAL_EFFECTS.removeValue(effect, true);
            effect.free();
        } else if(ADDITIVE_EFFECTS.contains(effect, true)) {
            ADDITIVE_EFFECTS.removeValue(effect, true);
            effect.free();
        }
    }

    public void update(float delta) {
        for(ParticleEffectPool.PooledEffect effect : getEffects()) {
            if(effect.isComplete()) remove(effect);
            else effect.update(delta);
        }
    }

    public void draw(Batch batch) {
        //draw all additive blended effects
        ParticleEffectPool.PooledEffect[] additives = ADDITIVE_EFFECTS.toArray(ParticleEffectPool.PooledEffect.class);
        for (ParticleEffectPool.PooledEffect additiveEffect : additives)
            additiveEffect.draw(batch);

        //We need to reset the batch to the original blend state as we have setEmittersCleanUpBlendFunction as false in additiveEffect
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        //draw all 'normal alpha' blended effects
        ParticleEffectPool.PooledEffect[] normies = NORMAL_EFFECTS.toArray(ParticleEffectPool.PooledEffect.class);
        for (ParticleEffectPool.PooledEffect normalEffect : normies)
            normalEffect.draw(batch);
    }

    public ParticleEffectPool.PooledEffect[] getEffects() {
        Array<ParticleEffectPool.PooledEffect> all = new Array<>();
        all.addAll(ADDITIVE_EFFECTS);
        all.addAll(NORMAL_EFFECTS);

        return all.toArray(ParticleEffectPool.PooledEffect.class);
    }

    @Override
    public void dispose() {
        atlas.dispose();
    }
}
