package info.kuechler.image.scale;

import static java.lang.Math.ceil;

import java.io.Serializable;
import java.util.Objects;

public class Rectangle implements Serializable {
    private static final long serialVersionUID = 1438752533688387396L;

    private final int width;
    private final int height;

    public static Rectangle of(final int width, final int height) {
        return new Rectangle(width, height);
    }

    private Rectangle(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public String toString() {
        return String.format("Rectangle [%s x %s]", width, height);
    }

    @Override
    public int hashCode() {
        return Objects.hash(height, width);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Rectangle other = (Rectangle) obj;
        return height == other.height && width == other.width;
    }

    public static Rectangle getScaledByHeight(final Rectangle source, final int targetHeight) {
        final double imgFactor = toDouble(source.getWidth()) / toDouble(source.getHeight());
        final double newWidth = imgFactor * toDouble(targetHeight);
        return Rectangle.of(toInt(newWidth), targetHeight);
    }

    public static Rectangle getScaledByWidth(final Rectangle source, final int targetWidth) {
        final double imgFactor = toDouble(source.getHeight()) / toDouble(source.getWidth());
        final double newHeight = imgFactor * toDouble(targetWidth);
        return Rectangle.of(targetWidth, toInt(newHeight));
    }

    public static Rectangle getBorderRectagle(final Rectangle source, final Rectangle border) {
        final double sW = toDouble(source.getWidth());
        final double sH = toDouble(source.getHeight());
        final double bW = toDouble(border.getWidth());
        final double bH = toDouble(border.getHeight());

        if ((sH / sW) > (bH / bW)) {
            return Rectangle.of(toInt(sW * (bH / sH)), border.getHeight());
        } else {
            return Rectangle.of(border.getWidth(), toInt(sH * (bW / sW)));
        }
    }

    private static double toDouble(final int i) {
        return (double) i;
    }

    private static int toInt(final double d) {
        return (int) ceil(d);
    }
}