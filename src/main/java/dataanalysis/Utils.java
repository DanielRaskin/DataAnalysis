package dataanalysis;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Common data and functions
 */
public class Utils {
    // BigDecimal zero with three digits after decimal point (scale = 3)
    public static final BigDecimal ZERO = new BigDecimal("0.000");

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // number of digits after decimal point in CTR and EvPM
    private static final int SCALE = 3;


    // calculate CTR
    public static BigDecimal calculateCtr(Integer impressions, Integer clicks) {
        if ((impressions == null) || (clicks == null) || (impressions == 0) || (clicks == 0)) {
            return ZERO;
        }
        return new BigDecimal(100 * clicks).divide(new BigDecimal(impressions), SCALE, RoundingMode.HALF_UP);
    }

    // calculate EvPM
    public static BigDecimal calculateEvpm(Integer impressions, Integer events) {
        if ((impressions == null) || (events == null) || (impressions == 0) || (events == 0)) {
            return ZERO;
        }
        return new BigDecimal(1000 * events).divide(new BigDecimal(impressions), SCALE, RoundingMode.HALF_UP);
    }

    public static LocalDateTime parseDateTime(String s) {
        return DATE_TIME_FORMATTER.parse(s.trim(), LocalDateTime::from);
    }

    public static String printDateTime(LocalDateTime timestamp) {
        return DATE_TIME_FORMATTER.format(timestamp);
    }
}
