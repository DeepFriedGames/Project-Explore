package com.shdwfghtr.explore;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.shdwfghtr.entity.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class World {
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

    private final Array<Entity> activeEntities = new Array<Entity>();
    private final Array<Entity> inactiveEntities = new Array<Entity>();
	private final Array<Entity> viewInterests = new Array<Entity>();
    private final Array<Spawner> spawners = new Array<Spawner>();

    public final Sector[][] sectorMap;

    private final HashMap<Character, TextureRegion> tileRegions = new HashMap<Character, TextureRegion>();
    public final TextureAtlas entityAtlas;

    private final EntityUpdateThread entityThread;
    public Color[] palette;
    public TextureAtlas sectorAtlas;
    public String name;
    public int index = 0;
    public float gravity, atmosphere;

    public World(int width, int height) {
        this.sectorMap = new Sector[height][width];
        this.entityAtlas = new TextureAtlas("atlas/Entity.atlas");
        this.entityThread = new EntityUpdateThread();
    }

    public synchronized void update(float delta) {
        //marks the player's current sector as explored
        if(!getSector(Player.CURRENT.getBox().getCenter(Asset.VECTOR2)).isExplored())
            getSector(Player.CURRENT.getBox().getCenter(Asset.VECTOR2)).setExplored(true);

        entityThread.delta = delta;
        entityThread.run();
    }

    public char getChar(float x, float y) {
        if(x >= 0 && y >= 0)
            return getSector(Asset.VECTOR2.set(x, y)).getChar(x, y);
        return ' ';
    }

    public void setChar(char c, float x, float y) {
        Sector sector = getSector(Asset.VECTOR2.set(x, y));
        int xi = (int) Math.floor((x - sector.getX()) / Tile.WIDTH);
        int yi = (int) Math.floor((y - sector.getY()) / Tile.HEIGHT);

        sector.setChar(c, xi, yi);
    }

    private char getChar(int x, int y) {
        int sx = (int) Math.floor(x / Sector.WIDTH),
                sy = (int) Math.floor(y / Sector.HEIGHT),
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
        drawBackgroundTexture(batch, "background" + index, palette[1], 0.92f, 0);
        drawEntities(batch);
        drawTiles(batch);
        drawBackgroundTexture(batch, "fog", new Color(1, 1, 1, atmosphere / 4), 1.0f, Asset.TIME);
    }

    private void drawTiles(Batch batch) {
        int x1 = (int) Math.floor(Asset.CAMERA.getX() / Tile.WIDTH),
                y1 = (int) Math.floor(Asset.CAMERA.getY() / Tile.HEIGHT),
                x2 = (int) Math.floor(Asset.CAMERA.getRight() / Tile.WIDTH),
                y2 = (int) Math.floor(Asset.CAMERA.getTop() / Tile.HEIGHT);

        for(int y = y1; y <= y2; y++)
            for(int x = x1; x <= x2; x++) {
                char t = getChar(x, y);
                if (Tile.isVisible(t))
                    try {
                        if(Tile.isRotational(t)) {
                            float rotation = getTileRotation(x, y);
                            batch.draw(tileRegions.get(t), x * Tile.WIDTH, y * Tile.HEIGHT,
                                    Tile.WIDTH / 2, Tile.HEIGHT / 2, Tile.WIDTH, Tile.HEIGHT,
                                    1.0f, 1.0f, rotation);

                        } else
                            batch.draw(tileRegions.get(t), x * Tile.WIDTH, y * Tile.HEIGHT);
                    } catch(NullPointerException e) {
                        System.out.println("unable to draw " + String.valueOf(t));
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

    private void drawBackgroundTexture(Batch batch, String name, Color color, float parallax, float offset) throws NullPointerException {
        TextureRegion bg = Asset.getBGAtlas().findRegion(name);
        if(bg == null) return;
        //draws a parallax scrolling background image
        int w = bg.getRegionWidth(),
                h = bg.getRegionHeight();
        float camX = Asset.CAMERA.getBox().x,
                camY = Asset.CAMERA.getBox().y;
        int cutX = Math.round((camX * parallax + offset) % w),
                cutY = h - Math.round((camY * parallax + offset) % h);

        Color c = new Color(batch.getColor());
        batch.setColor(color);

        batch.draw(new TextureRegion(bg, cutX, 0, w - cutX, cutY), camX, camY);
        batch.draw(new TextureRegion(bg, cutX, cutY, w - cutX, h - cutY), camX, camY + cutY);
        batch.draw(new TextureRegion(bg, 0, 0, cutX, cutY), camX + w - cutX, camY);
        batch.draw(new TextureRegion(bg, 0, cutY, cutX, h - cutY), camX + w - cutX, camY + cutY);

        batch.setColor(c.r, c.g, c.b, c.a);
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
        if(!activeEntities.contains(entity, true) && !Player.INVENTORY.contains(Entity.getSaveString(this, entity))) {
            entity.loadAnimation(this);
            inactiveEntities.add(entity);
        } else
            System.out.println(entity.getName() + " not added to world");
		
		if(entity.getImportance() > 0)
            viewInterests.add(entity);
    }

    private void setActive(Entity e, boolean activate) {
        if(activate) {
            inactiveEntities.removeValue(e, true);
            if(!activeEntities.contains(e, true))
                switch (e.drawLayer) {
                    case BACKGROUND:
                        activeEntities.insert(0, e);
                        break;
                    case FOREGROUND:
                        activeEntities.add(e);
                        break;
                    default:
                        activeEntities.insert(0, e);
                        break;
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
		viewInterests.removeValue(entity, true);
    }

    private void addSpawner(Spawner spawner) {
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

    Entity[] getInterests() {
        return viewInterests.toArray(Entity.class);
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

    void setSector(String fileName, int x, int y, boolean flipX, boolean flipY) {
        sectorMap[y][x] = new Sector(fileName, x*Sector.pWIDTH, y*Sector.pHEIGHT, flipX, flipY);

    }

    void setSector(Sector sector) {
        sectorMap[sector.getYi()][sector.getXi()] = sector;

    }

    public void createEntities() {
        LinkedHashMap<Door, Switch> switches = new LinkedHashMap<Door, Switch>();
        ArrayList<float[]> switchPositions = new ArrayList<float[]>();
        for(Sector[] array : sectorMap)
            for(final Sector sector : array) {
                for (int y = 0; y < Sector.HEIGHT; y++)
                    for (int x = 0; x < Sector.WIDTH; x++) {
                        float worldX = sector.getX() + x * Tile.WIDTH,
                                worldY = sector.getY() + y * Tile.HEIGHT;
                        char id = sector.getChar(x, y);
                        if (Character.isDigit(id)) continue;

                        if (id == '+')
                            switchPositions.add(new float[]{worldX, worldY});
                        else if (id == '$') {
                            if(index == 0)
                                addEntity(new Bergamot(this, worldX - 8, worldY - 8));
                            else if(index == 2)
                                addEntity(new Glibber(this, worldX - 8, worldY));
                        } else if (id == '@') {
                            if (Asset.RANDOM.nextFloat() < Item.PROB_GENERIC_ITEM) {
                                String itemName = Item.GENERIC_ITEMS.get(Asset.RANDOM.nextInt(Item.GENERIC_ITEMS.size()));
                                addEntity(new Item("item_" + itemName, worldX - 1, worldY - 1) {
                                    @Override
                                    public void draw(Batch batch) {
                                        if (!Tile.isSolid(sector.getChar(getCenterX(), getCenterY())))
                                            super.draw(batch);
                                    }
                                });
                            }
                        } else if (Character.isUpperCase(id)) {
                            //upper case letters in maps represent items
                            if (id == 'S') {
                                CURRENT = this;
                                Player.CURRENT = new Player(worldX, worldY);
                                Player.CURRENT.loadAnimations();
                                if(Asset.DATA.contains("power"))
                                    Player.load(Player.CURRENT);
                                addEntity(Player.CURRENT);
                            } else if (id == 'I') {
                                int index = Asset.RANDOM.nextInt(Item.OPTIONAL_ITEMS.size());
                                String name = Item.OPTIONAL_ITEMS.get(index);
                                System.out.println("index " + index + ": " + name);
                                addEntity(new Item("item_" + name, worldX - 1, worldY - 1) {
                                    @Override
                                    public void collideWith(Entity entity) {
                                        if (!Tile.isSolid(sector.getChar(getCenterX(), getCenterY())))
                                            super.collideWith(entity);
                                    }

                                    @Override
                                    public void draw(Batch batch) {
                                        if (!Tile.isSolid(sector.getChar(getCenterX(), getCenterY())))
                                            super.draw(batch);
                                    }
                                });
                            } else if (id == 'D') {
                                String name = null;
                                if (x == Sector.WIDTH - 1) name = "door_horizontal";
                                else if (y == Sector.HEIGHT - 1) name = "door_vertical";
                                if (name != null) {
                                    final Door door = new Door(name, worldX, worldY);
                                    Sector otherSector = getSector(sector.getXi() + 1, sector.getYi());
                                    if (y != Sector.HEIGHT - 1
                                            && (sector.getName().contains("item") ^ otherSector.getName().contains("item")
                                            ^ sector.getName().contains("boss") ^ otherSector.getName().contains("boss"))) {
                                        switches.put(door, new Switch() {
                                            @Override
                                            public void trigger() {
                                                super.trigger();
                                                Asset.DATA.putBoolean(Entity.getSaveString(door), false);
                                                Asset.DATA.flush();
                                                door.setLocked(false);
                                            }
                                        });
                                    }
                                    addEntity(door);
                                }
                            } else if (id == 'W')
                                addEntity(new Item("item_wall_jump", worldX, worldY));
                            else if (id == 'C')
                                addEntity(new Item("item_compression_orb", worldX, worldY));
                            else if (id == 'B')
                                addEntity(new Item("item_bomb", worldX, worldY));
                            else if (id == 'G')
                                addEntity(new Item("item_grapple_shot", worldX, worldY));
                            else if (id == 'A')
                                addEntity(new Item("item_mach_boots", worldX, worldY));
                            else if (id == 'J')
                                addEntity(new Item("item_double_jump", worldX, worldY));
                            else if (id == 'P')
                                addEntity(new Item("item_phase_shot", worldX, worldY));
                            else if (id == 'O') {
                                addEntity(new Item("item_oxygen_tank", worldX, worldY));
                            } else if (id == 'M') {
                                addEntity(new Item("item_missiles", worldX, worldY));
                            } else if (id == 'H') {
                                addEntity(new Item("item_armor_up", worldX, worldY));
                            }
                        } else if (Character.isLowerCase(id)) {
                            //lower case letters in maps represent critters
                            if (Asset.RANDOM.nextBoolean()) {
                                Entity enemy = null;
                                if (id == 'c')
                                    enemy = new Crawler(worldX, worldY);
                                else if (id == 'h')
                                    enemy = new Hopper(worldX, worldY);
                                else if (id == 'd')
                                    enemy = new Dropper(worldX, worldY);
                                else if (id == 's')
                                    enemy = new Snail(worldX, worldY);
                                else if(id == 'w')
                                    enemy = new WaspNest(worldX, worldY);
                                else if(id == 'g')
                                    enemy = new Gnats(worldX, worldY);
                                else if(id == 't')
                                    enemy = new Turret(Asset.RECTANGLE.set(worldX, worldY, Tile.WIDTH, Tile.HEIGHT), this);
                                if(enemy != null) {
                                    addSpawner(new Spawner(enemy));
                                    addEntity(enemy);
                                }
                            }
                        }
                    }
            }
        for(Door door : switches.keySet())
            if(switchPositions.size() > 0) {
                Switch s = switches.get(door);
                float[] pos = switchPositions.remove(Asset.RANDOM.nextInt(switchPositions.size()));
                setChar('=', pos[0], pos[1]);
                s.setPosition(pos[0], pos[1]);
                door.setLocked(Asset.DATA.getBoolean(Entity.getSaveString(this, door), true));
                if(!door.isLocked())
                    s.name = s.name.replace("_locked", "");
                addEntity(s);
            }
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() { return this.name; }

    public float getGravity() { return Asset.GRAVITY * gravity; }

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
            Asset.CAMERA.addTrauma(0.15f);
            RestoreTimer timer = BREAK_TIMER_POOL.obtain();
            timer.box.setPosition((int) Math.floor(x / Tile.WIDTH) * Tile.WIDTH,
                    (int) Math.floor(y / Tile.HEIGHT) * Tile.HEIGHT);
            timer.t = getChar(x, y);
            timer.reset();
            Vector2 center = timer.box.getCenter(Asset.VECTOR2);
            Asset.getMusicHandler().playSound("disruption", 0.7f, Asset.RANDOM.nextFloat() * 0.4f + 0.8f, (center.x - Player.CURRENT.getCenterX()) / Tile.WIDTH);
            ParticleEffectPool.PooledEffect effect = Asset.getParticles().obtain("break", false);
            Asset.Particles.colorEffect(effect, World.CURRENT.palette[2]);
            effect.setPosition(center.x, center.y);
            Asset.getParticles().add(effect);
            setChar(' ', x, y);
        }
    }

    public void dispose() {
        entityAtlas.dispose();
        sectorAtlas.dispose();
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

    public static TextureAtlas generateSectorAtlas(World world) {
        Pixmap.Format format = Pixmap.Format.RGB565;
        int width = world.getWidth(), height = world.getHeight();
        PixmapPacker packer =
                new PixmapPacker(width * Sector.WIDTH, height * Sector.HEIGHT,
                        format, 2, true);


        for(int yi=0; yi < height; yi++)
            for(int xi=0; xi < width; xi++) {
                Sector sector = world.getSector(xi, yi);
                if(packer.getPageIndex(sector.name) < 0) {
                    Pixmap pixmap = new Pixmap(Sector.WIDTH, Sector.HEIGHT, format);

                    if (sector.name.contains("dead")) {
                        TextureRegion tr = Asset.getUIAtlas().findRegion("hud_empty_cell");
                        Texture tex = tr.getTexture();
                        TextureData texData = tex.getTextureData();
                        if (!texData.isPrepared()) texData.prepare();
                        Pixmap texMap = texData.consumePixmap();

                        for (int ty = 0; ty < Sector.HEIGHT; ty++)
                            for (int tx = 0; tx < Sector.WIDTH; tx++)
                                pixmap.drawPixel(tx, ty,
                                        texMap.getPixel(tr.getRegionX() + tx, tr.getRegionY() + ty));

                    } else {
                        pixmap.setColor(world.palette[1]);
                        pixmap.fill();
                        pixmap.setColor(Color.WHITE);
                        //color in each pixel is a tile
                        for (int ty = 0; ty < Sector.HEIGHT; ty++)
                            for (int tx = 0; tx < Sector.WIDTH; tx++)
                                if (Tile.isSolid(sector.getChar(tx, Sector.HEIGHT - ty - 1)))
                                    pixmap.drawPixel(tx, ty);
                    }
                    //pack the pixmap
                    packer.pack(sector.name, pixmap);
                }
            }

        TextureAtlas atlas = packer.generateTextureAtlas(
                Texture.TextureFilter.Linear, Texture.TextureFilter.Linear, false);
        packer.dispose();
        return atlas;
    }

    public static Color[] generatePalette(float seed) {
        Color[] defaultPalette = Asset.getPalette("environment");

        int length = defaultPalette.length;

        Color[] palette = new Color[length];

        for(int i=0; i<length; i++) {
            HSL hsl = new HSL(defaultPalette[i]);
            float hue = hsl.h + seed;
            if(hue < 0) hue += 1;  //HSL requires hue in [0, 1]
            if(hue > 1) hue -= 1;
            hsl.h = hue;
            palette[i] = hsl.toRGB();
        }

        return palette;
    }

    public void generateTileRegions() {
        TextureAtlas tileAtlas = new TextureAtlas("atlas/Environment.atlas");
        recolorAtlasByRegion(Asset.getPalette("environment"),
                palette, tileAtlas,"tileSheet");

        for(Character c : Tile.ALL_TILES.toCharArray())
            if(Tile.isIndexed(c)) tileRegions.put(c, tileAtlas.findRegion(String.valueOf(c), index));
            else tileRegions.put(c, tileAtlas.findRegion(String.valueOf(c)));
    }

    public static void recolorAtlasByRegion(Color[] oldPalette, Color[] newPalette, TextureAtlas atlas, String... regionNames) {
        //get the original texture of the atlas
        int length = Math.min(oldPalette.length, newPalette.length);
        Texture first = atlas.getTextures().first();
        Texture texture = first;

        //recolor the texture
        for(String name : regionNames)
            for(int i=0; i < length; i++)
                texture = Asset.recolorTextureRegion(atlas.findRegion(name), oldPalette[i], newPalette[i]);

        //assign the texture to the texture regions
        TextureRegion[] regions = atlas.getRegions().toArray(TextureRegion.class);
        for(TextureRegion tr : regions)
            tr.setTexture(texture);
        atlas.getTextures().add(texture);
        atlas.getTextures().remove(first);

    }

    public static Rectangle getTileBox(float x, float y) {
        return new Rectangle(getTileX(x), getTileY(y), Tile.WIDTH, Tile.HEIGHT);
    }

    public boolean isBlocked(Vector2 v) {
        return isBlocked(v.x, v.y);
    }

    private static class RestoreTimer extends Timer implements Pool.Poolable {
        final transient Rectangle box = new Rectangle(0, 0, Tile.WIDTH, Tile.HEIGHT);
        transient char t;
        private RestoreTimer() {
            super(4.5f);
        }

        @Override
        public boolean onCompletion() {
            if (!box.overlaps(Player.CURRENT.getBox())) {
                Vector2 center = box.getCenter(Asset.VECTOR2);
                World.CURRENT.setChar(t, center.x, center.y);
                BREAK_TIMER_POOL.free(this);
                return true;
            } else {
                this.reset();
                return false;
            }
        }
    }

    private static class DisruptTimer extends Timer implements Pool.Poolable {
        final transient Rectangle box = new Rectangle(0, 0, Tile.WIDTH, Tile.HEIGHT);
        transient char t;
        private DisruptTimer() {
            super(0.1f);
        }

        @Override
        public boolean onCompletion() {
            Asset.getMusicHandler().playSound("disruption", 0.4f);
            ParticleEffectPool.PooledEffect effect = Asset.getParticles().obtain("disrupt", false);
            Asset.Particles.colorEffect(effect, World.CURRENT.palette[2]);
            Vector2 center = box.getCenter(Asset.VECTOR2);
            effect.setPosition(center.x, center.y);
            Asset.getParticles().add(effect);
            World.CURRENT.setChar(' ', center.x, center.y);
            RestoreTimer timer = BREAK_TIMER_POOL.obtain();
            timer.t = t;
            timer.box.setPosition(box.getPosition(Asset.VECTOR2));
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

            final Array<Entity> activate = new Array<Entity>();
            final Array<Entity> deactivate = new Array<Entity>();
            final Array<Entity> dead = new Array<Entity>();

            final Rectangle cameraBox = Asset.CAMERA.getBox();

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
