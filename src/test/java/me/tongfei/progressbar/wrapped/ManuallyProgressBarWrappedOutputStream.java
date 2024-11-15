package me.tongfei.progressbar.wrapped;

import me.tongfei.progressbar.ManuallyProgressBar;
import org.jetbrains.annotations.NotNull;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ManuallyProgressBarWrappedOutputStream extends FilterOutputStream {
    private final ManuallyProgressBar pb;

    public ManuallyProgressBarWrappedOutputStream(OutputStream out, ManuallyProgressBar pb) {
        super(out);
        this.pb = pb;
    }

    public ManuallyProgressBar getProgressBar() {
        return pb;
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
        pb.step();
    }

    @Override
    public void write(byte @NotNull [] b) throws IOException {
        out.write(b, 0, b.length);
        pb.stepBy(b.length);
    }

    @Override
    public void write(byte @NotNull [] b, int off, int len) throws IOException {
        out.write(b, off, len);
        pb.stepBy(len);
    }

    @Override
    public void close() throws IOException {
        out.close();
        pb.close();
    }
}
