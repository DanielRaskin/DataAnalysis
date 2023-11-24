package dataanalysis;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;


/**
 * Data for REST interface
 */
public class WebData {
    // sorted map timestamp -> CTR
    private NavigableMap<LocalDateTime, BigDecimal> ctr;
    // map of maps: event -> sorted map timestamp -> EvPM
    private Map<String, NavigableMap<LocalDateTime, BigDecimal>> evpm;
    // for DMA aggregation: dma -> impressions count + CTR
    private Map<String, CtrData> dmaCtr;
    // for siteId aggregation: siteId -> impressions count + CTR
    private Map<String, CtrData> siteIdCtr;
    // for DMA aggregation: event -> dma -> EvPM
    private Map<String, Map<String, BigDecimal>> eventDmaEvpm;
    // for siteId aggregation: event -> siteId -> EvPM
    private Map<String, Map<String, BigDecimal>> eventSiteIdEvpm;

    WebData(NavigableMap<LocalDateTime, BigDecimal> ctr,
            Map<String, NavigableMap<LocalDateTime, BigDecimal>> evpm,
            Map<String, CtrData> dmaCtr,
            Map<String, CtrData> siteIdCtr,
            Map<String, Map<String, BigDecimal>> eventDmaEvpm,
            Map<String, Map<String, BigDecimal>> eventSiteIdEvpm) {
        this.ctr = ctr;
        this.evpm = evpm;
        this.dmaCtr = dmaCtr;
        this.siteIdCtr = siteIdCtr;
        this.eventDmaEvpm = eventDmaEvpm;
        this.eventSiteIdEvpm = eventSiteIdEvpm;
    }

    public Collection<String> getEvents() {
        return evpm.keySet();
    }

    // get CTR
    public List<Ctr> getCtr(int stepInHours) {
        var result = new ArrayList<Ctr>();
        if (! ctr.isEmpty()) {
            // map is sorted, we start from the earliest timestamp
            var current = ctr.firstEntry().getKey();
            // latest timestamp
            var last = ctr.lastEntry();
            while (current.isBefore(last.getKey())) {
                // find the closest entry less than current timestamp, for this entry we know CTR
                var entry = ctr.floorEntry(current);
                result.add(new Ctr(current, entry.getValue()));
                // add step in hours
                current = current.plusHours(stepInHours);
            }
            result.add(new Ctr(last.getKey(), last.getValue()));
        }
        return result;
    }

    public List<Evpm> getEvpm(String event, int stepInHours) {
        var result = new ArrayList<Evpm>();
        // map for current event
        var evpmForEvent = evpm.get(event);
        if ((evpmForEvent != null) && (! evpm.isEmpty())) {
            // start from the earliest timestamp
            var current = evpmForEvent.firstEntry().getKey();
            var last = evpmForEvent.lastEntry();
            while (current.isBefore(last.getKey())) {
                // find the closest entry less than current timestamp, for this entry we know EvPM
                var entry = evpmForEvent.floorEntry(current);
                result.add(new Evpm(current, entry.getValue()));
                current = current.plusHours(stepInHours);
            }
            result.add(new Evpm(last.getKey(), last.getValue()));
        }
        return result;
    }

    // DMA data for given event
    public List<DmaData> getDmaData(String event) {
        var result = new ArrayList<DmaData>();
        // map DMA -> EvPM for the given event
        var dmaevpm = eventDmaEvpm.get(event);
        if (dmaevpm == null) {
            // if there is no EvPM data for given event, set it to zero always
            for (var entry : dmaCtr.entrySet()) {
                result.add(new DmaData(entry.getKey(), entry.getValue().getImpressions(),
                        entry.getValue().getCtr(), Utils.ZERO));
            }
        } else {
            for (var entry : dmaCtr.entrySet()) {
                // make DmaData from CTR info and EvPM info
                result.add(new DmaData(entry.getKey(), entry.getValue().getImpressions(),
                        entry.getValue().getCtr(), dmaevpm.get(entry.getKey())));
            }
        }
        return result;
    }

    // siteId data for given event
    public List<SiteIdData> getSiteIdData(String event) {
        var result = new ArrayList<SiteIdData>();
        // map siteId -> EvPM for the given event
        var siteidevpm = eventSiteIdEvpm.get(event);
        if (siteidevpm == null) {
            for (var entry : siteIdCtr.entrySet()) {
                result.add(new SiteIdData(entry.getKey(), entry.getValue().getImpressions(),
                        entry.getValue().getCtr(), Utils.ZERO));
            }
        } else {
            for (var entry : siteIdCtr.entrySet()) {
                // make SiteIdData from CTR info and EvPM info
                result.add(new SiteIdData(entry.getKey(), entry.getValue().getImpressions(),
                        entry.getValue().getCtr(), siteidevpm.get(entry.getKey())));
            }
        }
        return result;
    }

    // CTR data which is returned by REST interface
    static class Ctr {
        private String timestamp;
        private String ctr;

        public Ctr(LocalDateTime timestamp, BigDecimal ctr) {
            this.timestamp = Utils.printDateTime(timestamp);
            this.ctr = ctr.toString();
        }

        public String getTimestamp() {
            return timestamp;
        }

        public String getCtr() {
            return ctr;
        }
    }

    // EvPM data which is returned by REST interface
    static class Evpm {
        private String timestamp;
        private String evpm;

        public Evpm(LocalDateTime timestamp, BigDecimal evpm) {
            this.timestamp = Utils.printDateTime(timestamp);
            this.evpm = evpm.toString();
        }

        public String getTimestamp() {
            return timestamp;
        }

        public String getEvpm() {
            return evpm;
        }
    }

    // aggregation DMA data which is returned by REST interface
    static class DmaData {
        private String dma;
        private int impressions;
        private String ctr;
        private String evpm;

        public DmaData(String dma, int impressions, BigDecimal ctr, BigDecimal evpm) {
            this.dma = dma;
            this.impressions = impressions;
            this.ctr = ctr.toString();
            this.evpm = evpm.toString();
        }

        public String getDma() {
            return dma;
        }

        public int getImpressions() {
            return impressions;
        }

        public String getCtr() {
            return ctr;
        }

        public String getEvpm() {
            return evpm;
        }
    }

    // aggregation siteId data which is returned by REST interface
    static class SiteIdData {
        private String siteid;
        private int impressions;
        private String ctr;
        private String evpm;

        public SiteIdData(String siteid, int impressions, BigDecimal ctr, BigDecimal evpm) {
            this.siteid = siteid;
            this.impressions = impressions;
            this.ctr = ctr.toString();
            this.evpm = evpm.toString();
        }

        public String getSiteid() {
            return siteid;
        }

        public int getImpressions() {
            return impressions;
        }

        public String getCtr() {
            return ctr;
        }

        public String getEvpm() {
            return evpm;
        }
    }
}
