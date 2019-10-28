package info.kuechler.image.scale.metadata.impl;

import static info.kuechler.image.scale.ImageFormat.JPG;

import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;

import info.kuechler.image.scale.ImageFormat;
import info.kuechler.image.scale.metadata.MetadataProvider;

public class JPEGMetadataProvider implements MetadataProvider {

    @Override
    public boolean canHandle(final ImageFormat format) {
        return format == JPG;
    }

    @Override
    public IIOMetadata getMetadata(final ImageWriter writer, final BufferedImage source,
            final ICC_ColorSpace outColorSpace, String colorProfileName, final ImageWriteParam param)
            throws IIOInvalidTreeException {
        final IIOMetadata metadata = writer.getDefaultImageMetadata(ImageTypeSpecifier.createFromRenderedImage(source),
                param);
        metadata.mergeTree(metadata.getNativeMetadataFormatName(),
                createJpegICCTree(outColorSpace.getProfile(), metadata));
        return metadata;
    }

    protected static IIOMetadataNode createJpegICCTree(final ICC_Profile iccProfile, final IIOMetadata metadata) {
        final IIOMetadataNode root = new IIOMetadataNode(metadata.getNativeMetadataFormatName());

        final IIOMetadataNode jpegVariety = new IIOMetadataNode("JPEGvariety");
        root.appendChild(jpegVariety);
        root.appendChild(new IIOMetadataNode("markerSequence"));

        final IIOMetadataNode app0JFIF = new IIOMetadataNode("app0JFIF");
        jpegVariety.appendChild(app0JFIF);

        final IIOMetadataNode icc = new IIOMetadataNode("app2ICC");
        app0JFIF.appendChild(icc);
        icc.setUserObject(iccProfile);

        return root;
    }
}
