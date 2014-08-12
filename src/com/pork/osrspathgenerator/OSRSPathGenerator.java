package com.pork.osrspathgenerator;

public class OSRSPathGenerator {

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MapFrame("Old School RuneScape Path Generator").setVisible(true);
            }
        });
    }

}
