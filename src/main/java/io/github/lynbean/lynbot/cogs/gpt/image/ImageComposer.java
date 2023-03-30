package io.github.lynbean.lynbot.cogs.gpt.image;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import lombok.Builder;

@Builder
public class ImageComposer {
    private String topLeftURL;
    private String topRightURL;
    private String bottomLeftURL;
    private String bottomRightURL;

    public byte[] writeImageToBytes() throws IOException {
        BufferedImage topLeft = ImageIO.read(new URL(topLeftURL));
        BufferedImage topRight = ImageIO.read(new URL(topRightURL));
        BufferedImage bottomLeft = ImageIO.read(new URL(bottomLeftURL));
        BufferedImage bottomRight = ImageIO.read(new URL(bottomRightURL));

        int width = topLeft.getWidth() + topRight.getWidth();
        int height = topLeft.getHeight() + bottomLeft.getHeight();

        BufferedImage composite = new BufferedImage(
            width / 2, height / 2, BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D g2d = composite.createGraphics();
        g2d.drawImage(
            topLeft,
            0,
            0,
            topLeft.getWidth() / 2,
            topLeft.getHeight() / 2,
            null
        );
        g2d.drawImage(
            topRight,
            topLeft.getWidth() / 2,
            0,
            topRight.getWidth() / 2,
            topRight.getHeight() / 2,
            null
        );
        g2d.drawImage(
            bottomLeft,
            0,
            topLeft.getHeight() / 2,
            bottomLeft.getWidth() / 2,
            bottomLeft.getHeight() / 2,
            null
        );
        g2d.drawImage(
            bottomRight,
            topLeft.getWidth() / 2,
            topLeft.getHeight() / 2,
            bottomRight.getWidth() / 2,
            bottomRight.getHeight() / 2,
            null
        );
        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();
        ImageOutputStream stream = ImageIO.createImageOutputStream(baos);
        writer.setOutput(stream);

        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(1.0f);
        writer.write(null, new IIOImage(composite, null, null), param);
        stream.close();

        return baos.toByteArray();
    }
}
