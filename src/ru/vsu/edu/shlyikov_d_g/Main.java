package ru.vsu.edu.shlyikov_d_g;

import java.awt.*;
import java.util.Locale;

public class Main {

    public static void main(String[] args) {
        Locale.setDefault(Locale.ROOT);

        EventQueue.invokeLater(() -> new Window().setVisible(true));
    }
}
