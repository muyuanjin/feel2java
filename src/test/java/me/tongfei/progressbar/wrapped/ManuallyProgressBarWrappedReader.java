package me.tongfei.progressbar.wrapped;

import me.tongfei.progressbar.ManuallyProgressBar;
import org.jetbrains.annotations.NotNull;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

/**
 * A reader whose progress is tracked by a progress bar.
 *
 * @author Tongfei Chen
 */
public class ManuallyProgressBarWrappedReader extends FilterReader {
    private final ManuallyProgressBar pb;
    private long mark = 0;

    public ManuallyProgressBarWrappedReader(Reader in, ManuallyProgressBar pb) {
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
    public int read(char @NotNull [] b) throws IOException {
        int r = in.read(b);
        if (r != -1) pb.stepBy(r);
        return r;
    }

    @Override
    public int read(char[] b, int off, int len) throws IOException {
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
    public void mark(int readAheadLimit) throws IOException {
        in.mark(readAheadLimit);
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
