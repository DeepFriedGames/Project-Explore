package com.shdwfghtr.entity;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;

public class Item extends Entity {
    public static final ArrayList<String> OPTIONAL_ITEMS = new ArrayList<String>();
    public static final ArrayList<String> GENERIC_ITEMS = new ArrayList<String>();
    public static final float PROB_GENERIC_ITEM = 1.0f;

    Item() {
        super();
    }

    public Item(String name, float x, float y) {
        super(name, x, y, 18, 18);
        setScale(8 / 9f);
    }

    @Override
    void setAnimation(Animation<TextureRegion> animation) {
        animation.setFrameDuration(0.08f);
        super.setAnimation(animation);
    }
}
