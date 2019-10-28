package info.kuechler.image.scale;

import java.io.IOException;
import java.util.function.Function;

/**
 * 
 * {@link Function} which throws an {@link IOException}.
 *
 * @param <T>
 *            input value
 * @param <R>
 *            output value
 */
@FunctionalInterface
public interface IOExFunction<T, R> {
    R apply(T t) throws IOException;
}