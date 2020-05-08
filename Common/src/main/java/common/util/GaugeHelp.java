package common.util;

import com.codahale.metrics.Gauge;
import com.google.common.util.concurrent.AtomicDouble;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class GaugeHelp {
    public static class GaugeInt implements Gauge<AtomicInteger> {
        private final AtomicInteger value;

        public GaugeInt() {
            value = new AtomicInteger(0);
        }

        @Override
        public AtomicInteger getValue() {
            return value;
        }

        public void set(int val) {
            value.set(val);
        }
    }

    public static final class GaugeDouble implements Gauge<AtomicDouble> {
        private final AtomicDouble value;

        public GaugeDouble() {
            value = new AtomicDouble(0);
        }

        @Override
        public AtomicDouble getValue() {
            return value;
        }

        public void set(double val) {
            value.set(val);
        }
    }

    public static final class GaugeLong implements Gauge<AtomicLong> {
        public final AtomicLong value;

        public GaugeLong() {
            value = new AtomicLong(0);
        }

        @Override
        public AtomicLong getValue() {
            return value;
        }

        public void set(long val) {
            value.set(val);
        }
    }
}
