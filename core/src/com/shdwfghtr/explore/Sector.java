package com.shdwfghtr.explore;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Rectangle;
import com.shdwfghtr.screens.GameScreen;

import java.io.BufferedReader;

public class Sector {
	public static final int WIDTH = 17, HEIGHT = 14;
	static final int pHEIGHT = HEIGHT * Tile.HEIGHT;
	static final int pWIDTH = WIDTH * Tile.WIDTH;

    private final float x, y; //global position of the sector's bottom left corner
	private final char[][] charMap = new char[HEIGHT][WIDTH];
//    private final Tile[][] tileMap = new Tile[HEIGHT][WIDTH];
	public String name;
	private boolean explored;
	boolean UP, DOWN, RIGHT;
	public boolean LEFT;

    Sector(String name, float x, float y, boolean flipX, boolean flipY) {
		this.x = x;
        this.y = y;
		this.name = name;
        addTiles(name, flipX, flipY);
	}

	Sector(int x, int y) {
    	this("dead0", x*pWIDTH, y*pHEIGHT, false, false);
	}

	public char getChar(int xi, int yi) {
		//returns a tile based on indices
		return charMap[yi][xi];
	}

	char getChar(float x, float y) {
		int xi = (int) Math.floor((x - getX()) / Tile.WIDTH);
		int yi = (int) Math.floor((y - getY()) / Tile.HEIGHT);
    	return getChar(xi, yi);
	}

	public boolean isExplored() {
		return explored;
	}

	public void setExplored(boolean bool) {
        if(bool)
            ((GameScreen) ((GdxGame) Gdx.app.getApplicationListener()).getScreen()).setMapImage(name, getXi(), getYi());
        this.explored = bool;
        Asset.DATA.putBoolean(getSaveString(), bool);
        Asset.DATA.flush();
	}
    
    public String getSaveString() {
        return name + ',' + getXi() + ',' + getYi();
    }

	public Rectangle getBox() {
		return Asset.RECTANGLE.set(x, y, pWIDTH, pHEIGHT);
	}

	public float getX() {
		return x;
	}
	public float getY() {
		return y;
	}

	public float getTop() {
		return y + pHEIGHT;
	}

	public float getRight() {
		return x + pWIDTH;
	}

    void setChar(char c, int x, int y) {
        charMap[y][x] = c;
    }

	void addTiles(String name, boolean flipX, boolean flipY) {
		try {
			FileHandle handle = Gdx.files.internal("map/" + name + ".txt");
			BufferedReader reader = handle.reader(1024);

			for(int y = HEIGHT - 1; y >= 0; y --) {
				String line = reader.readLine();
				for(int x = 0; x < WIDTH; x ++) {
					int tx = x, ty = y;
					if (flipX) tx = WIDTH - 1 - x;
					if (flipY) ty = HEIGHT - 1 - y;

					char c = ' ';
					if(line != null && x < line.length()) c = line.charAt(x);
					if(Character.isWhitespace(c) && charMap[ty][tx] != '\u0000') continue;
					//tiles are not added if they are whitespace or would replace a solid and is not a
					//forced blank (!) or a door (D)

					if (flipX && c == '/') charMap[ty][tx] = '\\';
					else if (flipX && c == '\\') charMap[ty][tx] = '/';
					else charMap[ty][tx] = c;
				}
			}
		} catch (Exception e) {
    		System.out.println("error adding tiles for " + name);
    		e.printStackTrace();
		}
	}

	void setOpenness() {
		//By default sectors are closed on all sides, if whitespace is found on an edge, then that side is considered open,
		for(int ty=0; ty<HEIGHT; ty++) {
			if (Character.isWhitespace(getChar(0, ty))) LEFT = true;
			if (Character.isWhitespace(getChar(WIDTH - 1, ty))) RIGHT = true;
		}
		for(int tx=0; tx<WIDTH; tx++) {
			if (Character.isWhitespace(getChar(tx, 0))) DOWN = true;
			if (Character.isWhitespace(getChar(tx, HEIGHT - 1))) UP = true;
		}
	}

    public int getXi() {
        return (int) Math.floor(getX() / pWIDTH);
    }
    
	public int getYi() {
		return (int) Math.floor(getY() / pHEIGHT);
	}
    
    public String getName() {return name;}
}
