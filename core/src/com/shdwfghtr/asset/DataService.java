package com.shdwfghtr.asset;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.TimeUtils;
import com.shdwfghtr.entity.Door;
import com.shdwfghtr.entity.Entity;
import com.shdwfghtr.entity.Player;
import com.shdwfghtr.explore.GdxGame;
import com.shdwfghtr.explore.Sector;
import com.shdwfghtr.explore.World;

public class DataService {    
    private static final Preferences data = Gdx.app.getPreferences("data");

    public static void reset() {
        data.clear();
        data.flush();
    }

    public static long getSeed() {
        //reset the random number generator, then reload the star system seeds
        if(!data.contains("seed")) {
            long seed = TimeUtils.nanoTime();
            DataService.setSeed(seed);
        }
        return data.getLong("seed");
    }

    public static void setSeed(long seed) {
        data.putLong("seed", seed);
        data.flush();
    }

    public static void clearSeed() {
        data.remove("seed");
        data.flush();
    }

    public static void setSectorExplored(Sector sector, boolean explored) {
        data.putBoolean(getSaveString(sector), explored);
        data.flush();
    }

    public static void save(Player player) {
        data.putFloat("bullet_life", player.bullet_life);
        data.putFloat("speed", player.speed);
        data.putFloat("jump_speed", player.jump_speed);
        data.putFloat("power", player.power);
        data.putFloat("health", player.health);

        data.putInteger("maxHealth", player.maxHealth);
        data.putInteger("maxMissiles", player.maxMissiles);
        data.putInteger("missiles", player.missiles);
        data.putInteger("armor", player.armor);
        data.putInteger("maxArmor", player.maxArmor);

        //INVENTORY is awkward to save, we need to store the world seed and xy values so
        //if a player returns to a world the items don't respawn for easy pickups.
        data.flush();
        GdxGame.uiService.addMessage("Saved");
    }

    public static void load(Player player) {
        if(player == null) player = new Player();
        
        player.bullet_life = data.getFloat("bullet_life", player.bullet_life);
        player.speed = data.getFloat("speed", player.speed);
        player.jump_speed = data.getFloat("jump_speed", player.jump_speed);
        player.power = data.getFloat("power", player.power);

        player.maxHealth = data.getInteger("maxHealth", player.maxHealth);
        player.maxMissiles = data.getInteger("maxMissiles", player.maxMissiles);
        player.missiles = data.getInteger("missiles", player.missiles);
        player.armor = data.getInteger("armor", player.armor);
        player.maxArmor = data.getInteger("maxArmor", player.maxArmor);
        player.health = player.maxHealth;
    }

    public static void save(Door door) {
        data.putBoolean(getSaveString(door), false);
        data.flush();
    }

    public static String getSaveString(Entity e) {
        String str = e.getName().replace("_locked", "") + ","; //the _locked tag is cumbersome for saving
        str = str.concat(World.CURRENT.name + ",");
        str = str.concat(e.getX() + "," + e.getY());
        return str;
    }

    public static String getSaveString(World world, Entity e) {
        String str = e.getName().replace("_locked", "") + ","; //the _locked tag is cumbersome for saving
        str = str.concat(world.name + ",");
        str = str.concat(e.getX() + "," + e.getY());
        return str;
    }

    public static String getSaveString(Sector sector) {
        return sector.name + ',' + sector.getXi() + ',' + sector.getYi();
    }

    public static boolean hasPlayerData() {
        return data.contains("power");
    }

    public static boolean load(World world, Door door) {
        return data.getBoolean(getSaveString(world, door), true);
    }
}
