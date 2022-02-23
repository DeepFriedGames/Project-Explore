package com.shdwfghtr.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.shdwfghtr.explore.Asset;
import com.shdwfghtr.explore.GameState;
import com.shdwfghtr.explore.GdxGame;
import com.shdwfghtr.explore.Tile;
import com.shdwfghtr.explore.World;
import com.shdwfghtr.screens.GameOverScreen;
import com.shdwfghtr.screens.GameScreen;

import java.util.HashMap;
import java.util.Set;

public class Player extends Entity {
    public static final float MAX_CHARGE = 3f, CHARGE_RATE = 1.005f, SCALE = 7 / 8f;
    private static final float TRAUMA_RATIO = 0.1f / 3;  //the amount of trauma per damage
    public static final Preferences INVENTORY = Gdx.app.getPreferences("inventory");
    public static Player CURRENT;
    private static long soundID = -1;

    public ParticleEffectPool.PooledEffect oxygenEffect;
    private ParticleEffectPool.PooledEffect stepEffect;
    private final HashMap<String, Animation<TextureRegion>> animations = new HashMap<String, Animation<TextureRegion>>();
    public float charge = 1, jump_speed = 4.55f, bullet_life = 0.18f; //bullet_life is in seconds
    public boolean UP, SPIN, RUN, DOWN, MORPH, MISSILE;
    public int maxHealth = 99, maxMissiles, missiles, armor, maxArmor;

    public Player() {
        this(0, 0);
    }

    public Player(float x, float y) {
        super("player_stand_front", x, y);
        this.health = 99;
        this.power = 3;
        this.speed = 1.5f; //pixels per second
        this.persistent = true;
        this.importance = 1.0f;
        setSize(getRegionWidth() * SCALE, getRegionHeight() * SCALE);
        setScale(1 / SCALE);
        setOrigin(getWidth() / 2, 0);
    }

    public void loadAnimations() {
        Animation.PlayMode playMode;
        float duration;
        Array<TextureAtlas.AtlasRegion> regions = World.CURRENT.entityAtlas.getRegions();
        int size = regions.size;
        for (int i = 0; i < size; i++) {
            TextureAtlas.AtlasRegion ar = regions.get(i);

            if(ar.name.contains("player")) {
                if(!animations.containsKey(ar.name)){
                    duration = 0.1f;
                    playMode = Animation.PlayMode.LOOP;
                    if (ar.name.contains("walk")) duration = 0.8f / speed / 6;
                    else if (ar.name.contains("spin")) duration = 0.05f;
                    else if (ar.name.contains("phase")) duration = 0.016f;
                    else if (ar.name.contains("bomb")) duration = 0.2f;
                    else if (ar.name.contains("explosion")) {
                        duration = 0.02f;
                        playMode = Animation.PlayMode.NORMAL;
                    }
                    animations.put(ar.name, new Animation<TextureRegion>(duration, World.CURRENT.entityAtlas.findRegions(ar.name), playMode));

                }
            }
        }
    }

    @Override
    void takeDamage(float amount) {
        if(!hurt) {
            Asset.getMusicHandler().playSound("player_damage");
            if(amount > 0) armor--;
            amount -= armor;
            if(amount <= 0) amount = 0;
            hurt = true;
            health -= amount;
            Entity.HurtTimer hurtTimer = HURT_TIMER_POOL.obtain();
            hurtTimer.entity = this;
            hurtTimer.duration = 0.8f;
            hurtTimer.reset();
            Asset.CAMERA.addTrauma(amount * TRAUMA_RATIO);
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        final Sound razorJumpSound = Asset.getManager().get("audio/razor_jump.wav", Sound.class);
        if(SPIN && itemActive("razor_jump") && soundID < 0)
            soundID = razorJumpSound.loop(0.7f);
        else if(!SPIN || !itemActive("razor_jump")) {
            razorJumpSound.stop(soundID);
            soundID = -1;
        }
        if(oxygenEffect != null)
            oxygenEffect.setPosition(getCenterX(), getTop());

        if(missiles <= 0) {
            missiles = 0;
            MISSILE = false;
        } else if(missiles > maxMissiles) missiles = maxMissiles;
        if(health <= 0) ((GdxGame) Gdx.app.getApplicationListener()).setScreen(new GameOverScreen());
        else if(health > maxHealth) health = maxHealth;
        if(armor < 0) armor = 0;
        else if(armor > maxArmor) armor = maxArmor;

    }

    public boolean canJump() {
        if(itemActive("double_jump"))
            return (SPIN && d.y > -3.8 && d.y < -1.6) || onGround();
        else if(itemActive("wall_jump"))
            return (SPIN && ((World.CURRENT.isBlocked(getX() - 9, getY()) && d.x > 0)
                    || (World.CURRENT.isBlocked(getRight() + 9, getY()) && d.x < 0))) || onGround();
        else
            return onGround();
    }

    @Override
    public void draw(Batch batch) {
        try {
            String newName = "player_";

            if (MORPH) newName = newName.concat("roll");
            else if (SPIN && !DOWN)
                if (itemActive("wall_jump") && SPIN && ((World.CURRENT.isBlocked(getX() - 9, getY()) && d.x > 0)
                        || (World.CURRENT.isBlocked(getRight() + 9, getY()) && d.x < 0)))
                    newName = newName.concat("jump_wall");
                else if (itemActive("razor_jump")) newName = newName.concat("razor_spin");
                else newName = newName.concat("spin");
            else if (d.y != 0)
                if (UP) newName = newName.concat("jump_up");
                else if (DOWN) newName = newName.concat("jump_down");
                else newName = newName.concat("jump");
            else if (((GdxGame) Gdx.app.getApplicationListener()).getScreen() instanceof GameScreen)
                if (((GameScreen) ((GdxGame) Gdx.app.getApplicationListener()).getScreen()).getState() == GameState.PAUSE)
                    newName = newName.concat("stand_front");
                else if (d.x != 0)
                    if (UP) newName = newName.concat("walk_up");
                    else if (RUN) newName = newName.concat("run");
                    else newName = newName.concat("walk");
                else if (DOWN) newName = newName.concat("crouch");
                else if (UP) newName = newName.concat("stand_up");
                else newName = newName.concat("stand");

            if (MISSILE) newName = newName.concat("_missile");
            if (Asset.RANDOM.nextFloat() * 2 + 1 < charge) newName = newName.concat("charge");

            if (!newName.matches(name)) {
                name = newName;
                setAnimation(animations.get(name));
            }

            setRegion(getAnimation().getKeyFrame(Asset.TIME));
            setSize(getRegionWidth() * SCALE, getRegionHeight() * SCALE);
            if (itemActive("mach_boots"))
                for (int i = 1; i < speed * 2; i++)
                    if (Math.abs(d.x) > speed && Asset.RANDOM.nextFloat() < 0.4f)
                        batch.draw(this, getX() - 1 - (Math.abs(d.x) - speed) * i * d.x, getY() - (Math.abs(d.x) - speed) * i * d.y);
            super.draw(batch);

            //creates cute little dust particles when the player takes a step

            if (name.contains("walk") && (getAnimation().getKeyFrameIndex(Asset.TIME) == 1 || getAnimation().getKeyFrameIndex(Asset.TIME) == 4)) {
                float efx = left ? getX() : getRight() - 5;
                stepEffect = Asset.getParticles().obtain("step", true);

                if (World.CURRENT.isBlocked(efx, getY() - World.CURRENT.getGravity() * Gdx.graphics.getDeltaTime()) && stepEffect != null) {
                    Asset.Particles.colorEffect(stepEffect, World.CURRENT.palette[2]);
                    stepEffect.setPosition(efx, getY());
                }
            }
        } catch (Exception e) {
            System.out.println(getName());
            e.printStackTrace();
        }
    }

    @Override
    public void collideWith(Entity e) {
        if(e instanceof Item && !World.CURRENT.isBlocked(e.getCenterX(), e.getCenterY())) {
            Item item = (Item) e;
            String itemName = item.getName();
            if(itemName.contains("O2_large")) {
                health += 25;
                Asset.getMusicHandler().playSound("oxygen");
            } else if(itemName.contains("O2_small")) {
                health += 10;
                Asset.getMusicHandler().playSound("oxygen");
            } else if(itemName.contains("oxygen_tank")) {
                maxHealth += 100;
                health = maxHealth;
                Asset.getMusicHandler().playSound("oxygen");
            } else if(itemName.contains("ammo")) {
                missiles += 1;
                Asset.getMusicHandler().playSound("ammo");
            } else if(itemName.contains("armor")) {
                armor += 2;
                if(itemName.contains("armor_up"))
                    maxArmor += 2;
                Asset.getMusicHandler().playSound("armor");
            } else {
                Asset.getMusicHandler().playSound("power_up");
                if(itemName.contains("oxygen_tank")) {
                    maxHealth += 100;
                    health = maxHealth;
                } else if(itemName.contains("missile")) {
                    maxMissiles += 3;
                    missiles += 3;
                }
                else if(itemName.contains("power_up"))
                    power += 0.1f;
                else if(itemName.contains("speed_up"))
                    speed += 0.02f;
                else if(itemName.contains("jump_up"))
                    jump_speed += 0.05f;
                else if(itemName.contains("range_up"))
                    bullet_life += 0.6f;
            }
            final String message = itemName.replace("item_", "").toUpperCase().replace('_', ' ');
            Asset.MESSAGES.add(message);
            addToInventory(item);
        } if (e instanceof Snail || e instanceof Dropper) {
            if (!World.CURRENT.isBlocked(getCenterX(), getTop() + speed) && d.y <= 0
                    && Math.abs(getY() - e.getTop()) < Math.abs(d.y)
                    && getRight() > e.getX() && getX() < e.getRight()) {
                if (Math.abs(d.x) < Math.abs(e.d.x)) {
                    d.x = e.d.x;
                }
                if (Math.abs(d.y) < Math.abs(e.d.y)) {
                    d.y = e.d.y;
                }
                if (d.y < -speed) Asset.getMusicHandler().playSound("footstep");
                if (itemActive("razor_jump") && SPIN) e.takeDamage(12);
                setY(e.getTop());
                SPIN = false;
                if (MORPH) d.y /= -3;
                else d.y = Math.abs(e.d.y) < 0.01f ? 0 : e.d.y;
            }
        } else if(e instanceof Enemy && !hurt && e.power > 0) {
            if((itemActive("razor_jump") && SPIN) || (itemActive("mach_boots") && Math.abs(d.x) > speed*2))
                e.takeDamage(12);
            else {
                takeDamage(e.power);
                Asset.VECTOR2.set(getCenterX() - e.getCenterX(), 16);
                Asset.VECTOR2.limit(1.8f);
                d.set(Asset.VECTOR2);
            }
        }
    }

    @Override
    public void checkCollisions() {
        if(Math.abs(d.x) >= speed * 2 ) d.x = Math.copySign(speed * 2, d.x);
        d.y -= World.CURRENT.getGravity();

        float[] ys = {getY(), getCenterY(), getTop()};
        for(float y : ys)
            if(d.x != 0) {
                float x = d.x;
                if(d.x > 0) x += getRight();
                else if(d.x < 0) x += getX();
                Rectangle box = World.getTileBox(x, y);
                char c = World.CURRENT.getChar(x, y);
                if(c == '^') takeDamage(2);
                if(!World.CURRENT.isBlocked(x, y)) continue;
                if((itemActive("razor_jump") && SPIN) ||
                        (itemActive("mach_boots") && Math.abs(d.x) > speed * 2 && c == '>'))
                    World.CURRENT.breakTile(x, y);
                else {
                    if(getCenter().dst2(box.x + (d.x > 0 ? -(getWidth() + 1) : box.width), y) < Tile.WIDTH * Tile.WIDTH)
                        setX(box.x + (d.x > 0 ? -(getWidth() + 1) : box.width));

                    RUN = false;
                    d.x = 0;
                }
            }
        float[] xs = {getX() + d.x, getRight() + d.x};
        for(float x : xs) {
            float y = getY() + d.y;
            Rectangle box = World.getTileBox(x, y);
            char c = World.CURRENT.getChar(x, y);
            if(d.y < 0) {
                if(!World.CURRENT.isBlocked(x, y)) continue;
                if (itemActive("razor_jump") && SPIN && Tile.isBreakable(World.CURRENT.getChar(x, y)))
                    World.CURRENT.breakTile(x, y);
                else {
                    if(Asset.VECTOR2.set(getCenterX(), getY()).dst2(x, box.y + box.height) < Tile.HEIGHT * Tile.HEIGHT)
                        setY(box.y + box.height);

                    if (d.y < -3) {
                        Asset.getMusicHandler().playSound("footstep", 1, Asset.RANDOM.nextFloat() * 0.4f + 0.8f, 0);
                        stepEffect = Asset.getParticles().obtain("dust", true);

                        if (stepEffect != null) {
                            Asset.Particles.colorEffect(stepEffect, World.CURRENT.palette[2]);
                            stepEffect.setPosition(getX(), getY());
                        }
                    }
                    World.CURRENT.disruptTile(x, y);
                    SPIN = false;
                    if (MORPH) d.y /= -3;
                    else d.y = 0;
                    if (World.CURRENT.isSlick(getCenterX(), getY() - speed) || !RUN)
                        d.x *= Slick.FRICTION;
                }
            }
            if(d.y > 0) {
                y = getTop() + d.y;
                box.set(World.getTileBox(x, y));
                if(c == '^') takeDamage(2);
                if(!World.CURRENT.isBlocked(x, y)) continue;
                if(itemActive("razor_jump") && SPIN)
                    World.CURRENT.breakTile(x, y);
                else {
                    setY(box.y - getHeight());
                    d.y = -0.001f;
                }
            }
        }
    }

    public boolean itemActive(String itemName) {
        Set<String> keys = INVENTORY.get().keySet();
        for(String key : keys) {
            if(key.contains(itemName) && INVENTORY.getBoolean(key))
                return true;
        }
        return false;
    }

    public void fire() {
        SPIN = false;
        if(!SPIN)
            if(!MORPH) {
                Bullet bullet = Bullet.POOL.obtain();
                bullet.setDelete(false);
                //get the animation for the bullet
                if(MISSILE) {
                    bullet.name = "missile";
                    missiles --;
                } else if(itemActive("phase_shot")) bullet.name = "phase_shot";
                else if(itemActive("wide_shot")) bullet.name = "wide_shot";
                else bullet.name = "bullet";
                bullet.loadAnimation(World.CURRENT);
                Asset.getMusicHandler().playSound(bullet.name, 1, Asset.RANDOM.nextFloat() * 0.4f + 0.8f, 0);

                //set the width and height of the bullet
                float w = Asset.getEntityAtlas().findRegion(bullet.name).getRegionWidth(),
                        h = Asset.getEntityAtlas().findRegion(bullet.name).getRegionHeight();
                w += charge - 1;
                h += charge - 1;
                bullet.power = power * charge + (MISSILE ? 2 : 0);

                float x, y;
                //set the position of the bullet
                if(UP) {
                    y = getTop();
                    x = getCenterX() + (left ? -3 : 3) - w / 2;
                } else if(!onGround() && DOWN) {
                    y = getCenterY() - h / 2;
                    x = getCenterX() - w / 2;
                } else {
                    y = getTop() - 9 - h / 2;
                    x = getX() + (left ? -w : getWidth());
                }
                bullet.setBounds(x, y, w, h);

                //set the velocity of the bullet
                bullet.d.setZero();
                bullet.target = null;
                if(UP)
                    bullet.d.y = Bullet.SPEED;
                else if(!onGround() && DOWN)
                    bullet.d.y = -Bullet.SPEED;
                else
                    bullet.d.x = left ? -Bullet.SPEED : Bullet.SPEED;

                if(MISSILE && itemActive("homing_missile")) {
                    float oldDst2 = 100000f;
                    for(Entity e : World.CURRENT.getActiveEntities()) {
                        if (!(e instanceof Enemy) || !e.getBox().overlaps(Asset.CAMERA.getBox())) continue;
                        float newDst2 = e.getCenter().dst2(bullet.getCenterX(), bullet.getCenterY());
                        if (bullet.target == null || newDst2 < oldDst2) {
                            bullet.target = e;
                            oldDst2 = newDst2;
                        }
                    }
                }

                //without the long shot bullets will be deleted after 1/10 of a second
                if(!MISSILE) bullet.rangeTimer.reset(bullet_life);

                //bullet is added to the World.CURRENT
                World.CURRENT.addEntity(bullet);
            } else if(itemActive("bomb")) {
                Asset.getMusicHandler().playSound("bomb");
                Bomb bomb = Bomb.POOL.obtain();
                bomb.setDelete(false);
                bomb.setPower(charge);
                bomb.setPosition(getCenterX() - bomb.getWidth() / 2, getCenterY() - bomb.getHeight()/2);
                bomb.timer.reset();
                World.CURRENT.addEntity(bomb);
            }
    }

    public static float calculateJumpHeight(float initial_velocity) {
        float maxX = (initial_velocity - Asset.GRAVITY) / Asset.GRAVITY; //max of the jump parabola
        float maxHeight = (initial_velocity - Asset.GRAVITY)*maxX - 0.5f*Asset.GRAVITY*maxX*maxX; //jump height in pixels
        maxHeight /= Tile.HEIGHT; //jump height in meters
        maxHeight *= 100; //round to the nearest 100th
        maxHeight = Math.round(maxHeight);
        maxHeight /= 100;
        return maxHeight;
    }

    public static float calculateSpeed(float speed) {
        float mpsSpeed = speed; //speed starts in pixels per frame
        mpsSpeed *= 60; //assume 60 frames per second
        mpsSpeed /= Tile.WIDTH; //16 pixels per meter
        mpsSpeed *= 100; //round to the nearest 100th
        mpsSpeed = Math.round(mpsSpeed);
        mpsSpeed /= 100;
        return mpsSpeed;
    }

    public static float calculateBulletRange(float lifetime) {
        float range = lifetime; //how many seconds the bullet is alive for
        range *= 60; //assume 60 frames per second
        range *= Bullet.SPEED; //bullets have a speed of 6 pixels per frame
        range /= Tile.WIDTH; //16 pixels per meter
        range *= 100; //round to the nearest 100th
        range = Math.round(range);
        range /= 100;
        return range;
    }

    private void addToInventory(Item item) {
        String s = Entity.getSaveString(item);
        if(!INVENTORY.contains(s)) {
            INVENTORY.putBoolean(s, true);
            INVENTORY.flush();
            ((GameScreen) ((GdxGame) Gdx.app.getApplicationListener()).getScreen()).addItemActor(s);
        }
        item.destroy();
    }

    public void save() {
        Asset.DATA.putFloat("bullet_life", bullet_life);
        Asset.DATA.putFloat("speed", speed);
        Asset.DATA.putFloat("jump_speed", jump_speed);
        Asset.DATA.putFloat("power", power);
        Asset.DATA.putFloat("health", health);

        Asset.DATA.putInteger("maxHealth", maxHealth);
        Asset.DATA.putInteger("maxMissiles", maxMissiles);
        Asset.DATA.putInteger("missiles", missiles);
        Asset.DATA.putInteger("armor", armor);
        Asset.DATA.putInteger("maxArmor", maxArmor);

        //INVENTORY is awkward to save, we need to store the world seed and xy values so
        //if a player returns to a world the items don't respawn for easy pickups.
        Asset.DATA.flush();
        Asset.MESSAGES.add("Saved");
    }

    public static void load(Player player) {
        player.bullet_life = Asset.DATA.getFloat("bullet_life", player.bullet_life);
        player.speed = Asset.DATA.getFloat("speed", player.speed);
        player.jump_speed = Asset.DATA.getFloat("jump_speed", player.jump_speed);
        player.power = Asset.DATA.getFloat("power", player.power);

        player.maxHealth = Asset.DATA.getInteger("maxHealth", player.maxHealth);
        player.maxMissiles = Asset.DATA.getInteger("maxMissiles", player.maxMissiles);
        player.missiles = Asset.DATA.getInteger("missiles", player.missiles);
        player.armor = Asset.DATA.getInteger("armor", player.armor);
        player.maxArmor = Asset.DATA.getInteger("maxArmor", player.maxArmor);
        player.health = player.maxHealth;
    }
}
