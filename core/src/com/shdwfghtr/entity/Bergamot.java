package com.shdwfghtr.entity;

import com.badlogic.gdx.audio.Music;
import com.shdwfghtr.explore.Asset;
import com.shdwfghtr.explore.Tile;
import com.shdwfghtr.explore.Timer;
import com.shdwfghtr.explore.World;

public class Bergamot extends Boss {
    private Door door = null;
    private Enemy[] wormHives;
    private Crawler[] crawlers;
    private Snail snail;
    private float[] snailPos;
    private Wasp wasp;
    private transient Timer spawnTimer;

    public Bergamot(World world, float x, float y) {
        super("boss_bergamot_body", x, y);
        this.health = 40;
        initialize(world);
        this.world = world;

    }

    @Override
    public void initialize(final World world) {
        intro_music = Asset.getManager().get("audio/Bergamot_Intro.ogg", Music.class);
        main_music = Asset.getManager().get("audio/Bergamot.ogg", Music.class);
        wormHives = new Enemy[2];
        Enemy[] vines = new Enemy[4];
        crawlers = new Crawler[]{new Crawler(), new Crawler()};
        snail = new Snail();
        spawnTimer = new Timer(2) {
            @Override
            public boolean onCompletion() {
                int size = crawlers.length;
                for(int i=0; i<size; i++)
                    if(crawlers[i].isDead()) {
                        crawlers[i].respawn();
                        crawlers[i].setPosition(wormHives[i].getCenterX() - crawlers[i].getWidth() / 2,
                                wormHives[i].getTop() - crawlers[i].getHeight());
                        crawlers[i].left = (i == 0);
                        world.addEntity(crawlers[i]);
                    }
                if(snail.isDead()) {
                    snail.respawn();
                    snail.setPosition(snailPos[0], snailPos[1]);
                    world.addEntity(snail);
                }
                return true;
            }
        };
        //creates a couple of worm spawners
        wormHives[0] = new Enemy("boss_bergamot_nest_right", getRight() - 5.5f, getTop() - 9);
        wormHives[0].health = 10;
        world.addEntity(wormHives[0]);

        wormHives[1] = new Enemy("boss_bergamot_nest_left",getX() - 5.5f, getTop() - 9);
        wormHives[1].health = 10;
        world.addEntity(wormHives[1]);

        //creates some vines that hang from the ceiling
        for(int i = 0; i< vines.length; i++) {
            Enemy vine = new Enemy("boss_bergamot_vine");
            vine.setSize(9, 34);
            if(i < vines.length/2)
                vine.setPosition(getX() - Tile.WIDTH * (i + 1) - vine.getWidth(), getTop() - vine.getHeight());
            else
                vine.setPosition(getRight() + Tile.WIDTH * (i - 1), getTop() - vine.getHeight());
            vine.health = 6;
            vine.power = 10;
            if(vine.getX() > getCenterX()) vine.left = true;
            vines[i] = vine;
            world.addEntity(vine);
            vine.getAnimation().setFrameDuration(1.5f);
        }

        //creates an shield around Bergamot's petals that stops bullets
        Entity petals = new Entity();
        petals.setBounds(getX() - 16, getY() - 14, 64, 16);
        //sets the petal shield tiles to be solid
        for(int i=-1; i<=1; i++)
            world.setChar('=',getCenterX() + Tile.WIDTH * i, petals.getCenterY());
        petals.power = 4;
        world.addEntity(petals);

        //creates a snail that will respawn with the crawlers
        if(world.getChar(getCenterX() - Tile.WIDTH * 8, getCenterY() - Tile.HEIGHT * 8) == '!')
            snailPos = new float[]{getCenterX() - Tile.WIDTH * 8, getCenterY() - Tile.HEIGHT * 8};
        else {
            snailPos = new float[]{getCenterX() + Tile.WIDTH * 8, getCenterY() - Tile.HEIGHT * 8};
            snail.left = true;
        }
        snail.setPosition(snailPos[0], snailPos[1]);
        world.addEntity(snail);

        int size = crawlers.length;
        for(int i=0; i<size; i++) {
            crawlers[i].setPosition(wormHives[i].getCenterX() - crawlers[i].getWidth()/2,
                    wormHives[i].getTop() - crawlers[i].getHeight());
            crawlers[i].left = (i == 0);
            world.addEntity(crawlers[i]);
        }
        spawnTimer.reset();
        super.initialize(world);
    }

    @Override
    public void update(float delta) {
        if(!main_music.isPlaying() && !intro_music.isPlaying()) {
            Asset.getMusicHandler().setVolume(1);
            Asset.getMusicHandler().setMusic(intro_music, false);
        }
        if(door == null) {
            Asset.MESSAGES.add("-BERGAMOT-");
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
        if(spawnTimer.isComplete()) spawnTimer.reset();

        if(wasp == null) {
            if (Player.CURRENT.getCenterY() < getY()) {
                wasp = Wasp.POOL.obtain();
                wasp.respawn();
                wasp.setPosition(getCenterX() - wasp.getWidth() / 2, getCenterY() - wasp.getHeight() / 2);
                wasp.d.set(0, -wasp.speed);
                world.addEntity(wasp);
            }
        } else {
            if (!Asset.CAMERA.getBox().overlaps(wasp.getBox()) && Player.CURRENT.getCenterY() < getY()) {
                wasp.setPosition(getCenterX() - wasp.getWidth() / 2, getCenterY() - wasp.getHeight() / 2);
                wasp.d.set(0, -wasp.speed);
            }
            if (wasp.isDead()) {
                Wasp.POOL.free(wasp);
                wasp = null;
            }
        }

    }
//
//    @Override
//    public void draw(Batch batch) {
//        if(!hurt || Asset.RANDOM.nextBoolean())
//            batch.draw(getAnimation().getKeyFrame(Asset.TIME), getX() - 16, getY() - 16);
//    }

    @Override
    public void takeDamage(float amount) {
        if(!hurt) Asset.getMusicHandler().playSound("boss_damage", 1, 1, (getCenterX() - Player.CURRENT.getCenterX()) / 16f);
        super.takeDamage(amount);
    }

    @Override
    public void destroy() {
        super.destroy();
        if(Asset.TIMERS.contains(spawnTimer)) Asset.TIMERS.remove(spawnTimer);
    }
}
