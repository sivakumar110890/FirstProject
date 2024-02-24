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
package com.emagine.ussd.connection;

import org.json.JSONObject;

import com.emagine.ussd.model.InboundUssdMessage;
import com.emagine.ussd.model.UserInfo;

/**
 * @author shaju.kakkara
 *
 */
public interface IMessageListener {

	/**
	 * @param request
	 * @param session
	 * @return
	 * @throws Exception
	 */
	UssdMessage requestReceived(UssdMessage request, UssdSession session, UserInfo userInfo) throws Exception;

	/**
	 * @param request
	 * @param session
	 * @return
	 * @throws Exception
	 */
	UssdMessage responseReceived(UssdMessage request, UssdSession session) throws Exception;

	/**
	 * @param sessionId
	 * @param value
	 * @throws Exception
	 */
	void sessionTimedout(Integer sessionId, UssdSession value) throws Exception;

	/**
	 * @param sessionId
	 * @param ussdMessage
	 * @param status
	 * @throws Exception
	 */
	void logInfo(UssdMessage ussdMessage, String status) throws Exception;

	/**
	 * @param sessionId
	 * @param transmitMessage
	 * @throws Exception
	 */
	void logInfo(Integer sessionId, TransmitMessage transmitMessage) throws Exception;

	/**
	 * @param sessionId
	 * @param userInfo
	 * @param messageBody
	 * @param status
	 * @throws Exception
	 */
	void logInfo(Integer sessionId, UserInfo userInfo, String messageBody, String status) throws Exception;

	/**
	 * 
	 */
	void cleanUp();

	/**
	 * @param templateId
	 * @param langCode
	 * @return
	 * @throws Exception
	 */
	InboundUssdMessage requestReceived(String templateId, int langCode) throws Exception;

	/**
	 * @param messageBody
	 * @param userInfo
	 * @return
	 * @throws Exception
	 */
	InboundUssdMessage subMenuReqReceived(String messageBody, UserInfo userInfo) throws Exception;

	/**
	 * @param messageBody
	 * @param userInfo
	 * @return
	 * @throws Exception
	 */
	InboundUssdMessage subMenuReqReceivedForTgtUser(String messageBody, UserInfo userInfo) throws Exception;

	/**
	 *
	 * @param userInfo
	 * @return
	 * @throws Exception
	 */
	InboundUssdMessage morningSubMenuReqReceived(UserInfo userInfo) throws Exception;

	/**
	 * @param messageBody
	 * @param userInfo
	 * @param isMPesaReq
	 * @param provFlag
	 * @return
	 * @throws Exception
	 */
	InboundUssdMessage mlFinalReqReceived(String messageBody, UserInfo userInfo, boolean isMPesaReq, boolean provFlag)
			throws Exception;

	/**
	 * @param userInfo
	 * @return
	 * @throws Exception
	 */
	InboundUssdMessage moFinalReqReceived(UserInfo userInfo) throws Exception;

	/**
	 * @param msisdn
	 * @param langCode
	 * @return
	 * @throws Exception
	 */
	InboundUssdMessage getNotEnoughProdsMsg(String msisdn, int langCode) throws Exception;

	/**
	 * @param msisdn
	 * @param langCode
	 * @return
	 * @throws Exception
	 */
	InboundUssdMessage moFailureMsgReq(String msisdn, int langCode) throws Exception;

	/**
	 * @param langCode
	 * @return
	 * @throws Exception
	 */
	InboundUssdMessage userInEligible(int langCode) throws Exception;

	/**
	 * @param langCode
	 * @return
	 * @throws Exception
	 */
	InboundUssdMessage getErrorMenu(int langCode) throws Exception;

	/**
	 * @param userInfo
	 * @return
	 * @throws Exception
	 */
	InboundUssdMessage ragMainMenuReqReceived(UserInfo userInfo) throws Exception;

	/**
	 * @param messageBody
	 * @param userInfo
	 * @return
	 * @throws Exception
	 */
	InboundUssdMessage ragSubMenuReqReceived(String messageBody, UserInfo userInfo) throws Exception;

	/**
	 * @param messageBody
	 * @param userInfo
	 * @return
	 * @throws Exception
	 */
	InboundUssdMessage ragOfferInfoReqReceived(String messageBody, UserInfo userInfo) throws Exception;

	/**
	 * @param userInfo
	 * @return
	 * @throws Exception
	 */

	InboundUssdMessage sagMainMenuReqReceived(UserInfo userInfo) throws Exception;

	/**
	 * @param messageBody
	 * @param userInfo
	 * @return
	 * @throws Exception
	 */
	InboundUssdMessage sagSubMenuReqReceived(String messageBody, UserInfo userInfo) throws Exception;

	/**
	 * @param userInfo
	 * @return
	 * @throws Exception
	 */
	InboundUssdMessage sagOfferInfoReqReceived(UserInfo userInfo) throws Exception;

	/**
	 * @param templateId
	 * @param langCode
	 * @return
	 * @throws Exception
	 */

	InboundUssdMessage paymentMenuReqReceived(String templateId, int langCode) throws Exception;

	/**
	 * @param templateId
	 * @param langCode
	 * @return
	 * @throws Exception
	 */
	InboundUssdMessage currencyMenuReqReceived(String templateId, int langCode) throws Exception;

	/**
	 * @param templateId
	 * @param langCode
	 * @return
	 * @throws Exception
	 */
	InboundUssdMessage userWrongInput(String templateId, int langCode) throws Exception;

	/**
	 * @param templateId
	 * @param langCode
	 * @return
	 * @throws Exception
	 */
	InboundUssdMessage pedSubMenuReqReceived(String templateId, int langCode) throws Exception;

	/**
	 * @param templateId
	 * @param langCode
	 * @return
	 * @throws Exception
	 */
	InboundUssdMessage pedPlayReqReceived(String templateId, int langCode) throws Exception;

	/**
	 * @param userInfo
	 * @return
	 * @throws Exception
	 */
	InboundUssdMessage pedAvailablePlayReqReceived(UserInfo userInfo) throws Exception;

	/**
	 * @param userInfo
	 * @return
	 * @throws Exception
	 */
	InboundUssdMessage pedPlayReqReceived(UserInfo userInfo) throws Exception;

	/**
	 * @param userInfo
	 * @return
	 * @throws Exception
	 */
	InboundUssdMessage pedPrizeHistoryReqReceived(UserInfo userInfo) throws Exception;

	/**
	 * @param templateId
	 * @param langCode
	 * @return
	 * @throws Exception
	 */
	InboundUssdMessage getConsentMenu(String templateId, int langCode) throws Exception;

	/**
	 * @param templateId
	 * @param langCode
	 * @return
	 * @throws Exception
	 */
	InboundUssdMessage getDenialMsg(String templateId, int langCode) throws Exception;

	/**
	 * @param templateId
	 * @param langCode
	 * @return
	 * @throws Exception
	 */
	InboundUssdMessage getConsentOptOutMenu(String templateId, int langCode) throws Exception;

	/**
	 * @param templateId
	 * @param langCode
	 * @return
	 * @throws Exception
	 */
	InboundUssdMessage getConsentOptOutFinalTemplate(String templateId, int langCode) throws Exception;

	/**
	 * @param queryBaljson
	 * @return
	 * @throws Exception
	 */
	UserInfo getSubMenuForTgtMLUser(JSONObject queryBaljson) throws Exception;

	/**
	 * @param queryBaljson
	 * @param langCode
	 * @param inboundUssdMessage
	 * @param isMainMenu
	 * @throws Exception
	 */
	void getModifyTemplate(JSONObject queryBaljson, int langCode, InboundUssdMessage inboundUssdMessage,
			boolean isMainMenu) throws Exception;

	/**
	 * @param message
	 * @param userInfo
	 * @param inboundUssdMessage
	 * @param sessionId
	 */
	void getGenerateUssdResponse(TransmitMessage message, UserInfo userInfo, InboundUssdMessage inboundUssdMessage,
			Integer sessionId);

	/**
	 * @param templateId
	 * @param langCode
	 * @return
	 * @throws Exception
	 */
	InboundUssdMessage getUserTemplate(String templateId, int langCode) throws Exception;

	/**
	 *
	 * @param userInfo
	 * @return
	 * @throws Exception
	 */
	InboundUssdMessage townSubMenuReqReceived(UserInfo userInfo) throws Exception;

	/**
	 *
	 * @param queryBaljson
	 * @param sessionId
	 * @param messageBody
	 * @param userInfo
	 * @return
	 * @throws Exception
	 */
	TransmitMessage getSubMenuMLMorningRequest(JSONObject queryBaljson, Integer sessionId, String messageBody,
			UserInfo userInfo) throws Exception;


	/**
	 *
	 * @param queryBaljson
	 * @param sessionId
	 * @param messageBody
	 * @param userInfo
	 * @return
	 * @throws Exception
	 */
	TransmitMessage getSubMenuMLTownRequest(JSONObject queryBaljson, Integer sessionId, String messageBody,
			UserInfo userInfo) throws Exception;
	/**
	 *
	 * @param sessionId
	 * @param messageBody
	 * @param userInfo
	 * @param templateId
	 * @return
	 * @throws Exception
	 */
	TransmitMessage getNoOffersAvailableMsgRequest(Integer sessionId, String messageBody, UserInfo userInfo,  String templateId) throws Exception;

}
