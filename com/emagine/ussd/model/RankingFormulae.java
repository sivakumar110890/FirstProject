package com.emagine.ussd.model;

import java.util.ArrayList;
import java.util.List;

/**
 * RankingFormulae class
 */
public class RankingFormulae {

    private boolean aaEligible;
    private String prefPayMethod;
    private float aValue;
    private long airtimeBalance;
    private long aaBalance;
    private String cellId;
    private long locationNumber;
    private String poolId;
    private List<OfferParams> offerParams;

    public boolean isAaEligible() {
        return aaEligible;
    }

    public void setAaEligible(boolean aaEligible) {
        this.aaEligible = aaEligible;
    }

    public String getPrefPayMethod() {
        return prefPayMethod;
    }

    public void setPrefPayMethod(String prefPayMethod) {
        this.prefPayMethod = prefPayMethod;
    }

    public float getaValue() {
        return aValue;
    }

    public void setaValue(float aValue) {
        this.aValue = aValue;
    }

    public float getAirtimeBalance() {
        return airtimeBalance;
    }

    public void setAirtimeBalance(long airtimeBalance) {
        this.airtimeBalance = airtimeBalance;
    }

    public float getAaBalance() {
        return aaBalance;
    }

    public void setAaBalance(long aaBalance) {
        this.aaBalance = aaBalance;
    }

    public List<OfferParams> getOfferParams() {
        return new ArrayList<>(offerParams);
    }

    public void setOfferParams(List<OfferParams> offerParams) {
        this.offerParams = new ArrayList<>(offerParams);
    }

    public long getLocationNumber() {
        return locationNumber;
    }

    public void setLocationNumber(long locationNumber) {
        this.locationNumber = locationNumber;
    }

    public String getCellId() {
        return cellId;
    }

    public void setCellId(String cellId) {
        this.cellId = cellId;
    }

    public String getPoolId() {
        return poolId;
    }

    public void setPoolId(String poolId) {
        this.poolId = poolId;
    }

}
