package com.emagine.ussd.model;

/**
 * TownProdInfo class
 */
public class TownProdInfo {

	private String cellId;
	private String townName;

	public String getCellId() {
		return cellId;
	}

	public void setCellId(String cellId) {
		this.cellId = cellId;
	}

	public String getTownName() {
		return townName;
	}

	public void setTownName(String townName) {
		this.townName = townName;
	}

	@Override
	public String toString() {
		return "TownProdInfo [cellId=" + cellId + ", townName=" + townName + "]";
	}

}
