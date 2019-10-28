package info.kuechler.image.scale.metadata;

import info.kuechler.image.scale.ImageFormat;
import info.kuechler.image.scale.metadata.impl.DefaultMetadataProvider;
import info.kuechler.image.scale.metadata.impl.JPEGMetadataProvider;
import info.kuechler.image.scale.metadata.impl.PNGMetadataProvider;

public class MetadataProviders {

    public static final DefaultMetadataProvider DEFAULT_METADATA_PROVIDER = new DefaultMetadataProvider();
    public static final PNGMetadataProvider PNG_METADATA_PROVIDER = new PNGMetadataProvider();
    public static final JPEGMetadataProvider JPEG_METADATA_PROVIDER = new JPEGMetadataProvider();

    public static MetadataProvider get(final ImageFormat imageFormat) {
        switch (imageFormat) {
        case JPG:
            return JPEG_METADATA_PROVIDER;
        case PNG:
            return PNG_METADATA_PROVIDER;
        case GIF:
        default:
            return DEFAULT_METADATA_PROVIDER;
        }
    }

    private MetadataProviders() {
        // nothing
    }
}
