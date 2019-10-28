package info.kuechler.image.scale.metadata.impl;

import java.awt.color.ICC_ColorSpace;
import java.awt.image.BufferedImage;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;

import info.kuechler.image.scale.ImageFormat;
import info.kuechler.image.scale.metadata.MetadataProvider;

public class DefaultMetadataProvider implements MetadataProvider {
    @Override
    public boolean canHandle(final ImageFormat format) {
        return true;
    }

    @Override
    public IIOMetadata getMetadata(final ImageWriter writer, final BufferedImage source,
            final ICC_ColorSpace outColorSpace, String colorProfileName, final ImageWriteParam param)
            throws IIOInvalidTreeException {
        return writer.getDefaultImageMetadata(ImageTypeSpecifier.createFromRenderedImage(source), param);
    }
}
