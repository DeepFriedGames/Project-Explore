package com.shdwfghtr.explore;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.shdwfghtr.asset.InventoryService;
import com.shdwfghtr.asset.PaletteService;
import com.shdwfghtr.asset.TimeService;
import com.shdwfghtr.entity.Entity;
import com.shdwfghtr.entity.Player;
import com.shdwfghtr.entity.Slick;
import com.shdwfghtr.entity.Spawner;

import java.util.HashMap;

public class World {
    public static final float DEFAULT_GRAVITY = 0.14f;
    private static final Vector2 VECTOR2 = new Vector2();
    private static final Pool<RestoreTimer> BREAK_TIMER_POOL = new Pool<RestoreTimer>() {
        @Override
        protected RestoreTimer newObject() {
            return new RestoreTimer();
        }
    };
    private static final Pool<DisruptTimer> DISRUPT_TIMER_POOL = new Pool<DisruptTimer>() {
        @Override
        protected DisruptTimer newObject() {
            return new DisruptTimer();
        }
    };
    public static final int MIN_WIDTH = 16, MAX_WIDTH = 27, NUM_INDICES = 7;
    public static final float AVG_AREA = 320f, DENSITY = 0.18f;
    public static World CURRENT;

    private final Array<Entity> activeEntities = new Array<>();
    private final Array<Entity> inactiveEntities = new Array<>();
    private final Array<Spawner> spawners = new Array<>();

    public final Sector[][] sectorMap;

    public final HashMap<Character, TextureRegion> tileRegions = new HashMap<>();

    private final EntityUpdateThread entityThread;
    public Color[] palette;
    public String name;
    public int index = 0;
    public float gravity, atmosphere;
    private final TextureRegion backgroundRegion;
    private final TextureRegion atmosphereRegion;
    private Color atmosphereColor;

    public World(int width, int height) {
        this.sectorMap = new Sector[height][width];
        this.entityThread = new EntityUpdateThread();
        backgroundRegion = GdxGame.textureAtlasService.findBackgroundRegion( "background" + index);
        atmosphereRegion = GdxGame.textureAtlasService.findBackgroundRegion("fog");
    }

    public synchronized void update(float delta) {
        entityThread.delta = delta;
        entityThread.run();
    }

    public char getChar(float x, float y) {
        if(x >= 0 && y >= 0)
            return getSector(VECTOR2.set(x, y)).getChar(x, y);
        return ' ';
    }

    public void setChar(char c, float x, float y) {
        Sector sector = getSector(VECTOR2.set(x, y));
        int xi = (int) Math.floor((x - sector.x) / Tile.WIDTH);
        int yi = (int) Math.floor((y - sector.y) / Tile.HEIGHT);

        sector.setChar(c, xi, yi);
    }

    private char getChar(int x, int y) {
        int sx = x / Sector.WIDTH,
                sy = y / Sector.HEIGHT,
                tx = (int) Math.floor(x % Sector.WIDTH),
                ty = (int) Math.floor(y % Sector.HEIGHT);

        return getSector(sx, sy).getChar(tx, ty);
    }

    public static int getTileX(float x) {
        return (int) Math.floor(x / Tile.WIDTH) * Tile.WIDTH;
    }

    public static int getTileY(float y) {
        return (int) Math.floor(y / Tile.HEIGHT) * Tile.HEIGHT;
    }

    public void draw(Batch batch) {
        drawRegionInParallax(batch, backgroundRegion, palette[7], 0.92f, 0, 0);
        drawEntities(batch);
        drawTiles(batch);
        atmosphereColor = new Color(1, 1, 1, atmosphere / 8f);
        drawRegionInParallax(batch, atmosphereRegion, atmosphereColor, 1.0f, TimeService.GetTime(), 8 * MathUtils.sinDeg(TimeService.GetTime()));
    }

    private void drawTiles(Batch batch) {
        GameCamera camera = GdxGame.getCamera();
        int x1 = (int) Math.floor(camera.getX() / Tile.WIDTH),
                y1 = (int) Math.floor(camera.getY() / Tile.HEIGHT),
                x2 = (int) Math.floor(camera.getRight() / Tile.WIDTH),
                y2 = (int) Math.floor(camera.getTop() / Tile.HEIGHT);

        for(int y = y1; y <= y2; y++)
            for(int x = x1; x <= x2; x++) {
                char t = getChar(x, y);
                if (Tile.isVisible(t))
                    try {
                        if(Tile.isRotational(t)) {
                            float rotation = getTileRotation(x, y);
                            batch.draw(tileRegions.get(t), x * Tile.WIDTH, y * Tile.HEIGHT,
                                    Tile.WIDTH / 2f, Tile.HEIGHT / 2f, Tile.WIDTH, Tile.HEIGHT,
                                    1.0f, 1.0f, rotation);

                        } else
                            batch.draw(tileRegions.get(t), x * Tile.WIDTH, y * Tile.HEIGHT);
                    } catch(NullPointerException e) {
                        System.out.println("unable to draw " + t);
                    }
            }
    }

    private float getTileRotation(int x, int y) {
        if(Tile.isSolid(getChar(x, y - 1))
                && !Tile.isBreakable(getChar(x, y - 1)) && !Tile.isDisruptable(getChar(x, y - 1)))
            return 0;
        if(Tile.isSolid(getChar(x, y + 1))
                && !Tile.isBreakable(getChar(x, y + 1)) && !Tile.isDisruptable(getChar(x, y + 1)))
            return 180;
        if(Tile.isSolid(getChar(x - 1, y))
                && !Tile.isBreakable(getChar(x - 1, y)) && !Tile.isDisruptable(getChar(x - 1, y)))
            return 270;
        if(Tile.isSolid(getChar(x + 1, y))
                && !Tile.isBreakable(getChar(x + 1, y)) && !Tile.isDisruptable(getChar(x + 1, y)))
            return 90;
        return 0;
    }

    private void drawEntities(Batch batch) {
        for(Entity entity : getActiveEntities())
            entity.draw(batch);
    }

    //draws a region tiled with parallax scrolling
    private void drawRegionInParallax(Batch batch, TextureRegion region, Color color, float parallax, float offsetX, float offsetY) {
        if(region == null || GdxGame.getCamera() == null) return;
        Rectangle camera = GdxGame.getCamera().getBox();

        float regionWidth = region.getRegionWidth();
        float regionHeight = region.getRegionHeight();
        float drawX = camera.x * parallax + offsetX;
        float drawY = camera.y * parallax + offsetY;

        int widthsToDraw = MathUtils.ceil(camera.getWidth() / regionWidth);
        int heightsToDraw = MathUtils.ceil(camera.getHeight() / regionHeight);
        int regionLocalX = MathUtils.floor(drawX % regionWidth);
        int regionLocalY = MathUtils.floor(drawY % regionHeight);

        Color previousColor = new Color(batch.getColor());
        batch.setColor(color);

        for(int w = 0; w <= widthsToDraw; w ++)
            for(int h = 0; h <= heightsToDraw; h++){
                batch.draw(region
                        , camera.x - regionLocalX + w * regionWidth
                        , camera.y - regionLocalY + h * regionHeight
                );
            }

//        batch.draw(bg, camera.x - cutX, camera.y - cutY);
//        batch.draw(bg, camera.x - cutX, camera.y - cutY + h);
//        batch.draw(bg, camera.x - cutX + w, camera.y - cutY);
//        batch.draw(bg, camera.x - cutX + w, camera.y - cutY + h);

        batch.setColor(previousColor);
    }

    public Sector getSector(Vector2 worldPoint) {
        return getSector(worldPoint.x, worldPoint.y);
    }

    public Sector getSector(float worldX, float worldY) {
        //returns a sector based on a world point
        int sx = (int) Math.floor(worldX / Sector.pWIDTH);
        int sy = (int) Math.floor(worldY / Sector.pHEIGHT);

        return getSector(sx, sy);
    }

    public void addEntity(Entity entity) {
        if(!activeEntities.contains(entity, true) && !InventoryService.contains(this, entity)) {
            entity.loadAnimation(this);
            inactiveEntities.add(entity);
        } else
            System.out.println(entity.getName() + " not added to world");
    }

    private void setActive(Entity e, boolean activate) {
        if(activate) {
            inactiveEntities.removeValue(e, true);
            if(!activeEntities.contains(e, true))
                if (e.drawLayer == Entity.DrawLayer.FOREGROUND) {
                    activeEntities.add(e);
                } else {
                    activeEntities.insert(0, e);
                }
        } else if(!e.persistent) {
            activeEntities.removeValue(e, true);
            if(!inactiveEntities.contains(e, true))
                inactiveEntities.add(e);
        }
    }

    public void removeEntity(Entity entity) {
        activeEntities.removeValue(entity, true);
        inactiveEntities.removeValue(entity, true);
    }

    void addSpawner(Spawner spawner) {
        spawners.add(spawner);
    }

    public Sector getSector(int x, int y) {
        if(y >= 0 && x >=0 && y < getHeight() && x < getWidth())
            return sectorMap[y][x];
        else
			return null;
    }

    public int getWidth() {
        //returns the sector map's width in sectors
        return sectorMap[0].length;
    }

    public int getHeight() {
        //returns the sector map's height in sectors
        return sectorMap.length;
    }

    public Entity[] getActiveEntities() {
        return activeEntities.toArray(Entity.class);
    }

    private Entity[] getInactiveEntities() {
        return inactiveEntities.toArray(Entity.class);
    }

    private Spawner[] getSpawners() {
        return spawners.toArray(Spawner.class);
    }

    public boolean isBlocked(float x, float y) {
        return Tile.isSolid(getChar(x, y));
    }

    public boolean isSlick(float x, float y) {
        for(Entity e : getActiveEntities())
            if(e instanceof Slick)
                if(e.getBox().contains(x, y))
                    return true;
        return false;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() { return this.name; }

    public float getGravity() { return DEFAULT_GRAVITY * gravity; }

    public void disruptTile(float x, float y) {
        if(Tile.isDisruptable(getChar(x, y))) {
            DisruptTimer timer = DISRUPT_TIMER_POOL.obtain();
            timer.box.setPosition((int) Math.floor(x / Tile.WIDTH) * Tile.WIDTH,
                    (int) Math.floor(y / Tile.HEIGHT) * Tile.HEIGHT);
            timer.t = getChar(x, y);
            timer.reset();
        }
    }

    public void breakTile(float x, float y) {
        if(Tile.isBreakable(getChar(x, y))) {
            RestoreTimer timer = BREAK_TIMER_POOL.obtain();
            timer.box.setPosition((int) Math.floor(x / Tile.WIDTH) * Tile.WIDTH,
                    (int) Math.floor(y / Tile.HEIGHT) * Tile.HEIGHT);
            timer.t = getChar(x, y);
            timer.reset();
            Vector2 center = timer.box.getCenter(VECTOR2);
            GdxGame.audioService.playSound("disruption", 0.7f, MathUtils.random(0.8f, 1.2f), (center.x - Player.CURRENT.getCenterX()) / Tile.WIDTH);
            ParticleEffectPool.PooledEffect effect = GdxGame.particleService.obtain("break", false);
            PaletteService.colorEffect(effect, World.CURRENT.palette[2]);
            effect.setPosition(center.x, center.y);
            GdxGame.particleService.add(effect);
            setChar(' ', x, y);
        }
    }

    public static String getType(int index) {
        if(index == 0)
            return "planet_terran";
        else if(index == 1)
            return "planet_rocky";
        else if(index == 2)
            return "planet_ringed";
        else if(index == 3)
            return "planet_gaseous";
        else if(index == 4)
            return "planet_living";
        else if(index == 5)
            return "planet_station";
        else if(index == 6)
            return "planet_aquatic";
        else
            return "planet_barren";
    }

    public static Rectangle getTileBox(float x, float y) {
        return new Rectangle(getTileX(x), getTileY(y), Tile.WIDTH, Tile.HEIGHT);
    }

    private static class RestoreTimer extends TimeService.Timer implements Pool.Poolable {
        final transient Rectangle box = new Rectangle(0, 0, Tile.WIDTH, Tile.HEIGHT);
        transient char t;
        private RestoreTimer() {
            super(4.5f);
        }

        @Override
        public boolean onCompletion() {
            if (!box.overlaps(Player.CURRENT.getBox())) {
                Vector2 center = box.getCenter(VECTOR2);
                World.CURRENT.setChar(t, center.x, center.y);
                BREAK_TIMER_POOL.free(this);
                return true;
            } else {
                this.reset();
                return false;
            }
        }
    }

    private static class DisruptTimer extends TimeService.Timer implements Pool.Poolable {
        final transient Rectangle box = new Rectangle(0, 0, Tile.WIDTH, Tile.HEIGHT);
        transient char t;
        private DisruptTimer() {
            super(0.1f);
        }

        @Override
        public boolean onCompletion() {
            GdxGame.audioService.playSound("disruption", 0.4f);
            ParticleEffectPool.PooledEffect effect = GdxGame.particleService.obtain("disrupt", false);
            PaletteService.colorEffect(effect, World.CURRENT.palette[2]);
            Vector2 center = box.getCenter(VECTOR2);
            effect.setPosition(center.x, center.y);
            GdxGame.particleService.add(effect);
            World.CURRENT.setChar(' ', center.x, center.y);
            RestoreTimer timer = BREAK_TIMER_POOL.obtain();
            timer.t = t;
            timer.box.setPosition(box.getPosition(VECTOR2));
            timer.reset();
            DISRUPT_TIMER_POOL.free(this);
            return true;
        }
    }

    private class EntityUpdateThread extends Thread {
        float delta;

        @Override
        public synchronized void run() {
            Entity[] inactiveArray = getInactiveEntities();
            Entity[] activeArray = getActiveEntities();

            final Array<Entity> activate = new Array<>();
            final Array<Entity> deactivate = new Array<>();
            final Array<Entity> dead = new Array<>();

            final Rectangle cameraBox = GdxGame.getCamera().getBox();

            for(Entity inactive : inactiveArray)
                if(cameraBox.overlaps(inactive.getBox())
                        || cameraBox.overlaps(getSector(inactive.getCenterX(), inactive.getCenterY()).getBox()))
                    activate.add(inactive);

            //the world primarily checks collisions for all of the activeArray and tiles it contains
            Entity entity, other;
            int size = activeArray.length;
            for(int i = 0; i < size; i++) {
                entity = activeArray[i];

                entity.update(delta);

                for(int j = i + 1; j < size; j++) {
                    //check to see if the entity collides with any other entity
                    other = activeArray[j];
                    if(entity.getBox().overlaps(other.getBox())) {
                        //if their bounding boxes overlap, then the collision is passed on to the activeArray for handling
                        entity.collideWith(other);
                        other.collideWith(entity);
                    }
                }

                if(!(cameraBox.overlaps(entity.getBox())
                        || cameraBox.overlaps(getSector(entity.getCenterX(), entity.getCenterY()).getBox())))
                    deactivate.add(entity);

                if(entity.isDead())
                    dead.add(entity);
            }

            for(Spawner spawner : getSpawners())
                if(spawner.entity.isDead() && !cameraBox.overlaps(getSector(spawner.getCenter()).getBox()))
                    spawner.spawn();

            // post a Runnable to the rendering thread that processes the result
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    for(Object e : dead.toArray(Entity.class))
                        removeEntity((Entity) e);
                    for(Object e : activate.toArray(Entity.class))
                        setActive((Entity) e, true);
                    for(Object e : deactivate.toArray(Entity.class))
                        setActive((Entity) e, false);
                }
            });
        }
    }
}
