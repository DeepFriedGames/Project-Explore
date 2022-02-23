package com.shdwfghtr.entity;

/**
 * Created by Stuart on 2/17/2018.
 */

public class FixedChainEntity extends ChainEnemy {
    public FixedChainEntity(String name, float x, float y, Entity... bodies) {
        super(name, x, y, bodies);
    }

    @Override
    public void update(float delta) {

    }
}
