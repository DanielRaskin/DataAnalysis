package dataanalysis;

import java.time.LocalDateTime;

/**
 * Impression
 */
public class Impression {
    private LocalDateTime timestamp;
    private String uid;

    private Check check;
    private TimeCheck timeCheck;
    private UserTime userTime;
    private String dma;
    private String os;
    private String model;
    private String hardware;
    private String siteId;

    public Impression(LocalDateTime timestamp,
                      String uid,
                      Check check,
                      TimeCheck timeCheck,
                      UserTime userTime,
                      String dma,
                      String os,
                      String model,
                      String hardware,
                      String siteId) {
        this.timestamp = timestamp;
        this.uid = uid;
        this.check = check;
        this.timeCheck = timeCheck;
        this.userTime = userTime;
        this.dma = dma;
        this.os = os;
        this.model = model;
        this.hardware = hardware;
        this.siteId = siteId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getUid() {
        return uid;
    }

    public Check getCheck() {
        return check;
    }

    public TimeCheck getTimeCheck() {
        return timeCheck;
    }

    public UserTime getUserTime() {
        return userTime;
    }

    public String getDma() {
        return dma;
    }

    public String getOs() {
        return os;
    }

    public String getModel() {
        return model;
    }
    public String getHardware() {
        return hardware;
    }

    public String getSiteId() {
        return siteId;
    }

    static enum Check {
        FIRST,
        FROM_2_TO_5,
        FROM_6_TO_10,
        FROM_11_TO_20,
        FROM_21
    }

    static enum TimeCheck {
        LESS_THAN_A_MINUTE,
        FROM_1_TO_10_MINUTES,
        FROM_11_TO_30_MINUTES,
        FROM_31_TO_60_MINUTES,
        FROM_1_TO_3_HOURS,
        FROM_4_TO_24_HOURS,
        ONE_OR_MORE_DAYS,
        NEVER_BEFORE
    }

    static enum UserTime {
        FROM_0000_TO_0300,
        FROM_0300_TO_0600,
        FROM_0600_TO_0900,
        FROM_0900_TO_1200,
        FROM_1200_TO_1500,
        FROM_1500_TO_1800,
        FROM_1800_TO_2100,
        FROM_2100_TO_0000
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Impression) {
            var impression = (Impression)o;
            return (timestamp.equals(impression.timestamp) &&
                    uid.equals(impression.uid) &&
                    (check == impression.check) &&
                    (timeCheck == impression.timeCheck) &&
                    (userTime == impression.userTime) &&
                    dma.equals(impression.dma) &&
                    os.equals(impression.os) &&
                    model.equals(impression.model) &&
                    hardware.equals(impression.hardware) &&
                    siteId.equals(impression.siteId));
        } else {
            return false;
        }
    }
}
