package com.shdwfghtr.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.shdwfghtr.entity.Player;
import com.shdwfghtr.explore.Asset;
import com.shdwfghtr.explore.GdxGame;
import com.shdwfghtr.explore.Timer;
import com.shdwfghtr.explore.World;
import com.shdwfghtr.explore.WorldLoader;

import java.util.Comparator;
import java.util.Map;
import java.util.Random;

/**
 * This screen allows the player to move a ship about a galaxy and chose star systems to explore.
 * These star systems will have loaders to explore which will take the player to the game screen.
 */
public class TravelScreen extends Menu {
    private static final Random RANDOM = new Random(Asset.DATA.getLong("seed"));
    private static final float DIALOG_WIDTH = 240, DIALOG_HEIGHT = 144;

    private final Table menu = new Table();
    private final Array<Actor> items = new Array<Actor>();

    private WorldLoader[] loaders;
    private WorldLoader loading;
    private static final int BUTTON_SIZE = 32;//these buttons are square

    public TravelScreen() {
        super("Destination");
    }

    @Override
    public void show() {
        //reset the random number generator, then reload the star system seeds
        if(Asset.DATA.contains("seed"))
            RANDOM.setSeed(Asset.DATA.getLong("seed"));
        else {
            long seed = Asset.RANDOM.nextLong();
            RANDOM.setSeed(seed);
            Asset.DATA.putLong("seed", seed);
        }
        Asset.DATA.flush();

        Asset.getMusicHandler().setMusic("Intro");

        headerText = "";
        super.show();

        Asset.getCurtain().setBounds(0, 0, Asset.getStage().getWidth(), Asset.getStage().getHeight());
        Asset.getCurtain().addAction(Actions.alpha(1));
        Asset.getCurtain().setName("Curtain");
        Asset.getCurtain().setTouchable(Touchable.disabled);
        Asset.getStage().addActor(Asset.getCurtain());
        Asset.getCurtain().addAction(Actions.fadeOut(2));
        Asset.TIMERS.add(new Timer(2) {
            @Override
            public boolean onCompletion() {
                Asset.getCurtain().remove();
                return super.onCompletion();
            }
        });

        createStarSystem();
        createButtons();
        createPauseMenu();
    }

    private void createStarSystem() {
        int starSize = 256;

        int num_worlds = RANDOM.nextInt(4) + 3;
        loaders = new WorldLoader[num_worlds];
        for(int i = 0; i < num_worlds; i++) {
            final WorldLoader loader = new WorldLoader();

            loader.seed = Math.abs(RANDOM.nextLong());
            loader.width = RANDOM.nextInt(World.MAX_WIDTH - World.MIN_WIDTH) + World.MIN_WIDTH;
            loader.height = Math.round(World.AVG_AREA / loader.width);
            loader.area = loader.width * loader.height;
            loader.charMap = new char[loader.height][loader.width];
            loader.maxSectors = Math.round(loader.area * World.DENSITY);

            World world = new World(loader.width, loader.height);
            world.index = RANDOM.nextInt(World.NUM_INDICES);
            world.palette = World.generatePalette(RANDOM.nextFloat());
            world.gravity = RANDOM.nextFloat() * 2 + 0.5f;
            while(world.atmosphere <= 0)
                world.atmosphere = (float) RANDOM.nextGaussian() * 0.5f + 1.0f;
            loader.world = world;
            loaders[i] = loader;

            final int r = RANDOM.nextInt(Math.round(table.getHeight()/2)) + starSize/2; //radius of rotation
            final double a = RANDOM.nextFloat() * 2 * Math.PI; //starting angle
            final float s = RANDOM.nextFloat() / 50; //speed at which planets orbit

            TextureRegion tr = new TextureAtlas("atlas/Environment.atlas")
                    .findRegion(World.getType(world.index));

            Asset.recolorTextureRegion(tr, Asset.getPalette("environment"), world.palette);

            final Image planet = new Image(tr) {
                @Override
                public void act(float delta) {
                    super.act(delta);
                    float cX = table.getWidth() / 2, cY = table.getHeight() / 2;
                    float x = Math.round(cX + r * Math.cos(Asset.TIME * s + a)),
                            y = Math.round(cY + r * Math.sin(Asset.TIME * s + a) / 2);
                    setPosition(x, y);
                }
            };
            planet.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    super.clicked(event, x, y);
                    Dialog dialog = new Dialog(planet.getName(), Asset.getSkin()) {
                        @Override
                        protected void result(Object object) {
                            if(object.equals(0)){
                                //Travel to the destination
                                Asset.getCurtain().setBounds(0, 0, Asset.getStage().getWidth(), Asset.getStage().getHeight());
                                Asset.getStage().addActor(Asset.getCurtain());
                                Asset.getCurtain().addAction(Actions.fadeIn(1.0f));
                                World.CURRENT = loader.world;
                                loading = loader;
                                Thread thread = new Thread(loading);
                                thread.start();
                                Asset.getStage().addActor(new Label("Please wait...", Asset.getSkin()));

                            }
                            //remove dialog
                            remove();
                        }
                    };
                    dialog.setBounds(planet.getX() + planet.getWidth() / 2,
                            planet.getY() + planet.getHeight() / 2 - DIALOG_HEIGHT,
                            DIALOG_WIDTH, DIALOG_HEIGHT);

                    String text =
                            "Type:  " + World.getType(loader.world.index).replace("planet_", "").toUpperCase() + '\n' +
                                    "Gravity:  " + Math.round(loader.world.gravity * 100) + "%\n" +
                                    "Atmosphere:  " + Math.round(loader.world.atmosphere * 100) + '%';
                    dialog.text(text).button("Travel", 0).button("Return", 1);
                    Asset.getStage().addActor(dialog);
                }
            });
            float variance = 1 - loader.area / World.AVG_AREA;
            variance *= 32;
            float planetSize = planet.getDrawable().getMinWidth() * (2.25f + variance);
            planet.setSize(planetSize, planetSize);
            world.setName(Asset.Convert.toString(loader.seed));
            planet.setName("Planet " + world.getName());
            table.addActor(planet);
        }

        Actor star = new Actor() {
            final Animation<TextureRegion> animation = getStarAnimation();

            @Override
            public void draw(Batch batch, float parentAlpha) {
                Color batchColor = batch.getColor();

                batch.setColor(loaders[0].world.palette[7]);
                batch.draw(animation.getKeyFrame(Asset.TIME), getX(), getY(), getWidth(), getHeight());
                batch.setColor(batchColor);
            }
        };
        star.setBounds((table.getWidth() - starSize) / 2, (table.getHeight() - starSize) / 2, starSize, starSize);
        star.setTouchable(Touchable.disabled);
        star.setName("Star");
        table.addActor(star);

        GdxGame.fadeColor = loaders[0].world.palette[7];
    }

    private void createButtons() {
        final ImageButton buttonNew, buttonSettings, buttonStats;
        //Adds the listeners to each button so that they take the player to the proper menu
        buttonNew = new ImageButton(Asset.getSkin());
        buttonNew.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Asset.getCurtain().setBounds(0, 0, Asset.getStage().getWidth(), Asset.getStage().getHeight());
                Asset.getStage().addActor(Asset.getCurtain());
                Asset.getCurtain().addAction(Actions.fadeIn(1.0f));
                Asset.TIMERS.add(new Timer(1.0f) {
                    @Override
                    public boolean onCompletion() {
                        if(menu.hasParent()) menu.remove();
                        Asset.DATA.remove("seed");
                        Asset.DATA.flush();
                        goToScreen(new TravelScreen());
                        return true;
                    }
                });
            }
        });
        buttonNew.add(new Image(new TextureRegionDrawable(Asset.getUIAtlas().findRegion("icon_new"))));
        buttonNew.setName("New Button");

        buttonSettings = new ImageButton(Asset.getSkin());
        buttonSettings.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Asset.getMusicHandler().playSound("select");
                goToScreen(new OptionsMenu());
            }
        });
        buttonSettings.add(new Image(new TextureRegionDrawable(Asset.getUIAtlas().findRegion("icon_settings"))));
        buttonSettings.setName("Settings Button");

        buttonStats = new ImageButton(Asset.getSkin());
        buttonStats.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(menu.hasParent()) {
                    Asset.getMusicHandler().playSound("select");
                    Asset.getMusicHandler().setVolume(1f);
                    menu.remove();
                } else {
                    int size = items.size;
                    for (int n = 0; n < size; n++) {
                        Actor item = items.get(n);
                        if (item.hasParent()) continue;
                        Actor inv = menu.findActor("Inventory Label");
                        item.setPosition(inv.getX(), inv.getY() - item.getHeight() * (n + 2));
                        menu.addActor(item);
                    }

                    menu.setTouchable(Touchable.childrenOnly);
                    menu.addAction(Actions.fadeIn(0.6f));
                    table.addActor(menu);
                    Asset.getStage().setKeyboardFocus(menu);
                }
            }
        });
        buttonStats.add(new Image(new TextureRegionDrawable(Asset.getUIAtlas().findRegion("icon_stats"))));
        buttonStats.setName("Stats Button");

        menu.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if(keycode == Asset.CONTROLS.getInteger("back") || keycode == Asset.CONTROLS.getInteger("start"))
                    buttonStats.toggle();
                return true;
            }
        });

        float right = Math.round(table.getWidth() - BUTTON_SIZE);
        float top = Math.round(table.getHeight() - BUTTON_SIZE);
        buttonNew.setPosition(right, top);
        buttonSettings.setPosition(right, 0);
        buttonStats.setPosition(0, 0);
        for(ImageButton b : new ImageButton[]{buttonNew, buttonSettings, buttonStats}) {
            b.setSize(BUTTON_SIZE, BUTTON_SIZE);
            table.addActor(b);
        }
    }

    @Override
    public void render(float delta) {
        //sort the actors based on y-position
        table.getChildren().sort(new PlanetComparator());
        super.render(delta);

        if(loading != null) {
            table.setTouchable(Touchable.disabled);
            Asset.getMusicHandler().fadeOut(0.5f);

            if (loading.getProgress() >= 1 && !Asset.getCurtain().hasActions()) {

                World.recolorAtlasByRegion(Asset.getPalette("environment"),
                        loading.world.palette, loading.world.entityAtlas,
                        "bossSheet", "enemySheet");
                loading.world.generateTileRegions();
                loading.world.sectorAtlas = World.generateSectorAtlas(loading.world);
                loading.world.createEntities();
                goToScreen(new GameScreen(loading.world));
            }
        }
    }

    private Animation<TextureRegion> getStarAnimation() {
        return new Animation<TextureRegion>(0.2f, Asset.getEnvironmentAtlas().findRegions("star"), Animation.PlayMode.LOOP);
    }

    private void createPauseMenu() {
        menu.setBackground(new TextureRegionDrawable(new TextureRegion(Asset.getUIAtlas().findRegion("pauseMenu"))));
        menu.setBounds(BUTTON_SIZE, BUTTON_SIZE, Asset.getStage().getWidth() - 2*BUTTON_SIZE, Asset.getStage().getHeight() - 2*BUTTON_SIZE);
        menu.setName("Pause Menu");
        menu.setLayoutEnabled(false);
        menu.setTouchable(Touchable.disabled);
        final float menuBorder =  15f;
        final float menuBreak = menu.getWidth() / 4f;

        String head = "System " + Asset.Convert.toString(Math.abs(Asset.DATA.getLong("seed")));
        Label menuHeader = new Label(head, Asset.getSkin(), "title", Asset.getPalette("ui")[1]);
        Asset.GLYPH.setText(Asset.getBodyFont(), head);
        menuHeader.setSize(Asset.GLYPH.width, Asset.GLYPH.height);
        menuHeader.setPosition(menuBreak + menuBorder*2, menu.getHeight() - menuHeader.getHeight() - menuBorder);
        menuHeader.setTouchable(Touchable.disabled);
        menu.addActor(menuHeader);

        Label.LabelStyle style = new Label.LabelStyle(Asset.getBodyFont(), Color.WHITE);
        for(int i = 0; i< loaders.length; i++) {
            WorldLoader loader = loaders[i];
            int lines = 5;
            float labelHeight = Asset.getBodyFont().getLineHeight() * lines;
            final String name = "Planet " + loader.world.getName();
            Label label = new Label(name + '\n' +
                    "  Type: " + World.getType(loader.world.index).replace("planet_", "").toUpperCase() + '\n' +
                    "  Gravity: " + Math.round(loader.world.gravity * 100) + "%\n" +
                    "  Atmosphere: " + Math.round(loader.world.atmosphere * 100) + '%', style);
            float x = menuHeader.getX(),
                    y = menuHeader.getY() - labelHeight * (i + 1);
            if(y <= 0) {
                x += menuBreak + 2 * menuBorder;
                y += labelHeight * 5;
            }
            label.addListener(new ClickListener(){
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    super.clicked(event, x, y);
                    ((ImageButton) Asset.getStage().getRoot().findActor("Stats Button")).toggle();
                    Image planet = Asset.getStage().getRoot().findActor(name);
                    Gdx.input.setCursorPosition(Math.round(planet.getX() + planet.getWidth()/2),
                            Math.round(Asset.getStage().getHeight() - (planet.getY() + planet.getHeight()/2)));
                }

            });
            label.setPosition(x, y);
            menu.addActor(label);
        }

        //adds an image of the player
        TextureRegion tr = Asset.getEntityAtlas().findRegion("player_stand_front");
        Image player = new Image(tr);
        player.setSize(tr.getRegionWidth() * 3, tr.getRegionHeight() * 3);
        player.setPosition(menuBreak/2 - player.getWidth()/2, menu.getHeight() - player.getHeight() - menuBorder);
        player.setTouchable(Touchable.disabled);
        menu.addActor(player);

        //also adds the player's stats to the pause menu
        Actor stats = new Actor() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                Asset.getBodyFont().setColor(1, 1, 1, parentAlpha);
                Asset.getBodyFont().draw(batch, "Statistics", getX(), getY());
                float power = Player.CURRENT.power;
                float range = Player.calculateBulletRange(Player.CURRENT.bullet_life);
                float speed = Player.calculateSpeed(Player.CURRENT.speed);
                float jumpHeight = Player.calculateJumpHeight(Player.CURRENT.jump_speed);
                power *= 100; //round power to the nearest 100th
                power = Math.round(power);
                power /= 100;
                Asset.getBodyFont().draw(batch, "Power: " + power + " k", getX() + menuBorder, getY() - Asset.getBodyFont().getLineHeight());
                Asset.getBodyFont().draw(batch, "Range: " + range + " m", getX() + menuBorder, getY() - Asset.getBodyFont().getLineHeight() * 2);
                Asset.getBodyFont().draw(batch, "Speed: " + speed + " m/s", getX() + menuBorder, getY() - Asset.getBodyFont().getLineHeight() * 3);
                Asset.getBodyFont().draw(batch, "Jump:  " + jumpHeight + " m", getX() + menuBorder, getY() - Asset.getBodyFont().getLineHeight() * 4);
                Asset.getBodyFont().setColor(Color.WHITE);
            }
        };
        stats.setPosition(menuBorder, player.getY() - menuBorder);
        stats.setTouchable(Touchable.disabled);
        menu.addActor(stats);

        //adds a table to display the player's inventory
        Actor inv = new Actor() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                Asset.getBodyFont().setColor(1, 1, 1, parentAlpha);
                Asset.getBodyFont().draw(batch, "Inventory", getX(), getY());
                Asset.getBodyFont().setColor(Color.WHITE);
            }
        };
        inv.setName("Inventory Label");
        inv.setPosition(menuBorder, stats.getY() - Asset.getBodyFont().getLineHeight() * 5);
        inv.setTouchable(Touchable.disabled);
        menu.addActor(inv);

        Map<String, ?> inventory = Player.INVENTORY.get();
        for(String key : inventory.keySet()) {
            addItemActor(key);
        }

    }

    private void addItemActor(final String key) {
        final String[] id = key.split(",");
        if(id[0].contains("_up") || id[0].contains("ammo")
                || id[0].contains("missiles") || id[0].contains("oxygen")
                || id[0].contains("armor") || id[0].contains("small")
                || id[0].contains("large") || id[0].length() <= 0)
            return;
        Actor item = new Actor() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                Color c = new Color();
                if(Player.INVENTORY.getBoolean(key))
                    c.set(1, 1, 1, parentAlpha);
                else
                    c.set(0.3f, 0.3f, 0.3f, parentAlpha);
                Asset.getBodyFont().setColor(c);
                Asset.getBodyFont().draw(batch, getName(), getX(), getY() + getHeight());
                Asset.getBodyFont().setColor(Color.WHITE);
            }
        };
        item.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Player.INVENTORY.putBoolean(key, !Player.INVENTORY.getBoolean(key));
                Player.INVENTORY.flush();
                return super.touchDown(event, x, y, pointer, button);
            }
        });
        item.setName(id[0].replace("item", "").replace("_", " ").toUpperCase());
        item.setSize(menu.getWidth() / 4, Asset.getBodyFont().getLineHeight());
        item.setTouchable(Touchable.enabled);
        items.add(item);
    }
	
	@Override
	void goToPreviousScreen() {
		Dialog exitDialog = new Dialog("Exit Game", Asset.getSkin()) {
			@Override
			protected void result(Object object) {
				if(object.equals(0))
					Gdx.app.exit();
				remove();
			}
			
		};
		exitDialog.text("Are you sure you want \n to exit the game?").button("Yes", 0).button("No", 1);
		exitDialog.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
		exitDialog.setPosition((Asset.getStage().getWidth() - exitDialog.getWidth()) / 2,
                (Asset.getStage().getHeight() - exitDialog.getHeight()) /2);
		Asset.getStage().addActor(exitDialog);
	}

    private class PlanetComparator implements Comparator<Actor> {
        @Override
        public int compare(Actor a1, Actor a2) {
            float cY1 = a1.getY() + a1.getHeight() / 2,
                    cY2 = a2.getY() + a2.getHeight() / 2;
            if(cY1 < cY2) return 1;
            else if(cY1 == cY2) return 0;
            else return -1;
        }
    }
}
