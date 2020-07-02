package com.company;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.*;


public class Main extends JFrame
{
    public static String name;

    public static final String path = new File(" ").getAbsolutePath().strip();

    public Main()
    {
        init();
    }

    public static void main(String[] args)
    {
        Ascii.print();

        System.out.println("Hello human. Please enter your name:");
        name = new Scanner(System.in).nextLine();
        System.out.println("Greetings, " + name + ".");

        EventQueue.invokeLater(() -> {
            JFrame ex = new Main();
            ex.setVisible(true);
        });
    }

    public void init()
    {
        add(new Surface());

        setResizable(false);
        pack();

        setTitle("Checkers");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static boolean contains(ArrayList<int[]> list, int[] array)
    {
        return indexOf(list, array) != Integer.MIN_VALUE;
    }

    public static int indexOf(ArrayList<int[]> list, int[] array)
    {
        for (int i = 0; i < list.size(); i++)
        {
            if (Arrays.equals(list.get(i), array))
            {
                return i;
            }
        }
        return Integer.MIN_VALUE;
    }

    public static Object randomChoice(ArrayList<?> list)
    {
        return list.get(new Random().nextInt(list.size()));
    }

    public static int argmax(ArrayList<Double> list)
    {
        double max = list.get(0);
        int maxIndex = 0;
        for (int i = 0; i < list.size(); i++)
        {
            if (list.get(i) > max)
            {
                max = list.get(i);
                maxIndex = i;
            }
        }
        return maxIndex;
    }
}
