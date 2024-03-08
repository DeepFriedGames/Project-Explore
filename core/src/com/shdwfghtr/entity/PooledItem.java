package com.shdwfghtr.entity;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Pool;
import com.shdwfghtr.asset.TimeService;
import com.shdwfghtr.explore.GdxGame;

public class PooledItem extends Item implements Pool.Poolable {
    public static final Pool<PooledItem> POOL = new Pool<PooledItem>() {
        @Override
        protected PooledItem newObject() {
            return new PooledItem();
        }
    };
    final TimeService.Timer lifeTimer = new TimeService.Timer(4.2f) {
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
        TextureRegion tr = GdxGame.textureAtlasService.findEntityRegion(name);
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
    public void destroy() {
        super.destroy();
        POOL.free(this);
    }
}
