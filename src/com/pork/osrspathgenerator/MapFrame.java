package com.pork.osrspathgenerator;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class MapFrame extends JFrame {

    private JPanel tilePanel;
    private JScrollPane tileTableScrollPane;
    private JTable tileTable;
    private DefaultTableModel tableModel;
    private JButton generatePathButton;
    private JPanel settingsPanel;
    private JComboBox<PathAlgorithm> pathAlgorithmComboBox;
    private JLabel pathAlgorithmLabel;
    private JCheckBox drawUnreachableTilesCheckBox;
    private JCheckBox drawAllTilesCheckBox;
    private JCheckBox showDebugCheckBox;
    private JLabel creatorLabel;
    private MapPanel mapPanel;

    public MapFrame(String title) {
        super(title);
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        }
        catch (Exception e) {}
        initComponents();
    }

    public void setIsCalculating(boolean isCalculating) {
        if (isCalculating) {
            generatePathButton.setEnabled(false);
            generatePathButton.setText("Calculating path...");
        }
        else {
            generatePathButton.setText("Generate Path");
            generatePathButton.setEnabled(true);
        }
    }

    public void updateTileTable() {
        ArrayList<Tile> tiles = mapPanel.getPathTiles();
        for (int i=tableModel.getRowCount() - 1; i>=0; i--) {
            tableModel.removeRow(i);
        }
        for (int i=0; i<tiles.size(); i++) {
            Tile tile = tiles.get(i);
            tableModel.addRow(new Object[]{i + 1, tile.getX(), tile.getY()});
        }
        generatePathButton.setEnabled(tiles.size() > 0);
    }

    private void initComponents() {

        tilePanel = new JPanel();
        tileTableScrollPane = new JScrollPane();
        tileTable = new JTable();
        generatePathButton = new JButton();
        settingsPanel = new JPanel();
        pathAlgorithmLabel = new javax.swing.JLabel();
        pathAlgorithmComboBox = new javax.swing.JComboBox<PathAlgorithm>();
        drawUnreachableTilesCheckBox = new JCheckBox();
        drawAllTilesCheckBox = new JCheckBox();
        showDebugCheckBox = new JCheckBox();
        creatorLabel = new JLabel();
        mapPanel = new MapPanel(this);

        tableModel = new DefaultTableModel() {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        tilePanel.setBorder(BorderFactory.createTitledBorder(null, "Tiles", 0, 0, new Font("Verdana", 0, 12)));

        tableModel.addColumn("#");
        tableModel.addColumn("x");
        tableModel.addColumn("y");

        tileTable.setFont(new Font("Verdana", 0, 12));
        tileTable.setModel(tableModel);
        tileTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        tileTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                try {
                    ArrayList<Tile> pathTiles = mapPanel.getPathTiles();
                    ArrayList<Tile> selectedPathTiles = new ArrayList<Tile>();
                    for (int i : tileTable.getSelectedRows()) {
                        selectedPathTiles.add(pathTiles.get(i));
                    }
                    mapPanel.setSelectedPathTiles(selectedPathTiles);
                }
                catch (Exception ex) {}
            }

        });

        InputMap inputMap = tileTable.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = tileTable.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
        actionMap.put("delete", new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                int[] selectedRows = tileTable.getSelectedRows();
                ArrayList<Tile> tiles = mapPanel.getPathTiles();
                for (int i = selectedRows.length - 1; i >= 0; i--) {
                    tiles.remove(selectedRows[i]);
                }
                mapPanel.setPathTiles(tiles);
                updateTileTable();
            }

        });

        tileTableScrollPane.setViewportView(tileTable);

        generatePathButton.setEnabled(false);
        generatePathButton.setFont(new Font("Verdana", 0, 12));
        generatePathButton.setText("Generate Path");
        generatePathButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<Tile> pathTiles = mapPanel.getPathTiles();
                String pathCode = "Tile[] path = {";
                for (int i=0; i<pathTiles.size(); i++) {
                    Tile tile = pathTiles.get(i);
                    pathCode += "new Tile(" + tile.getX() + ", " + tile.getY() + ")" + (i == pathTiles.size() - 1 ? "};" : ", ");
                }
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(pathCode), null);
                JOptionPane.showMessageDialog(null, "The path code has been copied to your clipboard.", "Old School RuneScape Path Generator", JOptionPane.INFORMATION_MESSAGE);
            }
            
        });

        javax.swing.GroupLayout tilePanelLayout = new javax.swing.GroupLayout(tilePanel);
        tilePanel.setLayout(tilePanelLayout);
        tilePanelLayout.setHorizontalGroup(
                tilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(tilePanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(tilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(tileTableScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                        .addComponent(generatePathButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap())
        );
        tilePanelLayout.setVerticalGroup(
                tilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(tilePanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(tileTableScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(generatePathButton, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
        );

        settingsPanel.setBorder(BorderFactory.createTitledBorder(null, "Settings", 0, 0, new Font("Verdana", 0, 12)));

        pathAlgorithmLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        pathAlgorithmLabel.setText("Path algorithm");

        pathAlgorithmComboBox.setFont(new java.awt.Font("Verdana", 0, 12));
        pathAlgorithmComboBox.setModel(new DefaultComboBoxModel<PathAlgorithm>(PathAlgorithm.values()));
        pathAlgorithmComboBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                mapPanel.setPathAlgorithm((PathAlgorithm)pathAlgorithmComboBox.getSelectedItem());
                if (pathAlgorithmComboBox.getSelectedIndex() > 0) {
                    JOptionPane.showMessageDialog(null, "Note: path finding was implemented mainly for fun and learning purposes. It is fairly slow and buggy.", "Old School RuneScape Path Generator", JOptionPane.INFORMATION_MESSAGE);
                }
                updateTileTable();
            }

        });

        drawUnreachableTilesCheckBox.setFont(new Font("Verdana", 0, 12));
        drawUnreachableTilesCheckBox.setText("Draw unreachable tiles");
        drawUnreachableTilesCheckBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                mapPanel.setDrawUnreachableTiles(drawUnreachableTilesCheckBox.isSelected());
            }

        });

        drawAllTilesCheckBox.setFont(new Font("Verdana", 0, 12));
        drawAllTilesCheckBox.setText("Draw all tiles");
        drawAllTilesCheckBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                mapPanel.setDrawAllTiles(drawAllTilesCheckBox.isSelected());
            }
            
        });

        showDebugCheckBox.setFont(new Font("Verdana", 0, 12));
        showDebugCheckBox.setText("Show debug");
        showDebugCheckBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                mapPanel.setShowDebug(showDebugCheckBox.isSelected());
            }

        });

        javax.swing.GroupLayout settingsPanelLayout = new javax.swing.GroupLayout(settingsPanel);
        settingsPanel.setLayout(settingsPanelLayout);
        settingsPanelLayout.setHorizontalGroup(
                settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(settingsPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(settingsPanelLayout.createSequentialGroup()
                                                .addComponent(pathAlgorithmLabel)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(pathAlgorithmComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addComponent(showDebugCheckBox, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(drawUnreachableTilesCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(drawAllTilesCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap())
        );
        settingsPanelLayout.setVerticalGroup(
                settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(settingsPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(pathAlgorithmLabel)
                                        .addComponent(pathAlgorithmComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(drawUnreachableTilesCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(drawAllTilesCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(showDebugCheckBox)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        creatorLabel.setFont(new java.awt.Font("Verdana", 0, 12));
        creatorLabel.setForeground(new java.awt.Color(153, 153, 153));
        creatorLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        creatorLabel.setText("Created by Pork");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(settingsPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(tilePanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(creatorLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(mapPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(tilePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(settingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(creatorLabel))
                                        .addComponent(mapPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }

}
