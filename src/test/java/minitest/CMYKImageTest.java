package minitest;

import org.junit.Test;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class CMYKImageTest {

    public static BufferedImage readImage(InputStream stream) throws IOException {

        return readImage(ImageIO.createImageInputStream(stream));
    }

    public static BufferedImage readImage(ImageInputStream input) throws IOException {
        Iterator<?> readers = ImageIO.getImageReaders(input);
        if(readers == null || !readers.hasNext()) {
            return null;
        }

        ImageReader reader = (ImageReader) readers.next();
        reader.setInput(input);

        BufferedImage image;
        try {
            // 尝试读取图片 (包括颜色的转换).
            image = reader.read(0); //RGB

        } catch (IIOException e) {
            // 读取Raster (没有颜色的转换).
            Raster raster = reader.readRaster(0, null);//CMYK
            image = createJPEG4(raster);
        }

        return image;
    }



    private static BufferedImage createJPEG4(Raster raster) {
        int w = raster.getWidth();
        int h = raster.getHeight();
        byte[] rgb = new byte[w * h * 3];

        //彩色空间转换
        float[] Y = raster.getSamples(0, 0, w, h, 0, (float[]) null);
        float[] Cb = raster.getSamples(0, 0, w, h, 1, (float[]) null);
        float[] Cr = raster.getSamples(0, 0, w, h, 2, (float[]) null);
        float[] K = raster.getSamples(0, 0, w, h, 3, (float[]) null);

        for (int i = 0, imax = Y.length, base = 0; i < imax; i++, base += 3) {
            float k = 220 - K[i], y = 255 - Y[i], cb = 255 - Cb[i],
                    cr = 255 - Cr[i];

            double val = y + 1.402 * (cr - 128) - k;
            val = (val - 128) * .65f + 128;
            rgb[base] = val < 0.0 ? (byte) 0 : val > 255.0 ? (byte) 0xff
                    : (byte) (val + 0.5);

            val = y - 0.34414 * (cb - 128) - 0.71414 * (cr - 128) - k;
            val = (val - 128) * .65f + 128;
            rgb[base + 1] = val < 0.0 ? (byte) 0 : val > 255.0 ? (byte) 0xff
                    : (byte) (val + 0.5);

            val = y + 1.772 * (cb - 128) - k;
            val = (val - 128) * .65f + 128;
            rgb[base + 2] = val < 0.0 ? (byte) 0 : val > 255.0 ? (byte) 0xff
                    : (byte) (val + 0.5);
        }


        raster = Raster.createInterleavedRaster(new DataBufferByte(rgb, rgb.length), w, h, w * 3, 3, new int[]{0, 1, 2}, null);

        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        ColorModel cm = new ComponentColorModel(cs, false, true, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        return new BufferedImage(cm, (WritableRaster) raster, true, null);
    }


    @Test
    public void testImage() throws IOException {
        FileInputStream fileInputStream = new FileInputStream("C:\\Users\\091795960\\Pictures/3.jpg");
        BufferedImage bufferedImage = readImage(fileInputStream);
        System.out.println(bufferedImage);
        System.out.println(bufferedImage.getWidth()+":"+bufferedImage.getHeight());
    }
}
