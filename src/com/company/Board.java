package com.company;

import org.apache.commons.lang3.ArrayUtils;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;

public class Board implements Cloneable, Serializable
{
    private Player player1;
    private Player player2;

    private Player currentPlayer;

    private int[][] board;

    private int[] mandatoryMove;
    private int[] selectedSpace;
    private int[] selectedPiece;

    private int turnsSinceLastCapture;

    public Board(Player player1, Player player2, boolean start)
    {
        this.player1 = player1;
        this.player2 = player2;

        this.currentPlayer = this.player1;

        this.board = fillBoard(start);

        this.mandatoryMove = new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE};
        this.selectedSpace = new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE};
        this.selectedPiece = new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE};

        this.turnsSinceLastCapture = 0;
    }

    public Board(Player player1, Player player2)
    {
        this(player1, player2, false);
    }

    public Player getCurrentPlayer()
    {
        return currentPlayer;
    }

    public int[][] getBoard()
    {
        return board;
    }

    public int[] getSelectedSpace()
    {
        return selectedSpace;
    }

    public int[] getSelectedPiece()
    {
        return selectedPiece;
    }

    public int getTurnsSinceLastCapture()
    {
        return turnsSinceLastCapture;
    }

    public void setSelectedSpace(int[] selectedSpace)
    {
        this.selectedSpace = selectedSpace;
    }

    public void setSelectedPiece(int[] selectedPiece)
    {
        this.selectedPiece = selectedPiece;
    }

    private int[][] fillBoard(boolean start)
    {
        int[][] b = new int[8][8];

        for (int i : new ArrayList<Integer>(Arrays.asList(1, -1)))
        {
            for (int x = 1; x < 8; x += 2)
            {
                for (int y = 0; y < 3; y += 2)
                {
                    b[x][y] = i;
                }
            }
            for (int x = 0; x < 8; x += 2)
            {
                b[x][1] = i;
            }

            for (int[] row : b)
            {
                ArrayUtils.reverse(row);
            }
            ArrayUtils.reverse(b);
        }

        if (currentPlayer == player2 && start)
        {
            for (int[] row : b)
            {
                ArrayUtils.reverse(row);
            }
            ArrayUtils.reverse(b);
        }

        return b;
    }

    public ArrayList<int[]> getPossibleMoves(Player player)
    {
        ArrayList<int[]> possibleMoves = new ArrayList<>();
        boolean jumpAvailable = false;
        for (int x = 0; x < 8; x++)
        {
            for (int y = 0; y < 8; y++)
            {
                if (mandatoryMove[0] != Integer.MIN_VALUE && !Arrays.equals(new int[]{x, y}, mandatoryMove))
                {
                    continue;
                }

                int enemyPiece = (player == player2) ? -1 : 1;

                if (board[x][y] == -enemyPiece || board[x][y] == -2 * enemyPiece)
                {
                    for (int x_offset : new int[]{-1, 1})
                    {
                        for (int y_offset : new int[]{-1, 1})
                        {
                            if (y_offset == 1 && Math.abs(board[x][y]) == 1)
                            {
                                continue;
                            }

                            if (0 <= x + x_offset && x + x_offset < 8 && 0 <= y + y_offset && y + y_offset < 8)
                            {
                                if (board[x + x_offset][y + y_offset] == 0 && !jumpAvailable && mandatoryMove[0] == Integer.MIN_VALUE)
                                {
                                    possibleMoves.add(new int[]{x, y, x + x_offset, y + y_offset, Integer.MIN_VALUE, Integer.MIN_VALUE});
                                } else if (-enemyPiece * board[x + x_offset][y + y_offset] < 0)
                                {
                                    if (0 <= x + 2 * x_offset && x + 2 * x_offset < 8 && 0 <= y + 2 * y_offset && y + 2 * y_offset < 8)
                                    {
                                        if (board[x + 2 * x_offset][y + 2 * y_offset] == 0)
                                        {
                                            possibleMoves.add(new int[]{x, y, x + 2 * x_offset, y + 2 * y_offset, x + x_offset, y + y_offset});
                                            jumpAvailable = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (jumpAvailable)
        {
            ArrayList<int[]> $ = new ArrayList<>();
            for (int[] move : possibleMoves)
            {
                if (!ArrayUtils.contains(move, Integer.MIN_VALUE))
                {
                    $.add(move);
                }
            }
            possibleMoves = $;
        }
        return possibleMoves;
    }

    public ArrayList<int[]> getPossibleMovesForPiece(int[] space)
    {
        int x = space[0];
        int y = space[1];

        ArrayList<int[]> possibleMoves = new ArrayList<>();
        Player player = (board[x][y] > 0) ? player2 : player1;

        for (int[] move : getPossibleMoves(player))
        {
            if (move[0] == x && move[1] == y)
            {
                possibleMoves.add(move);
            }
        }
        return possibleMoves;
    }

    public void printMove(int[] move)
    {
        int[] m = move.clone();
        /*if (currentPlayer == player2)
        {
            for (int i = 0; i < m.length; i++)
            {
                if (m[i] != Integer.MIN_VALUE)
                {
                    m[i] = 7 - m[i];
                }
            }
        }*/
        if (m[m.length - 1] == Integer.MIN_VALUE)
        {
            System.out.println(String.format("%s made the move %s->%s.",
                    currentPlayer, Arrays.toString(Arrays.copyOfRange(m, 0, 2)),
                    Arrays.toString(Arrays.copyOfRange(m, 2, 4))));
        } else
        {
            System.out.println(String.format("%s made the move %s->%s, capturing %s.",
                    currentPlayer, Arrays.toString(Arrays.copyOfRange(m, 0, 2)),
                    Arrays.toString(Arrays.copyOfRange(m, 2, 4)),
                    Arrays.toString(Arrays.copyOfRange(move, 4, move.length))));
        }
    }

    public void move(int[] piece, int[] space, int[] capture, boolean show, boolean train)
    {
        int pieceToMove = board[piece[0]][piece[1]];
        board[piece[0]][piece[1]] = 0;
        board[space[0]][space[1]] = pieceToMove;
        if (show)
        {
            printMove(new int[]{piece[0], piece[1], space[0], space[1], capture[0], capture[1]});
        }

        // king
        if (space[1] == 0 && Math.abs(pieceToMove) != 2)
        {
            board[space[0]][space[1]] *= 2;
            turnsSinceLastCapture = 0;
        }

        if (capture[0] == Integer.MIN_VALUE)
        {
            mandatoryMove = new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE};
            turnsSinceLastCapture++;
            if (train)
            {
                train(piece, space, capture);
            }
            switchPlayers();
        } else
        {
            mandatoryMove = space.clone();
            board[capture[0]][capture[1]] = 0;
            turnsSinceLastCapture = 0;
            if (train)
            {
                train(piece, space, capture);
            }
            if (getPossibleMoves(currentPlayer).isEmpty())
            {
                mandatoryMove = new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE};
                switchPlayers();
            }
        }
    }

    private void switchPlayers()
    {
        for (int[] row : board)
        {
            ArrayUtils.reverse(row);
        }
        ArrayUtils.reverse(board);
        currentPlayer = getOpponent();
    }

    public Player getOpponent()
    {
        return (currentPlayer == player1) ? player2 : player1;
    }

    private void train(int[] piece, int[] space, int[] capture)
    {
        if (getOpponent().getNn() != null)
        {
            getOpponent().getNn().train();
            // There's got to be a better way to do this.
            getOpponent().getNn().setChildWithMove(new int[]{piece[0], piece[1], space[0], space[1], capture[0], capture[1]});
            getOpponent().getNn().train();
        }
    }

    public Player win()
    {
        if (getPossibleMoves(currentPlayer).isEmpty() && mandatoryMove[0] == Integer.MIN_VALUE)
        {
            return getOpponent();
        }
        return null;
    }

    @Override
    public String toString()
    {
        String s = "";
        for (int[] row : board)
        {
            s += Arrays.toString(row) + "\n";
        }
        return s;
    }

    @Override
    public Object clone()
    {
        Board newBoard = new Board(player1, player2);
        int[][] newGrid = new int[8][8];
        for (int x = 0; x < 8; x++)
        {
            System.arraycopy(board[x], 0, newGrid[x], 0, 8);
        }
        newBoard.board = newGrid;
        newBoard.currentPlayer = currentPlayer;
        newBoard.mandatoryMove = mandatoryMove.clone();
        return newBoard;
    }
}
