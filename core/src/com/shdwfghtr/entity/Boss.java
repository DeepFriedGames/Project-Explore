package com.shdwfghtr.entity;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.math.MathUtils;
import com.shdwfghtr.asset.TimeService;
import com.shdwfghtr.explore.GdxGame;
import com.shdwfghtr.explore.World;

public class Boss extends Enemy {
    //TODO make bosses perma-dead
//    public static final Preferences DATA = Gdx.app.getPreferences("boss");
    Door door = null;
    Music intro_music;
    Music main_music;
    World world;

    Boss(String name) {
        super(name);
    }

    Boss(String name, float x, float y) {
        super(name, x, y);
    }

    void initialize(World world) {
        drawLayer = DrawLayer.FOREGROUND;
        intro_music.setVolume(0.6f);
        intro_music.setOnCompletionListener(new Music.OnCompletionListener() {
            @Override
            public void onCompletion(Music music) {
                main_music.setVolume(0.7f);
                GdxGame.audioService.setMusic(main_music, true);
            }
        });
    }


    @Override
    public void destroy() {
        super.destroy();
        int size = 3;
        final Boss boss = this;
        final ParticleEffectPool.PooledEffect[] effects = new ParticleEffectPool.PooledEffect[size];
        for(int i=0; i<size; i++) {
            final ParticleEffectPool.PooledEffect effect = GdxGame.particleService.obtain("explosion", false);
            effect.setPosition(MathUtils.random((int) getWidth()) + getX(), MathUtils.random((int) getHeight()) + getY());
            effects[i] = effect;
        }

        final Entity explosion = new Entity("explosion", getX(), getY(), getWidth(), getHeight()) {
            @Override
            public void update(float delta) {
                for(ParticleEffectPool.PooledEffect effect : effects) {
                    if(MathUtils.random() < 0.05f) {
                        GdxGame.particleService.add(effect);
                        GdxGame.audioService.playSound("explosion");
                        float rx = MathUtils.random((int) this.getWidth()) + this.getX();
                        float ry = MathUtils.random((int) this.getHeight()) + this.getY();
                        effect.setPosition(rx, ry);
                        if(MathUtils.random() < 0.2f) {
                            PooledItem pooledItemDrop = new PooledItem(Item.GENERIC_ITEMS.get(MathUtils.random(Item.GENERIC_ITEMS.size())), rx, ry);
                            pooledItemDrop.lifeTimer.reset();
                            world.addEntity(pooledItemDrop);
                        }
                    }
                }
            }

            @Override
            public void destroy() {
                world.addEntity(new Item("armor_up", getX(), getY()));
                world.addEntity(new Item("oxygen_tank", getRight(), getY()));
                super.destroy();
                for(ParticleEffectPool.PooledEffect effect : effects)
                    GdxGame.particleService.remove(effect);
            }

            @Override
            public void draw(Batch batch) {
                if(MathUtils.randomBoolean())
                    batch.draw(boss.getAnimation().getKeyFrame(0), this.getX(), this.getY());
            }
        };
        world.addEntity(explosion);
        GdxGame.audioService.fadeOut(3);
        door.setLocked(false);
        TimeService.addTimer(new TimeService.Timer(3) {
            @Override
            public boolean onCompletion() {
                explosion.destroy();
                GdxGame.audioService.fadeIn(3);
                GdxGame.audioService.setMusic("World" + world.index, true);
                return true;
            }
        });
    }
}
