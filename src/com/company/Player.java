package com.company;

import java.io.Serializable;

public class Player implements Serializable
{
    private MCTS nn;
    private String name;

    public Node parent;

    public Player(MCTS nn, String name)
    {
        this.nn = nn;
        this.name = name;

        this.parent = null;
    }

    public MCTS getNn()
    {
        return nn;
    }

    public void setNn(MCTS nn)
    {
        this.nn = nn;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
