package com.shdwfghtr.entity;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Pool;
import com.shdwfghtr.explore.Asset;
import com.shdwfghtr.explore.Timer;

public class PooledItem extends Item implements Pool.Poolable {
    public static final Pool<PooledItem> POOL = new Pool<PooledItem>() {
        @Override
        protected PooledItem newObject() {
            return new PooledItem();
        }
    };
    final Timer lifeTimer = new Timer(4.2f) {
        @Override
        public boolean onCompletion() {
            destroy();
            return true;
        }
    };

    private PooledItem() { super(); }

    PooledItem(String name, float x, float y) {
        super(name, x, y);
    }

    public void set(String name, float x, float y) {
        this.lifeTimer.reset();
        this.name = name;
        TextureRegion tr = Asset.getEntityAtlas().findRegion(name);
        int width = tr.getRegionWidth();
        int height = tr.getRegionHeight();
        this.setBounds(x - width / 2, y - height / 2, width, height);
	}
    
    @Override
    public void reset() {
        setBounds(0, 0, 0, 0);
        name = "item_";
    }
    
    @Override
    void destroy() {
        super.destroy();
        POOL.free(this);
    }
}
