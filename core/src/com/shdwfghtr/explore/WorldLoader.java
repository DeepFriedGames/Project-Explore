package com.shdwfghtr.explore;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

import java.util.Arrays;
import java.util.HashMap;

public class WorldLoader implements Runnable {
    private static final int OVERFLOW_LIMIT = 800;
    private static final HashMap<String, Integer> VARIATIONS = new HashMap<String, Integer>();
    private static int[][] noise = new int[Sector.HEIGHT][Sector.WIDTH];
    public long seed;
    public char[][] charMap;
    public int width, height, area;
    public int maxSectors;
    private int x1, x2, y1, y2, sectorCount;
    private float progress;
    public World world;

    private static int getNumberOfMapVariations(String name) {
        if(VARIATIONS.containsKey(name))
            return VARIATIONS.get(name);
        else {
            int n = 0;
            while (Gdx.files.internal("map/" + name + String.valueOf(n) + ".txt").exists())
                n++;

            VARIATIONS.put(name, n);
            return n;
        }
    }

    @Override
    public void run() {
        Asset.RANDOM.setSeed(seed);

        generateCharacterMap();

        //each sector's identity is procedurally generated.
        for (int y = height - 1; y >= 0; y--)
            for (int x = 0; x < width; x++) {
                String name = generateSectorName(x, y);
                if (!name.contains("dead") || getChar(x + 1, y) != '%' || getChar(x - 1, y) != '%' || getChar(x, y + 1) != '%' || getChar(x, y - 1) != '%')
                    if(name.contains("start"))
                        world.setSector(name, x, y, getChar(x + 1, y) == '=', false);
                    else if(name.contains("boss") || name.contains("item"))
                        if(name.contains("shaft"))
                            world.setSector(name, x, y, false, (getChar(x, y - 1) == '|'));
                        else
                            world.setSector(name, x, y, (getChar(x + 1, y) == '|' || getChar(x + 1, y) == '='), false);
                    else if(name.contains("new"))
                        if(Asset.RANDOM.nextBoolean())
                            world.setSector(generateSector(x, y, 1, 1, name.contains("shaft"), Algorithm.RANDOM));
                        else
                            world.setSector(generateSector(x, y, 1, 1, name.contains("shaft"), Algorithm.SYMMETRIC));
                    else if(name.contains("tunnel"))
                        world.setSector(generateSector(x, y, 1, 1, name.contains("shaft"), Algorithm.TUNNEL));
                    else
                        world.setSector(name, x, y, Asset.RANDOM.nextBoolean(), false);
                else
                    world.setSector(new Sector(x, y));

                progress += 1 / ((float) area + 4);
            }

        //connects each sector with doorways, generates the sector's icons for the minimap, and adds entities to each tile
        generateSectors();
        progress = 1;
    }

    private String generateSectorName(int x, int y) {
        //the name of the sector will determine which file to use in assets.
        //sectors are separated by hall, shaft, item, start, boss, and dead
        //any sector may be pulled, the tileset and palette are determined at Asset.RANDOM.
        String name;
        char c = getChar(x, y);
        //determine from this table look up which type of txt map to use
        if(c == 'S')
            name = "start";
        else if(Character.isLetter(c))
            name = "item" + c;
        else if(c == '$')
            name = "boss" + world.index;
        else if(c == '|') {
            name = "shaft";
            if(getChar(x + 1, y) == '%' && getChar(x - 1, y) == '%'
                    && (getChar(x, y + 1) == '%' || getChar(x, y - 1) == '%'))
                name = name.concat("_new");
        } else if(c == '=') {
            name = "hall";
            if(getChar(x, y + 1) != '|' && getChar(x, y - 1) != '|'
                    && (getChar(x + 1, y) == '%' || getChar(x - 1, y) == '%'))
                name = name.concat("_new");
        } else
            name = "dead";

        int var = getNumberOfMapVariations(name);
        if(var > 0)
            var = Asset.RANDOM.nextInt(var);
        if(Gdx.files.internal("map/" + name.concat(String.valueOf(var)) + ".txt").exists())
            return name.concat(String.valueOf(var));
        else
            return name;
    }

    private void generateSectors() {
        Sector sector;
        char c;
        for(int y=0; y<height; y++)
            for(int x=0; x<width; x++) {
                sector = world.getSector(x, y);
                c = getChar(x, y);
                if(c == '|') {
                    if(getChar(x+1, y) == '=' || getChar(x+1, y) == 'I' || getChar(x+1, y) == '$') {
                        //addToInventory a door to the right hand side of the sector
                        int var = Asset.RANDOM.nextInt(getNumberOfMapVariations("doorR"));
                        sector.addTiles("doorR" + var, false, false);
                    } if(getChar(x-1, y) == '=' || getChar(x-1, y) == 'I' || getChar(x-1, y) == '$') {
                        //addToInventory a door to the left hand side of the sector
                        int var = Asset.RANDOM.nextInt(getNumberOfMapVariations("doorR"));
                        sector.addTiles("doorR" + var, true, false);
                    } if(getChar(x, y+1) == '=') {
                        //addToInventory a door to the top side of the sector
                        int var = Asset.RANDOM.nextInt(getNumberOfMapVariations("doorD"));
                        sector.addTiles("doorD" + var, false, true);
                    } if(getChar(x, y-1) == '=') {
                        //addToInventory a door to the top side of the sector
                        int var = Asset.RANDOM.nextInt(getNumberOfMapVariations("doorD"));
                        sector.addTiles("doorD" + var, false, false);
                    } if(getChar(x, y+1) == '%' || getChar(x, y+1) == 'I') {
                        //addToInventory a dead end to the top side of the sector
                        int var = Asset.RANDOM.nextInt(getNumberOfMapVariations("deadD"));
                        sector.addTiles("deadD" + var, false, true);
                    } if(getChar(x, y-1) == '%' || getChar(x, y-1) == 'I') {
                        //addToInventory a dead end to the top side of the sector
                        int var = Asset.RANDOM.nextInt(getNumberOfMapVariations("deadD"));
                        sector.addTiles("deadD" + var, false, false);
                    }
                } else if(c == '=') {
                    if(getChar(x+1, y) == '|' || getChar(x+1, y) == 'I' || getChar(x+1, y) == '$') {
                        //addToInventory a door to the right hand side of the sector
                        int var = Asset.RANDOM.nextInt(getNumberOfMapVariations("doorR"));
                        sector.addTiles("doorR" + var, false, false);
                    } if(getChar(x-1, y) == '|' || getChar(x-1, y) == 'I' || getChar(x-1, y) == '$') {
                        //addToInventory a door to the left hand side of the sector
                        int var = Asset.RANDOM.nextInt(getNumberOfMapVariations("doorR"));
                        sector.addTiles("doorR" + var, true, false);
                    } if(getChar(x+1, y) == '%') {
                        //addToInventory a dead end to the right hand side of the sector
                        int var = Asset.RANDOM.nextInt(getNumberOfMapVariations("deadR"));
                        sector.addTiles("deadR" + var, false, false);
                    } if(getChar(x-1, y) == '%') {
                        //addToInventory a dead end to the left hand side of the sector
                        int var = Asset.RANDOM.nextInt(getNumberOfMapVariations("deadR"));
                        sector.addTiles("deadR" + var, true, false);
                    } if(getChar(x, y+1) == '|') {
                        //addToInventory a door to the top side of the sector
                        int var = Asset.RANDOM.nextInt(getNumberOfMapVariations("doorD"));
                        sector.addTiles("doorD" + var, false, true);
                    } if(getChar(x, y-1) == '|') {
                        //addToInventory a door to the top side of the sector
                        int var = Asset.RANDOM.nextInt(getNumberOfMapVariations("doorD"));
                        sector.addTiles("doorD" + var, false, false);
                    }
                } else if(c == 'I') {
                    if(getChar(x+1, y) == '|' || getChar(x+1, y) == '=') {
                        int var = Asset.RANDOM.nextInt(getNumberOfMapVariations("doorR"));
                        sector.addTiles("doorR" + var, false, false);
                    }
                    if(getChar(x-1, y) == '|' || getChar(x-1, y) == '=') {
                        int var = Asset.RANDOM.nextInt(getNumberOfMapVariations("doorR"));
                        sector.addTiles("doorR" + var, true, false);
                    }
                }
                sector.setOpenness();
            }
    }

    private void generateCharacterMap() {
        //make each sector a dead end (denoted by an index of %)
        for(int y=0; y<height; y++)
            Arrays.fill(charMap[y], '%');

//        printCharacterMap();

        //create a random number of shafts (between 3 & 6) 
        int numShafts = Asset.RANDOM.nextInt(4) + 3;
        int[][] shafts = new int[numShafts][3];
        int minSize = 4, maxSize = 12;
        for(int n=0; n<numShafts; n++) {
            y1 = Asset.RANDOM.nextInt(height - 2) + 1;  //the initial y-value of the shaft
            y2 = 0;
            while(y2 == 0 || Math.abs(y1 - y2) < minSize || Math.abs(y1 - y2) > maxSize)
                y2 = Asset.RANDOM.nextInt(height - 2) + 1; //choose a second y-value that meets the criteria

            //now choose an x-value for the shaft, so that shafts are nicely spaced apart.
            boolean keepTrying = true;
            int oflow = 0;
            while(keepTrying && oflow < OVERFLOW_LIMIT) {
                keepTrying = false;
                x1 = Asset.RANDOM.nextInt(width - 2) + 1;
                for (int m=0; m<n; m++) {
                    int dist = Math.abs(x1 - shafts[m][0]);
                    if(dist < minSize || dist > maxSize) {
                        keepTrying = true;
                    }
                }
                oflow++;
            }
            createShaft(x1, y1, y2);
            shafts[n][0] = x1; //first number stored is the shaft x-value
            shafts[n][1] = Math.min(y1, y2); //then the shaft's smallest y-value
            shafts[n][2] = Math.max(y1, y2); //last the shaft's largest y-value
        }
//        printCharacterMap();

        //now that the shafts are made, lets make some halls
        //first let's make some halls that's connect each shaft
        for(int n=1; n<numShafts; n++) {
            x1 = shafts[n-1][0]; //the previous shaft's x-value
            x2 = shafts[n][0];  //the current shaft's x-value
            int maxY1 = Math.max(shafts[n - 1][1], shafts[n][1]);
            int minY2 = Math.min(shafts[n - 1][2], shafts[n][2]);
            if(minY2 == maxY1) y1 = minY2;
            else y1 = Asset.RANDOM.nextInt(Math.abs(minY2 - maxY1)) + maxY1;
            if(minY2 < maxY1) {
                createShaft(x1, y1, shafts[n-1][2]);
                createShaft(x2, y1, shafts[n][2]);
            }
            createHall(x1, x2, y1);
        }
//        printCharacterMap();
        //now from each shaft we'll make some more halls at Asset.RANDOM
        while(sectorCount < maxSectors) {
            int n = Asset.RANDOM.nextInt(numShafts);
            x1 = shafts[n][0];
            x2 = 0;
            while(x2 == 0 || Math.abs(x1 - x2) < minSize || Math.abs(x1 - x2) > maxSize)
                x2 = Asset.RANDOM.nextInt(width - 2) + 1;
            y1 = Asset.RANDOM.nextInt(shafts[n][2] - shafts[n][1]) + shafts[n][1];
            createHall(x1, x2, y1);
        }

//        printCharacterMap();

        //Add special rooms to each world, such as item pick ups and the like.
        //Main items include Generic Items, Compression Orb, Bombs, Wall Jump, Grapple Shot, Space Jump, Mach Boots, Phase Shot
        //Optional item rooms, generic items, and missile/O2/Health upgrades can be added to any world.
        //Optional items include: Long Shot, Speed boots, Charge Missile, Charge Shot, Wide Shot, Charge Bomb, Seeking Missile,
        //Razor Jump.        
        Array<Character> special = new Array<Character>();
        char[] main_items = {'I', 'C', 'B', 'W', 'G', 'J', 'T', 'P'};
        char[] opt_items = {'O', 'M', 'A'};
        char[] other = {'$'};
        special.addAll('S');
        int num_main_items = 1;
        int num_opt_items = Asset.RANDOM.nextInt(3) + 1;
        float prob_other = 1.0f;

        //adds main items to the world by choosing at random
        int i=0;
        while(i < num_main_items) {
            char choice = main_items[Asset.RANDOM.nextInt(main_items.length)];
            if(special.contains(choice, false)) continue; //this avoids duplicates
            special.add(choice);
            i++;
        }

        //adds optional items to the world by choosing randomly, allows duplicates
        i=0;
        while(i<num_opt_items) {
            special.add(opt_items[Asset.RANDOM.nextInt(opt_items.length)]);
            i++;
        }

        //if the probability for a non-item special sector is met, add one at random
        if(Asset.RANDOM.nextFloat() < prob_other)
            special.add(other[Asset.RANDOM.nextInt(other.length)]);

        int oflow = 0; //this overflow variable helps prevent searching for too long
        while(special.size > 0) {
            y1 = Asset.RANDOM.nextInt(height - 2) + 1;
            x1 = Asset.RANDOM.nextInt(width - 2) + 1;
            char c = special.peek();
            if(oflow > OVERFLOW_LIMIT) {
                System.out.println(c);
//                printCharacterMap();
                //if we have overstayed our welcome (not enough sectors to addToInventory specials)
                //then we make a new hall on an existing shaft
                int n = Asset.RANDOM.nextInt(numShafts);
                int y = Asset.RANDOM.nextInt(shafts[n][2] - shafts[n][1]) + shafts[n][1];
                x1 = shafts[n][0];
                x2 = 0;
                int over = 0; //this is a second overflow variable
                while(x2 == 0 || Math.abs(x1 - x2) < minSize || Math.abs(x1 - x2) > maxSize) {
                    x2 = Asset.RANDOM.nextInt(width - 2) + 1;
                    if(over > OVERFLOW_LIMIT) {
                        x1 = shafts[Asset.RANDOM.nextInt(numShafts)][0];
                        over = 0;
                    }
                    over++;
                }
                createHall(x1, x2, y);
                oflow = 0;

            } else if(getChar(x1, y1) == '=' && c != 'I' && c != '$' && c != 'S') {
                if(getChar(x1, y1+1) != '|' && getChar(x1, y1-1) != '|' && getChar(x1+1, y1) != '|' && getChar(x1-1, y1) != '|') {
                    charMap[y1][x1] = special.pop();
                }
            } else if(getChar(x1, y1) == '%' && (getChar(x1-1, y1) == '=' || getChar(x1+1, y1) == '=')) {
                if(c == 'S') charMap[y1][x1] = special.pop();
            } else if(getChar(x1, y1) == '%' && (getChar(x1+1, y1) == '=' ^ getChar(x1-1, y1) == '=' ^ getChar(x1+1, y1) == '|' ^ getChar(x1-1, y1) == '|')) {
                if(c == 'I') charMap[y1][x1] = special.pop();
                else if(c == '$') charMap[y1][x1] = special.pop();
            } else
                oflow++;  //every time a search fails oflow is increased by one
        }
    }
    private void createHall(int xi, int xt, int y) {
        //Dig the hall out of dead space
        int minX = Math.min(xi, xt);
        int maxX = Math.max(xi, xt);
        for(int x=minX; x<=maxX; x++) {
            if(getChar(x, y) == '%') {
                charMap[y][x] = '=';
                sectorCount++;
            }
        }
    }

    private void createShaft(int x, int yi, int yt) {
        //Dig the shaft out of dead space
        int minY = Math.min(yi, yt);
        int maxY = Math.max(yi, yt);
        for(int y=minY; y<=maxY; y++) {
            if (getChar(x, y) == '%') {
                charMap[y][x] = '|';
                sectorCount++;
            }
        }
    }

    private static Sector generateSector(int sectorX, int sectorY, int iterations, int radius, boolean shaft, Algorithm alg) {
        //let's make some noise!
        switch (alg) {
            case RANDOM:
                for(int y = 0; y < Sector.HEIGHT; y++)
                    for(int x = 0; x < Sector.WIDTH; x++)
                        noise[y][x] = Asset.RANDOM.nextInt(9) - 5;

                //for loop for each iteration of smoothing
                for(int i=0; i<iterations; i++)
                    noise = smoothNoise(noise, radius);

                break;
            case TUNNEL:
                for(int y = 0; y < Sector.HEIGHT; y++)
                    for(int x = 0; x < Sector.WIDTH; x++)
                        noise[y][x] = Asset.RANDOM.nextInt(4);

                //for loop for each iteration of smoothing
                for(int i=0; i<iterations; i++)
                    noise = smoothNoise(noise, radius);

                if(shaft) {
                    //we'll choose a Asset.RANDOM x value to start at and size to make the path
                    int size = Asset.RANDOM.nextInt(Sector.WIDTH - 6) + 4;
                    int xi = (Sector.WIDTH - size) / 2;

                    for(int y=0; y<Sector.HEIGHT; y++) {
                        int nextX = Asset.RANDOM.nextInt(size) + xi - size / 2;
                        while(nextX < 1 || nextX > Sector.WIDTH - 1 - size)
                            nextX = Asset.RANDOM.nextInt(size) + xi - size / 2;

                        xi = nextX;
                        for (int x = xi; x < xi + size; x++)
                            noise[y][x] = -1;
                    }
                } else {
                    //we'll choose a Asset.RANDOM y value to start at and size to make the path
                    int size = Asset.RANDOM.nextInt(Sector.HEIGHT - 8) + 6;
                    int yi = (Sector.HEIGHT - size) / 2;

                    for(int x=0; x<Sector.WIDTH; x++) {
                        int nextY = -1;
                        while(nextY < 1 || nextY > Sector.HEIGHT - 1 - size)
                            nextY = Asset.RANDOM.nextInt(size) + yi - size / 2;

                        yi = nextY;
                        for (int y = yi; y < yi + size; y++)
                            noise[y][x] = -1;
                    }
                }
                break;
            case SYMMETRIC:
                for(int y = 0; y < Sector.HEIGHT; y++)
                    for(int x = 0; x < Sector.WIDTH/2; x++) {
                        noise[y][x] = Asset.RANDOM.nextInt(9) - 5;
                        noise[y][Sector.WIDTH - 1 - x] = noise[y][x];
                    }

                //for loop for each iteration of smoothing
                for(int i=0; i<iterations; i++)
                    noise = smoothNoise(noise, radius);

                break;
        }

        if(shaft) {
            for (int y = 0; y < Sector.HEIGHT; y++) {
                if(noise[y][0] < 0)
                    noise[y][0] = 1;
                if(noise[y][Sector.WIDTH - 1] < 0)
                    noise[y][Sector.WIDTH - 1] = 1;
            }
        } else {
            for (int x = 0; x < Sector.WIDTH; x++) {
                if(noise[0][x] < 0)
                    noise[0][x] = 1;
                if(noise[Sector.HEIGHT - 1][x] < 0)
                    noise[Sector.HEIGHT - 1][x] = 1;
            }

        }

        //turns the noise into tile indices
        Sector sector = new Sector(sectorX, sectorY);
        if(shaft) sector.name = "generated_shaft";
        else sector.name = "generated_hall";

        for(int y=0; y<Sector.HEIGHT; y++) {
            for (int x = 0; x < Sector.WIDTH; x++) {
                char index;
                if (noise[y][x] < 0) {
                    sector.setChar(' ', x, y);
                    continue;
                } else {
                    float rand = Asset.RANDOM.nextFloat();
                    if (rand < 0.04) index = '@';
                    else if (rand < 0.10) index ='?';
                    else if (rand < 0.15) index = '#';
                    else index = Integer.toString(Asset.RANDOM.nextInt(3) + 1).charAt(0);
                }
                sector.setChar(index, x, y);
            }
        }

        return sector;
    }

    private static int[][] smoothNoise(int[][] n, int radius) {
        int width = n[0].length;
        int height = n.length;
        int[][] noise = new int[height][width];
        int sum, d;

        //loops to check each integer in the 2d array of numbers
        for(int y=0; y<height; y++)
            for(int x=0; x<width; x++) {
                sum = 0; //sum of all neighboring values
                d = 0; //number of neighboring values
                //loops to find the sum of the value's neighbors
                for (int yi = y - radius; yi <= y + radius; yi++)
                    for (int xi = x - radius; xi <= x + radius; xi++)
                        if (xi > 0 && yi > 0 && xi < width - 1 && yi < height - 1) {
                            sum += n[yi][xi];
                            d++;
                        }

                noise[y][x] = sum / d;  //the new noise value is equal to the average of neighbors
            }

        return noise;
    }

    private char getChar(int x, int y) {
        if(x >= 0 && y >= 0 && x < width && y < height)
            return charMap[y][x];
        else
            return '%';
    }

    public float getProgress() {
        return progress;
    }

    public enum Algorithm {
        RANDOM, SYMMETRIC, TUNNEL
    }

    @SuppressWarnings("unused")
    private void printCharacterMap() {
        for(int y=height-1; y>=0; y--) {
            for (int x = 0; x < width; x++)
                System.out.print(charMap[y][x]);
            System.out.println();
        }
    }
}
