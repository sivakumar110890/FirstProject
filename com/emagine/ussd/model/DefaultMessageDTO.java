/**
 *
 */
package com.emagine.ussd.model;

/**
 * DefaultMessageDTO class
 */
public class DefaultMessageDTO {
    private String defaultMsg;
    private String errorMsg;
    private int landCd;

    /**
     * @return the defaultMsg
     */
    public String getDefaultMsg() {
        return defaultMsg;
    }

    /**
     * @param defaultMsg
     *            the defaultMsg to set
     */
    public void setDefaultMsg(String defaultMsg) {
        this.defaultMsg = defaultMsg;
    }

    /**
     * @return the errorMsg
     */
    public String getErrorMsg() {
        return errorMsg;
    }

    /**
     * @param errorMsg
     *            the errorMsg to set
     */
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    /**
     * @return the landCd
     */
    public int getLandCd() {
        return landCd;
    }

    /**
     * @param landCd
     *            the landCd to set
     */
    public void setLandCd(int landCd) {
        this.landCd = landCd;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("DefaultMessageDTO [defaultMsg=%s, errorMsg=%s, landCd=%s]", defaultMsg, errorMsg, landCd);
    }
}
