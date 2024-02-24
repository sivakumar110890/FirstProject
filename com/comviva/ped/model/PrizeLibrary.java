package com.comviva.ped.model;

public class PrizeLibrary {
    private String prizeId;
    private int languageCode;
    private int probability;
    private String prizeDescription;
    private int maxWins;
    private String prizeType;
    private String redemptionCode;

    public PrizeLibrary() {

    }

    public PrizeLibrary(String prizeId, int languageCode, int probability, String prizeDescription, int maxWins, String prizeType, String redemptionCode) {
        super();
        this.prizeId = prizeId;
        this.languageCode = languageCode;
        this.probability = probability;
        this.prizeDescription = prizeDescription;
        this.maxWins = maxWins;
        this.prizeType = prizeType;
        this.redemptionCode = redemptionCode;
    }

    public String getPrizeId() {
        return prizeId;
    }

    public void setPrizeId(String prizeId) {
        this.prizeId = prizeId;
    }

    public int getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(int languageCode) {
        this.languageCode = languageCode;
    }

    public int getProbability() {
        return probability;
    }

    public void setProbability(int probability) {
        this.probability = probability;
    }

    public String getPrizeDescription() {
        return prizeDescription;
    }

    public void setPrizeDescription(String prizeDescription) {
        this.prizeDescription = prizeDescription;
    }

    public int getMaxWins() {
        return maxWins;
    }

    public void setMaxWins(int maxWins) {
        this.maxWins = maxWins;
    }

    public String getPrizeType() {
        return prizeType;
    }

    public void setPrizeType(String prizeType) {
        this.prizeType = prizeType;
    }

    public String getRedemptionCode() {
        return redemptionCode;
    }

    public void setRedemptionCode(String redemptionCode) {
        this.redemptionCode = redemptionCode;
    }

}
