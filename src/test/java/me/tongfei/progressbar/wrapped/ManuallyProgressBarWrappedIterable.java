package me.tongfei.progressbar.wrapped;

import me.tongfei.progressbar.ManuallyProgressBarBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

/**
 * Any iterable, when being iterated over, is tracked by a progress bar.
 *
 * @author Tongfei Chen
 */
public class ManuallyProgressBarWrappedIterable<T> implements Iterable<T> {
    private final Iterable<T> underlying;
    private final ManuallyProgressBarBuilder pbb;

    public ManuallyProgressBarWrappedIterable(Iterable<T> underlying, ManuallyProgressBarBuilder pbb) {
        this.underlying = underlying;
        this.pbb = pbb;
    }

    public ManuallyProgressBarBuilder getProgressBarBuilder() {
        return pbb;
    }

    @Override
    public @NotNull ManuallyProgressBarWrappedIterator<T> iterator() {
        Iterator<T> it = underlying.iterator();
        return new ManuallyProgressBarWrappedIterator<>(
                it,
                pbb.setInitialMax(underlying.spliterator().getExactSizeIfKnown()).build()
                // getExactSizeIfKnown return -1 if not known, then indefinite progress bar naturally
        );
    }
}
