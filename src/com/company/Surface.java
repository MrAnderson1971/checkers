package com.company;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Objects;
import java.util.Scanner;

public class Surface extends JPanel implements KeyListener, MouseListener, ActionListener
{

    public static final int WIDTH = 640;
    public static final int HEIGHT = 480;

    public static final int SQUARE_DIM = 50;

    public static final Color LIGHT_BROWN = new Color(181, 101, 29, 255);
    public static final Color DARK_BROWN = new Color(157, 82, 25, 255);

    public static final int[] RESIGN_TEXT_DIM = new int[]{149, 66};

    public static final int x_margin = (WIDTH - 8 * SQUARE_DIM) / 2;
    public static final int y_margin = (HEIGHT - 8 * SQUARE_DIM) / 2;

    private static Game game;
    private Timer timer;

    public Player hasResigned = null;

    public Surface()
    {
        init();
    }

    public void init()
    {
        Player player1 = new Player(null, Main.name);
        Player player2 = new Player(null, "SCP-079");

        game = new Game(player1, player2);

        //player1.setNn(new MCTS((Board) game.getBoard().clone(), player1, player2));
        player2.setNn(new MCTS((Board) game.getBoard().clone(), player2, player1));
        game.start();

        try
        {
            AudioInputStream audio = AudioSystem.getAudioInputStream(new File(Main.path + "music.wav"));
            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex)
        {
            ex.printStackTrace();
        }

        setBackground(Color.blue);
        setFocusable(true);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));

        addKeyListener(this);
        addMouseListener(this);

        timer = new Timer(140, this);
        timer.start();
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        render(g);
    }

    public void render(Graphics graphics)
    {
        Player winner = game.win();
        if ((winner == null || game.getBoard().getTurnsSinceLastCapture() > Game.MAX_TURNS_NO_CAPTURE) && hasResigned == null)
        {
            graphics.setColor(Color.blue);
            graphics.fillRect(0, 0, WIDTH, HEIGHT);

            // Draw light squares.
            graphics.setColor(LIGHT_BROWN);
            for (int x = 0; x < 8; x += 2)
            {
                for (int y = 0; y < 8; y += 2)
                {
                    graphics.fillRect(x * SQUARE_DIM + x_margin, y * SQUARE_DIM + y_margin, SQUARE_DIM, SQUARE_DIM);
                }
            }
            for (int x = 1; x < 8; x += 2)
            {
                for (int y = 1; y < 8; y += 2)
                {
                    graphics.fillRect(x * SQUARE_DIM + x_margin, y * SQUARE_DIM + y_margin, SQUARE_DIM, SQUARE_DIM);
                }
            }

            // Draw dark squares.
            graphics.setColor(DARK_BROWN);
            for (int x = 1; x < 8; x += 2)
            {
                for (int y = 0; y < 8; y += 2)
                {
                    graphics.fillRect(x * SQUARE_DIM + x_margin, y * SQUARE_DIM + y_margin, SQUARE_DIM, SQUARE_DIM);
                }
            }
            for (int x = 0; x < 8; x += 2)
            {
                for (int y = 1; y < 8; y += 2)
                {
                    graphics.fillRect(x * SQUARE_DIM + x_margin, y * SQUARE_DIM + y_margin, SQUARE_DIM, SQUARE_DIM);
                }
            }

            graphics.setColor(Color.black);
            graphics.setFont(new Font("Times New Roman", Font.PLAIN, 12));
            for (int x = 0; x < 8; x++)
            {
                graphics.drawString("" + (7 - x), x * SQUARE_DIM + x_margin, y_margin);
            }
            for (int y = 0; y < 8; y++)
            {
                graphics.drawString("" + (7 - y), x_margin, y * SQUARE_DIM + y_margin);
            }
            graphics.setFont(new Font("Times New Roman", Font.PLAIN, 36));
            graphics.drawString("Resign", 0, 36);

            // Highlight previous moves.
            graphics.setColor(Color.yellow);
            for (int[] move : game.getPreviousMoves())
            {
                graphics.fillRect(x_margin + (7 - move[0]) * SQUARE_DIM, y_margin + (7 - move[1]) * SQUARE_DIM, SQUARE_DIM, SQUARE_DIM);
            }

            // Draw selected spaces.
            int[] ss = game.getBoard().getSelectedSpace();
            Board b = game.getBoard();
            if (ss[0] != Integer.MIN_VALUE)
            {
                if ((b.getBoard()[ss[0]][ss[1]] > 0 && b.getCurrentPlayer() == game.getPlayer(2)) || (b.getBoard()[ss[0]][ss[1]] < 0 && b.getCurrentPlayer() == game.getPlayer(1)))
                {
                    if (!b.getPossibleMovesForPiece(ss).isEmpty())
                    {
                        graphics.setColor(Color.green);
                        graphics.fillRect(ss[0] * SQUARE_DIM + x_margin, ss[1] * SQUARE_DIM + y_margin, SQUARE_DIM, SQUARE_DIM);
                    }
                    b.setSelectedPiece(b.getSelectedSpace().clone());
                    game.setAvailableMoves(b.getPossibleMovesForPiece(b.getSelectedSpace()));
                    graphics.setColor(Color.black);

                    // Highlight available moves.
                    for (int[] move : game.getAvailableMoves())
                    {
                        graphics.fillOval(move[2] * SQUARE_DIM + x_margin + SQUARE_DIM / 2,
                                move[3] * SQUARE_DIM + y_margin + SQUARE_DIM / 2,
                                SQUARE_DIM / 8, SQUARE_DIM / 8);
                    }
                }
            }

            // Draw pieces.
            for (int x = 0; x < 8; x++)
            {
                for (int y = 0; y < 8; y++)
                {
                    if (Math.abs(b.getBoard()[x][y]) >= 1)
                    {
                        graphics.setColor((b.getBoard()[x][y] > 0) ? Color.red : Color.black);

                        graphics.fillOval(x * SQUARE_DIM + x_margin,
                                y * SQUARE_DIM + y_margin,
                                SQUARE_DIM, SQUARE_DIM);

                        // Draw kings.
                        if (Math.abs(b.getBoard()[x][y]) == 2)
                        {
                            graphics.setColor((b.getBoard()[x][y] > 0) ? Color.black : Color.red);
                            graphics.fillOval(x * SQUARE_DIM + x_margin + SQUARE_DIM / 4,
                                    y * SQUARE_DIM + y_margin + SQUARE_DIM / 4,
                                    SQUARE_DIM / 2, SQUARE_DIM / 2);
                        }
                    }
                }
            }

            Toolkit.getDefaultToolkit().sync();
        } else
        {
            System.out.println(Objects.requireNonNullElse(hasResigned, winner) + " won.");
            if (game.getPlayer(2).getNn() != null)
            {
                game.getPlayer(2).parent.export("player2.txt");
            }
            new Scanner(System.in).nextLine();
            System.exit(0);
        }
    }

    @Override
    public void keyTyped(KeyEvent e)
    {

    }

    @Override
    public void keyPressed(KeyEvent e)
    {

    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_ESCAPE)
        {
            game.getBoard().setSelectedSpace(new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE});
        }
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {

    }

    @Override
    public void mousePressed(MouseEvent e)
    {

    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        int x = (e.getX() - x_margin) / SQUARE_DIM;
        int y = (e.getY() - y_margin) / SQUARE_DIM;

        if (0 <= x && x < 8 && 0 <= y && y < 8)
        {
            game.getBoard().setSelectedSpace(new int[]{x, y});
        }

        if (e.getX() < RESIGN_TEXT_DIM[0] && e.getY() < RESIGN_TEXT_DIM[1] && game.getBoard().getCurrentPlayer().getNn() == null)
        {
            hasResigned = game.getBoard().getOpponent();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {

    }

    @Override
    public void mouseExited(MouseEvent e)
    {

    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        game.update();
        repaint();
    }
}
