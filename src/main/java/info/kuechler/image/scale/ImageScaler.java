package info.kuechler.image.scale;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import info.kuechler.image.scale.metadata.MetadataProvider;
import info.kuechler.image.scale.metadata.MetadataProviders;

/**
 * Scales images from type JPEG, PNG and GIF into this types.<br>
 * The target color space is sRGB.<br>
 * All size parameters are maximum values. The image size ratio will is preserved.<br>
 * Quality takes precedence over speed.<br>
 * If the method signature does not contain the target image format, the same is used as the source image.<br>
 * If the method signature does not contain a {@link MetadataProvider}, the target image format dependent
 * {@link MetadataProvider} is used. See {@link MetadataProviders}.<br>
 */
public interface ImageScaler {

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
    Path scaleByWidth(Path source, Path target, int targetWidth) throws IOException;

    /**
     * Scale method, see {@link ImageScaler} documentation for general information.
     * 
     * @param source
     *            the source file
     * @param target
     *            the target file. If the file exists, it will be overwritten.
     * @param targetWidth
     *            the target width. The height is calculated with a constant aspect ratio.
     * @param targetFormat
     *            the image target format
     * @param metadataProvider
     *            the {@link MetadataProvider} to write the target image metadata
     * @return the path of the target file.
     * @throws IOException
     *             error during the scaling process.
     */
    Path scaleByWidth(Path source, Path target, int targetWidth, ImageFormat targetFormat,
            MetadataProvider metadataProvider) throws IOException;

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
    Path scaleByHeight(Path source, Path target, int targetHeight) throws IOException;

    /**
     * Scale method, see {@link ImageScaler} documentation for general information.
     * 
     * @param source
     *            the source file
     * @param target
     *            the target file. If the file exists, it will be overwritten.
     * @param targetHeight
     *            the target height. The width is calculated with a constant aspect ratio.
     * @param targetFormat
     *            the image target format
     * @param metadataProvider
     *            the {@link MetadataProvider} to write the target image metadata
     * @return the path of the target file.
     * @throws IOException
     *             error during the scaling process.
     */
    Path scaleByHeight(Path source, Path target, int targetHeight, ImageFormat targetFormat,
            MetadataProvider metadataProvider) throws IOException;

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
    Path scaleByBox(Path source, Path target, Rectangle border) throws IOException;

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
     * @param targetFormat
     *            the image target format
     * @param metadataProvider
     *            the {@link MetadataProvider} to write the target image metadata
     * @return the path of the target file.
     * @throws IOException
     *             error during the scaling process.
     */
    Path scaleByBox(Path source, Path target, Rectangle border, ImageFormat targetFormat,
            MetadataProvider metadataProvider) throws IOException;

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
    Map<Rectangle, Path> scaleByBox(Path source, Path targetFolder, List<Rectangle> borders) throws IOException;

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
     * @param targetFormat
     *            the image target format
     * @param metadataProvider
     *            the {@link MetadataProvider} to write the target image metadata
     * @return a {@link Map} with the calculated target sizes and the created target files.
     * @throws IOException
     *             error during the scaling process.
     */
    Map<Rectangle, Path> scaleByBox(Path source, Path targetFolder, List<Rectangle> borders, ImageFormat targetFormat,
            MetadataProvider metadataProvider) throws IOException;

    /**
     * Scale an image into a collection of images. It is a low level method.
     * 
     * @param sourceFile
     *            the source file
     * @param borderCalculation
     *            Function which returns the target box sizes. Input is the original size from the source file. Has to
     *            return an {@link Iterable} of target sizes.
     * @param outputProvider
     *            Function which provide the concrete {@link OutputStream} for an target image. Input is the target size
     *            provided from the <code>borderCalculation</code> parameter. Has to return a {@link OutputStream}.
     *            <b>Attention: This stream is closed inside the method.</b> Function can throw an {@link IOException}
     *            which stops the conversion. This exception is thrown by this method.
     * 
     * @throws IOException
     *             An error occurs. Can throw during reading the source image, providing a target {@link OutputStream}
     *             or writing into this stream.
     */
    void scale(Path sourceFile, Function<Rectangle, Iterable<Rectangle>> borderCalculation,
            IOExFunction<Rectangle, OutputStream> outputProvider) throws IOException;

    /**
     * Scale an image into a collection of images. It is a low level method.
     * 
     * @param sourceFile
     *            the source file
     * @param borderCalculation
     *            Function which returns the target box sizes. Input is the original size from the source file. Has to
     *            return an {@link Iterable} of target sizes.
     * @param targetFormat
     *            the image target format
     * @param metadataProvider
     *            the {@link MetadataProvider} to write the target image metadata
     * @param outputProvider
     *            Function which provide the concrete {@link OutputStream} for an target image. Input is the target size
     *            provided from the <code>borderCalculation</code> parameter. Has to return a {@link OutputStream}.
     *            <b>Attention: This stream is closed inside the method.</b> Function can throw an {@link IOException}
     *            which stops the conversion. This exception is thrown by this method.
     * 
     * @throws IOException
     *             An error occurs. Can throw during reading the source image, providing a target {@link OutputStream}
     *             or writing into this stream.
     */
    void scale(Path sourceFile, Function<Rectangle, Iterable<Rectangle>> borderCalculation, ImageFormat targetFormat,
            MetadataProvider metadataProvider, IOExFunction<Rectangle, OutputStream> outputProvider) throws IOException;
}