package com.shdwfghtr.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
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
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.shdwfghtr.asset.ControllerService;
import com.shdwfghtr.asset.DataService;
import com.shdwfghtr.asset.PaletteService;
import com.shdwfghtr.asset.TimeService;
import com.shdwfghtr.entity.Player;
import com.shdwfghtr.explore.GdxGame;
import com.shdwfghtr.explore.WorldLoader;
import com.shdwfghtr.ui.ExitGameDialog;
import com.shdwfghtr.ui.PauseMenuTable;
import com.shdwfghtr.ui.WorldUIGroup;

import java.util.Comparator;
import java.util.Random;

/**
 * This screen allows the player to move a ship about a galaxy and chose star systems to explore.
 * These star systems will have loaders to explore which will take the player to the game screen.
 */
public class TravelScreen extends MenuScreen {
    private static final int BUTTON_SIZE = 48;//these buttons are square
    private static final int starSize = 256;

    private final Random random;
    private final Comparator<Actor> comparator = new CenterYActorComparator();
    private final WorldUIGroup[] worldUIGroups;

    private PauseMenuTable pauseMenu;

    public TravelScreen() {
        random = new Random(DataService.getSeed());

        int num_worlds = random.nextInt(4) + 3;
        worldUIGroups = new WorldUIGroup[num_worlds];
        for (int i = 0; i < num_worlds; i++) {
            worldUIGroups[i] = new WorldUIGroup(
                    new WorldLoader(Math.abs(random.nextLong()))
            );
        }
    }

    @Override
    public void show() {
        super.show();

        GdxGame.audioService.setMusic("Intro", true);
        GdxGame.uiService.DropCurtain(2.0f);

        Table table = new Table();
        table.setBounds(0, 0, getWidth(), getHeight());
        addActor(table);

        Label.LabelStyle style = new Label.LabelStyle(GdxGame.uiService.getHeaderFont(), Color.WHITE);
        Label header = new Label("Choose a Destination", style);
        table.add(header).pad(12).colspan(3);
        table.row();

        StarSystemGroup starSystem = this.new StarSystemGroup();
        table.add(starSystem).expand().colspan(3);
        starSystem.addOrbitActionsToWorldImages();
        table.row();

        ImageButton newSystemButton = new ImageButton(GdxGame.uiService.getSkin());
        newSystemButton.addListener(this.new NewSystemChangeListener());
        newSystemButton.add(new Image(new TextureRegionDrawable(GdxGame.textureAtlasService.findUIRegion("icon_new"))));
        newSystemButton.add("New System").pad(6);
        newSystemButton.setName("New System");
        table.add(newSystemButton).expandX().center();

        ImageButton optionsButton = new ImageButton(GdxGame.uiService.getSkin());
        optionsButton.addListener(this.new OptionsChangeListener());
        optionsButton.add(new Image(new TextureRegionDrawable(GdxGame.textureAtlasService.findUIRegion("icon_settings"))));
        optionsButton.add("Options").pad(6);
        optionsButton.setName("Options");
        table.add(optionsButton).expandX().center();

        ImageButton infoButton = new ImageButton(GdxGame.uiService.getSkin());
        infoButton.addListener(this.new InformationChangeListener());
        infoButton.add(new Image(new TextureRegionDrawable(GdxGame.textureAtlasService.findUIRegion("icon_stats"))));
        infoButton.add("Information").pad(6);
        infoButton.setName("Information");
        table.add(infoButton).expandX().center();

        pauseMenu = new PauseMenuTable(Player.CURRENT, worldUIGroups);
        pauseMenu.addListener(this.new PauseMenuInputListener());

        GdxGame.fadeColor = worldUIGroups[0].worldLoader.world.palette[7];
    }

    @Override
    public void render(float delta) {
        getChildren().sort(comparator);
        super.render(delta);

        for(WorldUIGroup worldUiGroup : worldUIGroups){
            if(worldUiGroup.worldLoader.getProgress() >= 1) {
                PaletteService.recolorAtlasByRegion(PaletteService.getPalette("environment"),
                        worldUiGroup.worldLoader.world.palette, GdxGame.textureAtlasService.entityAtlas,
                        "bossSheet", "enemySheet");
                worldUiGroup.worldLoader.generateTileRegions();
                GdxGame.textureAtlasService.generateSectorAtlas(worldUiGroup.worldLoader.world);
                worldUiGroup.worldLoader.createEntities();
                GdxGame.uiService.fadeOutCurtain(0.5f);
                goToScreen(new GameScreen(worldUiGroup));
            } else if(worldUiGroup.worldLoader.getProgress() > 0) {
                GdxGame.uiService.getStage().getRoot().setTouchable(Touchable.disabled);
                GdxGame.audioService.fadeOut(0.5f);
            }
        }
    }

    @Override
    void goToPreviousScreen() {
        Dialog exitDialog = new ExitGameDialog();
        GdxGame.uiService.getStage().addActor(exitDialog);
    }

    private void TogglePauseMenu() {
        if(pauseMenu.hasParent()) {
            GdxGame.audioService.playSound("select");
            GdxGame.audioService.setVolume(1f);
            pauseMenu.remove();
        } else {
            int size = pauseMenu.items.size;
            for (int n = 0; n < size; n++) {
                Actor item = pauseMenu.items.get(n);
                if (item.hasParent()) continue;
                Actor inv = pauseMenu.findActor("Inventory Label");
                item.setPosition(inv.getX(), inv.getY() - item.getHeight() * (n + 2));
                pauseMenu.addActor(item);
            }

            pauseMenu.setTouchable(Touchable.childrenOnly);
            pauseMenu.addAction(Actions.fadeIn(0.6f));
            TravelScreen.this.addActor(pauseMenu);
            GdxGame.uiService.getStage().setKeyboardFocus(pauseMenu);
        }
    }

    private class StarSystemGroup extends Group {
        Actor star;
        Array<Image> worldImages = new Array<>();

        public StarSystemGroup() {
            super();

            star = new StarActor();
            star.setBounds((this.getWidth() - starSize) / 2f, (this.getHeight() - starSize) / 2f
                    , starSize, starSize);
            this.addActor(star);

            for(WorldUIGroup worldUIGroup : TravelScreen.this.worldUIGroups) {
                this.addActor(worldUIGroup.worldImage);
                worldImages.add(worldUIGroup.worldImage);
            }
        }

        public void addOrbitActionsToWorldImages() {
            for(Image worldImage : worldImages) {
                float radius = random.nextFloat() * (getParent().getWidth() - star.getWidth()) / 2f + starSize / 2f;
                float angle = random.nextFloat() * 2 * MathUtils.PI;
                float speed = random.nextFloat();// / 100f;
                Action orbitAction = TravelScreen.this.new WorldOrbitAction(
                        getWidth() / 2f, getHeight() / 2f
                        , radius, angle, speed
                );
                worldImage.addAction(Actions.forever(orbitAction));
            }
        }
    }

	private class StarActor extends Actor {
        private final Animation<TextureRegion> animation;

        private StarActor() {
            Array<? extends TextureRegion> starAnimationFrames = GdxGame.textureAtlasService.findEnvironmentRegions("star");
            this.animation = new Animation<>(0.2f, starAnimationFrames, Animation.PlayMode.LOOP);
            this.setTouchable(Touchable.disabled);
            this.setName("Star");
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            Color batchColor = batch.getColor();

            batch.setColor(GdxGame.fadeColor);
            batch.draw(animation.getKeyFrame(TimeService.GetTime()), getX(), getY(), getWidth(), getHeight());
            batch.setColor(batchColor);
        }

    }

    public class WorldOrbitAction extends Action {
        private final float centerX, centerY, radius, angle, speed;

        public WorldOrbitAction(float centerX, float centerY, float radius, float angle, float speed) {
            this.centerX = centerX;
            this.centerY = centerY;
            this.radius = radius;
            this.angle = angle;
            this.speed = speed;
        }

        @Override
        public boolean act(float delta) {
            float x = MathUtils.round(centerX + radius * MathUtils.cos(TimeService.GetTime() * speed + angle)),
                    y = MathUtils.round(centerY + radius * MathUtils.sin(TimeService.GetTime() * speed + angle) / 4f);
            actor.setPosition(x, y);
            return true;
        }
    }

    private class PauseMenuInputListener extends InputListener {
        @Override
        public boolean keyDown(InputEvent event, int keycode) {
            if(keycode == ControllerService.getInput("back") || keycode == ControllerService.getInput("start"))
                TravelScreen.this.TogglePauseMenu();
            return true;
        }
    }

    private class InformationChangeListener extends ChangeListener {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
            TravelScreen.this.TogglePauseMenu();
        }
    }

    private class OptionsChangeListener extends ChangeListener{
        @Override
        public void changed(ChangeEvent event, Actor actor) {
            GdxGame.audioService.playSound("select");
            goToScreen(new OptionsMenuScreen());
        }
    }

    private class NewSystemChangeListener extends ChangeListener {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
            GdxGame.uiService.getCurtain().setBounds(0, 0, GdxGame.uiService.getStage().getWidth(), GdxGame.uiService.getStage().getHeight());
            GdxGame.uiService.getStage().addActor(GdxGame.uiService.getCurtain());
            GdxGame.uiService.getCurtain().addAction(Actions.fadeIn(1.0f));
            TimeService.addTimer(new TimeService.Timer(1.0f) {
                @Override
                public boolean onCompletion() {
                    if(pauseMenu.hasParent()) pauseMenu.remove();
                    DataService.clearSeed();
                    goToScreen(new TravelScreen());
                    return true;
                }
            });
        }
    }

    private static class CenterYActorComparator implements Comparator<Actor> {
        @Override
        public int compare(Actor a1, Actor a2) {
            float cY1 = a1.getY() + a1.getHeight() / 2;
            float cY2 = a2.getY() + a2.getHeight() / 2;
            return Float.compare(cY2, cY1);
        }
    }
}
