package com.company;

import java.io.*;
import java.util.*;

public class Node implements Serializable
{
    private Board board;
    private Player winner;
    private int[] move;
    private Player player;
    private Player opponent;

    private int games;
    private int wins;
    private int losses;
    private int draws;

    private Node parent;
    private ArrayList<Node> children;

    public Node(Board board, Player winner, int[] move, Player player, Player opponent)
    {
        this.board = board;
        this.winner = winner;
        this.move = move;
        this.player = player;
        this.opponent = opponent;

        this.games = this.wins = this.losses = this.draws = 0;
        this.parent = null;
        this.children = new ArrayList<>();
    }

    public int[] getMove()
    {
        return move;
    }

    public Board getBoard()
    {
        return board;
    }

    public Player getWinner()
    {
        return winner;
    }

    public Player getPlayer()
    {
        return player;
    }

    public Node getParent()
    {
        return parent;
    }

    public void addGame()
    {
        games++;
    }

    public void addWins()
    {
        wins++;
    }

    public void addDraws()
    {
        draws++;
    }

    public void addLosses()
    {
        losses++;
    }

    public void clearBoard()
    {
        this.board = null;
    }

    public void clearPlayers()
    {
        player = null;
        opponent = null;
    }

    public ArrayList<Node> getChildren()
    {
        return children;
    }

    public double getUCT()
    {
        if (games == 0)
        {
            return Double.NaN;
        }
        return (((double)wins) / games) + Math.sqrt(2 * Math.log(parent.games) / games);
    }

    public Node getBestUCT()
    {
        ArrayList<Double> ucts = new ArrayList<>();
        for (Node node : children)
        {
            ucts.add(node.getUCT());
        }

        if (ucts.contains(Double.NaN))
        {
            return (Node) Main.randomChoice(children);
        }
        return children.get(Main.argmax(ucts));
    }

    public void addChild(Board b, Player winning, int[] move)
    {
        Node newChild = new Node(b, winning, move, board.getCurrentPlayer(), board.getOpponent());
        newChild.parent = this;
        children.add(newChild);
    }

    public Node getChildWithMove(int[] move)
    {
        if (children.isEmpty())
        {
            throw new IndexOutOfBoundsException("Children is empty.");
        }

        for (Node child : children)
        {
            if (Arrays.equals(move, child.move))
            {
                return child;
            }
        }

        throw new IllegalArgumentException("No child with move " + Arrays.toString(move) + " found");
    }

    public Node getBestChild()
    {
        ArrayList<Node> winningMoves = new ArrayList<>();
        for (Node child : children)
        {
            if (child.winner == player)
            {
                winningMoves.add(child);
            }
        }

        if (!winningMoves.isEmpty())
        {
            return winningMoves.get(0);
        }

        ArrayList<Double> games = new ArrayList<>();
        for (Node child : children)
        {
            games.add((child.wins - 0.5 * child.draws - child.losses) / (double) child.games);
        }
        return children.get(Main.argmax(games));
    }

    /**
     * Make random moves until someone wins.
     * @return The winner, or null if draw.
     */
    public Player rollout()
    {
        Board copy = (Board) board.clone();
        while (true)
        {
            if (copy.win() != null)
            {
                return copy.win();
            }
            ArrayList<int[]> moves = copy.getPossibleMoves(copy.getCurrentPlayer());

            if (copy.getTurnsSinceLastCapture() > 25)
            {
                return null;
            }

            ArrayList<int[]> winningMoves = new ArrayList<>();
            for (int[] m : moves)
            {
                Board copyOfCopy = (Board) copy.clone();
                copyOfCopy.move(Arrays.copyOfRange(m, 0, 2), Arrays.copyOfRange(m, 2, 4), Arrays.copyOfRange(m, 4, m.length), false, false);
                if (copyOfCopy.win() != null)
                {
                    winningMoves.add(m);
                }
            }

            int[] selectedMove;
            if (!winningMoves.isEmpty())
            {
                selectedMove = winningMoves.get(0);
            } else
            {
                selectedMove = (int[]) Main.randomChoice(moves);
            }
            copy.move(Arrays.copyOfRange(selectedMove, 0, 2), Arrays.copyOfRange(selectedMove, 2, 4), Arrays.copyOfRange(selectedMove, 4, selectedMove.length), false, false);
        }
    }

    public void export(String path)
    {
        FileOutputStream f = null;
        ObjectOutputStream o = null;
        try
        {
            f = new FileOutputStream(new File(Main.path + path));
            o = new ObjectOutputStream(f);
            o.writeObject(this);
        } catch (IOException e)
        {

        } finally
        {
            try
            {
                f.close();
                o.close();
            } catch (IOException | NullPointerException e)
            {
            }
        }
    }

    public static Node load(String path)
    {
        FileInputStream f = null;
        ObjectInputStream o = null;
        Object out = null;
        try
        {
            f = new FileInputStream(new File(Main.path + path));
            o = new ObjectInputStream(f);
            out = o.readObject();
        } catch (IOException | ClassNotFoundException ex)
        {
            System.out.println("Save file not found.");
        } finally
        {
            try
            {
                f.close();
                o.close();
            } catch (IOException | NullPointerException ex)
            {

            }
        }
        return (Node) out;
    }

    @Override
    public String toString()
    {
        return (move == null) ? "empty" : Arrays.toString(move);
    }
}
