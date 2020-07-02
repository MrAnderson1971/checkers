package com.company;

import java.io.Serializable;
import java.util.*;

public class MCTS implements Serializable
{

    private Node root;
    private Player player;
    private Player opponent;

    public MCTS(Board board, Player player, Player opponent)
    {
        this.root = new Node(board, null, null, player, opponent);
        this.player = player;
        this.opponent = opponent;
    }

    public Node getRoot()
    {
        return root;
    }

    public void setRoot(Node root)
    {
        this.root = root;
    }

    public void train()
    {
        train(64);
    }

    public void train(int times)
    {
        Node leaf = traverse(root);

        ArrayList<int[]> moves = leaf.getBoard().getPossibleMoves(leaf.getBoard().getCurrentPlayer());
        Player result;
        if (!moves.isEmpty())
        {
            if (leaf.getWinner() == null)
            {
                /*
                Expand
                 */
                for (int[] move : moves)
                {
                    // Copy the board, then try a move.
                    Board boardCopy = (Board) leaf.getBoard().clone();
                    boardCopy.move(Arrays.copyOfRange(move, 0, 2), Arrays.copyOfRange(move, 2, 4), Arrays.copyOfRange(move, 4, move.length), false, false);
                    Player winning = boardCopy.win();
                    leaf.addChild(boardCopy, winning, move);
                }

                ArrayList<Node> winningMoves = new ArrayList<>();
                for (Node child : leaf.getChildren())
                {
                    if (child.getWinner() != null)
                    {
                        winningMoves.add(child);
                        break;
                    }
                }

                if (!winningMoves.isEmpty())
                {
                    leaf = winningMoves.get(0);
                    result = leaf.getWinner();
                } else
                {
                    /*
                    Rollout
                     */
                    leaf = (Node) Main.randomChoice(leaf.getChildren());
                    result = leaf.rollout();
                }
            } else
            {
                result = leaf.getWinner();
            }
        } else
        {
            result = leaf.getBoard().getOpponent();
        }
        backpropagate(leaf, result);

        if (times > 0)
        {
            train(times - 1);
        }
    }

    private void backpropagate(Node node, Player result)
    {
        if (node == null)
        {
            return;
        }

        node.addGame();

        if (result == node.getPlayer())
        {
            node.addWins();
        } else if (result == null)
        {
            node.addDraws();
        } else
        {
            node.addLosses();
        }
        backpropagate(node.getParent(), result);
    }

    private Node traverse(Node r)
    {
        while (!r.getChildren().isEmpty())
        {
            r = r.getBestUCT();
            r.getParent().clearBoard();
       }
        return r;
    }

    public void setChildWithMove(int[] move)
    {
        root = root.getChildWithMove(move);
    }

    public void setBestChild()
    {
        root = root.getBestChild();
    }
}
