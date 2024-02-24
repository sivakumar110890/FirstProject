package com.emagine.ussd.connection;

import org.json.JSONObject;
import org.springframework.stereotype.Component;
import com.emagine.ussd.model.InboundUssdMessage;
import com.emagine.ussd.model.UserInfo;
import com.emagine.ussd.service.InboundMessageService;

@Component
public class MessageListenerImpl implements IMessageListener {
    private InboundMessageService pedInboundMessageService;
    private InboundMessageService inboundMessageServiceObj;

    public MessageListenerImpl() {
        super();
        pedInboundMessageService = new InboundMessageService();
        inboundMessageServiceObj = new InboundMessageService();
    }

    @Override
    public UssdMessage requestReceived(UssdMessage ussdMessage, UssdSession ussdSession, UserInfo userInfo) {
        UssdMessage ussdMessageResponse = null;
        InboundMessageService inboundMessageService = new InboundMessageService();
        InboundUssdMessage inboundUssdMessage = inboundMessageService.getMenuForUser(ussdMessage, userInfo);
        if (null != inboundUssdMessage) {
            if (inboundUssdMessage.getIncomingLabel() == UssdMessage.REQUEST) {
                ussdMessageResponse = ussdSession.createRequestMessage();
            } else if (inboundUssdMessage.getIncomingLabel() == UssdMessage.NOTIFY) {
                ussdMessageResponse = ussdSession.createNotifyMessage();
            }
            if (null != ussdMessageResponse) {
                ussdMessageResponse.setProdIds(inboundUssdMessage.getProdIds());
                ussdMessageResponse.setMessageText(inboundUssdMessage.getClobString());
            }
            return ussdMessageResponse;
        }
        return null;
    }

    @Override
    public InboundUssdMessage requestReceived(String templateId, int langCode) throws Exception {
        InboundMessageService inboundMessageService = new InboundMessageService();
        return inboundMessageService.getMLMenuTemplate(templateId, langCode);
    }

    @Override
    public InboundUssdMessage ragMainMenuReqReceived(UserInfo userInfo) throws Exception {
        InboundMessageService inboundMessageService = new InboundMessageService();
        return inboundMessageService.getRagMainMenu(userInfo);
    }

    @Override
    public InboundUssdMessage ragSubMenuReqReceived(String messageBody, UserInfo userInfo) throws Exception {
        InboundMessageService inboundMessageService = new InboundMessageService();
        return inboundMessageService.getRagSubMenu(messageBody, userInfo);
    }

    @Override
    public InboundUssdMessage ragOfferInfoReqReceived(String messageBody, UserInfo userInfo) throws Exception {
        InboundMessageService inboundMessageService = new InboundMessageService();
        return inboundMessageService.getOfferInfoMenu(messageBody, userInfo);
    }
    
    @Override
    public InboundUssdMessage sagMainMenuReqReceived(UserInfo userInfo) throws Exception {
        InboundMessageService inboundMessageService = new InboundMessageService();
        return inboundMessageService.getSagMainMenu(userInfo);
    }

    @Override
    public InboundUssdMessage sagSubMenuReqReceived(String messageBody, UserInfo userInfo) throws Exception {
        InboundMessageService inboundMessageService = new InboundMessageService();
        return inboundMessageService.getSagSubMenu(messageBody, userInfo);
    }

    @Override
    public InboundUssdMessage sagOfferInfoReqReceived(UserInfo userInfo) throws Exception {
        InboundMessageService inboundMessageService = new InboundMessageService();
        return inboundMessageService.getSAGOfferInfoMenu(userInfo);
    }

    @Override
    public InboundUssdMessage subMenuReqReceived(String messageBody, UserInfo userInfo) throws Exception {
        InboundMessageService inboundMessageService = new InboundMessageService();
        return inboundMessageService.getSubMenuForMLUser(messageBody, userInfo);
    }

    @Override
    public InboundUssdMessage subMenuReqReceivedForTgtUser(String messageBody, UserInfo userInfo) throws Exception {
        InboundMessageService inboundMessageService = new InboundMessageService();
        return inboundMessageService.getSubMenuForTgtMLUser(messageBody, userInfo);
    }
    
	@Override
	public InboundUssdMessage morningSubMenuReqReceived(UserInfo userInfo) throws Exception {
		InboundMessageService inboundMessageService = new InboundMessageService();
		return inboundMessageService.getSubMenuForMorningUser(userInfo);
	} 
    
    @Override
    public InboundUssdMessage mlFinalReqReceived(String messageBody, UserInfo userInfo, boolean isMPesaReq, boolean provFlag) throws Exception {
        InboundMessageService inboundMessageService = new InboundMessageService();
        return inboundMessageService.finalReqReceivedForMLUser(messageBody, userInfo, isMPesaReq, provFlag);
    }
    
    @Override
    public InboundUssdMessage moFinalReqReceived(UserInfo userInfo) throws Exception {
        InboundMessageService inboundMessageService = new InboundMessageService();
        return inboundMessageService.finalReqReceivedForMOUser(userInfo);
    } 

    @Override
    public InboundUssdMessage getNotEnoughProdsMsg(String msisdn, int langCode) throws Exception {
        InboundMessageService inboundMessageService = new InboundMessageService();
        return inboundMessageService.getNotEnoughProdsMsg(msisdn, langCode);
    }
    
    @Override
    public InboundUssdMessage moFailureMsgReq(String msisdn , int langCode) throws Exception {
        InboundMessageService inboundMessageService = new InboundMessageService();
        return inboundMessageService.getMorningOfferFailureMsg(msisdn, langCode);
    }
    

    @Override
    public InboundUssdMessage userInEligible(int langCode) throws Exception {
        InboundMessageService inboundMessageService = new InboundMessageService();
        return inboundMessageService.getUserInEligibleTemplate(langCode);
    }


    @Override
    public InboundUssdMessage getErrorMenu(int langCode) throws Exception {
        InboundMessageService inboundMessageService = new InboundMessageService();
        return inboundMessageService.getErrorMenu(langCode);
    }

    @Override
    public UssdMessage responseReceived(UssdMessage ussdMessage, UssdSession ussdSession) {
        UssdMessage ussdMessageResponse = null;
        InboundMessageService inboundMessageService = new InboundMessageService();
        InboundUssdMessage inboundUssdMessage = inboundMessageService.respondToUserRequest(ussdMessage, ussdSession);

        if (null != inboundUssdMessage) {
            if (inboundUssdMessage.getIncomingLabel() == UssdMessage.REQUEST) {
                ussdMessageResponse = ussdSession.createRequestMessage();
            } else if (inboundUssdMessage.getIncomingLabel() == UssdMessage.NOTIFY) {
                ussdMessageResponse = ussdSession.createNotifyMessage();
            }
            if (ussdMessageResponse != null) {
                ussdMessageResponse.setSelProdId(inboundUssdMessage.getSelProdId());
                ussdMessageResponse.setMessageText(inboundUssdMessage.getClobString());
            }
            return ussdMessageResponse;
        }
        return null;
    }

    @Override
    public void sessionTimedout(Integer sessionId, UssdSession ussdSession) {
        InboundMessageService inboundMessageService = new InboundMessageService();
        inboundMessageService.timeout(sessionId, ussdSession);

    }

    @Override
    public void logInfo(UssdMessage ussdMessage, String status) {
        InboundMessageService inboundMessageService = new InboundMessageService();
        inboundMessageService.logInfo(ussdMessage, status);
    }

    @Override
    public void logInfo(Integer sessionId, TransmitMessage transmitMessage) {
        InboundMessageService inboundMessageService = new InboundMessageService();
        inboundMessageService.logInfo(sessionId, transmitMessage);
    }

    @Override
    public void logInfo(Integer sessionId, UserInfo userInfo, String messageBody, String status) throws Exception {
        InboundMessageService inboundMessageService = new InboundMessageService();
        inboundMessageService.logInfo(sessionId, userInfo, messageBody, status);
    }

    @Override
    public void cleanUp() {
        InboundMessageService inboundMessageService = new InboundMessageService();
        inboundMessageService.removeAllSessionData();
    }

    @Override
    public InboundUssdMessage paymentMenuReqReceived(String templateId, int langCode) throws Exception {
        return inboundMessageServiceObj.getMLMenuTemplate(templateId, langCode);
    }

    @Override
    public InboundUssdMessage currencyMenuReqReceived(String templateId, int langCode) throws Exception {
        return inboundMessageServiceObj.getMLMenuTemplate(templateId, langCode);
    }

    @Override
    public InboundUssdMessage userWrongInput(String templateId, int langCode) throws Exception {
        return inboundMessageServiceObj.getMLMenuTemplate(templateId, langCode);
    }

    @Override
    public InboundUssdMessage pedSubMenuReqReceived(String templateId, int langCode) throws Exception {

        return pedInboundMessageService.getMLMenuTemplate(templateId, langCode);
    }

    @Override
    public InboundUssdMessage pedPlayReqReceived(String templateId, int langCode) throws Exception {

        return pedInboundMessageService.getMLMenuTemplate(templateId, langCode);
    }

    @Override
    public InboundUssdMessage pedAvailablePlayReqReceived(UserInfo userInfo) throws Exception {
        return pedInboundMessageService.getPedAvailablePlays(userInfo);

    }

    @Override
    public InboundUssdMessage pedPlayReqReceived(UserInfo userInfo) throws Exception {

        return pedInboundMessageService.getPrizeForPlay(userInfo);

    }

    @Override
    public InboundUssdMessage pedPrizeHistoryReqReceived(UserInfo userInfo) throws Exception {
        return pedInboundMessageService.getPedPrizeHistory(userInfo);
    }
    
    @Override
    public InboundUssdMessage getConsentMenu(String templateId, int langCode) throws Exception{
    	 return inboundMessageServiceObj.getMLMenuTemplate(templateId, langCode);
    }
    
    @Override
    public InboundUssdMessage getDenialMsg(String templateId, int langCode) throws Exception {       
        return inboundMessageServiceObj.getMLMenuTemplate(templateId, langCode);
    }
    
    @Override
    public InboundUssdMessage getConsentOptOutMenu(String templateId, int langCode) throws Exception{
    	 return inboundMessageServiceObj.getMLMenuTemplate(templateId, langCode);
    }
    
    @Override
    public InboundUssdMessage getConsentOptOutFinalTemplate(String templateId, int langCode) throws Exception{
    	 return inboundMessageServiceObj.getMLMenuTemplate(templateId, langCode);
    }
    
    @Override
	public UserInfo getSubMenuForTgtMLUser(JSONObject queryBaljson) throws Exception {
		return inboundMessageServiceObj.getSubMenuForTgtMLUser(queryBaljson);
	}

	@Override
	public void getModifyTemplate(JSONObject queryBaljson, int langCode, InboundUssdMessage inboundUssdMessage, boolean isMainMenu)
			throws Exception {
		inboundMessageServiceObj.modifyTemplate(queryBaljson, langCode, inboundUssdMessage, isMainMenu);
	}
	
	@Override
	public void getGenerateUssdResponse(TransmitMessage message, UserInfo userInfo, InboundUssdMessage inboundUssdMessage, Integer sessionId) {
		inboundMessageServiceObj.generateUssdResponse(message, userInfo, inboundUssdMessage, userInfo.getUserMsgRef());
	}
    
    @Override
    public InboundUssdMessage getUserTemplate(String templateId, int langCode) throws Exception{
    	 return inboundMessageServiceObj.getMLMenuTemplate(templateId, langCode);
    }
    
    @Override
	public InboundUssdMessage townSubMenuReqReceived(UserInfo userInfo) throws Exception {	
		return inboundMessageServiceObj.getSubMenuForTownUser(userInfo);
	} 
    

    @Override
	public TransmitMessage getSubMenuMLMorningRequest(JSONObject queryBaljson, Integer sessionId, String messageBody,
			UserInfo userInfo) throws Exception {
    	return inboundMessageServiceObj.subMenuMLMorningRequest(queryBaljson, sessionId, messageBody, userInfo);
	}

	@Override
	public TransmitMessage getSubMenuMLTownRequest(JSONObject queryBaljson, Integer sessionId, String messageBody,
			UserInfo userInfo) throws Exception {
		return inboundMessageServiceObj.subMenuMLTownRequest(queryBaljson, sessionId, messageBody, userInfo);
	}

	@Override
	public TransmitMessage getNoOffersAvailableMsgRequest(Integer sessionId, String messageBody, UserInfo userInfo,
			String templateId) throws Exception {
		return inboundMessageServiceObj.noOffersAvailableMsgRequest(sessionId, messageBody, userInfo, templateId);
	}
}