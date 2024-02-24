package com.emagine.ussd.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * UserInfo class
 */
public class UserInfo {

    private String msisdn;
    private String destAddress;
    private String txId;
    private boolean randomFlag;
    private boolean mlFlag;
    private boolean j4uEligible;
    private String langCode;
    private int userMsgRef;
    private String messageBody;
    private String custBalance;
    private String offerRefreshFlag;
    private String selProdType;
    private String prodIds;
    private boolean updateCCR;
    private String locationNumber;
    private String cellId;
    private String poolId;
    private boolean morningOfferFlag;
	private boolean ragEligibleFlag;
    private boolean ragOptInFlag;
    private boolean ragNeverOptInFlag;
    private boolean ragGoalReachedFlag;
    private boolean isRagUser;
    private boolean isMpesaUser;
	private boolean isSagUser;
    private boolean sagEligibleFlag;
    private boolean sagOptInFlag;
    private boolean sagNeverOptInFlag;
    private boolean sagGoalReachedFlag;
    private HashMap<String, String> ragInfo = new HashMap<>();
    private HashMap<String, String> sagInfo = new HashMap<>();    
    

	private String selProductId;
    private String inBoundClobString;
    private int serviceOp;

    // Advance airtime eligibility check
    private int aaEligible;
    private Map<String, Long> aaEligibleUserProdIdProdPriceMap = new HashMap<>();
    // Advance airtime amount
    private Long airtimeAdvBal;
    private Long actBal;
    private Long provLoanAmt;
    private String prefPayMethod;
    private float aValue;
    private int selProdOption;
    private boolean pedEligibility;
    private boolean locationRandomFlag;    
    private long lStartTime ;
    private boolean j4uNewUser;
    private int offerCount;
    private boolean consentFlag;
    private boolean isBlacklistedUser;
    private String townName;
    
	public String getTownName() {
		return townName;
	}

	public void setTownName(String townName) {
		this.townName = townName;
	}
	
    public boolean isBlacklistedUser() {
		return isBlacklistedUser;
	}

	public void setBlacklistedUser(boolean isBlacklistedUser) {
		this.isBlacklistedUser = isBlacklistedUser;
	}

	public boolean isConsentFlag() {
		return consentFlag;
	}

	public void setConsentFlag(boolean consentFlag) {
		this.consentFlag = consentFlag;
	}	

    public int getOfferCount() {
		return offerCount;
	}

	public void setOfferCount(int offerCount) {
		this.offerCount = offerCount;
	}

	public boolean isJ4uNewUser() {
		return j4uNewUser;
	}

	public void setJ4uNewUser(boolean j4uNewUser) {
		this.j4uNewUser = j4uNewUser;
	}

	public long getlStartTime() {
		return lStartTime;
	}

	public void setlStartTime(long lStartTime) {
		this.lStartTime = lStartTime;
	}

	public boolean isLocationRandomFlag() {
        return locationRandomFlag;
    }

    public void setLocationRandomFlag(boolean locationRandomFlag) {
        this.locationRandomFlag = locationRandomFlag;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getDestAddress() {
        return destAddress;
    }

    public void setDestAddress(String destAddress) {
        this.destAddress = destAddress;
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public boolean isRandomFlag() {
        return randomFlag;
    }

    public void setRandomFlag(boolean randomFlag) {
        this.randomFlag = randomFlag;
    }

    public boolean isMlFlag() {
        return mlFlag;
    }

    public void setMlFlag(boolean mlFlag) {
        this.mlFlag = mlFlag;
    }

    public boolean isJFUEligible() {
        return j4uEligible;
    }

    public void setJFUEligible(boolean jFUEligible) {
        j4uEligible = jFUEligible;
    }

    public int getLangCode() {
        return Integer.parseInt(langCode);
    }

    public void setLangCode(String langCode) {
        this.langCode = langCode;
    }

    public int getUserMsgRef() {
        return userMsgRef;
    }

    public void setUserMsgRef(int userMsgRef) {
        this.userMsgRef = userMsgRef;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public String getCustBalance() {
        return custBalance;
    }

    public void setCustBalance(String custBalance) {
        this.custBalance = custBalance;
    }

    public String getOfferRefreshFlag() {
        return offerRefreshFlag;
    }

    public void setOfferRefreshFlag(String offerRefreshFlag) {
        this.offerRefreshFlag = offerRefreshFlag;
    }

    public String getSelProdType() {
        return selProdType;
    }

    public void setSelProdType(String selProdType) {
        this.selProdType = selProdType;
    }

    public String getProdIds() {
        return prodIds;
    }

    public void setProdIds(List<String> prodIds) {
    	if (null != prodIds) {
    		this.prodIds = prodIds.toString();
    	} else {
    		this.prodIds = null ;
    	}
    }

    public boolean isUpdateCCR() {
        return updateCCR;
    }

    public void setUpdateCCR(boolean updateCCR) {
        this.updateCCR = updateCCR;
    }

    public boolean isRagEligibleFlag() {
        return ragEligibleFlag;
    }

    public void setRagEligibleFlag(boolean ragEligibleFlag) {
        this.ragEligibleFlag = ragEligibleFlag;
    }

    public boolean isRagOptInFlag() {
        return ragOptInFlag;
    }

    public void setRagOptInFlag(boolean ragOptInFlag) {
        this.ragOptInFlag = ragOptInFlag;
    }

    public boolean isRagGoalReachedFlag() {
        return ragGoalReachedFlag;
    }

    public void setRagGoalReachedFlag(boolean ragGoalReachedFlag) {
        this.ragGoalReachedFlag = ragGoalReachedFlag;
    }

    public HashMap<String, String> getRagInfo() {
        return ragInfo;
    }

    public void setRagInfo(HashMap<String, String> ragInfo) {
        this.ragInfo = ragInfo;
    }

    public boolean isRagNeverOptInFlag() {
        return ragNeverOptInFlag;
    }

    public void setRagNeverOptInFlag(boolean ragNeverOptInFlag) {
        this.ragNeverOptInFlag = ragNeverOptInFlag;
    }

    public boolean isRagUser() {
        return isRagUser;
    }

    public void setRagUser(boolean isRagUser) {
        this.isRagUser = isRagUser;
    }

    public boolean isMpesaUser() {
        return isMpesaUser;
    }

    public void setMpesaUser(boolean isMpesaUser) {
        this.isMpesaUser = isMpesaUser;
    }

    public int getAaEligible() {
        return aaEligible;
    }

    public void setAaEligible(String aaEligible) {
        this.aaEligible = Integer.parseInt(aaEligible);
    }

    public Map<String, Long> getAaEligibleProdIdProdPriceMap() {
        return aaEligibleUserProdIdProdPriceMap;
    }

    public void setAaEligibleProdIdProdPriceMap(Map<String, Long> aaEligibleUserProdIds) {
        this.aaEligibleUserProdIdProdPriceMap = aaEligibleUserProdIds;
    }

    public Long getActBal() {
        return actBal;
    }

    public void setActBal(Long actBal) {
        this.actBal = actBal;
    }

    public long getAirtimeAdvBal() {
        return airtimeAdvBal;
    }

    public void setAirtimeAdvBal(Long l) {
        this.airtimeAdvBal = l;
    }

    public String getSelProductId() {
        return selProductId;
    }

    public void setSelProductId(String selProductId) {
        this.selProductId = selProductId;
    }

    public String getInBoundClobString() {
        return inBoundClobString;
    }

    public void setInBoundClobString(String inBoundClobString) {
        this.inBoundClobString = inBoundClobString;
    }

    public long getProvLoanAmt() {
        return provLoanAmt;
    }

    public void setProvLoanAmt(Long provLoanAmt) {
        this.provLoanAmt = provLoanAmt;
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

    public int getSelectionProdOption() {
        return selProdOption;
    }

    public void setSelectionProdOption(int selProdOption) {
        this.selProdOption = selProdOption;
    }

    public int getServiceOp() {
        return serviceOp;
    }

    public void setServiceOp(int serviceOp) {
        this.serviceOp = serviceOp;
    }

    public boolean isPedEligibility() {
        return pedEligibility;
    }

    public void setPedEligibility(boolean pedEligibility) {
        this.pedEligibility = pedEligibility;
    }

    public String getLocationNumber() {
        return locationNumber;
    }

    public void setLocationNumber(String locationNumber) {
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
    
    public boolean isMorningOfferFlag() {
		return morningOfferFlag;
	}

	public void setMorningOfferFlag(boolean morningOfferFlag) {
		this.morningOfferFlag = morningOfferFlag;
	}
	
	public boolean isSagUser() {
		return isSagUser;
	}

	public void setSagUser(boolean isSagUser) {
		this.isSagUser = isSagUser;
	}

	public boolean isSagEligibleFlag() {
		return sagEligibleFlag;
	}

	public void setSagEligibleFlag(boolean sagEligibleFlag) {
		this.sagEligibleFlag = sagEligibleFlag;
	}

	public boolean isSagOptInFlag() {
		return sagOptInFlag;
	}

	public void setSagOptInFlag(boolean sagOptInFlag) {
		this.sagOptInFlag = sagOptInFlag;
	}

	public boolean isSagNeverOptInFlag() {
		return sagNeverOptInFlag;
	}

	public void setSagNeverOptInFlag(boolean sagNeverOptInFlag) {
		this.sagNeverOptInFlag = sagNeverOptInFlag;
	}

	public boolean isSagGoalReachedFlag() {
		return sagGoalReachedFlag;
	}

	public void setSagGoalReachedFlag(boolean sagGoalReachedFlag) {
		this.sagGoalReachedFlag = sagGoalReachedFlag;
	}
	
	public HashMap<String, String> getSagInfo() {
		return sagInfo;
	}

	public void setSagInfo(HashMap<String, String> sagInfo) {
		this.sagInfo = sagInfo;
	}

	@Override
	public String toString() {
		return "UserInfo [msisdn=" + msisdn + ", destAddress=" + destAddress + ", txId=" + txId + ", randomFlag="
				+ randomFlag + ", mlFlag=" + mlFlag + ", j4uEligible=" + j4uEligible + ", langCode=" + langCode
				+ ", userMsgRef=" + userMsgRef + ", messageBody=" + messageBody + ", custBalance=" + custBalance
				+ ", offerRefreshFlag=" + offerRefreshFlag + ", selProdType=" + selProdType + ", prodIds=" + prodIds
				+ ", updateCCR=" + updateCCR + ", locationNumber=" + locationNumber + ", cellId=" + cellId + ", poolId="
				+ poolId + ", morningOfferFlag=" + morningOfferFlag + ", ragEligibleFlag=" + ragEligibleFlag
				+ ", ragOptInFlag=" + ragOptInFlag + ", ragNeverOptInFlag=" + ragNeverOptInFlag
				+ ", ragGoalReachedFlag=" + ragGoalReachedFlag + ", isRagUser=" + isRagUser + ", isMpesaUser="
				+ isMpesaUser + ", isSagUser=" + isSagUser + ", sagEligibleFlag=" + sagEligibleFlag + ", sagOptInFlag="
				+ sagOptInFlag + ", sagNeverOptInFlag=" + sagNeverOptInFlag + ", sagGoalReachedFlag="
				+ sagGoalReachedFlag + ", ragInfo=" + ragInfo + ", sagInfo=" + sagInfo + ", selProductId="
				+ selProductId + ", inBoundClobString=" + inBoundClobString + ", serviceOp=" + serviceOp
				+ ", aaEligible=" + aaEligible + ", aaEligibleUserProdIdProdPriceMap="
				+ aaEligibleUserProdIdProdPriceMap + ", airtimeAdvBal=" + airtimeAdvBal + ", actBal=" + actBal
				+ ", provLoanAmt=" + provLoanAmt + ", prefPayMethod=" + prefPayMethod + ", aValue=" + aValue
				+ ", selProdOption=" + selProdOption + ", pedEligibility=" + pedEligibility + ", locationRandomFlag="
				+ locationRandomFlag + ", lStartTime=" + lStartTime + ", j4uNewUser=" + j4uNewUser + ", offerCount="
				+ offerCount + ", consentFlag=" + consentFlag + ", isBlacklistedUser=" + isBlacklistedUser
				+ ", townName=" + townName + "]";
	}

    
}