package info.kuechler.image.scale.impl;

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

import info.kuechler.image.scale.IOExFunction;
import info.kuechler.image.scale.ImageFormat;
import info.kuechler.image.scale.ImageScaler;
import info.kuechler.image.scale.Rectangle;
import info.kuechler.image.scale.metadata.MetadataProvider;
import info.kuechler.image.scale.metadata.MetadataProviders;

public class ImageScalerImpl implements ImageScaler {
    private static final Logger LOG = LoggerFactory.getLogger(ImageScalerImpl.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public Path scaleByWidth(final Path source, final Path target, final int targetWidth) throws IOException {
        try (final OutputStream out = Files.newOutputStream(target, CREATE, WRITE)) {
            scale(source, sourceSize -> Collections.singletonList(Rectangle.getScaledByWidth(sourceSize, targetWidth)),
                    targetSize -> out);
        }
        return target;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path scaleByWidth(final Path source, final Path target, final int targetWidth,
            final ImageFormat targetFormat, final MetadataProvider metadataProvider) throws IOException {
        try (final OutputStream out = Files.newOutputStream(target, CREATE, WRITE)) {
            scale(source, sourceSize -> Collections.singletonList(Rectangle.getScaledByWidth(sourceSize, targetWidth)),
                    targetFormat, metadataProvider, targetSize -> out);
        }
        return target;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path scaleByHeight(final Path source, final Path target, final int targetHeight) throws IOException {
        try (final OutputStream out = Files.newOutputStream(target, CREATE, WRITE)) {
            scale(source,
                    sourceSize -> Collections.singletonList(Rectangle.getScaledByHeight(sourceSize, targetHeight)),
                    targetSize -> out);
        }
        return target;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path scaleByHeight(final Path source, final Path target, final int targetHeight,
            final ImageFormat targetFormat, final MetadataProvider metadataProvider) throws IOException {
        try (final OutputStream out = Files.newOutputStream(target, CREATE, WRITE)) {
            scale(source,
                    sourceSize -> Collections.singletonList(Rectangle.getScaledByHeight(sourceSize, targetHeight)),
                    targetFormat, metadataProvider, targetSize -> out);
        }
        return target;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path scaleByBox(final Path source, final Path target, final Rectangle border) throws IOException {
        try (final OutputStream out = Files.newOutputStream(target, CREATE, WRITE)) {
            scale(source, sourceSize -> Collections.singletonList(Rectangle.getBorderRectagle(sourceSize, border)),
                    targetSize -> out);
        }
        return target;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path scaleByBox(final Path source, final Path target, final Rectangle border, final ImageFormat targetFormat,
            final MetadataProvider metadataProvider) throws IOException {
        try (final OutputStream out = Files.newOutputStream(target, CREATE, WRITE)) {
            scale(source, sourceSize -> Collections.singletonList(Rectangle.getBorderRectagle(sourceSize, border)),
                    targetFormat, metadataProvider, targetSize -> out);
        }
        return target;
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
     * {@inheritDoc}
     */
    @Override
    public Map<Rectangle, Path> scaleByBox(final Path source, final Path targetFolder, final List<Rectangle> borders,
            final ImageFormat targetFormat, final MetadataProvider metadataProvider) throws IOException {
        final Map<Rectangle, Path> result = new HashMap<>();
        final Function<Rectangle, Iterable<Rectangle>> borderSupplier = sourceSize -> borders.stream()
                .map(border -> Rectangle.getBorderRectagle(sourceSize, border)).collect(Collectors.toList());
        final IOExFunction<Rectangle, OutputStream> outputProvider = targetSize -> {
            final Path fileOut = createTargetFile(targetFolder, source.getFileName(), targetSize);
            result.put(targetSize, fileOut);
            return Files.newOutputStream(fileOut, CREATE, WRITE);
        };

        scale(source, borderSupplier, targetFormat, metadataProvider, outputProvider);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void scale(final Path sourceFile, final Function<Rectangle, Iterable<Rectangle>> borderCalculation,
            final IOExFunction<Rectangle, OutputStream> outputProvider) throws IOException {
        final Optional<Path> fileNamePath = Optional.ofNullable(sourceFile.getFileName());
        if (fileNamePath.isPresent()) {
            final Optional<ImageFormat> format = ImageFormat.detectImageFormatByName(fileNamePath.get().toString());
            if (format.isPresent()) {
                final MetadataProvider metadataProvider = MetadataProviders.get(format.get());
                scale(sourceFile, borderCalculation, format.get(), metadataProvider, outputProvider);
            } else {
                throw new IOException("No format found " + format + " for image " + sourceFile);
            }
        } else {
            throw new IOException("File contains no file name " + sourceFile);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void scale(final Path sourceFile, final Function<Rectangle, Iterable<Rectangle>> borderCalculation,
            final ImageFormat targetFormat, final MetadataProvider metadataProvider,
            final IOExFunction<Rectangle, OutputStream> outputProvider) throws IOException {
        LOG.debug("Convert {} into {}.", sourceFile, targetFormat);
        final long start = System.currentTimeMillis();

        if (!metadataProvider.canHandle(targetFormat)) {
            throw new IOException("MetadataProvider cannot handle format " + targetFormat);
        }

        final BufferedImage in = ImageIO.read(sourceFile.toFile());
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
                write(outputStream, targetFormat, metadataProvider, colorSpace, colorProfileName, outScaled);
            }
        }
        LOG.debug(System.currentTimeMillis() - start + " ms");
    }

    /**
     * Write a image to a file.
     */
    protected void write(final OutputStream target, final ImageFormat targetFormat,
            final MetadataProvider metadataProvider, final ICC_ColorSpace outColorSpace, final String outProfileName,
            final BufferedImage source) throws IIOInvalidTreeException, IOException {
        final ImageWriter writer = ImageIO.getImageWritersByFormatName(targetFormat.getFormatName()).next();
        try (final ImageOutputStream outStream = ImageIO.createImageOutputStream(target)) {
            writer.setOutput(outStream);

            final ImageWriteParam param = writer.getDefaultWriteParam();
            final IIOMetadata metadata = metadataProvider.getMetadata(writer, source, outColorSpace, outProfileName,
                    param);

            writer.write(null, new IIOImage(source, null, metadata), param);
        } finally {
            writer.dispose();
        }
    }

    /**
     * Convert a {@link BufferedImage} into another {@link ColorSpaces}.
     */
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

    protected Path createTargetFile(final Path targetFolder, final Path originalFileName, final Rectangle size) {
        final String fileName = originalFileName.toString();
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
