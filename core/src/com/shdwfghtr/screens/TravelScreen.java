package com.shdwfghtr.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
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
import com.shdwfghtr.ui.GoToScreenAction;
import com.shdwfghtr.ui.PauseMenuTable;
import com.shdwfghtr.ui.WorldUIGroup;

import java.util.Comparator;
import java.util.Random;

public class TravelScreen extends MenuScreen {
    private static final int starSize = 256;

    private final Random random;
    private final Comparator<Actor> comparator = new CenterYActorComparator();
    private final WorldUIGroup[] worldUIGroups;

    private PauseMenuTable pauseMenu;
    private boolean leaving = false;

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
        GdxGame.fadeColor = worldUIGroups[0].worldLoader.world.palette[7];

        Table table = new Table();
        table.setBounds(0, 0, getWidth(), getHeight());
        addActor(table);

        Label.LabelStyle style = new Label.LabelStyle(GdxGame.uiService.getHeaderFont(), Color.WHITE);
        Label header = new Label("Choose a Destination", style);
        table.add(header).pad(12).colspan(3);
        table.row();

        Group starSystemGroup = this.new StarSystemGroup();
        table.add(starSystemGroup).expand().colspan(3).fill();
        table.row();

        pauseMenu = new PauseMenuTable(Player.CURRENT, worldUIGroups);
        pauseMenu.addListener(this.new PauseMenuInputListener());
        starSystemGroup.addActor(pauseMenu);

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
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        if(leaving) return;

        for (WorldUIGroup worldUiGroup : worldUIGroups) {
            if (worldUiGroup.worldLoader.getProgress() >= 1) {
                PaletteService.recolorAtlasByRegion(PaletteService.getPalette("environment"),
                        worldUiGroup.worldLoader.world.palette, GdxGame.textureAtlasService.entityAtlas,
                        "bossSheet", "enemySheet");
                worldUiGroup.worldLoader.generateTileRegions();
                GdxGame.textureAtlasService.generateSectorAtlas(worldUiGroup.worldLoader.world);
                worldUiGroup.worldLoader.createEntities();

                GdxGame.goToScreen(new GameScreen(worldUiGroup));
                leaving = true;
            } else if (worldUiGroup.worldLoader.getProgress() > 0) {
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

    private class StarSystemGroup extends Group {
        public StarSystemGroup() {
            super();

            Actor star = new StarActor();
            star.setSize(starSize, starSize);
            Color color = worldUIGroups[0].worldLoader.world.palette[7];
            float[] hsv = new float[3];
            hsv = color.toHsv(hsv);
            hsv[1] = 50 / 255f;
            star.setColor(color.fromHsv(hsv));
            this.addActor(star);

            for(WorldUIGroup worldUIGroup : TravelScreen.this.worldUIGroups) {
                Image worldImage = worldUIGroup.new WorldImage();
                this.addActor(worldImage);
                worldImage.addListener(worldUIGroup.new WorldClickListener());
            }
        }

        @Override
        protected void setParent(Group parent) {
            super.setParent(parent);
            for (Actor actor : getChildren())
                if (actor instanceof WorldUIGroup.WorldImage) {
                    WorldUIGroup.WorldImage worldImage = (WorldUIGroup.WorldImage) actor;
                    float c = random.nextFloat();
                    float radius = c * (parent.getWidth() - starSize) / 2f + starSize / 2f;
                    float angle = c * 2 * MathUtils.PI;
                    float speed = c / 100f;
                    Action orbitAction = TravelScreen.this.new WorldOrbitAction(
                            parent.getWidth() / 2f, parent.getHeight() / 2f
                            , radius, angle, speed
                    );
                    worldImage.addAction(Actions.forever(orbitAction));
                } else if (actor instanceof StarActor) {
                    actor.setPosition((parent.getWidth() - actor.getWidth()) / 2f
                            , (parent.getHeight() - actor.getHeight()) / 2f);
                }
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            this.getChildren().sort(comparator);
        }
    }

	private class StarActor extends Image {
        private final Animation<TextureRegionDrawable> animation;

        private StarActor() {
            Array<TextureAtlas.AtlasRegion> starAnimationFrames = GdxGame.textureAtlasService.findEnvironmentRegions("star");
            Array<TextureRegionDrawable> frames = new Array<>(starAnimationFrames.size);
            for(TextureAtlas.AtlasRegion region : starAnimationFrames){
                frames.add(new TextureRegionDrawable(region));
            }
            this.animation = new Animation<>(0.2f, frames);
            this.animation.setPlayMode(Animation.PlayMode.LOOP);
            this.setTouchable(Touchable.disabled);
            this.setName("Star");
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            this.setDrawable(animation.getKeyFrame(TimeService.GetTime()));
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
                pauseMenu.toggle();
            return true;
        }
    }

    private class InformationChangeListener extends ChangeListener {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
            pauseMenu.toggle();
        }
    }

    private class OptionsChangeListener extends ChangeListener{
        @Override
        public void changed(ChangeEvent event, Actor actor) {
            GdxGame.audioService.playSound("select");
            GdxGame.goToScreen(new OptionsMenuScreen());
        }
    }

    private class NewSystemChangeListener extends ChangeListener {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
            if (pauseMenu.isVisible()) pauseMenu.toggle();
            DataService.clearSeed();
            GdxGame.goToScreen(new TravelScreen());
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
