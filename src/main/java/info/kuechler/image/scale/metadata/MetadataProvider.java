package info.kuechler.image.scale.metadata;

import java.awt.color.ICC_ColorSpace;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;

import info.kuechler.image.scale.ImageFormat;

public interface MetadataProvider {

    IIOMetadata getMetadata(ImageWriter writer, BufferedImage source, ICC_ColorSpace outColorSpace,
            String colorProfileName, ImageWriteParam param) throws IOException;

    boolean canHandle(final ImageFormat format);
}