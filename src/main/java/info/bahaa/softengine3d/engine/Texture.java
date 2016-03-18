package info.bahaa.softengine3d.engine;

import javax.imageio.ImageIO;
import javax.vecmath.Color4f;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by bahaazaid on 3/18/2016.
 */
public class Texture {

    private byte[] buffer;
    private int width;
    private int height;

    public Texture(InputStream imageStream, int width, int height) throws IOException {
        this.buffer = ((DataBufferByte) ImageIO.read(imageStream).getData().getDataBuffer()).getData();
        this.width = width;
        this.height = height;
    }

    public Color4f map(double tu, double tv) {
        if (this.buffer == null) {
            return new Color4f(1, 1, 1, 1);
        }

        int u = Math.abs((int) (tu * width) % width);
        int v = Math.abs((int) (tv * height) % height);

        int pos = (u + v * width) * 3;
        byte b = this.buffer[pos];
        byte g = this.buffer[pos + 1];
        byte r = this.buffer[pos + 2];

        return new Color4f(r / 255.0f, g / 255.0f, b / 255.0f, 1.f);
    }
}
