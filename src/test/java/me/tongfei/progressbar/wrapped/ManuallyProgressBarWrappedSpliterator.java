package me.tongfei.progressbar.wrapped;

import me.tongfei.progressbar.ManuallyProgressBar;

import java.util.*;
import java.util.function.Consumer;

/**
 * Any spliterator whose parallel iteration is tracked by a multi-threaded progress bar.
 *
 * @author Tongfei Chen
 */
public class ManuallyProgressBarWrappedSpliterator<T> implements Spliterator<T>, AutoCloseable {
    private final Spliterator<T> underlying;
    private final ManuallyProgressBar pb;
    private final Set<Spliterator<T>> openChildren;

    public ManuallyProgressBarWrappedSpliterator(Spliterator<T> underlying, ManuallyProgressBar pb) {
        this(underlying, pb, Collections.synchronizedSet(new HashSet<>())); // has to be synchronized
    }

    private ManuallyProgressBarWrappedSpliterator(Spliterator<T> underlying, ManuallyProgressBar pb, Set<Spliterator<T>> openChildren) {
        this.underlying = underlying;
        this.pb = pb;
        this.openChildren = openChildren;
        this.openChildren.add(this);
    }

    public ManuallyProgressBar getProgressBar() {
        return pb;
    }

    @Override
    public void close() {
        pb.close();
    }

    private void registerChild(Spliterator<T> child) {
        openChildren.add(child);
    }

    private void removeThis() {
        openChildren.remove(this);
        if (openChildren.isEmpty()) close();
        // only closes the progressbar if no spliterator is working anymore
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        boolean r = underlying.tryAdvance(action);
        if (r) pb.step();
        else removeThis();
        return r;
    }

    @Override
    public Spliterator<T> trySplit() {
        Spliterator<T> u = underlying.trySplit();
        if (u != null) {
            ManuallyProgressBarWrappedSpliterator<T> child = new ManuallyProgressBarWrappedSpliterator<>(u, pb, openChildren);
            registerChild(child);
            return child;
        } else return null;
    }

    @Override
    public long estimateSize() {
        return underlying.estimateSize();
    }

    @Override
    public int characteristics() {
        return underlying.characteristics();
    }

    @Override // if not overridden, may return null since that is the default Spliterator implementation
    public Comparator<? super T> getComparator() {
        return underlying.getComparator();
    }

}
