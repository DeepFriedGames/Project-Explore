package com.shdwfghtr.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.shdwfghtr.asset.ControllerService;
import com.shdwfghtr.asset.DataService;
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
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final PauseMenuTable pauseMenu;
    private final HUDTable hud;
    private InputHandler input;
    private GameState state = GameState.PLAY;

    public final World world;
    public final GameCamera camera;

    public GameScreen(WorldUIGroup worldUiGroup) {
        this.world = worldUiGroup.worldLoader.world;
        int camHeight = Sector.pHEIGHT;
        int camWidth = Gdx.graphics.getWidth() * camHeight / Gdx.graphics.getHeight();
        this.camera = new GameCamera(camWidth, camHeight);
        WorldUIGroup.WorldMap worldMap = worldUiGroup.new WorldMap();

        this.pauseMenu = new PauseMenuTable(Player.CURRENT, worldMap);
        this.hud = new HUDTable(Player.CURRENT, worldMap.miniMap, GdxGame.uiService.getSkin());
    }

    @Override
    public void show() {
        GdxGame.audioService.setMusic("World" + world.index, true);
        GdxGame.audioService.fadeIn(5);

        Table table = new Table();
        table.add(hud).fillX().expandX().prefHeight(48);
        table.add(pauseMenu).fill().expand();

        GdxGame.uiService.getStage().addActor(table);
        GdxGame.uiService.getStage().addAction(Actions.fadeIn(1.0f));

        if(ControllerService.isTouch())
            pauseMenu.addListener(this.new ToggleTouchInputListener());

        input = ControllerService.GetInputHandler();
        input.setGameScreen(this);
        input.setPlayer(Player.CURRENT);
        input.toStage(GdxGame.uiService.getStage());
        camera.position.set(Player.CURRENT.getCenterX(), Player.CURRENT.getY() + Tile.HEIGHT, 0);

        GdxGame.uiService.getStage().addListener(new DebugTraumaInputListener());
    }

    @Override
    public void render(float delta) {
        switch(state){
            case PLAY:
                //marks the player's current sector as explored
                Sector playerSector = world.getSector(Player.CURRENT.getBox().getCenter(VECTOR2));
                if(!playerSector.explored){
                    playerSector.explored = true;
                    DataService.setSectorExplored(playerSector, true);
                }

                camera.update();
                world.update(delta);

                batch.setProjectionMatrix(camera.combined);
                batch.begin();
                world.draw(batch);
                batch.end();
                break;
            case PAUSE:
                batch.setProjectionMatrix(camera.combined);
                batch.begin();
                world.draw(batch);
                batch.end();
                break;
            case CUTSCENE:
                camera.update();
                world.update(delta);

                batch.setProjectionMatrix(camera.combined);
                batch.begin();
                world.draw(batch);
                batch.end();

                if(Player.CURRENT.d.x < 0) Player.CURRENT.d.x = -Player.CURRENT.speed;
                Player.CURRENT.getBox().setPosition(Player.CURRENT.getBox().getPosition(VECTOR2).add(Player.CURRENT.d));
                Player.CURRENT.checkCollisions();
                break;
        }
        input.act(delta);
    }

    @SuppressWarnings("unused")
    private void debugRender() {
        shapeRenderer.setAutoShapeType(true);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin();
        shapeRenderer.setColor(Color.WHITE);
        for(Entity e : world.getActiveEntities())
            shapeRenderer.rect(e.getX(), e.getY(), e.getWidth(), e.getHeight());

        shapeRenderer.setColor(Color.BLUE);
        Sector s = world.getSector(Player.CURRENT.getCenterX(), Player.CURRENT.getCenterY());
        for(int y=0; y<Sector.HEIGHT; y++)
            for(int x=0; x<Sector.WIDTH; x++)
                if(!Character.isLetter(s.getChar(x, y)))
                    shapeRenderer.rect(s.x + x * Tile.WIDTH, s.y + y * Tile.HEIGHT, Tile.WIDTH, Tile.HEIGHT);
        shapeRenderer.end();
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

    public void setState(GameState gameState) {
        switch (gameState) {
            case PLAY:
                pauseMenu.toggle();
                break;
            case PAUSE:
                pauseMenu.toggle();
                DataService.save(Player.CURRENT);
                break;
        }
        state = gameState;
    }

    private class DebugTraumaInputListener extends InputListener {
        @Override
        public boolean keyDown(InputEvent event, int keycode) {
            if(Input.Keys.NUM_0 <= keycode && keycode <= Input.Keys.NUM_9) {
                float amount = (keycode - 6) * 0.1f;
                camera.addTrauma(amount);
                return true;
            }
            return false;
        }
    }

    private class ToggleTouchInputListener extends InputListener {
        @Override
        public void	touchUp(InputEvent event, float x, float y, int pointer, int button) {
            setState(GameState.PLAY);
        }
    }
}
