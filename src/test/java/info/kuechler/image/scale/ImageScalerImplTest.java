package info.kuechler.image.scale;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import info.kuechler.image.scale.impl.ImageScalerImpl;
import info.kuechler.image.scale.metadata.MetadataProviders;

public class ImageScalerImplTest {
    private static final String TARGET_IMAGES = "target";
    private static final String TEST_IMAGES = "src/test/resources/imagescale";
    private ImageScaler scaler = new ImageScalerImpl();

    @Test
    public final void testJpegBox() throws IOException {
        final Path image = scaler.scaleByBox(Paths.get(TEST_IMAGES, "test001.jpg"),
                Paths.get(TARGET_IMAGES, "out001a.jpg"), Rectangle.of(100, 100));
        validateImageSize(image, 73, 100);
    }

    @Test
    public final void testJpegWidth() throws IOException {
        final Path image = scaler.scaleByWidth(Paths.get(TEST_IMAGES, "test001.jpg"),
                Paths.get(TARGET_IMAGES, "out001b.jpg"), 150);
        validateImageSize(image, 150, 209);
    }

    @Test
    public final void testJpegHeight() throws IOException {
        final Path image = scaler.scaleByHeight(Paths.get(TEST_IMAGES, "test001.jpg"),
                Paths.get(TARGET_IMAGES, "out001c.jpg"), 150);
        validateImageSize(image, 109, 150);
    }

    @Test
    public final void testJpeg2PngBox() throws IOException {
        final Path image = scaler.scaleByBox(Paths.get(TEST_IMAGES, "test001.jpg"),
                Paths.get(TARGET_IMAGES, "out001d.png"), Rectangle.of(100, 100), ImageFormat.PNG,
                MetadataProviders.get(ImageFormat.PNG));
        validateImageSize(image, 73, 100);
    }

    @Test
    public final void testJpeg2GifBox() throws IOException {
        final Path image = scaler.scaleByBox(Paths.get(TEST_IMAGES, "test001.jpg"),
                Paths.get(TARGET_IMAGES, "out001e.gif"), Rectangle.of(100, 100), ImageFormat.GIF,
                MetadataProviders.get(ImageFormat.GIF));
        validateImageSize(image, 73, 100);
    }

    @Test
    public final void testPngBox() throws IOException {
        final Path image = scaler.scaleByBox(Paths.get(TEST_IMAGES, "test002.png"),
                Paths.get(TARGET_IMAGES, "out002a.png"), Rectangle.of(300, 300));
        validateImageSize(image, 213, 300);
    }

    @Test
    public final void testGifBox() throws IOException {
        final Path image = scaler.scaleByBox(Paths.get(TEST_IMAGES, "test003.gif"),
                Paths.get(TARGET_IMAGES, "out003a.gif"), Rectangle.of(300, 300));
        validateImageSize(image, 213, 300);
    }

    private static void validateImageSize(Path image, int width, int height) throws IOException {
        final BufferedImage buffer = ImageIO.read(image.toFile());
        Assertions.assertEquals(width, buffer.getWidth());
        Assertions.assertEquals(height, buffer.getHeight());
    }
}
