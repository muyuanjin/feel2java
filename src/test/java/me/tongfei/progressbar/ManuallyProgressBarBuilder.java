package me.tongfei.progressbar;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author muyuanjin
 */
public class ManuallyProgressBarBuilder {
    private String taskName = "";
    private long initialMax = -1;
    private long updateIntervalMillis = 1000;
    private boolean continuousUpdate = false;
    private ProgressBarStyle style = ProgressBarStyle.COLORFUL_UNICODE_BLOCK;
    private ProgressBarConsumer consumer = null;
    private boolean clearDisplayOnFinish = false;
    private String unitName = "";
    private long unitSize = 1;
    private boolean showSpeed = false;
    private boolean hideEta = false;
    private Function<ProgressState, Optional<Duration>> eta = Util::linearEta;
    private DecimalFormat speedFormat;
    private ChronoUnit speedUnit = ChronoUnit.SECONDS;
    private long processed = 0;
    private Duration elapsed = Duration.ZERO;
    private int maxRenderedLength = -1;

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean initialMaxIsSet() {
        return this.initialMax != -1;
    }

    public ManuallyProgressBarBuilder showSpeed() {
        this.showSpeed = true;
        return this;
    }

    public ManuallyProgressBarBuilder setUnit(String unitName, long unitSize) {
        this.unitName = unitName;
        this.unitSize = unitSize;
        return this;
    }

    public ManuallyProgressBar build() {
        return new ManuallyProgressBar(
                taskName,
                initialMax,
                updateIntervalMillis,
                continuousUpdate,
                clearDisplayOnFinish,
                processed,
                elapsed,
                new DefaultProgressBarRenderer(
                        style, unitName, unitSize,
                        showSpeed, speedFormat, speedUnit,
                        !hideEta, eta),
                consumer == null ? Util.createConsoleConsumer(maxRenderedLength) : consumer
        );
    }

    public ManuallyProgressBarBuilder setTaskName(String taskName) {
        this.taskName = taskName;
        return this;
    }

    public ManuallyProgressBarBuilder setInitialMax(long initialMax) {
        this.initialMax = initialMax;
        return this;
    }

    public ManuallyProgressBarBuilder setUpdateIntervalMillis(long updateIntervalMillis) {
        this.updateIntervalMillis = updateIntervalMillis;
        return this;
    }

    public ManuallyProgressBarBuilder setContinuousUpdate(boolean continuousUpdate) {
        this.continuousUpdate = continuousUpdate;
        return this;
    }

    public ManuallyProgressBarBuilder setStyle(ProgressBarStyle style) {
        this.style = style;
        return this;
    }

    public ManuallyProgressBarBuilder setConsumer(ProgressBarConsumer consumer) {
        this.consumer = consumer;
        return this;
    }

    public ManuallyProgressBarBuilder setClearDisplayOnFinish(boolean clearDisplayOnFinish) {
        this.clearDisplayOnFinish = clearDisplayOnFinish;
        return this;
    }

    public ManuallyProgressBarBuilder setUnitName(String unitName) {
        this.unitName = unitName;
        return this;
    }

    public ManuallyProgressBarBuilder setUnitSize(long unitSize) {
        this.unitSize = unitSize;
        return this;
    }

    public ManuallyProgressBarBuilder setShowSpeed(boolean showSpeed) {
        this.showSpeed = showSpeed;
        return this;
    }

    public ManuallyProgressBarBuilder setHideEta(boolean hideEta) {
        this.hideEta = hideEta;
        return this;
    }

    public ManuallyProgressBarBuilder setEta(Function<ProgressState, Optional<Duration>> eta) {
        this.eta = eta;
        return this;
    }

    public ManuallyProgressBarBuilder setSpeedFormat(DecimalFormat speedFormat) {
        this.speedFormat = speedFormat;
        return this;
    }

    public ManuallyProgressBarBuilder setSpeedUnit(ChronoUnit speedUnit) {
        this.speedUnit = speedUnit;
        return this;
    }

    public ManuallyProgressBarBuilder setProcessed(long processed) {
        this.processed = processed;
        return this;
    }

    public ManuallyProgressBarBuilder setElapsed(Duration elapsed) {
        this.elapsed = elapsed;
        return this;
    }

    public ManuallyProgressBarBuilder setMaxRenderedLength(int maxRenderedLength) {
        this.maxRenderedLength = maxRenderedLength;
        return this;
    }
}
