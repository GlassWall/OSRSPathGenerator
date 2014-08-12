package com.pork.osrspathgenerator;

public enum PathAlgorithm {

    NONE("None"),
    A_STAR("A*");

    private String name;

    PathAlgorithm(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

}
