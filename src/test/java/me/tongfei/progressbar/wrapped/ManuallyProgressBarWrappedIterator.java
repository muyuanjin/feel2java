package me.tongfei.progressbar.wrapped;

import me.tongfei.progressbar.ManuallyProgressBar;

import java.util.Iterator;

/**
 * Any iterator whose iteration is tracked by a progress bar.
 *
 * @author Tongfei Chen
 */
public class ManuallyProgressBarWrappedIterator<T> implements Iterator<T>, AutoCloseable {
    private final Iterator<T> underlying;
    private final ManuallyProgressBar pb;

    public ManuallyProgressBarWrappedIterator(Iterator<T> underlying, ManuallyProgressBar pb) {
        this.underlying = underlying;
        this.pb = pb;
    }

    public ManuallyProgressBar getProgressBar() {
        return pb;
    }

    @Override
    public boolean hasNext() {
        boolean r = underlying.hasNext();
        if (!r) pb.close();
        return r;
    }

    @Override
    public T next() {
        T r = underlying.next();
        pb.step();
        return r;
    }

    @Override
    public void remove() {
        underlying.remove();
    }

    @Override
    public void close() {
        pb.close();
    }
}
