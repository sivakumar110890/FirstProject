/**
 * COPYRIGHT: Comviva Technologies Pvt. Ltd.
 * This software is the sole property of Comviva
 * and is protected by copyright law and international
 * treaty provisions. Unauthorized reproduction or
 * redistribution of this program, or any portion of
 * it may result in severe civil and criminal penalties
 * and will be prosecuted to the maximum extent possible
 * under the law. Comviva reserves all rights not
 * expressly granted. You may not reverse engineer, decompile,
 * or disassemble the software, except and only to the
 * extent that such activity is expressly permitted
 * by applicable law notwithstanding this limitation.
 * THIS SOFTWARE IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND/OR FITNESS FOR A PARTICULAR PURPOSE.
 * YOU ASSUME THE ENTIRE RISK AS TO THE ACCURACY
 * AND THE USE OF THIS SOFTWARE. Comviva SHALL NOT BE LIABLE FOR
 * ANY DAMAGES WHATSOEVER ARISING OUT OF THE USE OF OR INABILITY TO
 * USE THIS SOFTWARE, EVEN IF Comviva HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/
package com.emagine.ussd.service;

import static com.emagine.ussd.utils.USSDConstants.AA_ELIGIBLE;
import static com.emagine.ussd.utils.USSDConstants.ACCOUNT_BALANCE;
import static com.emagine.ussd.utils.USSDConstants.AIRTIME_ADVANCE_BALANCE;
import static com.emagine.ussd.utils.USSDConstants.A_VALUE;
import static com.emagine.ussd.utils.USSDConstants.CELL_ID_KEY_NAME;
import static com.emagine.ussd.utils.USSDConstants.LANG_CODE;
import static com.emagine.ussd.utils.USSDConstants.LOCATION_NUMBER_KEY;
import static com.emagine.ussd.utils.USSDConstants.MSISDN;
import static com.emagine.ussd.utils.USSDConstants.POOL_ID;
import static com.emagine.ussd.utils.USSDConstants.PREF_PAY_METHOD;
import static com.emagine.ussd.utils.USSDConstants.PRODUCT_TYPE;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.emagine.ussd.connection.UssdConnectionFactory;
import com.emagine.ussd.dao.LookUpDAO;
import com.emagine.ussd.model.OfferParams;
import com.emagine.ussd.model.RankingFormulae;
import com.emagine.ussd.utils.USSDConstants;


/**
 * @author shaju.kakkara
 *
 */
public class AirtimeAdvanceService {

	private static final Logger LOGGER = Logger.getLogger(AirtimeAdvanceService.class);
	private LookUpDAO lookUpDAO;

	/**
	 * 
	 */
	public AirtimeAdvanceService() {
		try {
			lookUpDAO = new LookUpDAO();
		} catch (Exception ex) {
			LOGGER.error("Exception occured in creating DAOs:: " + ex.getMessage(), ex);
		}
	}

	/**
	 * @param queryBaljson
	 * @return
	 * @throws Exception
	 */
	public RankingFormulae processRFCalculationForCellId(JSONObject queryBaljson) throws Exception {
		LOGGER.debug("processRFCalculationForCellId > START");
		RankingFormulae rf = new RankingFormulae();
		rf.setAirtimeBalance(queryBaljson.getLong(ACCOUNT_BALANCE));
		rf.setAaEligible(queryBaljson.has(AA_ELIGIBLE) && queryBaljson.getInt(AA_ELIGIBLE) == 1);
		rf.setAaBalance(queryBaljson.has(AIRTIME_ADVANCE_BALANCE) ? queryBaljson.getLong(AIRTIME_ADVANCE_BALANCE) : 0);
		rf.setPrefPayMethod(queryBaljson.getString(PREF_PAY_METHOD));
		rf.setaValue(Float.parseFloat(queryBaljson.getString(A_VALUE)));
		rf.setCellId(queryBaljson.getString(CELL_ID_KEY_NAME));
		rf.setLocationNumber(queryBaljson.getLong(LOCATION_NUMBER_KEY));
		rf.setPoolId(queryBaljson.getString(POOL_ID));
		LOGGER.debug("cell_id=> " + rf.getCellId());
		int langCode = queryBaljson.getInt(LANG_CODE);
		if (null != rf.getCellId()) {
			String poolId = rf.getPoolId();
			LOGGER.debug("poolId=> " + poolId);
			if (null != poolId && !poolId.isEmpty()) {
				rf = lookUpDAO.getRFParamsForLocation(queryBaljson.getString(MSISDN), queryBaljson.getString(PRODUCT_TYPE), langCode, rf, poolId);
				calculateRFValue(rf);
			} else {
				rf.setOfferParams(new ArrayList<>());
			}
		}
		return rf;
	}

	/**
	 * @param queryBaljson
	 * @return
	 * @throws Exception
	 */
	public RankingFormulae processRFRequest(JSONObject queryBaljson) throws Exception {
		LOGGER.debug("processRFRequest for ml offer =>");
		long startTime = System.currentTimeMillis();
		RankingFormulae rf = new RankingFormulae();
		rf.setAirtimeBalance(queryBaljson.getLong(USSDConstants.ACCOUNT_BALANCE));
		rf.setAaEligible(queryBaljson.has(AA_ELIGIBLE) && queryBaljson.getInt(AA_ELIGIBLE) == 1 );
		rf.setAaBalance(queryBaljson.has(AIRTIME_ADVANCE_BALANCE) ? queryBaljson.getLong(AIRTIME_ADVANCE_BALANCE) : 0);
		rf.setPrefPayMethod(queryBaljson.getString(USSDConstants.PREF_PAY_METHOD));
		rf.setaValue(Float.parseFloat(queryBaljson.getString(USSDConstants.A_VALUE)));
		rf.setCellId(queryBaljson.getString(USSDConstants.CELL_ID_KEY_NAME));
		rf.setLocationNumber(queryBaljson.getLong(USSDConstants.LOCATION_NUMBER_KEY));
		if (UssdConnectionFactory.checkSocialMenu.test(queryBaljson.getString(USSDConstants.USER_SELECTION))) {
			rf = lookUpDAO.getSocialRFParams(queryBaljson.getString(MSISDN),
					queryBaljson.getString(PRODUCT_TYPE),
					queryBaljson.getInt(USSDConstants.LANG_CODE), rf);
		} else {
			rf = lookUpDAO.getRFParams(queryBaljson.getString(MSISDN), queryBaljson.getString(PRODUCT_TYPE),
					queryBaljson.getInt(USSDConstants.LANG_CODE), rf);
		}
		calculateRFValue(rf);
		long endTime = System.currentTimeMillis();
		LOGGER.info("AA Procedure call time => " + (endTime - startTime) + " in ms");
		return rf;
	}

	private RankingFormulae calculateRFValue(RankingFormulae rf) {
		LOGGER.debug("calculateRFValue - ");
		int offersLength = rf.getOfferParams().size();
		OfferParams offerParams;
		LOGGER.debug("offersLength after prod info match=> " + offersLength);
		LOGGER.debug("PREF_PAY_METHOD: "+ rf.getPrefPayMethod());
		if (offersLength > 0) {
			for (int offerCount = 0; offerCount < offersLength; offerCount++) {
				offerParams = rf.getOfferParams().get(offerCount);
				LOGGER.debug("OfferParams: "+offerParams.toString());

				if (rf.getPrefPayMethod().equalsIgnoreCase(USSDConstants.PREF_PAY_MET_MPESA)) {				  
					setRfValue(rf, offerParams, offerCount); 
				} else {					
					if (rf.isAaEligible()) {
						setAirtimeAdvanceRfValue(rf, offerParams, offerCount);
					} else {					  
						setRfValue(rf, offerParams, offerCount);                       
					}
				}
				LOGGER.debug("RfValue - " + rf.getOfferParams().get(offerCount).getRfValue() + ", OfferId- " + rf.getOfferParams().get(offerCount).getOfferId());
			}
		}
		List<OfferParams> offerParamsList = rf.getOfferParams().stream()
				.sorted((offer1, offer2) -> Float.compare(offer2.getRfValue(), offer1.getRfValue()))
				.collect(Collectors.toList());

		rf.setOfferParams(offerParamsList);
		LOGGER.debug("Sorted Offer = >" + rf.getOfferParams());
		LOGGER.debug("calculateRFValue END ");
		return rf;
	}

	private void setRfValue(RankingFormulae rf, OfferParams offerParams, int offerCount) {
		if (offerParams.getOfferPrice() <= rf.getAirtimeBalance()) {
			rf.getOfferParams().get(offerCount).setRfValue(BigDecimal.valueOf(offerParams.getExpectedValue()).multiply(BigDecimal.valueOf(offerParams.getcValue())).floatValue());
			LOGGER.debug("Offer price <= AccountBalance -> RF= multiply EV with cValue");
		} else {
			rf.getOfferParams().get(offerCount).setRfValue(offerParams.getExpectedValue());
			LOGGER.debug("Offer price > AccountBalance -> Rf= EV");
		}            
	}

	private void setAirtimeAdvanceRfValue(RankingFormulae rf, OfferParams offerParams, int offerCount) {
		double netWeight = (double) rf.getAirtimeBalance() + ((double) rf.getAaBalance() * (double) rf.getaValue());
		LOGGER.debug("calculateRFValue netWeight - " + netWeight + " ,OfferId- " + rf.getOfferParams().get(offerCount).getOfferId());
		if (offerParams.getOfferPrice() <= netWeight) {
			rf.getOfferParams().get(offerCount).setRfValue(BigDecimal.valueOf(offerParams.getExpectedValue()).multiply(BigDecimal.valueOf(offerParams.getbValue())).floatValue());
			LOGGER.debug("Offer price <= netWeight -> Rf= multiply EV with bValue");
		} else {
			rf.getOfferParams().get(offerCount).setRfValue(offerParams.getExpectedValue());
			LOGGER.debug("Offer price > netWeight -> Rf= EV");
		}
	}

}
