package dev.crepe.global.error;

import lombok.Getter;
import org.springframework.lang.Nullable;

public enum CustomHttpStatus {

    BANNED_REQUEST(601, CustomSeries.BANNED_REQUEST, "Banned phone request"),
    REFRESH_TOKEN_EXPIRED(602, CustomSeries.BANNED_REQUEST, "Refresh token expired");

    public static final CustomHttpStatus[] VALUES;

    static {
        VALUES = values();
    }

    private final int value;

    private final CustomSeries series;

    @Getter
    private final String reasonPhrase;

    CustomHttpStatus(int value, CustomSeries series, String reasonPhrase) {
        this.value = value;
        this.series = series;
        this.reasonPhrase = reasonPhrase;
    }

    public int value() {
        return this.value;
    }

    public CustomSeries series() {
        return this.series;
    }

    public String toString() {
        return this.value + " " + name();
    }

    public static CustomHttpStatus valueOf(int statusCode) {
        CustomHttpStatus status = resolve(statusCode);
        if (status == null) {
            throw new IllegalArgumentException("No matching constant for [" + statusCode + "]");
        }
        return status;
    }

    @Nullable
    public static CustomHttpStatus resolve(int statusCode) {
        for (CustomHttpStatus status : VALUES) {
            if (status.value == statusCode) {
                return status;
            }
        }
        return null;
    }

    public enum CustomSeries {
        BANNED_REQUEST(6),
        REFRESH_TOKEN_EXPIRED(6);

        private final int value;

        CustomSeries(int value) {
            this.value = value;
        }

        public int value() {
            return this.value;
        }

        @Deprecated
        public static CustomSeries valueOf(CustomHttpStatus status) {
            return status.series;
        }

        public static CustomSeries valueOf(int statusCode) {
            CustomSeries series = resolve(statusCode);
            if (series == null) {
                throw new IllegalArgumentException("No matching constant for [" + statusCode + "]");
            }
            return series;
        }

        @Nullable
        public static CustomSeries resolve(int statusCode) {
            int seriesCode = statusCode / 100;
            for (CustomSeries series : values()) {
                if (series.value == seriesCode) {
                    return series;
                }
            }
            return null;
        }
    }
}
