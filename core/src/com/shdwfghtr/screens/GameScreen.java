package com.shdwfghtr.screens;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.shdwfghtr.entity.Burrower;
import com.shdwfghtr.entity.ChainEnemy;
import com.shdwfghtr.entity.Entity;
import com.shdwfghtr.entity.Player;
import com.shdwfghtr.explore.Asset;
import com.shdwfghtr.explore.GameState;
import com.shdwfghtr.explore.Sector;
import com.shdwfghtr.explore.Tile;
import com.shdwfghtr.explore.Timer;
import com.shdwfghtr.explore.World;
import com.shdwfghtr.input.GamepadHandler;
import com.shdwfghtr.input.InputHandler;
import com.shdwfghtr.input.KeyboardHandler;
import com.shdwfghtr.input.TouchHandler;

import java.util.Map;

public class GameScreen implements Screen {
    private static final int HUD_HEIGHT = Sector.HEIGHT * 3;
    private InputHandler input;
    private final TextureRegion msgBox = Asset.getUIAtlas().findRegion("message_box");
    private final SpriteBatch batch = new SpriteBatch();
    private final Array<Actor> items = new Array<Actor>();
    private final ShapeRenderer sr = new ShapeRenderer();
    private final Table menu = new Table(), hudTable = new Table();
    private GameState state;
    private Image[][] mapImages;
    public final World world;


    GameScreen(World world) { this.world = world;}

    @Override
    public void show() {
        Asset.getMusicHandler().setMusic("World" + world.index);
        Asset.getMusicHandler().fadeIn(5);
        Asset.getStage().clear();
        if(Asset.CONTROLS.getString("controller").matches("touch"))
            input = new TouchHandler();
        else if(Asset.CONTROLS.getString("controller").matches("keyboard"))
            input = new KeyboardHandler();
        else if(Asset.CONTROLS.getString("controller").matches("gamepad"))
            input = new GamepadHandler();

        createHUD();
        createPauseMenu();

        Asset.getStage().addActor(Asset.getCurtain());
        Asset.getCurtain().addAction(Actions.fadeOut(1.0f));

        input.setGameScreen(this);
        input.setPlayer(Player.CURRENT);
        input.toStage(Asset.getStage());
        Asset.CAMERA.position.set(Player.CURRENT.getCenterX(), Player.CURRENT.getY() + Tile.HEIGHT, 0);
        setState(GameState.PLAY);

        Asset.getStage().addListener(new InputListener() {
            ChainEnemy entity;
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if(7 <= keycode && keycode <= 16) {
					float amount = (keycode - 6) * 0.1f;
                    Asset.CAMERA.addTrauma(amount);
                return true;
                }
				return false;
            }
        });
    }

    @Override
    public void render(float delta) {
        if(getState() == GameState.CUTSCENE) {

            Asset.CAMERA.update();
            world.update(delta);

            batch.setProjectionMatrix(Asset.CAMERA.combined);
            batch.begin();
            world.draw(batch);
            batch.end();

            if(Player.CURRENT.d.x < 0) Player.CURRENT.d.x = -Player.CURRENT.speed;
            Player.CURRENT.getBox().setPosition(Player.CURRENT.getBox().getPosition(Asset.VECTOR2).add(Player.CURRENT.d));
            Player.CURRENT.checkCollisions();

        } else  if(getState() == GameState.PAUSE) {
            batch.setProjectionMatrix(Asset.CAMERA.combined);
            batch.begin();
            world.draw(batch);
            batch.end();

        } else if(getState() == GameState.PLAY) {
            //Oxygen is running out slowly
            if(Asset.TIME % (Player.CURRENT.armor/2 + world.atmosphere + 0.5f) < 0.017f) {
                Player.CURRENT.health--;
                Player.CURRENT.oxygenEffect = Asset.getParticles().obtain("oxygen", true);
            }


            for(Timer t : Asset.getTimers())
                if(t.update() && Asset.TIMERS.contains(t)) Asset.TIMERS.remove(t);

            Asset.CAMERA.update();
            world.update(delta);
            if(Asset.CONTROLS.getBoolean("particles")) Asset.getParticles().update(delta);

            batch.setProjectionMatrix(Asset.CAMERA.combined);
            batch.begin();
            world.draw(batch);
            if(Asset.CONTROLS.getBoolean("particles")) Asset.getParticles().draw(batch);
            batch.end();

//			debugRender();
        }

        Asset.getStage().act(delta);
        input.act(delta);
        Asset.getStage().draw();
        Asset.getStage().getBatch().setColor(Color.WHITE);
    }

    private void createHUD() {
        hudTable.setBounds(0, Asset.getStage().getHeight() - HUD_HEIGHT, Asset.getStage().getWidth(), HUD_HEIGHT);

        Table ammoTable = new Table();
        ammoTable.add(new Image(Asset.getUIAtlas().findRegion("hud_missile"))).left();
        ammoTable.add(new Label(": " + Player.CURRENT.missiles, Asset.getSkin()) {
            @Override
            public void act(float delta) {
                super.act(delta);
                setText(": " + Player.CURRENT.missiles);
            }
        }).left().expandX();

        Table healthTable = new Table();
        healthTable.add(new Table() {
            int nTanks = 0;
            TextureRegionDrawable full = new TextureRegionDrawable(Asset.getUIAtlas().findRegion("hud_O2_full"));
            TextureRegionDrawable empty = new TextureRegionDrawable(Asset.getUIAtlas().findRegion("hud_O2_empty"));

            @Override
            public void act(float delta) {
                int tanks = (int) Math.floor(Player.CURRENT.maxHealth / 100);
                while(nTanks < tanks) {
                    add(new Image(full));
                    nTanks++;
                }
                Cell<Image>[] cells = getCells().toArray(Cell.class);
                for(int i=0; i < cells.length; i++) {
                    Image image = cells[i].getActor();
                    if(image.getDrawable() == full && Player.CURRENT.health < (i + 1) * 100)
                        image.setDrawable(empty);
                    if(image.getDrawable() == empty && Player.CURRENT.health >= (i + 1) * 100)
                        image.setDrawable(full);

                }
            }
        }).expand().bottom().left();
        healthTable.row();
        healthTable.add(new Label("O2: ", Asset.getSkin()) {
            @Override
            public void act(float delta) {
                super.act(delta);
                setText("O2: " + String.valueOf((int) Math.floor(Player.CURRENT.health % 100)));
            }
        }).left();
        healthTable.row();
        healthTable.add(new Table() {
            int nArmor = 0;
            TextureRegionDrawable armor = new TextureRegionDrawable(Asset.getUIAtlas().findRegion("hud_armor"));

            @Override
            public void act(float delta) {
                int number = Player.CURRENT.maxArmor / 2;
                while(nArmor < number) {
                    add(new Image(armor));
                    nArmor++;
                }
                Cell<Image>[] cells = getCells().toArray(Cell.class);
                for(int i=0; i < cells.length; i++) {
                    Image image = cells[i].getActor();
                    if(image.getScaleX() != 1.0f && Player.CURRENT.armor >= (i + 1) * 2)
                        image.setScale(1.0f);
                    if(image.getScaleX() != 0.75f && Player.CURRENT.armor - i * 2 == 1)
                        image.setScale(0.75f);
                    if(image.getScaleX() != 0.0f && Player.CURRENT.armor < (i + 1) * 2)
                        image.setScale(0.0f);

                }
            }
        }).expandX().height(12).left().bottom();

        Table msgTable = new Table() {
            private final Timer msgTimer = new Timer(3) {
                @Override
                public boolean onCompletion() {
                    clearChildren();
                    Asset.MESSAGES.removeIndex(0);
                    addAction(Actions.moveBy(0, HUD_HEIGHT, 0.5f));
                    return true;
                }
            };
            @Override
            public void act(float delta) {
                if(Asset.MESSAGES.size > 0 && msgTimer.isComplete()) {
                    addAction(Actions.moveBy(0, - HUD_HEIGHT, 0.5f));
                    add(new Label(Asset.MESSAGES.first(), Asset.getSkin(), "font", Asset.getPalette("ui")[17]));
                    msgTimer.reset();
                }
                super.act(delta);
            }
        };
        msgTable.setBackground(new TextureRegionDrawable(new TextureRegion(msgBox)));
        msgTable.setBounds(Asset.getStage().getWidth() / 3, Asset.getStage().getHeight(), Asset.getStage().getWidth() / 3, HUD_HEIGHT);
        msgTable.setTouchable(Touchable.disabled);
        Asset.getStage().addActor(msgTable);

        Table miniMap = new Table() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                int width = Sector.WIDTH,
                        height = Sector.HEIGHT;
                Sector center = world.getSector(Player.CURRENT.getCenterX(), Player.CURRENT.getCenterY());
                int yi = center.getYi(),
                        xi = center.getXi();
                for(int y= yi -1; y<= yi +1; y++) {
                    for(int x= xi -2; x<= xi +2; x++) {
                        int col = x - xi + 2;
                        int row = y - yi + 1;
                        if(x >= 0 && y >= 0 && x < world.getWidth() && y < world.getHeight()) {
                            Sector sector = world.getSector(x, y);
                            if(world.sectorAtlas.findRegion(sector.name) != null)
                                batch.draw(world.sectorAtlas.findRegion(sector.isExplored() ? sector.name : "dead0"),
                                        getX() + col*width, getY() + row*height, width, height);
                        }
                    }
                }
            }
        };

        hudTable.pad(2.0f);
        hudTable.add(healthTable).expandY().width(80).left().bottom();
        hudTable.add(ammoTable).expand().left().bottom();
        hudTable.add(miniMap).width(Sector.WIDTH * 5).height(Sector.HEIGHT * 3).left();
        Asset.getStage().addActor(hudTable);
    }

    private void createPauseMenu() {
        menu.setBackground(new TextureRegionDrawable(Asset.getUIAtlas().findRegion("pauseMenu")));
        menu.setBounds(0, 0, Asset.getStage().getWidth(), Asset.getStage().getHeight() - HUD_HEIGHT - 1);
        menu.setName("Pause Menu");
        final float menuBorder =  15f;
        final float menuBreak = menu.getWidth() /4f;
        final float mapWidth = menu.getWidth() - menuBreak;

        final String head = "Planet " + world.getName();
        Label menuHeader = new Label(head, Asset.getSkin(), "title", Asset.getPalette("ui")[1]);
        menuHeader.setPosition(menuBreak + menuBorder*2, menu.getHeight() - menuHeader.getHeight() - menuBorder);
        menu.addActor(menuHeader);

        Label.LabelStyle style = new Label.LabelStyle(Asset.getBodyFont(), Color.WHITE);
        Label label = new Label("  Type: " + World.getType(world.index).toUpperCase() + '\n' +
                "  Gravity: " + Math.round(world.gravity * 100) + "%\n" +
                "  Atmosphere: " + Math.round(world.atmosphere * 100) + '%', style);
        label.setPosition(menuHeader.getX(), menuHeader.getY() - Asset.getBodyFont().getLineHeight()*4);
        menu.addActor(label);

        //adds an image representations of each sector to the pause menu
        mapImages = new Image[world.getHeight()][world.getWidth()];
        for(Sector[] array : world.sectorMap)
            for(Sector sector : array) {
                int xi = sector.getXi(),
                        yi = sector.getYi();
                if(sector.isExplored() && world.sectorAtlas.findRegion(sector.name) != null)
					mapImages[yi][xi] = new Image(world.sectorAtlas.findRegion(sector.name));
                else mapImages[yi][xi] = new Image(world.sectorAtlas.findRegion("dead0"));
                int width = Sector.WIDTH;
                int height = Sector.HEIGHT;
                mapImages[yi][xi].setPosition(menuBreak + mapWidth/2 + (xi - world.getWidth()/2) * width,
                        menu.getY() + menu.getHeight() / 2 + (yi - world.getHeight() / 2) * height);
                mapImages[yi][xi].setTouchable(Touchable.disabled);
                menu.addActor(mapImages[yi][xi]);
                sector.setExplored(Asset.DATA.getBoolean(sector.getSaveString(), false));

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
		
		if(Asset.CONTROLS.getString("controller").matches("touch"))
			menu.addListener(new InputListener() {
				@Override
				public void	touchUp(InputEvent event, float x, float y, int pointer, int button) {
					setState(GameState.PLAY);
				}
			});

    }

    public void addItemActor(final String key) {
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

    @SuppressWarnings("unused")
    private void debugRender() {
        sr.setAutoShapeType(true);
        sr.setProjectionMatrix(Asset.CAMERA.combined);
        sr.begin();
        sr.setColor(Color.WHITE);
        for(Entity e : world.getActiveEntities())
            sr.rect(e.getX(), e.getY(), e.getWidth(), e.getHeight());

        sr.setColor(Color.BLUE);
        Sector s = world.getSector(Player.CURRENT.getCenterX(), Player.CURRENT.getCenterY());
        for(int y=0; y<Sector.HEIGHT; y++)
            for(int x=0; x<Sector.WIDTH; x++)
                if(!Character.isLetter(s.getChar(x, y)))
                    sr.rect(s.getX() + x * Tile.WIDTH, s.getY() + y * Tile.HEIGHT, Tile.WIDTH, Tile.HEIGHT);
        sr.end();

    }

    @Override
    public void resize(int width, int height) {
        Asset.getStage().getViewport().update(width, height);

    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() { }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        Asset.getStage().getRoot().clearChildren();
        Asset.getStage().removeListener(input);
        world.dispose();
        Asset.TIMERS.clear();
    }

    public GameState getState() {
        return state;
    }

    public void setState(final GameState gameState) {
        if(gameState == GameState.PLAY) {
            //if the new state is play
            Asset.getMusicHandler().playSound("select");
            Asset.getMusicHandler().setVolume(1f);
            menu.setTouchable(Touchable.disabled);
            menu.addAction(Actions.fadeOut(0.6f));
            Asset.TIMERS.add(new Timer(0.6f){
                @Override
                public boolean onCompletion() {
                    menu.remove();
                    return true;
                }
            });
            state = gameState;
            Sector sector = world.getSector(Player.CURRENT.getCenter());
            mapImages[sector.getYi()][sector.getXi()].clear();
            mapImages[sector.getYi()][sector.getXi()].setColor(Color.WHITE);
        } else if(gameState == GameState.PAUSE) {
            // if the new state is pause
            menu.setTouchable(Touchable.childrenOnly);
            menu.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0.6f)));
            Asset.getStage().addActor(menu);
            Asset.getMusicHandler().playSound("select");
            Asset.getMusicHandler().setVolume(0.5f);
            Sector sector = world.getSector(Player.CURRENT.getCenter());
            mapImages[sector.getYi()][sector.getXi()].addAction(Actions.forever(Actions.sequence(Actions.fadeOut(0.3f), Actions.fadeIn(0.2f))));

            int size = items.size;
            for(int n=0; n<size; n++) {
                Actor item = items.get(n);
                if(item.hasParent()) continue;
                Actor inv = Asset.getStage().getRoot().findActor("Inventory Label");
                item.setPosition(inv.getX(), inv.getY() - item.getHeight() * (n+2));
                menu.addActor(item);
            }

            Player.CURRENT.save();
            state = gameState;
        } else state = gameState;
    }

    public void setMapImage(String name, int xi, int yi) {
        TextureRegion region = world.sectorAtlas.findRegion(name);
        if(region != null) {
            float menuBreak = menu.getWidth() / 4;
            float mapWidth = menu.getWidth() - menuBreak;
            mapImages[yi][xi].remove();
            mapImages[yi][xi] = new Image(region);
            int width = Sector.WIDTH;
            int height = Sector.HEIGHT;
            mapImages[yi][xi].setPosition(menuBreak + +mapWidth/2 + (xi - world.getWidth()/2) * width,
                    menu.getY() + menu.getHeight() / 2 + (yi - world.getHeight() / 2) * height);
            mapImages[yi][xi].setTouchable(Touchable.enabled);
            menu.addActor(mapImages[yi][xi]);
        }
    }
}
