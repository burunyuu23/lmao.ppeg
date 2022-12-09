package ru.vsu.edu.shlyikov_d_g.my;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Random;

import static java.lang.Math.round;
import static java.lang.Math.sqrt;

public class JPEGCompressor {
    private BufferedImage image;
    private int[][] Y, Cb, Cr;
    public double a, b;
    private int[][] DCP = new int[][]{
            {16,11,	10,	16,	24	,40,	51,	61},
            {12,12,	14,	19,	26	,58,	60,	55},
            {14,13,	16,	24,	40	,57,	69,	56},
            {14,17,	22,	29,	51	,87,	80,	62},
            {18,22,	37,	56,	68	,109,	103,	77},
            {24,35,	55,	64,	81	,104,	113,	92},
            {49,64,	78,	87,	103,	121,	120,	101},
            {72,92,	95,	98,	112,	100,	103,	99 }};

            private int[][] DCPq = new int[][]{
                    {17,18,	24,	47,	99,	99,	99,	99},
                    {18,21,	26,	66,	99,	99,	99,	99},
                    {24,26,	56,	99,	99,	99,	99,	99},
                    {47,66,	99,	99,	99,	99,	99,	99},
                    {99,99,	99,	99,	99,	99,	99,	99},
                    {99,99,	99,	99,	99,	99,	99,	99},
                    {99,99,	99,	99,	99,	99,	99,	99},
                    {99,99,	99,	99,	99,	99,	99,	99}};

    private List<Integer> listY = new ArrayList<>();
    private List<Integer> listCb = new ArrayList<>();
    private List<Integer> listCr = new ArrayList<>();


    public JPEGCompressor(BufferedImage image) {
        this.image = image;
    }

    public Image compress(){
        RGB2YCbCr();
        sampling();

        for (int i = 0; i < image.getWidth() - 8; i+=8) {
            for (int j = 0; j < image.getHeight() - 8; j+=8) {
                int[][] y  = new int[8][8];
                int[][] cb = new int[8][8];
                int[][] cr = new int[8][8];
                for (int k = i; k < i + 8; k++) {
                    for (int z = j; z < j + 8; z++) {
                         y[k - i][z - j] = Y[k][z] ;
                        cb[k - i][z - j] = Cb[k][z];
                        cr[k - i][z - j] = Cr[k][z];
                    }
                }

                double[][] arrY = new double[8][8];
//                arrY = unDCT(DCT(y));
                arrY = unDCT(unZigZag(zigZag(DCT(y),true),true));
                double[][] arrCb = new double[8][8];
//                arrCb = unDCT(DCT(cb));
                arrCb = unDCT(unZigZag(zigZag(DCT(cb),false),false));
                double[][] arrCr = new double[8][8];
//                arrCr = unDCT(DCT(cr));
                arrCr = unDCT(unZigZag(zigZag(DCT(cr),false),false));

                YCbCr2RGB(i, j, arrY, arrCb, arrCr);
            }
        }

        return this.image;
    }

    public void YCbCr2RGB(int i, int j, double[][] YArr, double[][] CbArr, double[][] CrArr) {
        for (int x = i; x < i + 8; x++) {
            for (int y = j; y < j + 8; y++) {
                int R, G, B;
                double Y, U, V;

                Y = Math.max(0, Math.min(YArr[x - i][y - j], 255));
                U = Math.max(0, Math.min(CbArr[x - i][y - j], 255));
                V = Math.max(0, Math.min(CrArr[x - i][y - j], 255));

                R = (int) (Y + V/0.877);
                R = Math.max(0, Math.min(R, 255));

                G = (int) (Y - (0.299 * V + 0.114 * U)/0.587);
                G = Math.max(0, Math.min(G, 255));

                B = (int) (Y + U/0.492);
                B = Math.max(0, Math.min(B, 255));

                this.image.setRGB(x, y, new Color(R,G,B).getRGB());
            }
        }
    }

    public void RGB2YCbCr() {
        Y = new int[image.getWidth()][image.getHeight()];
        Cb = new int[image.getWidth()][image.getHeight()];
        Cr = new int[image.getWidth()][image.getHeight()];
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color color = new Color(image.getRGB(x, y));
                int R, G, B;
                R = color.getRed();
                G = color.getGreen();
                B = color.getBlue();
                Y[x][y]  =  (int) (0.299 * R + 0.587 * G + 0.114 * B);
                Cr[x][y] = (int) (0.877 * (R - Y[x][y]));
                Cb[x][y] = (int) (0.492 * (B - Y[x][y]));

                Cb[x][y] = Math.max(0, Cb[x][y]);
                Cr[x][y] = Math.max(0, Cr[x][y]);
            }
        }
    }

    public void sampling() {
        for (int x = 0; x < image.getWidth() - b; x+=b) {
            for (int y = 0; y < image.getHeight() - b; y+=b) {

                int firstCb = Cb[x][y];
                int firstCr = Cr[x][y];

                for (int i = x; i < x+b; i++) {
                    for (int j = y; j < y+b; j++) {
                        Cb[i][j] = firstCb;
                        Cr[i][j] = firstCr;
                    }
                }
            }
        }
    }

    public double[][] DCT(int[][] color){
        double[][] DCT = new double[8][8];
        for(int i=0;i<8;i++)
        {
            double epsI = eps(i);
            for(int j=0;j<8;j++)
            {
                double epsJ = eps(j);
                double sum = 0;
                for (int x = 0; x < 8; x++) {
                    for (int y = 0; y < 8; y++) {
                        sum += (color[x][y]*epsI*epsJ*Math.cos((2*x+1)*i*Math.PI/16)*Math.cos((2*y+1)*j*Math.PI/16));
                    }
                }
                DCT[i][j] = round((1.0/4)*sum);
            }
        }
//                for(int i=0;i<8;i++) {
//                    for (int j = 0; j < 8; j++) {
//                        DCT[i][j] = color[i][j];
//                    }
//                }
        return DCT;
    }

    public double[][] unDCT(double[][] color){
        double[][] DCT = new double[8][8];
        for(int i=0;i<8;i++)
        {
            for(int j=0;j<8;j++)
            {
                double sum = 0;
                for (int x = 0; x < 8; x++) {
                    for (int y = 0; y < 8; y++) {
                        sum += (eps(x) * eps(y) * color[x][y]*Math.cos((2*i+1)*x*Math.PI/16)*Math.cos((2*j+1)*y*Math.PI/16));
                    }
                }
                DCT[i][j] = ((1.0/4)*sum);
            }
        }
        return DCT;
//        return color;
    }

    private double eps(int k){
        if (k==0) return 1.0/sqrt(2.0);
        else return 1;
    }

    public double[][] zigZag(double[][] arr, boolean isY)
    {
        double[][] ans = new double[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ans[i][j] = quanting(arr[i][j], i, j, isY);
            }
        }
        return ans;
    }

    public double[][] unZigZag(double[][] arr, boolean isY)
    {
        double[][] ans = new double[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ans[i][j] = unquanting(arr[i][j], i, j, isY);
            }
        }
        return ans;
    }

//    public double[][] zigZag(double[][] arr, boolean isY){
//        double[][] ans = new double[8][8];
//        int i = 0;
//        int j = 0;
//        int k = 0;
//        int counter = 0;
//        boolean flag = false;
//        ans[i][j] = quanting(arr[i][j], i,j, isY);
//
//        while (counter != 63) {
//            if (counter >= 35) flag = true;
//            if (flag) {
//                k++;
//                i++;
//            }
//            else j++;
//
//            while (j != k) {
//                counter++;
//                ans[i][j] = quanting(arr[i][j], i,j, isY);
//                i++;
//                j--;
//            }
//            counter++;
//            ans[i][j] = quanting(arr[i][j], i,j, isY);
//
//            if (counter >= 35) flag = true;
//            if (flag) {
//                k++;
//                j++;
//            }
//            else i++;
//
//            while (i != k) {
//                counter++;
//                ans[i][j] = quanting(arr[i][j], i,j, isY);
//                i--;
//                j++;
//            }
//            counter++;
//            ans[i][j] = quanting(arr[i][j], i,j, isY);
//        }
//        return ans;
//    }
//
//    public double[][] unZigZag(double[][] arr, boolean isY){
//        double[][] ans = new double[8][8];
//        int i = 0;
//        int j = 0;
//        int k = 0;
//        int counter = 0;
//        boolean flag = false;
//        ans[i][j] = unquanting(arr[i][j], i,j, isY);
//
//        while (counter != 63) {
//            if (counter >= 35) flag = true;
//            if (flag) {
//                k++;
//                i++;
//            }
//            else j++;
//
//            while (j != k) {
//                counter++;
//                ans[i][j] = unquanting(arr[i][j], i,j, isY);
//                i++;
//                j--;
//            }
//            counter++;
//            ans[i][j] = unquanting(arr[i][j], i,j, isY);
//
//            if (counter >= 35) flag = true;
//            if (flag) {
//                k++;
//                j++;
//            }
//            else i++;
//
//            while (i != k) {
//                counter++;
//                ans[i][j] = unquanting(arr[i][j], i,j, isY);
//                i--;
//                j++;
//            }
//            counter++;
//            ans[i][j] = unquanting(arr[i][j], i,j, isY);
//        }
//        return ans;
//    }

    public double unquanting(double element, int i, int j, boolean isY) {
        return round(element * (isY ? DCP[i][j] : DCPq[i][j]) / a);
    }

    public double quanting(double element, int i, int j, boolean isY) {
        return round(element / (isY ? DCP[i][j] : DCPq[i][j]) * a);
    }

//    public double unquanting(double element, int i, int j, boolean isY) {
//        return round(element * a);
//    }
//
//    public double quanting(double element, int i, int j, boolean isY) {
//        return round(element / a);
//    }

//    public double unquanting(double element, int counter, boolean isY) {
//        return round(element * (isY ? DCP[counter] : DCPq[counter]));
//    }
//
//    public double quanting(double element, int counter, boolean isY) {
//        return round(element / (isY ? DCP[counter] : DCPq[counter]));
//    }
}
