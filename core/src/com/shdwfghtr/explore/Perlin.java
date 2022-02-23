package com.shdwfghtr.explore;

import com.badlogic.gdx.math.MathUtils;

public class Perlin {
	private static final float STEP_SIZE = 0.05f;
	float[] grid;
	
	public Perlin(int size) {
		this.grid = new float[size];
		
		for(int i=0; i < size; i++)
			this.grid[i] = MathUtils.random();
	}
	
	public Perlin() {
		this(19991);
	}
	
	public float getFloat(float progress) {
		progress /= STEP_SIZE;
		int start = ((int) progress) % grid.length;
		float remainder = progress - ((int) progress);
		
		return getFloat(start, remainder);
	}
	
	public float getFloat(int start, float progress) {
		int end = (start + 1) % grid.length;
		return cerp(grid[start], grid[end], progress);
	}
	
	public float getFloat(float progress, float min, float max) {
		return (getFloat(progress) * (max - min)) + min;
	}
	
	//cosine interpolation
	private static float cerp(float start, float end, float progress) {
		float mu = (1 - MathUtils.cos(progress * MathUtils.PI)) / 2;
		return start * (1 - mu) + end * mu;
	}
}