package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class MainUI extends JFrame {
    private static final String MINE = "*";
    private static final String FLAG = "F";
    private JButton[][] buttons;
    private boolean[][] mines;
    private int[][] counts;
    private int size;
    private int numMines;
    private int remainingCells;
    private boolean gameOver;
    private JPanel gamePanel;
    private JLabel statusLabel;
    private JLabel mineCountLabel;
    private JLabel timerLabel;
    private JToggleButton flagButton;
    private Timer gameTimer;
    private int seconds;
    private boolean gameStarted;

    public MainUI() {
        setTitle("Minesweeper");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        seconds = 0;
        gameTimer = new Timer(1000, e -> {
            seconds++;
            updateTimer();
        });

        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game");
        JMenuItem newGame10 = new JMenuItem("New Game (10x10 - 10 mines)");
        JMenuItem newGame15 = new JMenuItem("New Game (15x15 - 20 mines)");

        newGame10.addActionListener(e -> initializeGame(10, 10));
        newGame15.addActionListener(e -> initializeGame(15, 20));

        gameMenu.add(newGame10);
        gameMenu.add(newGame15);
        menuBar.add(gameMenu);
        setJMenuBar(menuBar);

        JPanel topPanel = new JPanel(new BorderLayout());

        JPanel controlPanel = new JPanel(new FlowLayout());
        flagButton = new JToggleButton("Flag Mode (F)");
        flagButton.setMnemonic(KeyEvent.VK_F);
        controlPanel.add(flagButton);

        JPanel infoPanel = new JPanel(new GridLayout(1, 3));
        statusLabel = new JLabel("Click to start");
        mineCountLabel = new JLabel("Mines: 0");
        timerLabel = new JLabel("Time: 0:00");

        Font labelFont = new Font("Arial", Font.BOLD, 14);
        statusLabel.setFont(labelFont);
        mineCountLabel.setFont(labelFont);
        timerLabel.setFont(labelFont);

        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mineCountLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);

        infoPanel.add(mineCountLabel);
        infoPanel.add(statusLabel);
        infoPanel.add(timerLabel);

        topPanel.add(controlPanel, BorderLayout.NORTH);
        topPanel.add(infoPanel, BorderLayout.SOUTH);

        gamePanel = new JPanel();
        add(topPanel, BorderLayout.NORTH);
        add(gamePanel, BorderLayout.CENTER);

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke('F'), "toggleFlag");
        getRootPane().getActionMap().put("toggleFlag", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                flagButton.setSelected(!flagButton.isSelected());
            }
        });

        initializeGame(10, 10);
        pack();
        setLocationRelativeTo(null);
    }

    private void updateTimer() {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        timerLabel.setText(String.format("Time: %d:%02d", minutes, secs));
    }

    private void initializeGame(int size, int numMines) {
        this.size = size;
        this.numMines = numMines;
        this.remainingCells = size * size - numMines;
        this.gameOver = false;
        this.gameStarted = false;

        gameTimer.stop();
        seconds = 0;
        updateTimer();

        gamePanel.removeAll();
        gamePanel.setLayout(new GridLayout(size, size));

        buttons = new JButton[size][size];
        mines = new boolean[size][size];
        counts = new int[size][size];

        mineCountLabel.setText("Mines: " + numMines);
        statusLabel.setText("Click to start");
        flagButton.setSelected(false);

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                buttons[i][j] = new JButton();
                buttons[i][j].setPreferredSize(new Dimension(35, 35));
                buttons[i][j].setMargin(new Insets(0, 0, 0, 0));
                final int row = i;
                final int col = j;

                buttons[i][j].addActionListener(e -> {
                    if (!gameStarted && !flagButton.isSelected()) {
                        startGame();
                    }
                    if (!gameOver) {
                        if (flagButton.isSelected()) {
                            handleFlag(row, col);
                        } else {
                            handleClick(row, col);
                        }
                    }
                });

                gamePanel.add(buttons[i][j]);
            }
        }

        Random random = new Random();
        int minesPlaced = 0;
        while (minesPlaced < numMines) {
            int row = random.nextInt(size);
            int col = random.nextInt(size);
            if (!mines[row][col]) {
                mines[row][col] = true;
                minesPlaced++;
            }
        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                counts[i][j] = countAdjacentMines(i, j);
            }
        }

        pack();
        updateMineCount();
    }

    private void startGame() {
        gameStarted = true;
        gameTimer.start();
        statusLabel.setText("Game in progress");
    }

    private void handleClick(int row, int col) {
        if (buttons[row][col].isEnabled() && !buttons[row][col].getText().equals(FLAG)) {
            if (mines[row][col]) {
                gameOver = true;
                gameTimer.stop();
                revealAllMines();
                statusLabel.setText("Game Over!");
            } else {
                reveal(row, col);
                if (remainingCells == 0) {
                    gameOver = true;
                    gameTimer.stop();
                    statusLabel.setText("You Win!");
                    revealAllMines();
                }
            }
        }
    }

    private void handleFlag(int row, int col) {
        if (buttons[row][col].isEnabled()) {
            if (buttons[row][col].getText().equals(FLAG)) {
                buttons[row][col].setText("");
            } else {
                buttons[row][col].setText(FLAG);
            }
            updateMineCount();
        }
    }

    private void updateMineCount() {
        int flagCount = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (buttons[i][j].getText().equals(FLAG)) {
                    flagCount++;
                }
            }
        }
        mineCountLabel.setText("Mines: " + (numMines - flagCount));
    }

    private void reveal(int row, int col) {
        if (!buttons[row][col].isEnabled() || buttons[row][col].getText().equals(FLAG)) {
            return;
        }

        buttons[row][col].setEnabled(false);
        remainingCells--;

        if (counts[row][col] > 0) {
            buttons[row][col].setText(String.valueOf(counts[row][col]));
            switch (counts[row][col]) {
                case 1: buttons[row][col].setForeground(Color.BLUE); break;
                case 2: buttons[row][col].setForeground(new Color(0, 128, 0)); break;
                case 3: buttons[row][col].setForeground(Color.RED); break;
                case 4: buttons[row][col].setForeground(new Color(0, 0, 128)); break;
                case 5: buttons[row][col].setForeground(new Color(128, 0, 0)); break;
                default: buttons[row][col].setForeground(Color.BLACK); break;
            }
        } else {
            buttons[row][col].setText("");
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    int newRow = row + i;
                    int newCol = col + j;
                    if (newRow >= 0 && newRow < size && newCol >= 0 && newCol < size) {
                        reveal(newRow, newCol);
                    }
                }
            }
        }
    }

    private void revealAllMines() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (mines[i][j]) {
                    if (!buttons[i][j].getText().equals(FLAG)) {
                        buttons[i][j].setText(MINE);
                        buttons[i][j].setBackground(Color.RED);
                    } else {
                        buttons[i][j].setBackground(Color.GREEN);
                    }
                } else if (buttons[i][j].getText().equals(FLAG)) {
                    buttons[i][j].setBackground(Color.ORANGE);
                }
                buttons[i][j].setEnabled(false);
            }
        }
    }

    private int countAdjacentMines(int row, int col) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int newRow = row + i;
                int newCol = col + j;
                if (newRow >= 0 && newRow < size && newCol >= 0 && newCol < size && mines[newRow][newCol]) {
                    count++;
                }
            }
        }
        return count;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainUI().setVisible(true);
        });
    }
}