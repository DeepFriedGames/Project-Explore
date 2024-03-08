package com.shdwfghtr.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
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
import com.shdwfghtr.asset.ControllerService;
import com.shdwfghtr.asset.ConversionService;
import com.shdwfghtr.asset.DataService;
import com.shdwfghtr.asset.InventoryService;
import com.shdwfghtr.asset.PaletteService;
import com.shdwfghtr.asset.TimeService;
import com.shdwfghtr.entity.Player;
import com.shdwfghtr.explore.GdxGame;
import com.shdwfghtr.explore.World;
import com.shdwfghtr.explore.WorldLoader;
import com.shdwfghtr.ui.PauseMenuTable;

import java.util.Comparator;
import java.util.Random;

/**
 * This screen allows the player to move a ship about a galaxy and chose star systems to explore.
 * These star systems will have loaders to explore which will take the player to the game screen.
 */
public class TravelScreen extends Menu {
    private static final float DIALOG_WIDTH = 240, DIALOG_HEIGHT = 144;

    private PauseMenuTable menu;
    private WorldLoader[] loaders;
    private WorldLoader loading;
    private static final int BUTTON_SIZE = 32;//these buttons are square

    public TravelScreen() {
        super("Destination");
    }

    @Override
    public void show() {
        GdxGame.audioService.setMusic("Intro", true);

        headerText = "";
        super.show();

        GdxGame.uiService.DropCurtain(2.0f);

        createStarSystem(DataService.getSeed());
        menu = new PauseMenuTable(Player.CURRENT, loaders);
        createButtons();
    }

    private void createStarSystem(long seed) {
        int starSize = 256;

        Random random = new Random(seed);
        int num_worlds = random.nextInt(4) + 3;
        loaders = new WorldLoader[num_worlds];
        for(int i = 0; i < num_worlds; i++) {
            final WorldLoader loader = new WorldLoader(Math.abs(random.nextLong()));
            loaders[i] = loader;

            final int r = random.nextInt(Math.round(table.getHeight()/2)) + starSize/2; //radius of rotation
            final double a = random.nextFloat() * 2 * Math.PI; //starting angle
            final float s = random.nextFloat() / 50; //speed at which planets orbit

            TextureRegion tr = GdxGame.textureAtlasService.findEnvironmentRegion(World.getType(loader.world.index));
            PaletteService.recolorTextureRegion(tr, PaletteService.getPalette("environment"), loader.world.palette);
            final Image planet = new Image(tr) {
                @Override
                public void act(float delta) {
                    super.act(delta);
                    float cX = table.getWidth() / 2, cY = table.getHeight() / 2;
                    float x = Math.round(cX + r * Math.cos(TimeService.GetTime() * s + a)),
                            y = Math.round(cY + r * Math.sin(TimeService.GetTime() * s + a) / 2);
                    setPosition(x, y);
                }
            };
            planet.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    super.clicked(event, x, y);
                    Dialog dialog = new Dialog(planet.getName(), GdxGame.uiService.getSkin()) {
                        @Override
                        protected void result(Object object) {
                            if(object.equals(0)){
                                //Travel to the destination
                                GdxGame.uiService.getCurtain().setBounds(0, 0, GdxGame.uiService.getStage().getWidth(), GdxGame.uiService.getStage().getHeight());
                                GdxGame.uiService.getStage().addActor(GdxGame.uiService.getCurtain());
                                GdxGame.uiService.getCurtain().addAction(Actions.fadeIn(1.0f));
                                World.CURRENT = loader.world;
                                loading = loader;
                                Thread thread = new Thread(loading);
                                thread.start();
                                GdxGame.uiService.getStage().addActor(new Label("Please wait...", GdxGame.uiService.getSkin()));

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
                    GdxGame.uiService.getStage().addActor(dialog);
                }
            });
            float variance = 1 - loader.area / World.AVG_AREA;
            variance *= 32;
            float planetSize = planet.getDrawable().getMinWidth() * (2.25f + variance);
            planet.setSize(planetSize, planetSize);
            planet.setName("Planet " +loader.world.getName());
            table.addActor(planet);
        }

        Actor star = new Actor() {
            final Animation<TextureRegion> animation = getStarAnimation();

            @Override
            public void draw(Batch batch, float parentAlpha) {
                Color batchColor = batch.getColor();

                batch.setColor(loaders[0].world.palette[7]);
                batch.draw(animation.getKeyFrame(TimeService.GetTime()), getX(), getY(), getWidth(), getHeight());
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
        buttonNew = new ImageButton(GdxGame.uiService.getSkin());
        buttonNew.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GdxGame.uiService.getCurtain().setBounds(0, 0, GdxGame.uiService.getStage().getWidth(), GdxGame.uiService.getStage().getHeight());
                GdxGame.uiService.getStage().addActor(GdxGame.uiService.getCurtain());
                GdxGame.uiService.getCurtain().addAction(Actions.fadeIn(1.0f));
                TimeService.addTimer(new TimeService.Timer(1.0f) {
                    @Override
                    public boolean onCompletion() {
                        if(menu.hasParent()) menu.remove();
                        DataService.clearSeed();
                        goToScreen(new TravelScreen());
                        return true;
                    }
                });
            }
        });
        buttonNew.add(new Image(new TextureRegionDrawable(GdxGame.textureAtlasService.findUIRegion("icon_new"))));
        buttonNew.setName("New Button");

        buttonSettings = new ImageButton(GdxGame.uiService.getSkin());
        buttonSettings.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GdxGame.audioService.playSound("select");
                goToScreen(new OptionsMenu());
            }
        });
        buttonSettings.add(new Image(new TextureRegionDrawable(GdxGame.textureAtlasService.findUIRegion("icon_settings"))));
        buttonSettings.setName("Settings Button");

        buttonStats = new ImageButton(GdxGame.uiService.getSkin());
        buttonStats.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(menu.hasParent()) {
                    GdxGame.audioService.playSound("select");
                    GdxGame.audioService.setVolume(1f);
                    menu.remove();
                } else {
                    int size = menu.items.size;
                    for (int n = 0; n < size; n++) {
                        Actor item = menu.items.get(n);
                        if (item.hasParent()) continue;
                        Actor inv = menu.findActor("Inventory Label");
                        item.setPosition(inv.getX(), inv.getY() - item.getHeight() * (n + 2));
                        menu.addActor(item);
                    }

                    menu.setTouchable(Touchable.childrenOnly);
                    menu.addAction(Actions.fadeIn(0.6f));
                    table.addActor(menu);
                    GdxGame.uiService.getStage().setKeyboardFocus(menu);
                }
            }
        });
        buttonStats.add(new Image(new TextureRegionDrawable(GdxGame.textureAtlasService.findUIRegion("icon_stats"))));
        buttonStats.setName("Stats Button");

        menu.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if(keycode == ControllerService.getInput("back") || keycode == ControllerService.getInput("start"))
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
            GdxGame.audioService.fadeOut(0.5f);

            if (loading.getProgress() >= 1 && !GdxGame.uiService.getCurtain().hasActions()) {
                PaletteService.recolorAtlasByRegion(PaletteService.getPalette("environment"),
                        loading.world.palette, GdxGame.textureAtlasService.entityAtlas,
                        "bossSheet", "enemySheet");
                loading.generateTileRegions();
                GdxGame.textureAtlasService.generateSectorAtlas(loading.world);
                loading.createEntities();
                goToScreen(new GameScreen(loading.world));
            }
        }
    }

    private Animation<TextureRegion> getStarAnimation() {
        return new Animation<>(0.2f, GdxGame.textureAtlasService.findEnvironmentRegions("star"), Animation.PlayMode.LOOP);
    }

	@Override
	void goToPreviousScreen() {
		Dialog exitDialog = new Dialog("Exit Game", GdxGame.uiService.getSkin()) {
			@Override
			protected void result(Object object) {
				if(object.equals(0))
					Gdx.app.exit();
				remove();
			}
			
		};
		exitDialog.text("Are you sure you want \n to exit the game?").button("Yes", 0).button("No", 1);
		exitDialog.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
		exitDialog.setPosition((GdxGame.uiService.getStage().getWidth() - exitDialog.getWidth()) / 2,
                (GdxGame.uiService.getStage().getHeight() - exitDialog.getHeight()) /2);
		GdxGame.uiService.getStage().addActor(exitDialog);
	}

    private static class PlanetComparator implements Comparator<Actor> {
        @Override
        public int compare(Actor a1, Actor a2) {
            float cY1 = a1.getY() + a1.getHeight() / 2,
                    cY2 = a2.getY() + a2.getHeight() / 2;
            return Float.compare(cY2, cY1);
        }
    }
}
