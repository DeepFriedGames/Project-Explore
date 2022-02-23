package com.shdwfghtr.entity;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.shdwfghtr.explore.Asset;
import com.shdwfghtr.explore.Timer;
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
                Asset.getMusicHandler().setMusic(main_music, true);
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
            final ParticleEffectPool.PooledEffect effect = Asset.getParticles().obtain("explosion", false);
            effect.setPosition(Asset.RANDOM.nextInt((int) getWidth()) + getX(), Asset.RANDOM.nextInt((int) getHeight()) + getY());
            effects[i] = effect;
        }

        final Entity explosion = new Entity("explosion", getX(), getY(), getWidth(), getHeight()) {
            @Override
            public void update(float delta) {
                for(ParticleEffectPool.PooledEffect effect : effects) {
                    if(Asset.RANDOM.nextFloat() < 0.05f) {
                        Asset.getParticles().add(effect);
                        Asset.getMusicHandler().playSound("explosion");
                        float rx = Asset.RANDOM.nextInt((int) this.getWidth()) + this.getX();
                        float ry = Asset.RANDOM.nextInt((int) this.getHeight()) + this.getY();
                        effect.setPosition(rx, ry);
                        if(Asset.RANDOM.nextFloat() < 0.2f) {
                            PooledItem pooledItemDrop = new PooledItem(Item.GENERIC_ITEMS.get(Asset.RANDOM.nextInt(Item.GENERIC_ITEMS.size())), rx, ry);
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
                    Asset.getParticles().remove(effect);
            }

            @Override
            public void draw(Batch batch) {
                if(Asset.RANDOM.nextBoolean())
                    batch.draw(boss.getAnimation().getKeyFrame(0), this.getX(), this.getY());
            }
        };
        world.addEntity(explosion);
        Asset.getMusicHandler().fadeOut(3);
        door.setLocked(false);
        Asset.TIMERS.add(new Timer(3) {
            @Override
            public boolean onCompletion() {
                explosion.destroy();
                Asset.getMusicHandler().fadeIn(3);
                Asset.getMusicHandler().setMusic("World" + world.index);
                return true;
            }
        });
    }
}
