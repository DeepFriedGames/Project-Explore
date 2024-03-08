package com.shdwfghtr.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.shdwfghtr.asset.TimeService;
import com.shdwfghtr.explore.GdxGame;
import com.shdwfghtr.explore.Tile;
import com.shdwfghtr.explore.World;
import com.shdwfghtr.screens.GameScreen;

public class Glibber extends Boss {
    private Body body;
    private static final int BODY_HEALTH = 12;
    private static final Vector2 EYE_POSITION = new Vector2(14, 21);
    private static final int EFFECT_WIDTH = 80;
    private final float spawnX, spawnY;
    private boolean intro = true;
    private TimeService.Timer spawnTimer;

    public Glibber(World world, float x, float y) {
        super("boss_glibber_eye", x, y);
        this.spawnX = x;
        this.spawnY = y;
        this.health = 50;
        this.speed = 1.2f;
        this.power = 12;
        this.intro = true;
        initialize(world);
        this.world = world;

    }

    @Override
    public void initialize(final World world) {
        GdxGame.audioService.setMusic("audio/Bergamot_Intro.ogg", false);  //TODO make Glibber intro and music
        GdxGame.audioService.queueMusic("audio/Bergamot.ogg");

        spawnTimer = new TimeService.Timer(8) {
            @Override
            public boolean onCompletion() {
                if(body.isDead()) {
                    body.respawn();
                    body.setPosition(spawnX + (Tile.WIDTH - 42)/2, spawnY + (Tile.HEIGHT - 42)/2);
                    world.addEntity(body);
                    intro = false;
                }
                return true;
            }

            @Override
            public void reset() {
                ParticleEffectPool.PooledEffect effect = GdxGame.particleService.obtain("boss_glibber", true);
                effect.setPosition(spawnX + (Tile.WIDTH - EFFECT_WIDTH)/2, spawnY - 3*Tile.HEIGHT);
                super.reset();
            }
        };

        //creates a goopy body around the eye
        body = new Body(spawnX + (Tile.WIDTH - 42)/2, spawnY + (Tile.HEIGHT - 42)/2);
        body.setDelete(true);
        ParticleEffectPool.PooledEffect effect = GdxGame.particleService.obtain("glibber", true);
        effect.setPosition(spawnX + (Tile.WIDTH - EFFECT_WIDTH)/2, spawnY - 3*Tile.HEIGHT);
        world.addEntity(body);

        super.initialize(world);
    }

    @Override
    public void collideWith(Entity entity) {
        if(entity instanceof Bullet && intro)
            entity.destroy();
        else
            super.collideWith(entity);
    }

    @Override
    public void update(float delta) {
        if(!main_music.isPlaying() && !intro_music.isPlaying()) {
            GdxGame.audioService.setVolume(1);
            GdxGame.audioService.setMusic(intro_music, false);
        }
        if(door == null) {
            GdxGame.uiService.addMessage("-GLIBBER-");
            float minDst2 = 1000000f;
            for(Entity e : world.getActiveEntities()) {
                if (!(e instanceof Door)) continue;

                float dst2 = getCenter().dst2(e.getCenterX(), e.getCenterY());
                if(dst2 < minDst2) {
                    door = (Door) e;
                    minDst2 = dst2;
                }
            }
            door.openTimer.onCompletion();
            door.setLocked(true);
        }

        if(body.isDead() && !TimeService.contains(spawnTimer))
            if(!isDead()) spawnTimer.reset();

        if(intro) d.setZero();
        else if(body.isDead()) {
            //bounce around like a dingus
            if(d.isZero(0.001f)) {
                left = MathUtils.randomBoolean();
                if(left) d.x = speed;
                else d.x = -speed;

            }
        } else if(body.crawling && getBox().overlaps(body.getBox())){
            if(body.left) {
                if (body.d.angle() == 180) EYE_POSITION.set(13, 21);
                else if (body.d.angle() == 270) EYE_POSITION.set(5, 13);
                else if (body.d.angle() == 0) EYE_POSITION.set(13, 5);
                else if (body.d.angle() == 90) EYE_POSITION.set(21, 13);
            } else {
                if (body.d.angle() == 0) EYE_POSITION.set(13, 21);
                else if (body.d.angle() == 90) EYE_POSITION.set(5, 13);
                else if (body.d.angle() == 180) EYE_POSITION.set(13, 5);
                else if (body.d.angle() == 270) EYE_POSITION.set(21, 13);
            }
            //stay attached to the body
            float dx = body.getX() + EYE_POSITION.x - getX(),
                    dy = body.getY() + EYE_POSITION.y - getY();
            if (Math.abs(dx) >= speed) {
                d.x = Math.copySign(speed, dx);
                body.d.setZero();
            } else {
                d.x = 0;
                setX(body.getX() + EYE_POSITION.x);
            }
            if (Math.abs(dy) >= speed) {
                d.y = Math.copySign(speed, dy);
                body.d.setZero();
            } else {
                d.y = 0;
                setY(body.getY() + EYE_POSITION.y);
            }

        }
        super.update(delta);
    }

//    @Override
//    public void draw(Batch batch) {
//        //draw the body
//        TextureRegion tr;
//        //this top if statement creates a flashing effect if the entity is hurt
//        if(!hurt || MathUtils.randomBoolean()) {
//            //draw the eye looking at the player
//            float angle = getCenter().sub(Player.CURRENT.getCenter()).angle();
//            tr = getAnimation().getKeyFrame(0);
//            batch.draw(tr, getX(), getY(), getWidth()/2, getHeight()/2, getWidth(), getHeight(), 1, 1, angle);
//
//            if(body.isDead()) {
//                tr = GdxGame.textureAtlasService.findEntityRegion("boss_glibber_regroup");
//                float p = (Asset.TIME - spawnTimer.start) / spawnTimer.duration;
//                if(p > 1) p = 0;
//                batch.draw(tr, spawnX + (Tile.WIDTH - p*tr.getRegionWidth())/2, spawnY + (Tile.HEIGHT - p*tr.getRegionHeight())/2,
//                        p*tr.getRegionWidth(), p*tr.getRegionHeight());
//            }
//        }
//    }

    @Override
    void checkCollisions() {
        Rectangle box;
        if(!intro && (body.isDead() || !getBox().overlaps(body.getBox()))) {
            d.sub(0, world.getGravity());
            float[] xs = {getX(), getRight()};
            float[] ys = {getY(), getTop()};
            for (float y : ys) {
                if (d.x > 0 && world.isBlocked(getRight() + d.x, y)) {
                    box = World.getTileBox(getRight() + d.x, y);
                    setPosition(box.x - getWidth(), getY());
                    left = true;
                    d.x = -speed;
                }
                if (d.x < 0 && world.isBlocked(getX() + d.x, y)) {
                    box = World.getTileBox(getX() + d.x, y);
                    setPosition(box.x + box.width, getY());
                    left = false;
                    d.x = speed;
                }
            }
            for (float x : xs) {
                if (d.y <= 0 && world.isBlocked(x + d.x, getY() + d.y)) {
                    box = World.getTileBox(x + d.x, getY() + d.y);
                    setPosition(getX(), box.y + box.height);
                    if(MathUtils.randomBoolean()) d.y = 5f;
                    else d.y /= -2;
                }
                if (d.y >= 0 && world.isBlocked(x + d.x, getTop() + d.y)) {
                    box = World.getTileBox(x + d.x, getTop() + d.y);
                    setPosition(getX(), box.y - getHeight());
                    d.y = -0.001f;
                }
            }
        }
    }

    @Override
    public void takeDamage(float amount) {
        if(!hurt) GdxGame.audioService.playSound("boss_damage", 1, 1, (getCenterX() - Player.CURRENT.getCenterX()) / 16f);
        super.takeDamage(amount);
    }

    @Override
    public void destroy() {
        super.destroy();
        body.destroy();
        TimeService.remove(spawnTimer);
    }

    private class Body extends Enemy {
        private boolean crawling = false;

        private Body(float x, float y) {
            super("boss_glibber_body", x, y);
            this.speed = 0.6f;
            this.health = BODY_HEALTH;
            this.power = 8;
            this.drawLayer = DrawLayer.FOREGROUND;
        }

        @Override
        public void collideWith(Entity other) {
            if(other instanceof Bullet) {
                System.out.println(other.toString());
                System.out.println(this.toString());
                float angle = MathUtils.random(60f, 120f);
                Goop goop = Goop.POOL.obtain();
                goop.setPosition(other.getCenterX(), other.getTop());
                goop.d.setAngleDeg(angle);
                world.addEntity(goop);
            }
            super.collideWith(other);
        }

//        @Override
//        public void draw(Batch batch) {
//            if(crawling)
//                super.draw(batch);
//            else
//                batch.draw(GdxGame.textureAtlasService.findEntityRegion("boss_glibber_regroup"), x, y, width, height);
//        }

        @Override
        void checkCollisions() {
            if(crawling) {
                Enemy.geemerCollisionAI(this);
                if(onGround()) Slick.spawn(getCenterX(), getY() - speed);
            } else {
                Rectangle box;
                d.sub(0, world.getGravity());
                float[] xs = {getX(), getCenterX(), getRight()};
                for (float x : xs) {
                    if (d.y <= 0 && world.isBlocked(x + d.x, getY() + d.y)) {
                        box = World.getTileBox(x + d.x, getY() + d.y);
                        left = Player.CURRENT.getCenterX() < getCenterX();
                        setPosition(getX(), box.y + box.height);
                        d.set(left ? -speed : speed, 0);
                        crawling = true;
                        return;
                    }
                    if (d.y >= 0 && world.isBlocked(x + d.x, getTop() + d.y)) {
                        box = World.getTileBox(x + d.x, getTop() + d.y);
                        setPosition(getX(), box.y - getHeight());
                        d.set(speed, 0);
                        crawling = true;
                        return;
                    }
                }
            }

        }

        @Override
        public void respawn() {
            super.respawn();
            this.health = BODY_HEALTH;
            this.crawling = false;
            this.setSize(42, 42);
            this.setPosition(spawnX + (getWidth() - 48)/2, spawnY);
            this.d.setAngleDeg(0);
        }
    }

    private static class Goop extends Enemy implements Pool.Poolable {
        private static final Array<TextureAtlas.AtlasRegion> FRAMES = GdxGame.textureAtlasService.findEntityRegions("boss_glibber_piece");
        private static final Pool<Goop> POOL = new Pool<Goop>() {
            @Override
            protected Goop newObject() {
                Goop goop = new Goop();
                goop.texture = FRAMES.get(MathUtils.random(FRAMES.size));
                goop.setSize(goop.texture.getRegionWidth(), goop.texture.getRegionHeight());
                return goop;
            }
        };
        private TextureRegion texture;

        private Goop() {
            super("boss_glibber_piece");
            this.power = 3;
            this.health = 1;
            this.speed = 2.8f;
            this.d.set(0, -speed);
            this.drawLayer = DrawLayer.FOREGROUND;
        }

        @Override
        public void update(float delta) {
            super.update(delta);
            d.y -= World.CURRENT.getGravity();
            if(!GdxGame.getCamera().getBox().contains(getX() + d.x, getY() + d.y)) destroy();
        }

//        public void draw(Batch batch) {
//            batch.draw(texture, x, y, width / 2, height / 2, width, height, 1.0f, 1.0f, d.angle());
//        }

        @Override
        public void reset() {
            super.respawn();
            this.health = 1;
            this.setPosition(0, 0);
            this.d.setAngle(270);
            this.setSize(7, 6);
            World.CURRENT.removeEntity(this);
        }
    }
}
