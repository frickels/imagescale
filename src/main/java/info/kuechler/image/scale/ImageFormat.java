package info.kuechler.image.scale;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public enum ImageFormat {
    JPG("JPEG", Arrays.asList("jpg", "jpeg"), Arrays.asList("image/jpeg")), //
    GIF("GIF", Arrays.asList("gif"), Arrays.asList("image/gif")), //
    PNG("PNG", Arrays.asList("png"), Arrays.asList("image/png"));

    private final String formatName;
    private final List<String> fileEndings;
    private final List<String> mimeTypes;

    private ImageFormat(final String formatName, final List<String> fileEndings, final List<String> mimeTypes) {
        this.formatName = formatName;
        this.fileEndings = Collections.unmodifiableList(fileEndings);
        this.mimeTypes = Collections.unmodifiableList(mimeTypes);
    }

    public String getFormatName() {
        return formatName;
    }

    public List<String> getFileEndings() {
        return fileEndings;
    }

    public List<String> getMimeTypes() {
        return mimeTypes;
    }

    public static Optional<ImageFormat> detectImageFormatByName(final String name) {
        return Arrays.stream(values()).filter(format -> {
            return format.getFileEndings().stream()
                    .filter(ending -> ('.' + ending)
                            .equalsIgnoreCase(name.substring(Math.max(0, name.length() - ending.length() - 1))))
                    .findAny().isPresent();
        }).findAny();
    }

    public static Optional<ImageFormat> detectImageFormatByFileEnding(final String fileEnding) {
        return Arrays.stream(values()).filter(format -> {
            return format.getFileEndings().stream().filter(ending -> ending.equalsIgnoreCase(fileEnding)).findAny()
                    .isPresent();
        }).findAny();
    }

    public static Optional<ImageFormat> detectImageFormatByMime(final String mimeType) {
        return Arrays.stream(values()).filter(format -> {
            return format.getMimeTypes().stream().filter(mime -> mime.equalsIgnoreCase(mimeType)).findAny().isPresent();
        }).findAny();
    }

}
