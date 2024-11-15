package com.muyuanjin.feel.util;

import com.diogonunes.jcolor.Attribute;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import me.tongfei.progressbar.ManuallyProgressBar;
import me.tongfei.progressbar.ManuallyProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;

import java.io.FileDescriptor;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.diogonunes.jcolor.Ansi.colorize;
import static com.diogonunes.jcolor.Attribute.*;

/**
 * @author muyuanjin
 */
@UtilityClass
public class BenchmarkUtil {
    public static void benchmark1000(IRunnable runnable) {
        benchmark(1000, 1000, runnable);
    }

    public static void benchmark100(IRunnable runnable) {
        benchmark(100, 100, runnable);
    }

    public static void benchmark10(IRunnable runnable) {
        benchmark(10, 10, runnable);
    }

    public static void benchmark(IRunnable runnable, int times) {
        int sqrt = (int) Math.sqrt(times);
        benchmark(sqrt, sqrt, runnable);
    }

    private static final Attribute[] BOX_STYLE = {BRIGHT_RED_TEXT(), BOLD()};
    private static final Attribute[] BAR_STYLE = {BRIGHT_BLUE_TEXT(), ITALIC(), BOLD()};
    private static final Attribute[] TEXT_STYLE = {BRIGHT_YELLOW_TEXT(), BOLD()};

    /**
     * 多线程测试，自动打印结果
     *
     * @param threadNum  线程数量
     * @param forEachNum 每个线程重复次数
     * @param runnable   运行对象
     */
    @SneakyThrows
    public static void benchmark(int threadNum, int forEachNum, IRunnable runnable) {
        refresh();
        long start = System.nanoTime();
        CountDownLatch count = new CountDownLatch(threadNum);
        ManuallyProgressBarBuilder pbb = new ManuallyProgressBarBuilder()
                .setInitialMax((long) threadNum * forEachNum)
                .setStyle(ProgressBarStyle.COLORFUL_UNICODE_BLOCK)
                .setTaskName("Benchmark")
                .setUpdateIntervalMillis(200)
                .setUnit("次", 1);
        try (ManuallyProgressBar bar = pbb.build()) {
            for (int i = 0; i < threadNum; i++) {
                new Thread(() -> {
                    for (int j = 0; j < forEachNum; j++) {
                        runnable.run();
                        bar.step();
                    }
                    count.countDown();
                }).start();
            }
            count.await();
            FileDescriptor.err.sync();
        }
        long nsAll = System.nanoTime() - start;
        int styleLength = colorize("", TEXT_STYLE).length();
        String lineOne = colorize("运行次数", TEXT_STYLE) + "：  " + (long) threadNum * forEachNum;
        String lineTwo = colorize("总计耗时", TEXT_STYLE) + "：  " + TimeUnit.NANOSECONDS.toMillis(nsAll) + "ms";
        long nsPer = nsAll / forEachNum / threadNum;
        String lineThree;
        if (nsPer < 1000) {
            lineThree = colorize("平均耗时", TEXT_STYLE) + "：  " + nsPer + "ns";
        } else {
            lineThree = colorize("平均耗时", TEXT_STYLE) + "：  " + TimeUnit.NANOSECONDS.toMillis(nsPer * 1000) / 1000F + "ms";
        }

        // Banner文字和彩虹渐变效果
        String bannerText = "BENCHMARK";
        // 根据Banner宽度调整内容宽度，并打印内容
        int contentWidth = bannerText.length() + 22; // 加上边框的额外宽度
        // 打印顶
        printTopBanner(bannerText, contentWidth);
        // 打印内容
        printContentLine(contentWidth + styleLength, lineOne, lineTwo, lineThree);
        // 打印底部边框
        printBottomBorder(contentWidth);
        refresh();
        System.out.println();
    }

    @SneakyThrows
    private static void refresh() {
        System.out.flush();
        FileDescriptor.out.sync();
        System.err.flush();
        FileDescriptor.err.sync();
    }

    // 打印顶部边框
    private static void printTopBanner(String banner, int width) {
        if (width <= banner.length() - 2) {
            width = banner.length() + 2;
        }
        System.out.print(colorize("╭", BOX_STYLE));
        int bannerLength = banner.length();

        for (int i = 0; i < (width - bannerLength) / 2; i++) {
            System.out.print(colorize("─", BOX_STYLE));
        }

        System.out.print(colorize(banner, BAR_STYLE));
        for (int i = 0; i < (width - bannerLength) / 2; i++) {
            System.out.print(colorize("─", BOX_STYLE));
        }
        System.out.println(colorize("╮", BOX_STYLE));
    }

    // 居中打印一行，根据banner宽度调整
    private static void printContentLine(int width, String... lines) {
        int maxLength = 0;
        for (String line : lines) {
            maxLength = Math.max(maxLength, line.length());
        }
        int padding = (width - maxLength - 2) / 2; // 减去边框字符
        for (String line : lines) {
            System.out.print(colorize("│", BOX_STYLE));
            System.out.print(" ".repeat(padding) + line);
            System.out.println(" ".repeat(width - padding - line.length() - 3) + colorize("│", BOX_STYLE));
        }
    }

    // 打印底部边框
    private static void printBottomBorder(int width) {
        System.out.print(colorize("╰", BOX_STYLE));
        for (int i = 0; i < width; i++) {
            System.out.print(colorize("─", BOX_STYLE));
        }
        System.out.println(colorize("╯", BOX_STYLE));
    }
}
