package ru.vsu.edu.shlyikov_d_g.my;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class CoolCompressor {
    private final BufferedImage image;
    public int block;

    public CoolCompressor(BufferedImage image) {
        this.image = image;
    }

    public BufferedImage compress(){
        kMid();
        return image;
    }

    private void kMid(){
        int x;
        int y;
        for (x = 0; x+block < image.getWidth(); x+=block) {
            for (y = 0; y+block < image.getHeight(); y+=block) {
                doMid(x, y, block);
            }
            doMid(x, y, image.getHeight()-y);
        }
        for (y = 0; y+block < image.getHeight(); y+=block) {
            doMid(x, y, image.getWidth()-x);
        }
    }

    private void doMid(int x, int y, int block) {
        List<Color> list = new ArrayList<>();
        if (block != this.block) {
            for (int i = x; i < x + (x + block <= image.getWidth() ? block : 1); i++) {
                for (int j = y; j < y + (y + block <= image.getHeight() ? block : 1); j++) {
                    list.add(new Color(image.getRGB(x, y)));
                }
            }
            while (list.size() > 1) {
                Color a = list.remove(0);
                Color b = list.remove(0);
                Color p = new Color((a.getRed() + b.getRed()) / 2,
                        (a.getGreen() + b.getGreen()) / 2,
                        (a.getBlue() + b.getBlue()) / 2);
                list.add(p);
            }
            for (int i = x; i < x + (x + block <= image.getWidth() ? block : 1); i++) {
                for (int j = y; j < y + (y + block <= image.getHeight() ? block : 1); j++) {
                    image.setRGB(i, j, list.get(0).getRGB());
                }
            }
        } else {
            for (int i = x; i < x + block; i++) {
                for (int j = y; j < y + block; j++) {
                    list.add(new Color(image.getRGB(x, y)));
                }
            }
            while (list.size() > 1) {
                Color a = list.remove(0);
                Color b = list.remove(0);
                Color p = new Color((a.getRed() + b.getRed()) / 2,
                        (a.getGreen() + b.getGreen()) / 2,
                        (a.getBlue() + b.getBlue()) / 2);
                list.add(p);
            }
            for (int i = x; i < x + block; i++) {
                for (int j = y; j < y + block; j++) {
                    image.setRGB(i, j, list.get(0).getRGB());
                }
            }
        }
    }
}
