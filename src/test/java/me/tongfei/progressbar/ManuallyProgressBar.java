package me.tongfei.progressbar;

import me.tongfei.progressbar.wrapped.*;

import java.io.*;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.stream.BaseStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static me.tongfei.progressbar.Util.createConsoleConsumer;

/**
 * @author muyuanjin
 */
@SuppressWarnings("UnusedReturnValue")
public class ManuallyProgressBar implements AutoCloseable {
    private final ProgressState progress;
    private final ProgressBarConsumer consumer;
    private final ProgressUpdateAction action;
    private final boolean clearDisplayOnFinish;
    private final long updateIntervalMillis;
    private volatile long lastUpdate;

    /**
     * Creates a progress bar with the specific taskName name and initial maximum value.
     *
     * @param task       Task name
     * @param initialMax Initial maximum value
     */
    public ManuallyProgressBar(String task, long initialMax) {
        this(
                task, initialMax, 1000, false, false,
                System.err, ProgressBarStyle.COLORFUL_UNICODE_BLOCK,
                "", 1L, false, null,
                ChronoUnit.SECONDS, 0L, Duration.ZERO
        );
    }

    /**
     * Creates a progress bar with the specific taskName name, initial maximum value,
     * customized update interval (default 1000 ms), the PrintStream to be used, and output style.
     *
     * @param task             Task name
     * @param initialMax       Initial maximum value
     * @param continuousUpdate Rerender every time the update interval happens regardless of progress count.
     * @param style            Draw style
     * @param showSpeed        Should the calculated speed be displayed
     * @param speedFormat      Speed number format
     */
    public ManuallyProgressBar(
            String task,
            long initialMax,
            long updateIntervalMillis,
            boolean continuousUpdate,
            boolean clearDisplayOnFinish,
            PrintStream os,
            ProgressBarStyle style,
            String unitName,
            long unitSize,
            boolean showSpeed,
            DecimalFormat speedFormat,
            ChronoUnit speedUnit,
            long processed,
            Duration elapsed
    ) {
        this(task, initialMax, updateIntervalMillis, continuousUpdate, clearDisplayOnFinish, processed, elapsed,
                new DefaultProgressBarRenderer(
                        style, unitName, unitSize,
                        showSpeed, speedFormat, speedUnit,
                        true, Util::linearEta
                ),
                createConsoleConsumer(os)
        );
    }

    /**
     * Creates a progress bar with the specific name, initial maximum value, customized update interval (default 1s),
     * and the provided progress bar renderer ({@link ProgressBarRenderer}) and consumer ({@link ProgressBarConsumer}).
     *
     * @param task             Task name
     * @param initialMax       Initial maximum value
     * @param continuousUpdate Rerender every time the update interval happens regardless of progress count.
     * @param processed        Initial completed process value
     * @param elapsed          Initial elapsedBeforeStart second before
     * @param renderer         Progress bar renderer
     * @param consumer         Progress bar consumer
     */
    public ManuallyProgressBar(
            String task,
            long initialMax,
            long updateIntervalMillis,
            boolean continuousUpdate,
            boolean clearDisplayOnFinish,
            long processed,
            Duration elapsed,
            ProgressBarRenderer renderer,
            ProgressBarConsumer consumer
    ) {
        this.consumer = consumer;
        this.progress = new ProgressState(task, initialMax, processed, elapsed);
        this.action = new ProgressUpdateAction(progress, renderer, consumer, continuousUpdate, clearDisplayOnFinish);
        this.updateIntervalMillis = updateIntervalMillis;
        this.clearDisplayOnFinish = clearDisplayOnFinish;
        this.action.refresh();
        this.lastUpdate = System.currentTimeMillis();
    }

    /**
     * Advances this progress bar by a specific amount.
     *
     * @param n Step size
     */
    public ManuallyProgressBar stepBy(long n) {
        progress.stepBy(n);
        refresh();
        return this;
    }

    /**
     * Advances this progress bar to the specific progress value.
     *
     * @param n New progress value
     */
    public ManuallyProgressBar stepTo(long n) {
        boolean back = n < progress.current;
        progress.stepTo(n);
        if (back) {
            action.forceRefresh();  // fix #124
        } else {
            refresh();
        }
        return this;
    }

    /**
     * Advances this progress bar by one step.
     */
    public ManuallyProgressBar step() {
        progress.stepBy(1);
        refresh();
        return this;
    }

    private void refresh() {
        long now = System.currentTimeMillis();
        if (now - lastUpdate > updateIntervalMillis) synchronized (this) {
            if (now - lastUpdate > updateIntervalMillis) {
                action.refresh();
                lastUpdate = now;
            }
        }
    }


    /**
     * Gives a hint to the maximum value of the progress bar.
     *
     * @param n Hint of the maximum value. A value of -1 indicates that the progress bar is indefinite.
     */
    public ManuallyProgressBar maxHint(long n) {
        if (n < 0)
            progress.setAsIndefinite();
        else {
            progress.setAsDefinite();
            progress.maxHint(n);
        }
        return this;
    }

    /**
     * Pauses this current progress.
     */
    public ManuallyProgressBar pause() {
        progress.pause();
        return this;
    }

    /**
     * Resumes this current progress.
     */
    public ManuallyProgressBar resume() {
        progress.resume();
        return this;
    }

    /** Resets the progress bar to its initial state (where progress equals to 0). */
    public ManuallyProgressBar reset() {
        progress.reset();
        action.forceRefresh();  // force refresh, fixing #124
        return this;
    }

    /**
     * <p>Stops this progress bar, effectively stops tracking the underlying process.</p>
     * <p>Implements the {@link AutoCloseable} interface which enables the try-with-resource
     * pattern with progress bars.</p>
     *
     */
    @Override
    public void close() {
        action.refresh();
        if (clearDisplayOnFinish) consumer.clear();
        consumer.close();
        TerminalUtils.closeTerminal();
    }

    /**
     * Sets the extra message at the end of the progress bar.
     *
     * @param msg New message
     */
    public ManuallyProgressBar setExtraMessage(String msg) {
        progress.setExtraMessage(msg);
        return this;
    }

    /**
     * Returns the current progress.
     */
    public long getCurrent() {
        return progress.getCurrent();
    }

    /**
     * Returns the maximum value of this progress bar.
     */
    public long getMax() {
        return progress.getMax();
    }

    public long getStart() {
        return progress.getStart();
    }

    /**
     * Returns the progress normalized to the interval [0, 1].
     */
    public double getNormalizedProgress() {
        return progress.getNormalizedProgress();
    }

    /**
     * Returns the instant when the progress bar started.
     * If a progress bar is resumed after a pause, it returns the instant when the progress was resumed.
     */
    public Instant getStartInstant() {
        return progress.startInstant;
    }

    /**
     * Returns the duration that this progress bar has been running before it was resumed.
     * If a progress bar starts afresh, it should return zero.
     */
    public Duration getElapsedBeforeStart() {
        return progress.getElapsedBeforeStart();
    }

    /**
     * Returns the duration that this progress bar has been running after it was resumed.
     * If a progress bar has not been paused before, it should return the total duration starting from creation.
     */
    public Duration getElapsedAfterStart() {
        return progress.getElapsedAfterStart();
    }

    /**
     * Returns the total duration that this progress bar has been running from start,
     * excluding the period when it has been paused.
     */
    public Duration getTotalElapsed() {
        return progress.getTotalElapsed();
    }

    /**
     * Returns the name of this task.
     */
    public String getTaskName() {
        return progress.getTaskName();
    }

    /**
     * Returns the extra message at the end of the progress bar.
     */
    public String getExtraMessage() {
        return progress.getExtraMessage();
    }

    /** Checks if the progress bar is indefinite, i.e., its maximum value is unknown. */
    public boolean isIndefinite() {
        return progress.indefinite;
    }

    // STATIC WRAPPER METHODS

    /**
     * Wraps an iterator so that when iterated, a progress bar is shown to track the traversal progress.
     *
     * @param it   Underlying iterator
     * @param task Task name
     */
    public static <T> Iterator<T> wrap(Iterator<T> it, String task) {
        return wrap(it,
                new ManuallyProgressBarBuilder().setTaskName(task).setInitialMax(-1)
        ); // indefinite progress bar
    }

    /**
     * Wraps an iterator so that when iterated, a progress bar is shown to track the traversal progress.
     *
     * @param it  Underlying iterator
     * @param pbb Progress bar builder
     */
    public static <T> Iterator<T> wrap(Iterator<T> it, ManuallyProgressBarBuilder pbb) {
        return new ManuallyProgressBarWrappedIterator<>(it, pbb.build());
    }

    /**
     * Wraps an {@link Iterable} so that when iterated, a progress bar is shown to track the traversal progress.
     * <p>
     * Sample usage: {@code
     * for (T x : ProgressBar.wrap(collection, "Traversal")) { ... }
     * }
     * </p>
     *
     * @param ts   Underlying iterable
     * @param task Task name
     */
    public static <T> Iterable<T> wrap(Iterable<T> ts, String task) {
        return wrap(ts, new ManuallyProgressBarBuilder().setTaskName(task));
    }

    /**
     * Wraps an {@link Iterable} so that when iterated, a progress bar is shown to track the traversal progress.
     * For this function the progress bar can be fully customized by using a {@link ManuallyProgressBarBuilder}.
     *
     * @param ts  Underlying iterable
     * @param pbb An instance of a {@link ManuallyProgressBarBuilder}
     */
    public static <T> Iterable<T> wrap(Iterable<T> ts, ManuallyProgressBarBuilder pbb) {
        if (!pbb.initialMaxIsSet())
            pbb.setInitialMax(Util.getSpliteratorSize(ts.spliterator()));
        return new ManuallyProgressBarWrappedIterable<>(ts, pbb);
    }

    /**
     * Wraps an {@link InputStream} so that when read, a progress bar is shown to track the reading progress.
     *
     * @param is   Input stream to be wrapped
     * @param task Name of the progress
     */
    public static InputStream wrap(InputStream is, String task) {
        ManuallyProgressBarBuilder pbb = new ManuallyProgressBarBuilder().setTaskName(task);
        return wrap(is, pbb);
    }

    /**
     * Wraps an {@link InputStream} so that when read, a progress bar is shown to track the reading progress.
     * For this function the progress bar can be fully customized by using a {@link ManuallyProgressBarBuilder}.
     *
     * @param is  Input stream to be wrapped
     * @param pbb An instance of a {@link ManuallyProgressBarBuilder}
     */
    public static InputStream wrap(InputStream is, ManuallyProgressBarBuilder pbb) {
        if (!pbb.initialMaxIsSet())
            pbb.setInitialMax(Util.getInputStreamSize(is));
        return new ManuallyProgressBarWrappedInputStream(is, pbb.build());
    }

    /**
     * Wraps an {@link OutputStream} so that when written, a progress bar is shown to track the writing progress.
     *
     * @param os   Output stream to be wrapped
     * @param task Name of the progress
     */
    public static OutputStream wrap(OutputStream os, String task) {
        ManuallyProgressBarBuilder pbb = new ManuallyProgressBarBuilder().setTaskName(task);
        return wrap(os, pbb);
    }

    /**
     * Wraps an {@link OutputStream} so that when written, a progress bar is shown to track the writing progress.
     * For this function the progress bar can be fully customized by using a {@link ManuallyProgressBarBuilder}.
     *
     * @param os  Output stream to be wrapped
     * @param pbb An instance of a {@link ManuallyProgressBarBuilder}
     */
    public static OutputStream wrap(OutputStream os, ManuallyProgressBarBuilder pbb) {
        return new ManuallyProgressBarWrappedOutputStream(os, pbb.build());
    }

    /**
     * Wraps a {@link Reader} so that when read, a progress bar is shown to track the reading progress.
     *
     * @param reader Reader to be wrapped
     * @param task   Name of the progress
     */
    public static Reader wrap(Reader reader, String task) {
        ManuallyProgressBarBuilder pbb = new ManuallyProgressBarBuilder().setTaskName(task);
        return wrap(reader, pbb);
    }

    /**
     * Wraps a {@link Reader} so that when read, a progress bar is shown to track the reading progress.
     * For this function the progress bar can be fully customized by using a {@link ManuallyProgressBarBuilder}.
     *
     * @param reader Reader to be wrapped
     * @param pbb    An instance of a {@link ManuallyProgressBarBuilder}
     */
    public static Reader wrap(Reader reader, ManuallyProgressBarBuilder pbb) {
        return new ManuallyProgressBarWrappedReader(reader, pbb.build());
    }

    /**
     * Wraps a {@link Writer} so that when written, a progress bar is shown to track the writing progress.
     *
     * @param writer Writer to be wrapped
     * @param task   Name of the progress
     */
    public static Writer wrap(Writer writer, String task) {
        ManuallyProgressBarBuilder pbb = new ManuallyProgressBarBuilder().setTaskName(task);
        return wrap(writer, pbb);
    }

    /**
     * Wraps a {@link Writer} so that when written, a progress bar is shown to track the writing progress.
     * For this function the progress bar can be fully customized by using a {@link ManuallyProgressBarBuilder}.
     *
     * @param writer Writer to be wrapped
     * @param pbb    An instance of a {@link ManuallyProgressBarBuilder}
     */
    public static Writer wrap(Writer writer, ManuallyProgressBarBuilder pbb) {
        return new ManuallyProgressBarWrappedWriter(writer, pbb.build());
    }

    /**
     * Wraps a {@link Spliterator} so that when iterated, a progress bar is shown to track the traversal progress.
     *
     * @param sp   Underlying spliterator
     * @param task Task name
     */
    public static <T> Spliterator<T> wrap(Spliterator<T> sp, String task) {
        ManuallyProgressBarBuilder pbb = new ManuallyProgressBarBuilder().setTaskName(task);
        return wrap(sp, pbb);
    }

    /**
     * Wraps a {@link Spliterator} so that when iterated, a progress bar is shown to track the traversal progress.
     * For this function the progress bar can be fully customized by using a {@link ManuallyProgressBarBuilder}.
     *
     * @param sp  Underlying spliterator
     * @param pbb An instance of a {@link ManuallyProgressBarBuilder}
     */
    public static <T> Spliterator<T> wrap(Spliterator<T> sp, ManuallyProgressBarBuilder pbb) {
        if (!pbb.initialMaxIsSet())
            pbb.setInitialMax(Util.getSpliteratorSize(sp));
        return new ManuallyProgressBarWrappedSpliterator<>(sp, pbb.build());
    }

    /**
     * Wraps a {@link Stream} so that when iterated, a progress bar is shown to track the traversal progress.
     *
     * @param stream Underlying stream (can be sequential or parallel)
     * @param task   Task name
     */
    public static <T, S extends BaseStream<T, S>> Stream<T> wrap(S stream, String task) {
        ManuallyProgressBarBuilder pbb = new ManuallyProgressBarBuilder().setTaskName(task);
        return wrap(stream, pbb);
    }

    /**
     * Wraps a {@link Stream} so that when iterated, a progress bar is shown to track the traversal progress.
     * For this function the progress bar can be fully customized by using a {@link ManuallyProgressBarBuilder}.
     *
     * @param stream Underlying stream (can be sequential or parallel)
     * @param pbb    An instance of a {@link ManuallyProgressBarBuilder}
     */
    public static <T, S extends BaseStream<T, S>> Stream<T> wrap(S stream, ManuallyProgressBarBuilder pbb) {
        Spliterator<T> sp = wrap(stream.spliterator(), pbb);
        return StreamSupport.stream(sp, stream.isParallel());
    }

    /**
     * Wraps an array so that when iterated, a progress bar is shown to track the traversal progress.
     *
     * @param array Array to be wrapped
     * @param task  Task name
     * @return Wrapped array, of type {@link Stream}.
     */
    public static <T> Stream<T> wrap(T[] array, String task) {
        ManuallyProgressBarBuilder pbb = new ManuallyProgressBarBuilder().setTaskName(task);
        return wrap(array, pbb);
    }

    /**
     * Wraps an array so that when iterated, a progress bar is shown to track the traversal progress.
     * For this function the progress bar can be fully customized by using a {@link ManuallyProgressBarBuilder}.
     *
     * @param array Array to be wrapped
     * @param pbb   An instance of a {@link ManuallyProgressBarBuilder}
     * @return Wrapped array, of type {@link Stream}.
     */
    public static <T> Stream<T> wrap(T[] array, ManuallyProgressBarBuilder pbb) {
        pbb.setInitialMax(array.length);
        return wrap(Arrays.stream(array), pbb);
    }

    /** Creates a new builder to customize a progress bar. */
    public static ManuallyProgressBarBuilder builder() {
        return new ManuallyProgressBarBuilder();
    }

}

