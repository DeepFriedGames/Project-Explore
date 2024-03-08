package com.shdwfghtr.asset;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.shdwfghtr.explore.GdxGame;
import com.shdwfghtr.explore.HSL;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

public class PaletteService {
    private static final HashMap<String, Color[]> palettes = new HashMap<>();
    private static final Color[] defaultPalette = new Color[]{Color.RED, Color.GREEN, Color.BLUE, Color.WHITE};

    public static Color[] getPalette(String key) {
        if(palettes.containsKey(key))
            return palettes.get(key);
        else {
            String fileName = "palette/" + key + ".pal";
            FileHandle file = Gdx.files.internal(fileName);
            if(file.exists()) {
                Color[] palette = paletteFromPal(file);
                palettes.put(key, palette);
                return palette;
            }
            System.out.println("Could not locate palette file: " + fileName + "\nReturning default palette");
        }
        return defaultPalette;
    }

    private static Color[] paletteFromPal(FileHandle handle) {
        try {
            BufferedReader reader = handle.reader(256);

            String line = reader.readLine();
            if(line.matches("JASC-PAL")) {
                reader.readLine(); //this line is useless, it always says 0100

                int size = Integer.parseInt(reader.readLine());
                Color[] palette = new Color[size];

                for(int i=0; i < size; i++) {
                    line = reader.readLine();
                    String[] val = line.split(" ");
                    float r = Integer.parseInt(val[0]) / 255f,
                            g = Integer.parseInt(val[1]) / 255f,
                            b = Integer.parseInt(val[2]) / 255f,
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

    public static Color[] generatePalette(float seed) {
        Color[] defaultPalette = getPalette("environment");

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

    public static void colorEffect(ParticleEffectPool.PooledEffect effect, Color color) {
        ParticleEmitter[] emitters = effect.getEmitters().toArray(ParticleEmitter.class);
        for(ParticleEmitter emitter : emitters) {
            float[] colors = emitter.getTint().getColors();
            colors[0] = color.r;
            colors[1] = color.g;
            colors[2] = color.b;
        }
    }

    public static void recolorAtlasByRegion(Color[] oldPalette, Color[] newPalette, TextureAtlas atlas, String... regionNames) {
        //get the original texture of the atlas
        int length = Math.min(oldPalette.length, newPalette.length);
        Texture first = atlas.getTextures().first();
        Texture texture = first;

        //recolor the texture
        for(String name : regionNames)
            for(int i=0; i < length; i++)
                texture = PaletteService.recolorTextureRegion(atlas.findRegion(name), oldPalette[i], newPalette[i]);

        //assign the texture to the texture regions
        TextureRegion[] regions = atlas.getRegions().toArray(TextureRegion.class);
        for(TextureRegion tr : regions)
            tr.setTexture(texture);
        atlas.getTextures().add(texture);
        atlas.getTextures().remove(first);
    }

    public static void recolorTextureRegion(TextureRegion region, Color[] oldPalette, Color[] newPalette) {
        //changes all instances of Color1 in the given Texture to Color2
        int length = Math.min(oldPalette.length, newPalette.length);

        for(int i=0; i < length; i++)
            recolorTextureRegion(region, oldPalette[i], newPalette[i]);
    }

    public static Texture recolorTextureRegion(TextureRegion region, Color oldColor, Color newColor) {
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
}
