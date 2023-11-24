package dataanalysis;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Data processor
 * This class is used to read and process data
 */
public class DataProcessor {
    private static final String IMPRESSION_DATA_FILE_NAME = "interview.X.csv";
    private static final String EVENT_DATA_FILE_NAME = "interview.y.csv";

    private NavigableSet<Data> dataSet = new TreeSet<>();


    public DataProcessor() {
        try {
            read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    // read data from data source to internal data structure
    public void read() throws IOException {
        var impressions = new HashSet<Impression>();
        // read impressions
        try (var impressionReader = new LineNumberReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(IMPRESSION_DATA_FILE_NAME)))) {
            for (var impression : new CsvDataProvider<>(new ImpressionAdapter(), impressionReader, true)) {
                impressions.add(impression);
            }
        }
        // map uid -> impression
        var uidMap = new HashMap<String, Data>();
        for (var impression : impressions) {
            var data = new Data();
            data.uid = impression.getUid();
            data.timestamp = impression.getTimestamp();
            data.dma = impression.getDma();
            data.siteId = impression.getSiteId();
            uidMap.put(data.uid, data);
            dataSet.add(data);
        }

        // read events
        try (var eventReader = new LineNumberReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(EVENT_DATA_FILE_NAME)))) {
            for (var event : new CsvDataProvider<>(new EventAdapter(), eventReader, true)) {
                // try to find data by uid, if not found event will be skipped because we don't know even its timestamp
                var uidData = uidMap.get(event.getUid());
                if (uidData != null) {
                    // get data by uid and save event
                    var data = new Data();
                    data.timestamp = uidData.timestamp;
                    data.eventType = event.getType();
                    data.isClick = event.isFirstClick();
                    data.dma = uidData.dma;
                    data.siteId = uidData.siteId;
                    dataSet.add(data);
                }
            }
        }
    }

    // this method processes internal data structures into data for REST interfaces
    public WebData process() {
        // counters
        var impressions = 0;
        var clicks = 0;
        var events = new HashMap<String, Integer>();
        var dmaClicks = new HashMap<String, Integer>();
        var siteIdClicks = new HashMap<String, Integer>();
        var dmaImpressions = new HashMap<String, Integer>();
        var siteIdImpressions = new HashMap<String, Integer>();
        var dmaEvents = new HashMap<String, Map<String, Integer>>();
        var siteIdEvents = new HashMap<String, Map<String, Integer>>();

        // data for Web. These structures should be filled
        var ctr = new TreeMap<LocalDateTime, BigDecimal>();
        var evpm = new HashMap<String, NavigableMap<LocalDateTime, BigDecimal>>();
        var dmaCtr = new HashMap<String, CtrData>();
        var siteIdCtr = new HashMap<String, CtrData>();
        var eventDmaEvpm = new HashMap<String, Map<String, BigDecimal>>();
        var eventSiteIdEvpm = new HashMap<String, Map<String, BigDecimal>>();

        for (var data : dataSet) {
            if (data.eventType == null) {
                // this is an impression
                // increment counters: impressions, impressions for given DMA, impressions for given siteId
                impressions++;
                incValue(dmaImpressions, data.dma);
                incValue(siteIdImpressions, data.siteId);
            } else {
                // this is an event
                if (data.isClick) {
                    // this is a click
                    // increment counters: clicks, clicks for given DMA, clicks for given siteId
                    clicks++;
                    incValue(dmaClicks, data.dma);
                    incValue(siteIdClicks, data.siteId);
                } else {
                    // this is a non-click event
                    // increment counters: events of given type, events of given type for given DMA, events of given type for given siteId
                    incValue(events, data.eventType);
                    incValue(dmaEvents, data.dma, data.eventType);
                    incValue(siteIdEvents, data.siteId, data.eventType);
                }
            }
            if ((data.eventType != null) && (! data.isClick)) {
                // non-click event, update evpm
                putEvpm(evpm, data.eventType, data.timestamp, Utils.calculateEvpm(impressions, events.get(data.eventType)));
            }
            if ((data.eventType == null) || data.isClick) {
                // impression or click event, update ctr
                ctr.put(data.timestamp, Utils.calculateCtr(impressions, clicks));
            }
        }
        // update evpm info for earliest and latest timestamp
        var earliestTimestamp = dataSet.first().timestamp;
        var latestTimestamp = dataSet.last().timestamp;
        for (var entry : evpm.entrySet()) {
            // set EvPM for the earliest timestamp to zero if not set
            if (entry.getValue().get(earliestTimestamp) == null) {
                putEvpm(evpm, entry.getKey(), latestTimestamp, Utils.ZERO);
            }
            // EvPM for latest timestamp
            putEvpm(evpm, entry.getKey(), latestTimestamp, Utils.calculateEvpm(impressions, events.get(entry.getKey())));
        }

        // EvPM data for aggregation tables
        for (var event : events.keySet()) {
            for (var dma : dmaImpressions.keySet()) {
                putEvpm(eventDmaEvpm, dmaImpressions, dmaEvents, dma, event);
            }
            for (var siteId : siteIdImpressions.keySet()) {
                putEvpm(eventSiteIdEvpm, siteIdImpressions, siteIdEvents, siteId, event);
            }
        }

        // CTR data for aggregation tables
        for (var entry : dmaImpressions.entrySet()) {
            dmaCtr.put(entry.getKey(), new CtrData(entry.getValue(), dmaClicks.get(entry.getKey())));
        }
        for (var entry : siteIdImpressions.entrySet()) {
            siteIdCtr.put(entry.getKey(), new CtrData(entry.getValue(), siteIdClicks.get(entry.getKey())));
        }
        return new WebData(ctr, evpm, dmaCtr, siteIdCtr, eventDmaEvpm, eventSiteIdEvpm);
    }

    private static void putEvpm(Map<String, NavigableMap<LocalDateTime, BigDecimal>> map, String event, LocalDateTime timestamp, BigDecimal value) {
        var m = map.get(event);
        if (m == null) {
            m = new TreeMap<LocalDateTime, BigDecimal>();
            map.put(event, m);
        }
        m.put(timestamp, value);
    }

    private void putEvpm(Map<String, Map<String, BigDecimal>> result, Map<String, Integer> impressionsMap, Map<String, Map<String, Integer>> eventsMap, String key, String event) {
        var eventsCountMap = eventsMap.get(key);
        BigDecimal value = (eventsCountMap == null) ? Utils.ZERO : Utils.calculateEvpm(impressionsMap.get(key), eventsCountMap.get(event));
        var map = result.get(event);
        if (map == null) {
            map = new HashMap<String, BigDecimal>();
            result.put(event, map);
        }
        map.put(key, value);
    }

    private static void incValue(Map<String, Integer> map, String key) {
        var v = map.get(key);
        v = (v == null) ? 1 : (v + 1);
        map.put(key, v);
    }

    private static void incValue(Map<String, Map<String, Integer>> map, String key1, String key2) {
        var m = map.get(key1);
        if (m == null) {
            m = new HashMap<String, Integer>();
            m.put(key2, 1);
            map.put(key1, m);
        } else {
            incValue(m, key2);
        }
    }

    // internal data structure to store loaded data
    private static class Data implements Comparable<Data> {
        String uid;
        LocalDateTime timestamp;
        String dma;
        String siteId;
        String eventType;
        boolean isClick;

        @Override
        public int compareTo(@NotNull Data data) {
            var result = this.timestamp.compareTo(data.timestamp);
            return (result == 0) ? (this.hashCode() - data.hashCode()) : result;
        }
    }
}
