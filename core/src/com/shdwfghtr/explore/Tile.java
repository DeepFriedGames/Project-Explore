package com.shdwfghtr.explore;

public class Tile {
    public static final int WIDTH = 16, HEIGHT = 16;
    public static final String ALL_TILES = "I>*+=0123456789\\/|-^#?%@";
    private static final String INDEXED_TILES = "0123456789\\/|-^#?%@";
    private static final String SOLID_TILES = "0123456789\\/|-#?@=+";
    private static final String VISIBLE_TILES = "0123456789\\/|-^#?%@+";
    private static final String BREAKABLE_TILES = "#@I";
    private static final String DISRUPTABLE_TILES = "?";
    private static final String ROTATIONAL_TILES = "5689^%";

    static boolean isIndexed(char t) {
        return INDEXED_TILES.contains(String.valueOf(t));
    }

    static boolean isSolid(char t) {
        return SOLID_TILES.contains(String.valueOf(t));
    }

    static boolean isVisible(char t) {
        return VISIBLE_TILES.contains(String.valueOf(t));
    }

    public static boolean isBreakable(char t) {
        return BREAKABLE_TILES.contains(String.valueOf(t));
    }

    static boolean isDisruptable(char t) {
        return DISRUPTABLE_TILES.contains(String.valueOf(t));
    }

    static boolean isRotational(char t) { return ROTATIONAL_TILES.contains(String.valueOf(t)); }
}
