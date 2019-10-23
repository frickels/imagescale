package info.kuechler.image.scale.scale;

import java.io.IOException;

@FunctionalInterface
public interface IOExFunction<T, R> {
    R apply(T t) throws IOException;
}