package ru.vsu.edu.shlyikov_d_g.standart;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import java.awt.image.BufferedImage;
import java.util.Iterator;

public class StandartCompressor {
    private ImageWriteParam param;
    private IIOImage image;

    public ImageWriteParam getParam() {
        return param;
    }

    public IIOImage getImage() {
        return image;
    }

    public void compressing(BufferedImage input){
        float quality = 0.1f;

        // create a BufferedImage as the result of decoding the supplied InputStream

        // get all image writers for JPG format
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");

        if (!writers.hasNext())
            throw new IllegalStateException("No writers found");

        ImageWriter writer = writers.next();

        ImageWriteParam param = writer.getDefaultWriteParam();

        // compress to a given quality
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);

        this.image = new IIOImage(input, null, null);
        this.param = param;
    }
}
