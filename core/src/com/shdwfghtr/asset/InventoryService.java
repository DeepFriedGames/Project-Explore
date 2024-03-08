package com.shdwfghtr.asset;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.shdwfghtr.entity.Entity;
import com.shdwfghtr.entity.Item;
import com.shdwfghtr.explore.GdxGame;
import com.shdwfghtr.explore.World;
import com.shdwfghtr.screens.GameScreen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

public class InventoryService {
    private static final Preferences inventory = Gdx.app.getPreferences("inventory");

    public static void initialize() {
        String[] itemsOpt = {"charge_shot", "charge_missile", "wide_shot", "homing_missile",
                "razor_jump", "charge_bomb", "swift_boots"};

        for(String item : itemsOpt) {
            if(!inventory.get().containsKey(item))
                Item.OPTIONAL_ITEMS.add(item);
        }

        String[] itemsGen = {"power_up", "jump_up", "speed_up", "range_up"};
        Collections.addAll(Item.GENERIC_ITEMS, itemsGen);
    }

    public static void reset() {
        inventory.clear();
        inventory.flush();
    }

    public static boolean isActive(String itemName) {
        Set<String> keys = inventory.get().keySet();
        for(String key : keys) {
            if(key.contains(itemName) && inventory.getBoolean(key))
                return true;
        }
        return false;
    }

    public static void addItem(Item item) {
        String s = DataService.getSaveString(item);
        if(!inventory.contains(s)) {
            inventory.putBoolean(s, true);
            inventory.flush();
        }
        item.destroy();
    }

    public static void toggleActive(String item) {
        inventory.putBoolean(item, !isActive(item));
        inventory.flush();
    }

    public static Actor[] getInventoryActors() {
        ArrayList<Actor> actors = new ArrayList<>();

        for(String item : inventory.get().keySet()) {
            String[] id = item.split(",");
            if(id[0].contains("_up") || id[0].contains("ammo")
                    || id[0].contains("missiles") || id[0].contains("oxygen")
                    || id[0].contains("armor") || id[0].contains("small")
                    || id[0].contains("large") || id[0].length() <= 0)
                continue;

            actors.add(getItemActor(item));
        }
        Actor[] array = new Actor[actors.size()];
        return actors.toArray(array);
    }

    private static Actor getItemActor(final String key) {
        Actor item = new Actor() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                Color c = new Color();
                if(InventoryService.isActive(key))
                    c.set(1, 1, 1, parentAlpha);
                else
                    c.set(0.3f, 0.3f, 0.3f, parentAlpha);
                GdxGame.uiService.getBodyFont().setColor(c);
                GdxGame.uiService.getBodyFont().draw(batch, getName(), getX(), getY() + getHeight());
                GdxGame.uiService.getBodyFont().setColor(Color.WHITE);
            }
        };
        item.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                InventoryService.toggleActive(key);
                return super.touchDown(event, x, y, pointer, button);
            }
        });
        item.setName(key.split(",")[0]
                .replace("item", "")
                .replace("_", " ")
                .toUpperCase());
        return item;
    }

    public static boolean contains(World world, Entity entity) {
        return inventory.contains(DataService.getSaveString(world, entity));
    }
}
