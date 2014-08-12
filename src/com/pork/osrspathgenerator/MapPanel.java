package com.pork.osrspathgenerator;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MapPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {

    private final int PREFERRED_PANEL_WIDTH = 800;
    private final int PREFERRED_PANEL_HEIGHT = 500;
    private final int PREFERRED_OFFSET_X = -500;
    private final int PREFERRED_OFFSET_Y = -500;
    private final double PREFERRED_ZOOM_LEVEL = 0.5;
    private final int PREFERRED_PATH_TILE_INTERVAL = 20;

    private final String MAP_IMAGE_PATH = "/resources/map.jpg";
    private final String UNREACHABLE_TILE_DATA_PATH = "/resources/unreachable_tiles.dat";
    private final double TILE_SIZE = 2;
    private final int TILE_X_MODIFIER = 1984;
    private final int TILE_Y_MODIFIER = 639;

    private MapFrame mapFrame;
    private BufferedImage map;
    private int mapWidth;
    private int mapHeight;
    private int offsetX;
    private int offsetY;
    private double zoomLevel;
    private int pathTileInterval;
    private Point mousePoint;
    private Tile hoveredTile;
    private PathAlgorithm pathAlgorithm;
    private boolean calculatingPath;
    private boolean isControlDown;
    private boolean mouseIsInPanel;
    private boolean drawUnreachableTiles;
    private boolean drawAllTiles;
    private boolean showDebug;
    private Tile[][] unmodifiedTiles;
    private ArrayList<Tile> pathTiles;
    private ArrayList<Tile> selectedPathTiles;
    private ArrayList<Tile> unreachableTiles;

    public MapPanel(MapFrame mapFrame) {
        try {
            this.mapFrame = mapFrame;
            map = ImageIO.read(getClass().getResource(MAP_IMAGE_PATH));
            mapWidth = map.getWidth();
            mapHeight = map.getHeight();
            offsetX = PREFERRED_OFFSET_X;
            offsetY = PREFERRED_OFFSET_Y;
            zoomLevel = PREFERRED_ZOOM_LEVEL;
            pathTileInterval = PREFERRED_PATH_TILE_INTERVAL;
            mousePoint = new Point(0, 0);
            hoveredTile = new Tile(0, 0);
            pathAlgorithm = null;
            calculatingPath = false;
            isControlDown = false;
            mouseIsInPanel = false;
            drawUnreachableTiles = false;
            drawAllTiles = false;
            showDebug = false;
            unmodifiedTiles = new Tile[(int)(mapWidth / TILE_SIZE)][(int)(mapHeight / TILE_SIZE)];
            pathTiles = new ArrayList<Tile>();
            selectedPathTiles = new ArrayList<Tile>();
            unreachableTiles = new ArrayList<Tile>();

            for (int i=0; i<unmodifiedTiles.length; i++) {
                for (int j=0; j<unmodifiedTiles[i].length; j++) {
                    unmodifiedTiles[i][j] = new Tile(i, j);
                }
            }

            BufferedReader tileFileReader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(UNREACHABLE_TILE_DATA_PATH)));
            String line;
            while ((line = tileFileReader.readLine()) != null) {
                String[] tileCoordinates = line.split(",");
                Tile modifiedTile = new Tile(Integer.parseInt(tileCoordinates[0]), Integer.parseInt(tileCoordinates[1]));
                Tile unmodifiedTile = removeModifiers(modifiedTile);
                unreachableTiles.add(modifiedTile);
                unmodifiedTiles[unmodifiedTile.getX()][unmodifiedTile.getY()].setWalkable(false);
            }
            tileFileReader.close();

            setPreferredSize(new Dimension(PREFERRED_PANEL_WIDTH, PREFERRED_PANEL_HEIGHT));
            addMouseListener(this);
            addMouseMotionListener(this);
            addMouseWheelListener(this);
            addKeyListener(this);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Tile> getPathTiles() {
        return pathTiles;
    }

    public void calculatePath() {
        if (!calculatingPath) {
            new Thread(new PathCalculator()).start();
        }
    }

    public void setPathTiles(ArrayList<Tile> pathTiles) {
        this.pathTiles = pathTiles;
        repaint();
    }

    public void setSelectedPathTiles(ArrayList<Tile> selectedPathTiles) {
        this.selectedPathTiles = selectedPathTiles;
        repaint();
    }

    public void setPathAlgorithm(PathAlgorithm pathAlgorithm) {
        this.pathAlgorithm = pathAlgorithm;
        pathTiles.clear();
        repaint();
    }

    public void setDrawUnreachableTiles(boolean drawUnreachableTiles) {
        this.drawUnreachableTiles = drawUnreachableTiles;
        repaint();
    }

    public void setDrawAllTiles(boolean drawAllTiles) {
        this.drawAllTiles = drawAllTiles;
        repaint();
    }

    public void setShowDebug(boolean showDebug) {
        this.showDebug = showDebug;
        repaint();
    }

    public double getTileSize() {
        return TILE_SIZE * zoomLevel;
    }

    public Tile removeModifiers(Tile tile) {
        return new Tile(tile.getX() - TILE_X_MODIFIER, mapHeight - tile.getY() + TILE_Y_MODIFIER);
    }

    public Tile addModifiers(Tile tile) {
        return new Tile(tile.getX() + TILE_X_MODIFIER, mapHeight + TILE_Y_MODIFIER - tile.getY());
    }

    public int distance(Tile startTile, Tile endTile) {
        int distanceX = Math.abs(endTile.getX() - startTile.getX());
        int distanceY = Math.abs(endTile.getY() - startTile.getY());
        int sum = distanceX + distanceY;
        return distanceX == distanceY ? sum / 2 : sum;
    }

    public Point tileToScreenPoint(Tile tile) {
        double tileSize = getTileSize();
        return new Point((int)((tile.getX() - TILE_X_MODIFIER) * tileSize) + offsetX, (int)((mapHeight - (tile.getY() - TILE_Y_MODIFIER)) * tileSize) + offsetY);
    }

    public Tile screenPointToTile(Point point) {
        double tileSize = getTileSize();
        return new Tile((int)((point.getX() - offsetX) / tileSize) + TILE_X_MODIFIER, mapHeight - (int)((point.getY() - offsetY) / tileSize) + TILE_Y_MODIFIER);
    }

    @Override
    public void paint(Graphics g) {
        long paintStartTime = System.nanoTime();
        int width = getWidth();
        int height = getHeight();
        double tileSize = getTileSize();
        int mouseX = mousePoint.x;
        int mouseY = mousePoint.y;
        int hoveredTileX = hoveredTile.getX();
        int hoveredTileY = hoveredTile.getY();

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);
        g.drawImage(map, offsetX, offsetY, (int)(mapWidth * zoomLevel), (int)(mapHeight * zoomLevel), null);
        g.setColor(Color.WHITE);

        if (drawUnreachableTiles) {
            g.setColor(new Color(255, 0, 0, 150));
            for (Tile tile : unreachableTiles) {
                Point tilePoint = tileToScreenPoint(tile);
                if (tilePoint.x >= -tileSize && tilePoint.x <= width && tilePoint.y >= -tileSize && tilePoint.y <= height) {
                    g.fillRect(tilePoint.x, tilePoint.y, (int)tileSize, (int)tileSize);
                }
            }
        }

        if (drawAllTiles) {
            g.setColor(new Color(255, 255, 255, 40));
            for (double i = 0; i < Math.min(width - offsetX, mapWidth * zoomLevel); i += tileSize) {
                g.drawLine((int)i + offsetX, offsetY, (int)i + offsetX, Math.min(height, offsetY + (int)(mapHeight * zoomLevel)));
            }
            for (double i = 0; i < Math.min(height - offsetY, mapHeight * zoomLevel); i += tileSize) {
                g.drawLine(offsetX, (int)i + offsetY, Math.min(width, offsetX + (int)(mapWidth * zoomLevel)), (int)i + offsetY);
            }
        }

        for (int i=0; i<pathTiles.size(); i++) {
            Tile tile = pathTiles.get(i);
            Point tilePoint = tileToScreenPoint(tile);

            g.setColor(new Color(0, 255, 255));

            if (!calculatingPath && i < pathTiles.size() - 1) {
                Point nextTilePoint = tileToScreenPoint(pathTiles.get(i + 1));
                g.drawLine((tilePoint.x + (int)tileSize / 2), (tilePoint.y + (int)tileSize / 2), (nextTilePoint.x + (int)tileSize / 2), (nextTilePoint.y + (int)tileSize / 2));
            }

            boolean found = false;
            for (Tile selectedTile : selectedPathTiles) {
                if (tile.getX() == selectedTile.getX() && tile.getY() == selectedTile.getY()) {
                    found = true;
                    break;
                }
            }
            if (found) {
                g.setColor(Color.GREEN);
            }

            g.fillRect(tilePoint.x, tilePoint.y, (int)tileSize, (int)tileSize);

        }

        g.setColor(Color.WHITE);

        if (mouseIsInPanel) {
            Point hoveredTilePoint = tileToScreenPoint(hoveredTile);
            g.drawRect(hoveredTilePoint.x, hoveredTilePoint.y, (int)tileSize, (int)tileSize);
            g.drawString("Tile: " + hoveredTileX + ", " + hoveredTileY, 5, 15);
        }

        if (showDebug) {
            Tile unmodifiedTile = removeModifiers(hoveredTile);
            g.drawString("Unmodified tile: " + unmodifiedTile.getX() + ", " + unmodifiedTile.getY(), 5, height - 70);
            g.drawString("Mouse: " + mouseX + ", " + mouseY, 5, height - 55);
            g.drawString("Map offset: " + offsetX + ", " + offsetY, 5, height - 40);
            g.drawString("Zoom level: " + zoomLevel, 5, height - 25);
            g.drawString("Last refresh rate: " + (System.nanoTime() - paintStartTime) + "ns", 5, height - 10);
        }

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int hoveredTileX = hoveredTile.getX();
        int hoveredTileY = hoveredTile.getY();
        if (e.getButton() == 1 && !calculatingPath) {
            boolean found = false;
            for (Tile tile : pathTiles) {
                if (tile.getX() == hoveredTileX && tile.getY() == hoveredTileY) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                pathTiles.add(new Tile(hoveredTileX, hoveredTileY));
            }
        }
        else if (e.getButton() == 3) {
            for (Tile tile : pathTiles) {
                if (tile.getX() == hoveredTileX && tile.getY() == hoveredTileY) {
                    pathTiles.remove(tile);
                    break;
                }
            }
        }
        mapFrame.updateTileTable();
        calculatePath();
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        requestFocus();
        mouseIsInPanel = true;
        repaint();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        mouseIsInPanel = false;
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        Tile mouseTilePoint = screenPointToTile(e.getPoint());
        hoveredTile = new Tile(mouseTilePoint.getX(), mouseTilePoint.getY());
        if (isControlDown) {
            Tile lastTileAdded = pathTiles.isEmpty() ? new Tile(0, 0) : pathTiles.get(pathTiles.size() - 1);
            if (Math.abs(hoveredTile.getX() - lastTileAdded.getX()) >= pathTileInterval || Math.abs(hoveredTile.getY() - lastTileAdded.getY()) >= pathTileInterval) {
                pathTiles.add(new Tile(hoveredTile.getX(), hoveredTile.getY()));
                mapFrame.updateTileTable();
            }
        }
        else {
            setCursor(new Cursor(Cursor.MOVE_CURSOR));
            offsetX -= mousePoint.x - e.getX();
            offsetY -= mousePoint.y - e.getY();
        }
        mousePoint.setLocation(e.getX(), e.getY());
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Tile mouseTilePoint = screenPointToTile(e.getPoint());
        hoveredTile = new Tile(mouseTilePoint.getX(), mouseTilePoint.getY());
        mousePoint.setLocation(e.getX(), e.getY());
        repaint();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int mouseX = mousePoint.x;
        int mouseY = mousePoint.y;
        double zoomMultiplier = e.getUnitsToScroll() <= 0 ? 1.1 : 1 / 1.1;
        zoomLevel *= zoomMultiplier;
        offsetX = (int)((offsetX * zoomMultiplier) - ((mouseX * zoomMultiplier) - mouseX));
        offsetY = (int)((offsetY * zoomMultiplier) - ((mouseY * zoomMultiplier) - mouseY));
        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
            isControlDown = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
            isControlDown = false;
        }
    }

    private class PathCalculator implements Runnable {

        @Override
        public void run() {
            calculatingPath = true;
            mapFrame.setIsCalculating(true);
            ArrayList<Tile> allCalculatedPaths = new ArrayList<Tile>();
            if (pathAlgorithm == PathAlgorithm.A_STAR) {
                if (pathTiles.size() >= 2) {
                    for (int i=0; i<pathTiles.size() - 1; i++) {
                        Tile startTileUnmodified = pathTiles.get(i);
                        Tile endTileUnmodified = pathTiles.get(i + 1);
                        Tile startTile = removeModifiers(startTileUnmodified);
                        Tile endTile = removeModifiers(endTileUnmodified);
                        if (distance(startTile, endTile) > 1) {
                            ArrayList<Tile> calculatedPath = new ArrayList<Tile>();
                            ArrayList<Tile> openList = new ArrayList<Tile>();
                            ArrayList<Tile> closedList = new ArrayList<Tile>();
                            Tile currentTile = startTile;
                            openList.add(currentTile);
                            while (!currentTile.equals(endTile) && !openList.isEmpty()) {
                                closedList.add(currentTile);
                                openList.remove(currentTile);
                                for (int x=-1; x<=1; x++) {
                                    for (int y=-1; y<=1; y++) {
                                        int indexX = currentTile.getX() + x;
                                        int indexY = currentTile.getY() + y;
                                        Tile tile = unmodifiedTiles[indexX][indexY];
                                        if (!unreachableTiles.contains(addModifiers(tile)) && !closedList.contains(tile) && indexX >= 0 && indexX<= unmodifiedTiles.length && indexY >= 0 && indexY <= unmodifiedTiles[indexX].length) {
                                            if (openList.contains(tile)) {
                                                if (currentTile.getGValue() < tile.getGValue()) {
                                                    tile.setParentTile(currentTile);
                                                }
                                            }
                                            else {
                                                tile.setHValue(Math.abs(endTile.getX() - tile.getX()) + Math.abs(endTile.getY() - tile.getY()));
                                                tile.setGValue(currentTile.getGValue());
                                                tile.setParentTile(currentTile);
                                                openList.add(tile);
                                            }
                                        }
                                    }
                                }
                                currentTile = openList.isEmpty() ? null : openList.get(0);
                                for (Tile tile : openList) {
                                    if (tile.getFValue() < currentTile.getFValue()) {
                                        currentTile = tile;
                                    }
                                }
                            }
                            while (!currentTile.equals(startTile)) {
                                Tile parentTile = currentTile.getParentTile();
                                calculatedPath.add(0, addModifiers(parentTile));
                                currentTile = parentTile;
                            }
                            pathTiles.remove(endTileUnmodified);
                            allCalculatedPaths.addAll(calculatedPath);
                        }
                    }
                }
                pathTiles.addAll(allCalculatedPaths);
            }
            mapFrame.updateTileTable();
            mapFrame.setIsCalculating(false);
            calculatingPath = false;
            repaint();
        }
    }

}
