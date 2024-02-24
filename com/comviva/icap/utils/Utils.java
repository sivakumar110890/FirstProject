package com.comviva.icap.utils;

import static com.comviva.icap.utils.ICAPConstants.ATTRIBUTE;
import static com.comviva.icap.utils.ICAPConstants.ERROR_CODE;
import static com.comviva.icap.utils.ICAPConstants.ERROR_MESSAGE;
import static com.comviva.icap.utils.ICAPConstants.ICAP_STATUS;
import static com.comviva.icap.utils.ICAPConstants.ICAP_LANGUAGE_CATEGORY;
import static com.comviva.icap.utils.ICAPConstants.NAME;
import static com.comviva.icap.utils.ICAPConstants.ICAP_OFFER_PAYMENT_METHOD;
import static com.comviva.icap.utils.ICAPConstants.ICAP_PAYMENT_METHOD;
import static com.comviva.icap.utils.ICAPConstants.SECURITY_KEY;
import static com.comviva.icap.utils.ICAPConstants.ICAP_SUBSCRIBER_START_DATE;
import static com.comviva.icap.utils.ICAPConstants.ICAP_SUBSCRIBER_STATE;
import static com.comviva.icap.utils.ICAPConstants.SUCCESS;
import static com.comviva.icap.utils.ICAPConstants.USER_ID;
import static com.comviva.icap.utils.ICAPConstants.VALUE;
import java.io.StringReader;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * The Utils class provides utility methods for parsing XML data in the context of the ICAP service.
 * <p>
 * It includes methods for securely parsing XML, extracting information, and mapping attributes to JSON keys.
 * </p>
 *
 * @author Anket Pratap Singh
 */
public class Utils {

	private static final Logger LOGGER = Logger.getLogger(Utils.class);

	// Private constructor to prevent instantiation
	private Utils() {
	}

	/**
     * Parses XML data and populates a JSONObject with the extracted information.
     *
     * @param xmlData   The XML data to be parsed.
     * @param response  The JSONObject to be populated with parsed information.
     */
	public static void parseXML(String xmlData, JSONObject response) {
		try {
			DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
			documentFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			documentFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			documentFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			documentFactory.setNamespaceAware(true);
			documentFactory.setValidating(false);

			DocumentBuilder builder = documentFactory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(xmlData)));

			// Check the "status" element in the header
			String status = getNodeValue(document, ICAP_STATUS);
			response.put(ICAP_STATUS, status);

			if (!status.equalsIgnoreCase(SUCCESS)) {
				String errorCode = getNodeValue(document, ERROR_CODE);
				String errorMessage = getNodeValue(document, ERROR_MESSAGE);
				response.put(ERROR_CODE, errorCode);
				response.put(ERROR_MESSAGE, errorMessage);
			} else {

				NodeList attributeNodes = document.getElementsByTagName(ATTRIBUTE);
				for (int i = 0; i < attributeNodes.getLength(); i++) {
					Element attributeElement = (Element) attributeNodes.item(i);
					Element nameElement = (Element) attributeElement.getElementsByTagName(NAME).item(0);
					Element valueElement = (Element) attributeElement.getElementsByTagName(VALUE).item(0);

					if (nameElement != null && valueElement != null) {
						String name = nameElement.getTextContent();
						String value = valueElement.getTextContent();
						// Map attribute names to JSON keys
						mapAttributeToJSONKey(response, name, value);

					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception :: ", e);
		}
	}

	/**
     * Retrieves the text content of the first occurrence of a specified XML element.
     *
     * @param document  The XML document.
     * @param tagName   The name of the XML element.
     * @return The text content of the specified XML element, or an empty string if not found.
     */
	private static String getNodeValue(Document document, String tagName) {
		NodeList nodeList = document.getElementsByTagName(tagName);
		if (nodeList.getLength() > 0) {
			return nodeList.item(0).getTextContent();
		}
		return "";
	}

	/**
     * Maps attribute names to JSON keys based on predefined mappings.
     *
     * @param response  The JSONObject to which attribute values are mapped.
     * @param name      The attribute name.
     * @param value     The attribute value.
     */
	private static void mapAttributeToJSONKey(JSONObject response, String name, String value) {
		switch (name) {
			case USER_ID:
				response.put(USER_ID, value);
				break;
			case SECURITY_KEY:
				response.put(SECURITY_KEY, value);
				break;
			case ICAP_LANGUAGE_CATEGORY:
				response.put(ICAP_LANGUAGE_CATEGORY, value);
				break;
			case ICAP_SUBSCRIBER_STATE:
				response.put(ICAP_SUBSCRIBER_STATE, value);
				break;
			case ICAP_PAYMENT_METHOD:
				response.put(ICAP_PAYMENT_METHOD, value);
				break;
			case ICAP_OFFER_PAYMENT_METHOD:
				response.put(ICAP_OFFER_PAYMENT_METHOD, value);
				break;
			case ICAP_SUBSCRIBER_START_DATE:
				response.put(ICAP_SUBSCRIBER_START_DATE, value);
				break;
			default:
				// Handle unknown attribute names (optional)
				break;
		}
	}

}
