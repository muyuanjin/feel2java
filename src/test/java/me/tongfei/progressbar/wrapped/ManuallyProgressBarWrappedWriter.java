package me.tongfei.progressbar.wrapped;

import me.tongfei.progressbar.ManuallyProgressBar;
import org.jetbrains.annotations.NotNull;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * A writer whose progress is tracked by a progress bar.
 *
 * @author Tongfei Chen
 */
public class ManuallyProgressBarWrappedWriter extends FilterWriter {
    private final ManuallyProgressBar pb;

    public ManuallyProgressBarWrappedWriter(Writer out, ManuallyProgressBar pb) {
        super(out);
        this.pb = pb;
    }

    @Override
    public void write(int c) throws IOException {
        out.write(c);
        pb.step();
    }

    @Override
    public void write(char[] buff, int off, int len) throws IOException {
        out.write(buff, off, len);
        pb.stepBy(len);
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        out.write(str, off, len);
        pb.stepBy(len);
    }

    @Override
    public void write(@NotNull String str) throws IOException {
        out.write(str);
        pb.stepBy(str.length());
    }

    @Override
    public void close() throws IOException {
        out.close();
        pb.close();
    }
}
