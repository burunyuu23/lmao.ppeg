package ru.vsu.edu.shlyikov_d_g;

import ru.vsu.edu.shlyikov_d_g.my.JPEGCompressor;
import ru.vsu.edu.shlyikov_d_g.standart.StandartCompressor;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

public class Window extends JFrame {
        JFileChooser fc = new JFileChooser();
    JPanel outputContainer = new JPanel();
    JPanel inputContainer = new JPanel();
    JPanel imageBefore = new JPanel();
    JPanel imageAfter = new JPanel();
    JButton attach = new JButton("Аттачнуть");
    JButton decode = new JButton("Декоднуть");
    private BufferedImage image;
    BufferedImage imageOutput;

    private final JSlider qualitySlider = new JSlider();
    private final JSlider blocksSlider = new JSlider();

    private final JLabel qualityLabel = new JLabel();
    private final JLabel blocksLabel = new JLabel();

    private final JLabel inputSize = new JLabel();
    private final JLabel outputSize = new JLabel();

    private double quality = 1;
    private int blocks = 2;

    ByteArrayOutputStream baos;


    public Window(){
        setResizable(false);
        setMinimumSize(new Dimension(1600,900));
        setVisible(true);

        setLayout(null);
        inputContainer.setLayout(null);

        inputContainer.setVisible(true);
        inputContainer.setBounds(0 , 0, getWidth()/2, 900);
        add(inputContainer);


        outputContainer.setLayout(null);
        outputContainer.setVisible(true);
        outputContainer.setBounds(getWidth()/2 - 50 , 0, getWidth()/2, 900);
        add(outputContainer);

        imageAfter.setBounds(outputContainer.getWidth()/16, outputContainer.getHeight()/16,
                7 * outputContainer.getWidth() / 8, (int) (outputContainer.getHeight()/1.5));
        imageAfter.setBackground(Color.MAGENTA);
        imageAfter.setVisible(true);
        imageAfter.setLayout(null);
        outputContainer.add(imageAfter);

        outputSize.setVisible(true);
        outputSize.setBounds(imageAfter.getWidth()/2 + 20, imageAfter.getY() + imageAfter.getHeight() + 5, imageAfter.getWidth(), 10);
        outputSize.setText("non-file");
        outputContainer.add(outputSize);

        imageBefore.setBounds(inputContainer.getWidth()/16, inputContainer.getHeight()/16,
                7 * inputContainer.getWidth() / 8, (int) (inputContainer.getHeight()/1.5));
        imageBefore.setBackground(Color.RED);
        imageBefore.setVisible(true);
        imageBefore.setLayout(null);
        inputContainer.add(imageBefore);

        decode.addActionListener(a -> {
            try {
                Path currentRelativePath = Paths.get("");

                String s = currentRelativePath.toAbsolutePath().toString();

                File compressedImageFile = new File(s + "/src/ru/vsu/edu/shlyikov_d_g/out/myimage_compressed.jpg");

                OutputStream os = new FileOutputStream(compressedImageFile);

                Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");

                ImageWriter writer = writers.next();
                ImageOutputStream ios = ImageIO.createImageOutputStream(os);
                writer.setOutput(ios);

                ColorModel cm = image.getColorModel();
                boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
                WritableRaster raster = image.copyData(null);
                BufferedImage jpegImage = new BufferedImage(cm, raster, isAlphaPremultiplied, null);

                JPEGCompressor jpegCompressor = new JPEGCompressor(jpegImage);
                jpegCompressor.a = quality;
                jpegCompressor.b = blocks;
                writer.write((RenderedImage) jpegCompressor.compress());

//                CoolCompressor compressor = new CoolCompressor(image);
//                compressor.block = cC;
//                writer.write((RenderedImage) compressor.compress());

//                writer.write(null, standartCompressor.getImage(), standartCompressor.getParam());
                ios.close();
                os.close();

                imageOutput = ImageIO.read(compressedImageFile);
                outputSize.setText(compressedImageFile.length() / 1024 + " Kb");
                System.out.println("done!");
            } catch (IOException e) {
                e.printStackTrace();
            }
            repaint();
        });

        fc.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(".jpg") || f.isDirectory();
            }
            public String getDescription() {
                return ".JPG";
            }
        });

        qualitySlider.setVisible(true);
        inputContainer.add(qualitySlider);
        qualitySlider.setBounds(imageBefore.getX(), imageBefore.getY() + imageBefore.getHeight() + 70, imageBefore.getWidth(), 10);
        qualitySlider.setMaximum(100);
        qualitySlider.setMinimum(1);
        qualityLabel.setVisible(true);
        qualityLabel.setText("quality = " + quality);
        qualityLabel.setBounds(imageBefore.getX() + imageBefore.getWidth() / 2 - qualityLabel.getWidth()  - 40, qualitySlider.getY() + 15, imageBefore.getWidth(), 10);
        inputContainer.add(qualityLabel);

        blocksSlider.setVisible(true);
        inputContainer.add(blocksSlider);
        blocksSlider.setBounds(imageBefore.getX(), qualityLabel.getY() + qualityLabel.getHeight() + 5, imageBefore.getWidth(), 10);
        blocksSlider.setMaximum(200);
        blocksSlider.setMinimum(1);
        blocksLabel.setVisible(true);
        blocksLabel.setText("blocks = " + blocks);
        inputContainer.add(blocksLabel);
        blocksLabel.setBounds(imageBefore.getX() + imageBefore.getWidth()/2 - blocksLabel.getWidth() - 40, blocksSlider.getY() + 15, imageBefore.getWidth(), 10);

        inputSize.setVisible(true);
        inputSize.setText("non-file");
        inputContainer.add(inputSize);

        inputSize.setBounds(imageBefore.getX() + blocksSlider.getWidth()/2 - 30, blocksLabel.getY() + 15, blocksSlider.getWidth(), 10);


        qualitySlider.addChangeListener(arg0 -> {
            quality = qualitySlider.getValue() / 100.0;
            qualityLabel.setText("quality = " + quality);
        });
        blocksSlider.addChangeListener(arg0 -> {
            blocks = blocksSlider.getValue();
            blocksLabel.setText("blocks = " + blocks);
        });

        attach.addActionListener(a -> {
            fc.showDialog(this, "Прикрепить");
            try {
                File file = fc.getSelectedFile();
                image = ImageIO.read(file);

                inputSize.setText(file.length() / 1024 + " Kb");

                baos = new ByteArrayOutputStream(1000);
                ImageIO.write(image, "jpg", baos);
                baos.flush();
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            repaint();
        });

        attach.setBounds(imageBefore.getX(), imageBefore.getY() + imageBefore.getHeight() + 10, imageBefore.getWidth()/2 - 5, 50);
        attach.setVisible(true);
        decode.setBounds(imageBefore.getX() + imageBefore.getWidth()/2 + 5, imageBefore.getY() + imageBefore.getHeight() + 10, imageBefore.getWidth()/2 - 5, 50);
        decode.setVisible(true);
        inputContainer.add(attach);
        inputContainer.add(decode);

        pack();
    }

    @Override
    public void paint(Graphics gr) {
        super.paint(gr);
        Graphics2D g = (Graphics2D) gr;
        if (image != null) g.drawImage(image, inputContainer.getX() + imageBefore.getX() + 8, inputContainer.getY() + imageBefore.getY() + 31,imageBefore.getWidth(),imageBefore.getHeight(), inputContainer);
        if (imageOutput != null) g.drawImage(imageOutput, outputContainer.getX() + imageAfter.getX() + 8, outputContainer.getY() + imageAfter.getY() + 31,imageAfter.getWidth(),imageAfter.getHeight(), outputContainer);
    }

    @Override
    public void paintComponents(Graphics g) {
        repaint();
    }
}
