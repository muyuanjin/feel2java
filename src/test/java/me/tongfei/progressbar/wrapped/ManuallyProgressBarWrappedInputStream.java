package me.tongfei.progressbar.wrapped;

import me.tongfei.progressbar.ManuallyProgressBar;
import org.jetbrains.annotations.NotNull;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Any input stream whose progress is tracked by a progress bar.
 *
 * @author Tongfei Chen
 */
public class ManuallyProgressBarWrappedInputStream extends FilterInputStream {
    private final ManuallyProgressBar pb;
    private long mark = 0;

    public ManuallyProgressBarWrappedInputStream(InputStream in, ManuallyProgressBar pb) {
        super(in);
        this.pb = pb;
    }

    public ManuallyProgressBar getProgressBar() {
        return pb;
    }

    @Override
    public int read() throws IOException {
        int r = in.read();
        if (r != -1) pb.step();
        return r;
    }

    @Override
    public int read(byte @NotNull [] b) throws IOException {
        int r = in.read(b);
        if (r != -1) pb.stepBy(r);
        return r;
    }

    @Override
    public int read(byte @NotNull [] b, int off, int len) throws IOException {
        int r = in.read(b, off, len);
        if (r != -1) pb.stepBy(r);
        return r;
    }

    @Override
    public long skip(long n) throws IOException {
        long r = in.skip(n);
        pb.stepBy(r);
        return r;
    }

    @Override
    public void mark(int readLimit) {
        in.mark(readLimit);
        mark = pb.getCurrent();
    }

    @Override
    public void reset() throws IOException {
        in.reset();
        pb.stepTo(mark);
    }

    @Override
    public void close() throws IOException {
        in.close();
        pb.close();
    }

}
