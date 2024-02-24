/**
 *
 */
package com.emagine.ussd.model;

/**
 * TemplateDTO class
 */
public class TemplateDTO {
    private String tempalteId;
    private String template;
    private int langCd;
    private String offerOrderCSV;

    /**
     * @return the tempalteId
     */
    public String getTempalteId() {
        return tempalteId;
    }

    /**
     * @param tempalteId
     *            the tempalteId to set
     */
    public void setTempalteId(String tempalteId) {
        this.tempalteId = tempalteId;
    }

    /**
     * @return the template
     */
    public String getTemplate() {
        return template;
    }

    /**
     * @param template
     *            the template to set
     */
    public void setTemplate(String template) {
        this.template = template;
    }

    /**
     * @return the langCd
     */
    public int getLangCd() {
        return langCd;
    }

    /**
     * @param langCd
     *            the langCd to set
     */
    public void setLangCd(int langCd) {
        this.langCd = langCd;
    }

    /**
     * @return the offerOrderCSV
     */
    public String getOfferOrderCSV() {
        return offerOrderCSV;
    }

    /**
     * @param offerOrderCSV
     *            the offerOrderCSV to set
     */
    public void setOfferOrderCSV(String offerOrderCSV) {
        this.offerOrderCSV = offerOrderCSV;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("TemplateDTO [tempalteId=%s, template=%s, langCd=%s, offerOrderCSV=%s]", tempalteId,
                        template, langCd, offerOrderCSV);
    }
}
