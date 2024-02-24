package com.emagine.ussd.model;

/**
 * MLOfferMsg class
 */
public class MLOfferMsg {

    private String[] prodIdsAry;
    private String[] rfValuesAry;
    private String menuContent;

    public String[] getProdIds() {
        return prodIdsAry.clone();
    }

    public void setProdIds(String prodIds) {
        this.prodIdsAry = prodIds.split(",");
    }

    public String getMenuContent() {
        return menuContent;
    }

    public void setMenuContent(String menuContent) {
        this.menuContent = menuContent;
    }

    public String[] getRfValues() {
        return rfValuesAry;
    }

    public void setRfValues(String rfValues) {
        if (rfValues != null && !"NULL".equals(rfValues)) {
            this.rfValuesAry = rfValues.split(",");
        }
    }

}
