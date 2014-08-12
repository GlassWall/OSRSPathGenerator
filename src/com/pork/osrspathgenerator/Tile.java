package com.pork.osrspathgenerator;

public class Tile {

    private int x;
    private int y;
    private boolean isWalkable;

    private int hValue;
    private int gValue;
    private Tile parentTile;

    public Tile(int x, int y) {
        this.x = x;
        this.y = y;
        isWalkable = true;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isWalkable() {
        return isWalkable;
    }

    public int getHValue() {
        return hValue;
    }

    public int getGValue() {
        return gValue;
    }

    public int getFValue() {
        return gValue + hValue;
    }

    public Tile getParentTile() {
        return parentTile;
    }

    public void setWalkable(boolean isWalkable) {
        this.isWalkable = isWalkable;
    }

    public void setHValue(int hValue) {
        this.hValue = hValue;
    }

    public void setGValue(int gValue) {
        this.gValue = gValue;
    }

    public void setParentTile(Tile parentTile) {
        this.parentTile = parentTile;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Tile) {
            Tile tile = (Tile)object;
            return x == tile.getX() && y == tile.getY() && isWalkable == tile.isWalkable();
        }
        return false;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

}
