package info.kuechler.image.scale.scale;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.twelvemonkeys.image.ResampleOp;
import com.twelvemonkeys.imageio.color.ColorSpaces;

import info.kuechler.image.scale.metadata.DefaultMetadataProvider;
import info.kuechler.image.scale.metadata.JPEGMetadataProvider;
import info.kuechler.image.scale.metadata.MetadataProvider;
import info.kuechler.image.scale.metadata.PNGMetadataProvider;

/**
 * Scales images from type JPEG, PNG and GIF. <br>
 * The target color space is sRGB.<br>
 * All size parameters are maximum values. The image size ratio will is preserved. <br>
 * Quality takes precedence over speed.
 */
public class ImageScaler {
    private static final Logger LOG = LoggerFactory.getLogger(ImageScaler.class);

    /**
     * Scale method, see {@link ImageScaler} documentation for general information.
     * 
     * @param source
     *            the source file
     * @param target
     *            the target file. If the file exists, it will be overwritten.
     * @param targetWidth
     *            the target width. The height is calculated with a constant aspect ratio.
     * @return the path of the target file.
     * @throws IOException
     *             error during the scaling process.
     */
    public Path scaledByWidth(final Path source, final Path target, final int targetWidth) throws IOException {
        try (final OutputStream out = Files.newOutputStream(target, CREATE, WRITE)) {
            scale(source, sourceSize -> Collections.singletonList(Rectangle.getScaledByWidth(sourceSize, targetWidth)),
                    targetSize -> out);
        }
        return target;
    }

    /**
     * Scale method, see {@link ImageScaler} documentation for general information.
     * 
     * @param source
     *            the source file
     * @param target
     *            the target file. If the file exists, it will be overwritten.
     * @param targetHeight
     *            the target height. The width is calculated with a constant aspect ratio.
     * @return the path of the target file.
     * @throws IOException
     *             error during the scaling process.
     */
    public Path scaledByHeight(final Path source, final Path target, final int targetHeight) throws IOException {
        try (final OutputStream out = Files.newOutputStream(target, CREATE, WRITE)) {
            scale(source,
                    sourceSize -> Collections.singletonList(Rectangle.getScaledByHeight(sourceSize, targetHeight)),
                    targetSize -> out);
        }
        return target;
    }

    /**
     * Scale method, see {@link ImageScaler} documentation for general information.
     * 
     * @param source
     *            the source file
     * @param target
     *            the target file. If the file exists, it will be overwritten.
     * @param border
     *            the target size. The height and width is calculated with a constant aspect ratio. One value is the
     *            same like the box the other value is equals or smaller.
     * @return the path of the target file.
     * @throws IOException
     *             error during the scaling process.
     */
    public Path scaleByBox(final Path source, final Path target, final Rectangle border) throws IOException {
        try (final OutputStream out = Files.newOutputStream(target, CREATE, WRITE)) {
            scale(source, sourceSize -> Collections.singletonList(Rectangle.getBorderRectagle(sourceSize, border)),
                    targetSize -> out);
        }
        return target;
    }

    /**
     * Scale method, see {@link ImageScaler} documentation for general information. <br>
     * Creates a collection of images from the same source image.
     * 
     * @param source
     *            the source file
     * @param target
     *            the target file. If the file exists, it will be overwritten.
     * @param borders
     *            the target sizes. The height and width is calculated with a constant aspect ratio. One value is the
     *            same like the box the other value is equals or smaller.
     * @return a {@link Map} with the calculated target sizes and the created target files.
     * @throws IOException
     *             error during the scaling process.
     */
    public Map<Rectangle, Path> scaleByBox(final Path source, final Path targetFolder, final List<Rectangle> borders)
            throws IOException {
        final Map<Rectangle, Path> result = new HashMap<>();
        final Function<Rectangle, Iterable<Rectangle>> borderSupplier = sourceSize -> borders.stream()
                .map(border -> Rectangle.getBorderRectagle(sourceSize, border)).collect(Collectors.toList());
        final IOExFunction<Rectangle, OutputStream> outputProvider = targetSize -> {
            final Path fileOut = createTargetFile(targetFolder, source.getFileName(), targetSize);
            result.put(targetSize, fileOut);
            return Files.newOutputStream(fileOut, CREATE, WRITE);
        };

        scale(source, borderSupplier, outputProvider);
        return result;
    }

    /**
     * Scale an image into a collection of images.
     * 
     * @param sourceFile
     *            the source file
     * @param borderCalculation
     *            Function which returns the target box sizes. Input is the original size from the source file. Has to
     *            return an {@link Iterable} of target sizes.
     * @param outputProvider
     *            Function which provide the concrete {@link OutputStream} for an target image. Input is the target
     *            size. Has to return a {@link OutputStream}. Attention: This stream is closed inside the method.
     *            Function can throw an {@link IOException} which stops the conversion. This exception is thrown by this
     *            method.
     * @throws IOException
     *             An error occurs. Can throw during reading the source image, providing a target {@link OutputStream}
     *             or writing into this stream.
     */
    public void scale(final Path sourceFile, final Function<Rectangle, Iterable<Rectangle>> borderCalculation,
            final IOExFunction<Rectangle, OutputStream> outputProvider) throws IOException {
        final long start = System.currentTimeMillis();
        final Optional<Path> fileNamePath = Optional.ofNullable(sourceFile.getFileName());
        if (fileNamePath.isPresent()) {
            final BufferedImage in = ImageIO.read(sourceFile.toFile());
            final Optional<ImageFormat> format = ImageFormat.detectImageFormat(fileNamePath.get().toString());
            if (format.isPresent()) {
                final int targetType = (in.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB
                        : BufferedImage.TYPE_INT_ARGB;
                final Rectangle sourceSize = Rectangle.of(in.getWidth(), in.getHeight());
                final String colorProfileName = "sRGB";
                final ICC_ColorSpace colorSpace = (ICC_ColorSpace) ColorSpaces.getColorSpace(ColorSpace.CS_sRGB);

                // convert
                final BufferedImage colorSpacedImage = convertToColorSpace(colorSpace, in);

                // scale
                final Iterable<Rectangle> targetSizes = borderCalculation.apply(sourceSize);
                for (Rectangle targetSize : targetSizes) {
                    LOG.debug("Source size={},  Target size = {}", sourceSize, targetSize);
                    final BufferedImageOp resampler = new ResampleOp(targetSize.getWidth(), targetSize.getHeight(),
                            ResampleOp.FILTER_LANCZOS);
                    final BufferedImage outScaled = resampler.filter(colorSpacedImage,
                            new BufferedImage(targetSize.getWidth(), targetSize.getHeight(), targetType));

                    // write
                    try (final OutputStream outputStream = outputProvider.apply(targetSize);) {
                        write(outputStream, format.get(), colorSpace, colorProfileName, outScaled);
                    }
                }
            } else {
                throw new IOException("No format found " + format + " for image " + sourceFile);
            }
        } else {
            throw new IOException("File contains no file name " + sourceFile);
        }
        LOG.debug(System.currentTimeMillis() - start + " ms");

    }

    protected void write(final OutputStream target, final ImageFormat format, final ICC_ColorSpace outColorSpace,
            final String outProfileName, final BufferedImage source) throws IIOInvalidTreeException, IOException {
        final ImageWriter writer = ImageIO.getImageWritersByFormatName(format.getFormatName()).next();
        try (final ImageOutputStream outStream = ImageIO.createImageOutputStream(target)) {
            writer.setOutput(outStream);

            final MetadataProvider metadataProvider;
            if (format == ImageFormat.JPG) {
                metadataProvider = new JPEGMetadataProvider();
            } else if (format == ImageFormat.PNG) {
                metadataProvider = new PNGMetadataProvider();
            } else {
                metadataProvider = new DefaultMetadataProvider();
            }
            final ImageWriteParam param = writer.getDefaultWriteParam();
            final IIOMetadata metadata = metadataProvider.getMetadata(writer, source, outColorSpace, outProfileName,
                    param);

            writer.write(null, new IIOImage(source, null, metadata), param);
        } finally {
            writer.dispose();
        }
    }

    protected BufferedImage convertToColorSpace(final ColorSpace colorSpace, final BufferedImage source) {
        LOG.debug("Before colorModel={}, colorSpace={}", source.getColorModel(),
                source.getColorModel().getColorSpace());
        // There is no guarantee that this will work. In this case the image will be converted too.
        if (Objects.equals(source.getColorModel().getColorSpace(), colorSpace)) {
            LOG.trace("Color Space is the same. Return source.");
            return source;
        }
        final Map<Key, Object> renderHints = new HashMap<>();
        renderHints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        renderHints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        final ColorConvertOp cco = new ColorConvertOp(colorSpace, new RenderingHints(renderHints));
        final BufferedImage out = cco.filter(source, null);
        LOG.debug("After colorModel={}, colorSpace={}", out.getColorModel(), out.getColorModel().getColorSpace());
        return out;
    }

    protected Path createTargetFile(final Path targetFolder, final Path fileNamePath, final Rectangle size) {
        final String fileName = fileNamePath.toString();
        final int point = fileName.lastIndexOf('.');
        final String body;
        final String ending;
        if (point >= 0) {
            body = fileName.substring(0, point);
            ending = fileName.substring(point);
        } else {
            body = fileName;
            ending = "";
        }
        return targetFolder.resolve(body + '_' + size.getWidth() + 'x' + size.getHeight() + ending);
    }
}
