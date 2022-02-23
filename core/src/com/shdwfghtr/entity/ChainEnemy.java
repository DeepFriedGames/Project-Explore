package com.shdwfghtr.entity;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by Stuart on 2/16/2018.
 * Not so much an entity itself as a collection of entities
 * imagine a chain of entities, the parent entity dictates
 * the movement of the others, the rest follow the one in front
 */

public class ChainEnemy extends Enemy {
    private static final Vector2 VECTOR2 = new Vector2();
    Entity[] bodyLinks;

    ChainEnemy(String name, float x, float y, Entity... bodies) {
        super(name, x, y);
        this.bodyLinks = bodies;
        this.persistent = true;
    }

    @Override
    public void update(float delta) {
        if(!d.isZero())
            setRotation(d.angle());
        for(int i = 0; i < bodyLinks.length; i++) {
            Entity link = bodyLinks[i],
                    follow;

            if(i > 0) follow = bodyLinks[i - 1];
            else follow = this;

            float maxDist = (Math.max(link.getWidth(), link.getHeight()) + Math.min(follow.getWidth(), follow.getHeight())) / 2;

            VECTOR2.set(follow.getCenter().add(follow.d));

            if(link.getCenter().dst2(VECTOR2) > maxDist * maxDist) {
                link.d.set(VECTOR2).sub(link.getCenter()).limit2(d.len2());
                link.setRotation(link.d.angle());
            } else
                link.d.setZero();

        }

        super.update(delta);

    }

    public Entity[] getBodyLinks() {
        return bodyLinks;
    }
}
