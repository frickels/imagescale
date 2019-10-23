package info.kuechler.image.scale.metadata;

import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;

import org.w3c.dom.Node;

public class PNGMetadataProvider implements MetadataProvider {
    @Override
    public IIOMetadata getMetadata(final ImageWriter writer, final BufferedImage source,
            final ICC_ColorSpace outColorSpace, final String colorProfileName, final ImageWriteParam param)
            throws IOException {
        final IIOMetadata metadata = writer.getDefaultImageMetadata(ImageTypeSpecifier.createFromRenderedImage(source),
                param);

        final Node nativeTree = metadata.getAsTree(metadata.getNativeMetadataFormatName());
        nativeTree.appendChild(createPngICCProfile(outColorSpace, colorProfileName));
        metadata.mergeTree(metadata.getNativeMetadataFormatName(), nativeTree);
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

    protected static IIOMetadataNode createPngICCProfile(final ICC_ColorSpace colorSpace, final String profileName)
            throws IOException {
        final IIOMetadataNode iccp = new IIOMetadataNode("iCCP");
        iccp.setUserObject(getAsDeflatedBytes(colorSpace));
        iccp.setAttribute("profileName", profileName);
        iccp.setAttribute("compressionMethod", "deflate");
        return iccp;
    }

    protected static byte[] getAsDeflatedBytes(final ICC_ColorSpace colorSpace) throws IOException {
        final byte[] data = colorSpace.getProfile().getData();
        try (final ByteArrayOutputStream deflated = new ByteArrayOutputStream();) {
            try (final DeflaterOutputStream deflater = new DeflaterOutputStream(deflated);) {
                deflater.write(data);
                deflater.flush();
            }
            deflated.flush();
            return deflated.toByteArray();
        }
    }
}
