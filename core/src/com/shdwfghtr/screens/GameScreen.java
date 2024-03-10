package com.shdwfghtr.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;
import com.shdwfghtr.asset.ControllerService;
import com.shdwfghtr.asset.DataService;
import com.shdwfghtr.asset.InventoryService;
import com.shdwfghtr.asset.TimeService;
import com.shdwfghtr.entity.Entity;
import com.shdwfghtr.entity.Player;
import com.shdwfghtr.explore.GameCamera;
import com.shdwfghtr.explore.GameState;
import com.shdwfghtr.explore.GdxGame;
import com.shdwfghtr.explore.Sector;
import com.shdwfghtr.explore.Tile;
import com.shdwfghtr.explore.World;
import com.shdwfghtr.input.InputHandler;
import com.shdwfghtr.ui.HUDTable;
import com.shdwfghtr.ui.PauseMenuTable;
import com.shdwfghtr.ui.WorldUIGroup;

public class GameScreen implements Screen {
    private static final Vector2 VECTOR2 = new Vector2();
    private final SpriteBatch batch = new SpriteBatch();
    private final Array<Actor> items = new Array<>();
    private final ShapeRenderer sr = new ShapeRenderer();
    private final PauseMenuTable menu;
    private InputHandler input;
    private GameState state;
    public final World world;
    public final GameCamera camera;

    public GameScreen(WorldUIGroup worldUiGroup) {
        this.world = worldUiGroup.worldLoader.world;
        this.camera = new GameCamera();
        this.menu = new PauseMenuTable(Player.CURRENT, worldUiGroup);
    }

    @Override
    public void show() {
        GdxGame.audioService.setMusic("World" + world.index, true);
        GdxGame.audioService.fadeIn(5);
        GdxGame.uiService.getStage().clear();
        input = ControllerService.GetInputHandler();

        GdxGame.uiService.getStage().addActor(new HUDTable(Player.CURRENT, world, GdxGame.uiService.getSkin()));

        if(ControllerService.isTouch())
            menu.addListener(new InputListener() {
                @Override
                public void	touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    setState(GameState.PLAY);
                }
            });

        GdxGame.uiService.fadeOutCurtain(1.0f);

        input.setGameScreen(this);
        input.setPlayer(Player.CURRENT);
        input.toStage(GdxGame.uiService.getStage());
        camera.position.set(Player.CURRENT.getCenterX(), Player.CURRENT.getY() + Tile.HEIGHT, 0);
        setState(GameState.PLAY);

        GdxGame.uiService.getStage().addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if(7 <= keycode && keycode <= 16) {
					float amount = (keycode - 6) * 0.1f;
                    camera.addTrauma(amount);
                return true;
                }
				return false;
            }
        });
    }

    @Override
    public void render(float delta) {
        if(getState() == GameState.CUTSCENE) {
            camera.update();
            world.update(delta);

            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            world.draw(batch);
            batch.end();

            if(Player.CURRENT.d.x < 0) Player.CURRENT.d.x = -Player.CURRENT.speed;
            Player.CURRENT.getBox().setPosition(Player.CURRENT.getBox().getPosition(VECTOR2).add(Player.CURRENT.d));
            Player.CURRENT.checkCollisions();

        } else  if(getState() == GameState.PAUSE) {
            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            world.draw(batch);
            batch.end();

        } else if(getState() == GameState.PLAY) {
            //Oxygen is running out slowly
            if(TimeService.GetTime() % (Player.CURRENT.armor / 2f + world.atmosphere + 0.5f) < 0.017f) {
                Player.CURRENT.health--;
                Player.CURRENT.oxygenEffect = GdxGame.particleService.obtain("oxygen", true);
            }

            camera.update();
            world.update(delta);

            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            world.draw(batch);
            batch.end();

//			debugRender();
        }
        //marks the player's current sector as explored
        Sector playerSector = world.getSector(Player.CURRENT.getBox().getCenter(VECTOR2));
        if(!playerSector.explored){
            playerSector.explored = true;
            DataService.setSectorExplored(playerSector, true);
        }

        input.act(delta);
    }

    @SuppressWarnings("unused")
    private void debugRender() {
        sr.setAutoShapeType(true);
        sr.setProjectionMatrix(camera.combined);
        sr.begin();
        sr.setColor(Color.WHITE);
        for(Entity e : world.getActiveEntities())
            sr.rect(e.getX(), e.getY(), e.getWidth(), e.getHeight());

        sr.setColor(Color.BLUE);
        Sector s = world.getSector(Player.CURRENT.getCenterX(), Player.CURRENT.getCenterY());
        for(int y=0; y<Sector.HEIGHT; y++)
            for(int x=0; x<Sector.WIDTH; x++)
                if(!Character.isLetter(s.getChar(x, y)))
                    sr.rect(s.x + x * Tile.WIDTH, s.y + y * Tile.HEIGHT, Tile.WIDTH, Tile.HEIGHT);
        sr.end();

    }

    @Override
    public void resize(int width, int height) {
        GdxGame.uiService.resize(width,height);
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
        GdxGame.uiService.getStage().getRoot().clearChildren();
        GdxGame.uiService.getStage().removeListener(input);
        TimeService.clearTimers();
    }

    public GameState getState() {
        return state;
    }

    public void setState(final GameState gameState) {
        if(gameState == GameState.PLAY) {
            //if the new state is play
            GdxGame.audioService.playSound("select");
            GdxGame.audioService.setVolume(1f);
            menu.setTouchable(Touchable.disabled);
            menu.addAction(Actions.fadeOut(0.6f));
            TimeService.addTimer(new TimeService.Timer(0.6f){
                @Override
                public boolean onCompletion() {
                    menu.remove();
                    return true;
                }
            });
            state = gameState;
            //Sector sector = world.getSector(Player.CURRENT.getCenter());
            for (Actor item : items) {
                item.remove();
            }
            items.clear();
        } else if(gameState == GameState.PAUSE) {
            // if the new state is pause
            menu.setTouchable(Touchable.childrenOnly);
            menu.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0.6f)));
            GdxGame.uiService.getStage().addActor(menu);
            GdxGame.audioService.playSound("select");
            GdxGame.audioService.setVolume(0.5f);
            //Sector sector = world.getSector(Player.CURRENT.getCenter());
            //mapImages[sector.getYi()][sector.getXi()].addAction(Actions.forever(Actions.sequence(Actions.fadeOut(0.3f), Actions.fadeIn(0.2f))));

            items.addAll(InventoryService.getInventoryActors());
            for(Actor item : items){
                item.setSize(menu.getWidth() / 4, GdxGame.uiService.getBodyFont().getLineHeight());
                item.setTouchable(Touchable.enabled);
                menu.add(item);
            }

            DataService.save(Player.CURRENT);
            state = gameState;
        } else state = gameState;
    }
}
