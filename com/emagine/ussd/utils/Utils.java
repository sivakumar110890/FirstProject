package com.emagine.ussd.utils;

import static com.emagine.ussd.utils.USSDConstants.SUBID_CNTRYCD;
import static com.emagine.ussd.utils.USSDConstants.USSD_MORNING_OFFER_WINDOW_END;
import static com.emagine.ussd.utils.USSDConstants.USSD_MORNING_OFFER_WINDOW_START;
import static com.emagine.ussd.utils.USSDConstants.TWO_HUNDRED;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.kafka.common.config.ConfigException;
import org.apache.log4j.Logger;

import com.emagine.ussd.config.PropertiesLoader;

/**
 * Utils class
 * @author Anket Pratap Singh
 */
public class Utils {

	private static final Logger LOG = Logger.getLogger(Utils.class);
	private static Pattern patternInvalid = Pattern.compile("[^0-9]+") ;

	/**
     * Checks if a given string is null or empty.
     *
     * @param str The string to check.
     * @return {@code true} if the string is null or empty, otherwise {@code false}.
     */
	public static boolean isNullOrEmpty(String str) { 
		return null  == str || str.trim().equals("");
	}

	/**
     * Checks if a given string is numeric.
     *
     * @param str The string to check.
     * @return {@code true} if the string is numeric, otherwise {@code false}.
     */
	public static boolean isNumeric(String str) {
		try {
			Long.parseLong(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	/**
	 * Returns the current {@java.util.Date} in Client's timezone.
	 *
	 * @return
	 * @throws Exception
	 */
	public static Date getCurrentTimeStamp() {
		String timezone = null;
		try {
			timezone = PropertiesLoader.getValue(USSDConstants.AUDIT_TIMEZONE);
		} catch (Exception e) {
			timezone = "GMT+01";
			Logger.getRootLogger().error("Error occurred in reading AUDIT_TIMEZONE property:: " + e.getMessage(), e);
		}
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(timezone));
		cal.add(Calendar.MILLISECOND, cal.get(Calendar.ZONE_OFFSET));
		return cal.getTime();
	}

	/**
     * Converts a date to a string with the specified date format.
     *
     * @param date       The date to convert.
     * @param dateFormat The desired date format.
     * @return The formatted date string.
     */
	public static String getDateAsString(final Date date, final String dateFormat) {
		return new SimpleDateFormat(dateFormat).format(date);

	}

	/**
     * Converts a string to a database-friendly format, handling special characters and limiting length.
     *
     * @param comments The string to convert.
     * @return The converted string.
     */
	public static String convertToDBString(String comments) {
		if (null != comments) {
			if (comments.contains("'")) {
				comments = comments.replace("'", "");
			}
			if (comments.length() > TWO_HUNDRED) {
				comments = comments.substring(0, TWO_HUNDRED);
			}
		}
		return comments;
	}

	/**
     * Generates a unique transaction ID using {@link UUID}.
     *
     * @return A randomly generated transaction ID.
     */
	public synchronized String getTransactionID() {
		return UUID.randomUUID().toString();
	}

	/**
     * Checks if a string contains special characters.
     *
     * @param strLocationNumber The string to check.
     * @return {@code true} if the string contains special characters, otherwise {@code false}.
     */
	public static boolean containsSpecialChar (String strLocationNumber) {
		boolean flag= false ;

		Matcher m = patternInvalid.matcher(strLocationNumber);
		if (m.find()) {
			flag = true;
		}

		return flag;		
	}

	/**
     * Checks if the current time falls within the morning offer window.
     *
     * @return {@code true} if the current time is within the morning offer window, otherwise {@code false}.
     * @throws ConfigException If there is an error reading the configuration.
     */
	public static boolean checkMorningWindow() throws ConfigException {

		try {
			LOG.info("Morning offer Window Time check");
			LocalDateTime localTime = LocalDateTime.now();
			LocalTime currentLocalTime = localTime.toLocalTime();
			LocalTime startTime = LocalTime.parse(PropertiesLoader.getValue(USSD_MORNING_OFFER_WINDOW_START));
			LocalTime endTime = LocalTime.parse(PropertiesLoader.getValue(USSD_MORNING_OFFER_WINDOW_END));
			LOG.info("MORNING OFFER START TIME ::" + startTime);
			LOG.info("MORNING OFFER CURRENT TIME ::" + currentLocalTime);
			LOG.info("MORNING OFFER ENDTIME ::" + endTime);
			int greaterThenStartTime = currentLocalTime.compareTo(startTime);
			int lessThenEndTime = currentLocalTime.compareTo(endTime);
			if (greaterThenStartTime > 0 && lessThenEndTime < 0) {
				LOG.info("Customer falls in Morning offer window time!");
				return true;
			}else {
				LOG.info("Customer doesn't fall in Morning offer window time!"); 
				return false;
			} 
		}catch (Exception e) {
			LOG.error("Exception: ", e);
			return false; 
		}
	} 
	
	/**
     * Gets the MSISDN without the country code.
     *
     * @param msisdn The MSISDN with the country code.
     * @return The MSISDN without the country code.
     * @throws Exception If an error occurs.
     */
	public static String getMsisdnWithoutCcode(String msisdn) throws Exception {
		return msisdn.replaceFirst(PropertiesLoader.getValue(SUBID_CNTRYCD), "");		
	}

}
