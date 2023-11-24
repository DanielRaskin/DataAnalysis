package dataanalysis;

import java.math.BigDecimal;

/**
 * CTR data (impressions + CTR)
 */
public class CtrData {
    private int impressions;
    private BigDecimal ctr;

    CtrData(Integer impressions, Integer clicks) {
        this.impressions = (impressions == null) ? 0 : impressions;
        this.ctr = Utils.calculateCtr(impressions, clicks);
    }
    public int getImpressions() {
        return impressions;
    }
    public BigDecimal getCtr() {
        return ctr;
    }
}

