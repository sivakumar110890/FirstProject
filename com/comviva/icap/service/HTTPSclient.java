package com.comviva.icap.service;

import static com.comviva.icap.utils.ICAPConstants.*;
import static com.comviva.icap.utils.Utils.parseXML;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.emagine.ussd.config.PropertiesLoader;

/**
 * The HTTPSclient class provides functionality for making HTTPS requests.
 * <p>
 * This class encapsulates the logic for creating and managing HTTP client instances
 * that can send HTTPS requests. It is designed to be used in the context of the ICAP service.
 * </p>
 *
 * @author Anket Pratap Singh
 */
public class HTTPSclient {
	private static final Logger LOGGER = Logger.getLogger(HTTPSclient.class);
	
	private static int logDisableFlag;
	static {
		try {
			logDisableFlag = PropertiesLoader.getIntValue(ICAP_LOG_DISABLE_FLAG);
		} catch (Exception e) {
			LOGGER.error("Error in loading ICAP login EndPoint :: ", e);
		}
	}
	
	/**
     * Sends an HTTP GET request to the specified URL and processes the response.
     *
     * @param url The URL to which the GET request is sent.
     * @return A JSONObject containing the response from the server.
     * @throws Exception If an error occurs during the HTTP request.
     */
	public JSONObject sendRequest(String url) throws Exception {
		JSONObject response = new JSONObject();
		if(logDisableFlag == 1) {
			LOGGER.info("Inside sendRequest:: " + url);
		}
		HttpURLConnection connection = null;
		try {
			// SSL certificate setup and connection initialization
			HostnameVerifier hv = (String urlHostName, SSLSession session) -> urlHostName
					.equalsIgnoreCase(session.getPeerHost());
			trustAllHttpsCertificates();
			HttpsURLConnection.setDefaultHostnameVerifier(hv);
			LOGGER.debug("Trusted the certificate!");

			URL postURL = new URL(url);
			String strProtocol = postURL.getProtocol();
			if (HTTPS.equals(strProtocol)) {
				connection = (HttpsURLConnection) postURL.openConnection();
			} else {
				connection = (HttpURLConnection) postURL.openConnection();
			}
			// Set the request method to GET
			connection.setRequestMethod(GET);

			// Set timeouts and other connection properties
			connection.setReadTimeout(PropertiesLoader.getIntValue(READ_TIMEOUT_MILLIS));
			connection.setConnectTimeout(PropertiesLoader.getIntValue(CONN_TIMEOUT_MILLIS));
			connection.setUseCaches(false);

			// Get the response code
			int responseCode = connection.getResponseCode();
			LOGGER.debug("After getting response code :: " + responseCode);
			LOGGER.debug("Response :: " + HttpStatus.getStatusText(responseCode));

			if (connection.getInputStream() != null) {
				LOGGER.debug("Inside get stream ");
				BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				StringBuilder builder = new StringBuilder();
				String output;
				while ((output = br.readLine()) != null) {
					builder.append(output);
				}
				LOGGER.info("Response received from ICAP :: " + builder.toString());
				parseXML(builder.toString(), response);
			}

		} catch (Exception e) {
			response.put(ICAP_STATUS, FAILURE);
			response.put(ERROR_CODE, PropertiesLoader.getValue(ICAP_CONN_FAILED_CD));
			response.put(ERROR_MESSAGE, PropertiesLoader.getValue(ICAP_CONN_FAILED_MSG));
			LOGGER.error("Exception :: ", e);
		}
		return response;
	}

	private static void trustAllHttpsCertificates() {
		try {
			javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
			javax.net.ssl.TrustManager tm = new CustomTM();
			trustAllCerts[0] = tm;
			javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("TLSv1.2");
			sc.init(null, trustAllCerts, null);
			javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (KeyManagementException e) {
			LOGGER.error("KeyManagementException :: ", e);

		} catch (NoSuchAlgorithmException e) {
			LOGGER.error("NoSuchAlgorithmException :: ", e);
		}

	}

	public static class CustomTM implements javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager {
		@Override
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return new java.security.cert.X509Certificate[0];
		}

		@Override
		public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
				throws java.security.cert.CertificateException {
			try {
				if (PropertiesLoader.getBooleanValue(SSL_CERTIFICATE_CHECK_ENABLED)) {
					certs[0].checkValidity();
				}
			} catch (Exception e) {
				LOGGER.error("Exception in Checking Certificate Validity");
				throw new CertificateException("Certificate not valid or trusted.", e);
			}
		}

		@Override
		public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
				throws CertificateException {
			try {
				if (PropertiesLoader.getBooleanValue(SSL_CERTIFICATE_CHECK_ENABLED)) {
					certs[0].checkValidity();
				}
			} catch (Exception e) {
				LOGGER.error("Exception in Checking Certificate Validity");
				throw new CertificateException("Certificate not valid or trusted.", e);
			}
		}
	}
}
