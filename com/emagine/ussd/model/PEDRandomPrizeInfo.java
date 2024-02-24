package com.emagine.ussd.model;

/**
 * PEDRandomPrizeInfo class
 */
public class PEDRandomPrizeInfo {
    private Integer minRange ;
    private Integer maxRange ;
    private String prizeID ;
    private Integer weightage ;

    public int getMinRange() {
        return minRange;
    }

    public void setMinRange(Integer minRange) {
        this.minRange = minRange;
    }

    public int getMaxRange() {
        return maxRange;
    }

    public void setMaxRange(Integer maxRange) {
        this.maxRange = maxRange;
    }

    public String getPrizeID() {
        return prizeID;
    }

    public void setPrizeID(String prizeID) {
        this.prizeID = prizeID;
    }

    public int getWeightage() {
        return weightage;
    }

    public void setWeightage(Integer weightage) {
        this.weightage = weightage;
    }

}
