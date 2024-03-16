package com.shdwfghtr.explore;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Rectangle;

import java.io.BufferedReader;
import java.io.IOException;

public class Sector {
	public static final int WIDTH = 17, HEIGHT = 14; //20, 18 in future
	public static final int pHEIGHT = HEIGHT * Tile.HEIGHT;
	public static final int pWIDTH = WIDTH * Tile.WIDTH;
	private static final Rectangle RECTANGLE = new Rectangle();

	private final char[][] charMap = new char[HEIGHT][WIDTH];
	public final float x, y; //global position of the sector's bottom left corner
	public final String name;
	public boolean explored;
	public boolean UP, DOWN, LEFT, RIGHT;

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
		try {
			return charMap[yi][xi];
		} catch(ArrayIndexOutOfBoundsException ex){
			System.out.println(ex.getMessage());
			return ' ';
		}
	}

	char getChar(float x, float y) {
		int xi = (int) Math.floor((x - this.x) / Tile.WIDTH);
		int yi = (int) Math.floor((y - this.y) / Tile.HEIGHT);
    	return getChar(xi, yi);
	}

	public Rectangle getBox() {
		return RECTANGLE.set(x, y, pWIDTH, pHEIGHT);
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
			FileHandle handle = Gdx.files.internal("map/" + name + ".txt");
			if(!handle.exists()){
				System.out.println("File not found: " + handle.name()
						+ "\n using dead0.txt");
				handle = Gdx.files.internal("map/dead0.txt");
			}
			BufferedReader reader = handle.reader(1024);

			for(int y = HEIGHT - 1; y >= 0; y --) {
				String line = "";
				try {
					line = reader.readLine();
				} catch(IOException e){
					e.printStackTrace();
				}
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
        return (int) Math.floor(this.x / pWIDTH);
    }

	public int getYi() {
		return (int) Math.floor(this.y / pHEIGHT);
	}
}
