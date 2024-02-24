package com.emagine.ussd.model;

import java.util.ArrayList;
import java.util.List;

/**
 * InboundUssdMessage class
 */
public class InboundUssdMessage {

    private String clobString;
    private int incomingLabel;
    private String prodIds;
    private String selProdId;
    private String prodPrices;
    private List<String> productIds;
    private String townName;
    
    // Define the regex pattern for unwanted characters
    private String unwantedCharactersRegex = "��|`|���|���";

    public List<String> getProductIds() {
        return new ArrayList<>(productIds);
    }

    public void setProductIds(List<String> productIds) {
        this.productIds = new ArrayList<>(productIds);
    }

    public String getClobString() {
        return clobString;
    }

    public void setClobString(String clobString) {
        this.clobString = clobString.replaceAll(unwantedCharactersRegex, "'");
    }

    public int getIncomingLabel() {
        return incomingLabel;
    }

    public void setIncomingLabel(int incomingLabel) {
        this.incomingLabel = incomingLabel;
    }

    public String getProdIds() {
        return prodIds;
    }

    public void setProdIds(List<String> prodIds) {
        this.prodIds = prodIds.toString();
    }

    public String getSelProdId() {
        return selProdId;
    }

    public void setSelProdId(String selProdId) {
        this.selProdId = selProdId;
    }

    public String getProdPrices() {
        return prodPrices;
    }

    public void setProdPrices(List<String> prodPrices) {
        this.prodPrices = prodPrices.toString();
    }

	public String getTownName() {
		return townName;
	}

	public void setTownName(String townName) {
		this.townName = townName;
	}

}