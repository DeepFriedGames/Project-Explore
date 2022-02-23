package com.shdwfghtr.explore;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Input.Peripheral;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;
import com.shdwfghtr.entity.Item;
import com.shdwfghtr.entity.Player;
import com.shdwfghtr.input.GamepadHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

public class Asset {
    //Global Resources
    private final AssetManager manager;
    private final MusicHandler musicHandler;
    private final Particles particles;
    private final Stage stage;
    private TextureAtlas uiAtlas, entityAtlas, environmentAtlas, bgAtlas, explosionAtlas;
    private Skin skin;
    private final Image curtain = new Image(new Texture(1, 1, Pixmap.Format.RGB565));
    private final HashMap<String, Color[]> palettes = new HashMap<String, Color[]>();

    public Asset() {
        this.manager = new AssetManager();
        this.musicHandler = new MusicHandler();
        this.particles = new Particles();
        this.stage = new Stage();
        Gdx.input.setInputProcessor(stage);
    }

    void load() {
        //here we load a butt ton of assets, which I would LOVE to do systematically, but java
        //doesn't support that, so brute force it is.  Just don't forget to add new assets here.
        manager.load("atlas/BG.atlas", TextureAtlas.class);
        manager.load("atlas/Entity.atlas", TextureAtlas.class);
        manager.load("atlas/Environment.atlas", TextureAtlas.class);
        manager.load("atlas/Explosions.atlas", TextureAtlas.class);
        manager.load("atlas/particle.atlas", TextureAtlas.class);
        manager.load("atlas/UI.atlas", TextureAtlas.class);

        manager.load("audio/ammo.wav", Sound.class);
        manager.load("audio/armor.wav", Sound.class);
        manager.load("audio/bomb.wav", Sound.class);
        manager.load("audio/boss_damage.wav", Sound.class);
        manager.load("audio/boss_explosion.wav", Sound.class);
        manager.load("audio/bullet.wav", Sound.class);
        manager.load("audio/damage1.wav", Sound.class);
        manager.load("audio/disruption.wav", Sound.class);
        manager.load("audio/door_close.wav", Sound.class);
        manager.load("audio/door_open.wav", Sound.class);
        manager.load("audio/enemy.wav", Sound.class);
        manager.load("audio/enemy_damage.wav", Sound.class);
        manager.load("audio/explosion.wav", Sound.class);
        manager.load("audio/footstep.wav", Sound.class);
        manager.load("audio/gnat.wav", Sound.class);
        manager.load("audio/item.wav", Sound.class);
        manager.load("audio/jump.wav", Sound.class);
        manager.load("audio/missile.wav", Sound.class);
        manager.load("audio/oxygen.wav", Sound.class);
        manager.load("audio/phase_shot.wav", Sound.class);
        manager.load("audio/player_damage.wav", Sound.class);
        manager.load("audio/power_up.wav", Sound.class);
        manager.load("audio/razor_jump.wav", Sound.class);
        manager.load("audio/select.wav", Sound.class);
        manager.load("audio/select_disabled.wav", Sound.class);
        manager.load("audio/switch.wav", Sound.class);
        manager.load("audio/wide_shot.wav", Sound.class);

        manager.load("audio/Bergamot.ogg", Music.class);
        manager.load("audio/Bergamot_Intro.ogg", Music.class);
        manager.load("audio/Intro.ogg", Music.class);
        manager.load("audio/Item.ogg", Music.class);
        manager.load("audio/World0.ogg", Music.class);
        manager.load("audio/World1.ogg", Music.class);
        manager.load("audio/World2.ogg", Music.class);
        manager.load("audio/World3.ogg", Music.class);
        manager.load("audio/World4.ogg", Music.class);

        palettes.put("entity", paletteFromPal(Gdx.files.internal("palette/entity.pal")));
        palettes.put("environment", paletteFromPal(Gdx.files.internal("palette/environment.pal")));
        palettes.put("ui", paletteFromPal(Gdx.files.internal("palette/ui.pal")));

        particles.addParticleEffect(Gdx.files.internal("particle/boss_explosion.p"));
        particles.addParticleEffect(Gdx.files.internal("particle/break.p"));
        particles.addParticleEffect(Gdx.files.internal("particle/bullet.p"));
        particles.addParticleEffect(Gdx.files.internal("particle/damage.p"));
        particles.addParticleEffect(Gdx.files.internal("particle/disrupt.p"));
        particles.addParticleEffect(Gdx.files.internal("particle/dust.p"));
        particles.addParticleEffect(Gdx.files.internal("particle/explosion.p"));
        particles.addParticleEffect(Gdx.files.internal("particle/flame.p"));
        particles.addParticleEffect(Gdx.files.internal("particle/glibber.p"));
        particles.addParticleEffect(Gdx.files.internal("particle/gnats.p"));
        particles.addParticleEffect(Gdx.files.internal("particle/lava.p"));
        particles.addParticleEffect(Gdx.files.internal("particle/midasium.p"));
        particles.addParticleEffect(Gdx.files.internal("particle/oxygen.p"));
        particles.addParticleEffect(Gdx.files.internal("particle/slick.p"));
        particles.addParticleEffect(Gdx.files.internal("particle/spark.p"));
        particles.addParticleEffect(Gdx.files.internal("particle/stars.p"));
        particles.addParticleEffect(Gdx.files.internal("particle/step.p"));

        manager.load("skin/font-export.fnt", BitmapFont.class);
        manager.load("skin/font-title-export.fnt", BitmapFont.class);
    }

    private Color[] paletteFromPal(FileHandle handle) {
        try {
            BufferedReader reader = handle.reader(256);

            String line = reader.readLine();
            if(line.matches("JASC-PAL")) {
                reader.readLine(); //this line is useless, it always says 0100

                int size = Integer.valueOf(reader.readLine());
                Color[] palette = new Color[size];

                for(int i=0; i < size; i++) {
                    line = reader.readLine();
                    String[] val = line.split(" ");
                    float r = Integer.valueOf(val[0]) / 255f,
                            g = Integer.valueOf(val[1]) / 255f,
                            b = Integer.valueOf(val[2]) / 255f,
                            a = 1;
                    palette[i] = new Color(r, g, b, a);
                }

                return palette;
            } else {
                System.out.print(handle.path() + " is not .pal format");
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void initializeResources() {
        uiAtlas = manager.get("atlas/UI.atlas", TextureAtlas.class);
        bgAtlas = manager.get("atlas/BG.atlas", TextureAtlas.class);
        environmentAtlas = manager.get("atlas/Environment.atlas", TextureAtlas.class);
        entityAtlas = manager.get("atlas/Entity.atlas", TextureAtlas.class);
        explosionAtlas = manager.get("atlas/Explosions.atlas", TextureAtlas.class);

        skin = new Skin(Gdx.files.internal("skin/neutralizer-ui.json"));

        if(!VisUI.isLoaded()) VisUI.load(skin);
    }

    void dispose() {
        manager.dispose();
        uiAtlas.dispose();
        entityAtlas.dispose();
        environmentAtlas.dispose();
        bgAtlas.dispose();
        skin.dispose();
        stage.dispose();
        particles.atlas.dispose();

    }

    //Global static variables
    public static float TIME;

    //Global static constants
    public static final Preferences CONTROLS = Gdx.app.getPreferences("controls");
    public static final Preferences OPTIONS = Gdx.app.getPreferences("options");
    public static final Preferences DATA = Gdx.app.getPreferences("data");
    public static final Random RANDOM = new Random();
    public static final Vector2 VECTOR2	= new Vector2();
    public static final Rectangle RECTANGLE = new Rectangle();
    public static final int CAM_WIDTH = Sector.pWIDTH;
    public static final int CAM_HEIGHT = Sector.WIDTH * 10;
    public static final float GRAVITY = 0.14f;
    public static final GlyphLayout GLYPH = new GlyphLayout();
    public static final GameCamera CAMERA = new GameCamera();
    public static final Array<String> MESSAGES = new Array<String>();
    public static final ArrayList<Timer> TIMERS = new ArrayList<Timer>();
    public static final String[] INPUT_LIST = {"up", "down", "left", "right", "jump", "shoot", "dash", "switch", "start", "back", "x-axis", "y-axis"};

    public static AssetManager getManager() {
        return ((GdxGame) Gdx.app.getApplicationListener()).asset.manager;
    }

    public static TextureAtlas getUIAtlas() {
        return ((GdxGame) Gdx.app.getApplicationListener()).asset.uiAtlas;
    }

    public static TextureAtlas getEntityAtlas() {
        return ((GdxGame) Gdx.app.getApplicationListener()).asset.entityAtlas;
    }

    public static TextureAtlas getEnvironmentAtlas() {
        return ((GdxGame) Gdx.app.getApplicationListener()).asset.environmentAtlas;
    }

    public static TextureAtlas getExplosionAtlas() {
        return ((GdxGame) Gdx.app.getApplicationListener()).asset.explosionAtlas;
    }

    static TextureAtlas getBGAtlas() {
        return ((GdxGame) Gdx.app.getApplicationListener()).asset.bgAtlas;
    }

    public static Stage getStage() {
        return ((GdxGame) Gdx.app.getApplicationListener()).asset.stage;
    }

    public static BitmapFont getBodyFont() {
        return getSkin().getFont("font");
    }

    public static BitmapFont getHeaderFont() {
        return getSkin().getFont("title");
    }

    public static Skin getSkin() {
        return ((GdxGame) Gdx.app.getApplicationListener()).asset.skin;
    }

    public static Image getCurtain() {
        return ((GdxGame) Gdx.app.getApplicationListener()).asset.curtain;
    }

    public static MusicHandler getMusicHandler() {
        return ((GdxGame) Gdx.app.getApplicationListener()).asset.musicHandler;
    }

    public static Particles getParticles() {
        return ((GdxGame) Gdx.app.getApplicationListener()).asset.particles;
    }

    @SuppressWarnings("all")
    public static Color[] getPalette(String key) {
        return ((GdxGame) Gdx.app.getApplicationListener()).asset.palettes.get(key);
    }

    public static void initialize() {
//	    resetData();
        Player.CURRENT = new Player();
        Player.load(Player.CURRENT);

        String[] itemsOpt = {"charge_shot", "charge_missile", "wide_shot", "homing_missile",
                "razor_jump", "charge_bomb", "swift_boots"};
        boolean add;
        for(String item : itemsOpt) {
            add = true;
            for(String key : Player.INVENTORY.get().keySet())
                if(key.contains(item)) add = false;
            if(add) Item.OPTIONAL_ITEMS.add(item);
        }

        String[] itemsGen = {"power_up", "jump_up", "speed_up", "range_up"};
        Collections.addAll(Item.GENERIC_ITEMS, itemsGen);

        if(CONTROLS.contains("controller"))
            if((CONTROLS.getString("controller").matches("gamepad") && Controllers.getControllers().size < 1)
                    || (CONTROLS.getString("controller").matches("keyboard") && !Gdx.input.isPeripheralAvailable(Peripheral.HardwareKeyboard))
                    || (CONTROLS.getString("controller").matches("touch") && !Gdx.input.isPeripheralAvailable(Peripheral.MultitouchScreen))) {
                CONTROLS.remove("controller");
            }
        if(!CONTROLS.contains("controller")) resetControls();
        CONTROLS.flush();

    }

    public static Timer[] getTimers() {
        Timer[] array = new Timer[TIMERS.size()];
        return TIMERS.toArray(array);
    }

    public static void resetData() {
        Asset.DATA.clear();
        Asset.DATA.flush();
        Player.INVENTORY.clear();
        Player.INVENTORY.flush();
    }

    public static void resetControls() {
        System.out.println("resetting controls");
        if(Gdx.input.isPeripheralAvailable(Peripheral.MultitouchScreen)) {
            CONTROLS.putString("controller", "touch");
            //The touchscreen controls are in an ordered list called buttons
            //each button gets it's name from the corresponding items in the INPUT_LIST
            //these names are then used to pass on commands
            CONTROLS.putInteger("up", 0);
            CONTROLS.putInteger("down", 1);
            CONTROLS.putInteger("left", 2);
            CONTROLS.putInteger("right", 3);
            CONTROLS.putInteger("jump", 4);
            CONTROLS.putInteger("shoot", 5);
            CONTROLS.putInteger("dash", 6);
            CONTROLS.putInteger("switch", 7);
            CONTROLS.putInteger("start", 8);
            CONTROLS.putInteger("back", 9);
        } if(Gdx.input.isPeripheralAvailable(Peripheral.HardwareKeyboard)) {
            CONTROLS.putString("controller", "keyboard");
            CONTROLS.putInteger("up", Keys.UP);
            CONTROLS.putInteger("down", Keys.DOWN);
            CONTROLS.putInteger("left", Keys.LEFT);
            CONTROLS.putInteger("right", Keys.RIGHT);
            CONTROLS.putInteger("jump", Keys.Z);
            CONTROLS.putInteger("shoot", Keys.X);
            CONTROLS.putInteger("dash", Keys.CONTROL_LEFT);
            CONTROLS.putInteger("switch", Keys.ALT_LEFT);
            CONTROLS.putInteger("start", Keys.ENTER);
            CONTROLS.putInteger("back", Keys.ESCAPE);
        } if(Controllers.getControllers().size > 0) {
            CONTROLS.putString("controller", "gamepad");
            GamepadHandler.GAMEPAD = Controllers.getControllers().first();
            ControllerMapping mapping = GamepadHandler.GAMEPAD.getMapping();
            CONTROLS.putInteger("up", mapping.buttonDpadUp);
            CONTROLS.putInteger("down", mapping.buttonDpadDown);
            CONTROLS.putInteger("left", mapping.buttonDpadLeft);
            CONTROLS.putInteger("right", mapping.buttonDpadRight);
            CONTROLS.putInteger("jump", mapping.buttonA);
            CONTROLS.putInteger("shoot", mapping.buttonB);
            CONTROLS.putInteger("dash", mapping.buttonX);
            CONTROLS.putInteger("switch", mapping.buttonY);
            CONTROLS.putInteger("start", mapping.buttonStart);
            CONTROLS.putInteger("back", mapping.buttonBack);
        }
        CONTROLS.putBoolean("use_axis", false);
        CONTROLS.flush();
    }

    public static void recolorTextureRegion(TextureRegion region, Color oldPalette[], Color newPalette[]) {
        //changes all instances of Color1 in the given Texture to Color2
        int length = Math.min(oldPalette.length, newPalette.length);

        for(int i=0; i < length; i++)
            recolorTextureRegion(region, oldPalette[i], newPalette[i]);
    }

    static Texture recolorTextureRegion(TextureRegion region, Color oldColor, Color newColor) {
        //changes all instances of Color1 in the given Texture to Color2
        Texture tex = region.getTexture();
        TextureData texData = tex.getTextureData();
        if(!texData.isPrepared()) texData.prepare();
        Pixmap pixmap = texData.consumePixmap();
        pixmap.setColor(newColor);

        int y1 = region.getRegionY() - 1, x1 = region.getRegionX() - 1;
        int y2 = y1 + region.getRegionHeight() + 2, x2 = x1 + region.getRegionWidth() + 2;
        for (int y = y1; y < y2; y++) {
            for (int x = x1; x < x2; x++) {
                if (pixmap.getPixel(x, y) == Color.rgba8888(oldColor))
                    pixmap.drawPixel(x, y);
            }
        }
        region.setTexture(new Texture(pixmap));
        return region.getTexture();

    }

    public static class Particles {
        private final HashMap<String, ParticleEffectPool> POOLS = new HashMap<String, ParticleEffectPool>();
        private final Array<ParticleEffectPool.PooledEffect> ADDITIVE_EFFECTS = new Array<ParticleEffectPool.PooledEffect>();
        private final Array<ParticleEffectPool.PooledEffect> NORMAL_EFFECTS = new Array<ParticleEffectPool.PooledEffect>();
        final TextureAtlas atlas = new TextureAtlas("atlas/particle.atlas");

        private void addParticleEffect(FileHandle handle) {
            ParticleEffect effect = new ParticleEffect();
            effect.load(handle, atlas);
            POOLS.put(handle.nameWithoutExtension(), new ParticleEffectPool(effect, 1, 5));
        }

        public ParticleEffectPool.PooledEffect obtain(String key, Boolean add) {
            ParticleEffectPool.PooledEffect effect = POOLS.get(key).obtain();
            if(add) add(effect);
            return effect;
        }

        public void add(ParticleEffectPool.PooledEffect effect) {
            if(Asset.CONTROLS.getBoolean("particles")) {
                if(effect.getEmitters().first().isAdditive()) {
                    if (!ADDITIVE_EFFECTS.contains(effect, true)) {
                        ADDITIVE_EFFECTS.add(effect);
                        effect.setEmittersCleanUpBlendFunction(false);
                    }
                } else {
                    if (!NORMAL_EFFECTS.contains(effect, true))  NORMAL_EFFECTS.add(effect);
                }
                effect.start();
            }
        }

        public void remove(ParticleEffectPool.PooledEffect effect) {
            if(NORMAL_EFFECTS.contains(effect, true)) {
                NORMAL_EFFECTS.removeValue(effect, true);
                effect.free();
            } else if(ADDITIVE_EFFECTS.contains(effect, true)) {
                ADDITIVE_EFFECTS.removeValue(effect, true);
                effect.free();
            }
        }

        public void update(float delta) {
            for(ParticleEffectPool.PooledEffect effect : getEffects()) {
                if(effect.isComplete()) remove(effect);
                else effect.update(delta);
            }
        }

        public static void colorEffect(ParticleEffectPool.PooledEffect effect, Color color) {
            ParticleEmitter[] emitters = effect.getEmitters().toArray(ParticleEmitter.class);
            for(ParticleEmitter emitter : emitters) {
                float[] colors = emitter.getTint().getColors();
                colors[0] = color.r;
                colors[1] = color.g;
                colors[2] = color.b;

            }
        }

        public void draw(Batch batch) {
            //draw all additive blended effects
            ParticleEffectPool.PooledEffect[] additives = ADDITIVE_EFFECTS.toArray(ParticleEffectPool.PooledEffect.class);
            for (ParticleEffectPool.PooledEffect additiveEffect : additives)
                additiveEffect.draw(batch);

            //We need to reset the batch to the original blend state as we have setEmittersCleanUpBlendFunction as false in additiveEffect
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            //draw all 'normal alpha' blended effects
            ParticleEffectPool.PooledEffect[] normies = NORMAL_EFFECTS.toArray(ParticleEffectPool.PooledEffect.class);
            for (ParticleEffectPool.PooledEffect normalEffect : normies)
                normalEffect.draw(batch);
        }

        ParticleEffectPool.PooledEffect[] getEffects() {
            Array<ParticleEffectPool.PooledEffect> all = new Array<ParticleEffectPool.PooledEffect>();
            all.addAll(ADDITIVE_EFFECTS);
            all.addAll(NORMAL_EFFECTS);

            return all.toArray(ParticleEffectPool.PooledEffect.class);
        }
    }

    static class Cursors {
        private static String CURRENT = "";

        static void setCurrent(String name) {
            if(CURRENT.matches(name)) return;

            CURRENT = name;
            Cursor cursor = Gdx.graphics.newCursor(
                    new Pixmap(Gdx.files.internal("cursor/" + CURRENT + ".png")),
                    0, 0);
            Gdx.graphics.setCursor(cursor);
        }
    }

    public static class Convert {
        private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

        public static String toString(long l) {
            StringBuilder result = new StringBuilder();
            long tmp = l;
            while (tmp != 0) {
                long module = tmp % ALPHABET.length();
                result.insert(0, ALPHABET.charAt((int) Math.abs(module)));
                tmp /= ALPHABET.length();
            }
            return result.toString();
        }

        public static long toLong(String s) {
            long result = 0;
            int power = 0;
            for (int i = s.length() - 1; i >= 0; i--) {
                int mantissa = ALPHABET.indexOf(s.charAt(i));
                result += mantissa * Math.pow(ALPHABET.length(), power++);
            }
            return result;
        }

    }
}
