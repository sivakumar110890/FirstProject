package com.emagine.ussd.model;

/**
 * ProductInfo class
 */
public class ProductInfo {
    public static final String SEP_COMMA = ",";
    private String productID ;
    private int langCode ;
    private String productDesc ;
    private String bValue ;
    private String cValue ;
    private String productType ;
    private String productSubType ;
    private String poolID ;

    public String getPoolID() {
        return poolID;
    }

    public void setPoolID(String poolID) {
        this.poolID = poolID;
    }    

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public int getLangCode() {
        return langCode;
    }

    public void setLangCode(int langCode) {
        this.langCode = langCode;
    }

    public String getProductDesc() {
        return productDesc;
    }

    public void setProductDesc(String productDesc) {
        this.productDesc = productDesc;
    }

    public String getbValue() {
        return bValue;
    }

    public void setbValue(String bValue) {
        this.bValue = bValue;
    }

    public String getcValue() {
        return cValue;
    }

    public void setcValue(String cValue) {
        this.cValue = cValue;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getProductSubType() {
        return productSubType;
    }

    public void setProductSubType(String productSubType) {
        this.productSubType = productSubType;
    }

    @Override
    public String toString () {
        StringBuilder builder = new StringBuilder() ;
        builder.append(productID).append(SEP_COMMA)
                .append(langCode).append(SEP_COMMA)
                .append(productDesc).append(SEP_COMMA)
                .append(bValue).append(SEP_COMMA)
                .append(cValue) ;

        return builder.toString() ;
    }

}
