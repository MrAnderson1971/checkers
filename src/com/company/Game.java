package com.company;

import java.util.*;

public class Game
{

    public static final int MAX_TURNS_NO_CAPTURE = 25;

    private Player player1;
    private Player player2;

    private Board board;
    private ArrayList<int[]> availableMoves;

    private ArrayList<int[]> previousMoves;

    public Game(Player player1, Player player2)
    {
        this.player1 = player1;
        this.player2 = player2;

        this.board = new Board(this.player1, this.player2, true);
        this.availableMoves = new ArrayList<>();
        this.previousMoves = new ArrayList<>();
    }

    public Board getBoard()
    {
        return board;
    }

    public Player win()
    {
        if (board.getPossibleMoves(board.getCurrentPlayer()).isEmpty())
        {
            return board.getOpponent();
        }
        return null;
    }

    public ArrayList<int[]> getAvailableMoves()
    {
        return availableMoves;
    }

    public ArrayList<int[]> getPreviousMoves()
    {
        return previousMoves;
    }

    public void setAvailableMoves(ArrayList<int[]> availableMoves)
    {
        this.availableMoves = availableMoves;
    }

    public Player getPlayer(int p)
    {
        switch (p)
        {
            case 1:
                return player1;
            case 2:
                return player2;
            default:
                throw new IllegalArgumentException("No player exists.");
        }
    }

    public void start()
    {
        for (Player player : new ArrayList<Player>(Arrays.asList(player1, player2)))
        {
            if (player.getNn() != null)
            {
                Node load = Node.load("player2.txt");
                if (load != null)
                {
                    player.getNn().setRoot(load);
                }
                player.getNn().train(128);
                player.parent = player.getNn().getRoot();
            }
        }
    }

    public void update()
    {
        if (board.getCurrentPlayer().getNn() != null)
        {
            previousMoves.clear();
            int[] bestPossibleMove = getBestPossibleMove();
            board.move(Arrays.copyOfRange(bestPossibleMove, 0, 2), Arrays.copyOfRange(bestPossibleMove, 2, 4),
                    Arrays.copyOfRange(bestPossibleMove, 4, bestPossibleMove.length), true, false);
            previousMoves.add(Arrays.copyOfRange(bestPossibleMove, 0, 2));
            previousMoves.add(Arrays.copyOfRange(bestPossibleMove, 2, 4));
        } else if (board.getSelectedSpace()[0] != Integer.MIN_VALUE && board.getSelectedPiece()[0] != Integer.MIN_VALUE)
        {
            ArrayList<int[]> m = new ArrayList<>();
            ArrayList<int[]> c = new ArrayList<>();
            for (int[] i : availableMoves)
            {
                m.add(Arrays.copyOfRange(i, 2, 4));
            }
            for (int[] i : availableMoves)
            {
                c.add(Arrays.copyOfRange(i, 4, i.length));
            }

            if (Main.contains(m, board.getSelectedSpace()))
            {
                previousMoves.clear();
                int[] capture = c.get(Main.indexOf(m, board.getSelectedSpace()));
                board.move(board.getSelectedPiece(), board.getSelectedSpace(), capture, true, true);
                previousMoves.add(board.getSelectedPiece());
                previousMoves.add(board.getSelectedSpace());
                board.setSelectedPiece(new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE});
                board.setSelectedSpace(new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE});
            }
        }
    }

    public int[] getBestPossibleMove()
    {
        board.getCurrentPlayer().getNn().train();
        board.getCurrentPlayer().getNn().setBestChild();
        board.getCurrentPlayer().getNn().train();
        return board.getCurrentPlayer().getNn().getRoot().getMove();
    }
}
