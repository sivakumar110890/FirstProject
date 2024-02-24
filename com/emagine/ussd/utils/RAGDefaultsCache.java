package com.emagine.ussd.utils;

import java.time.LocalDate;
import java.util.List;

import org.apache.log4j.Logger;

import com.emagine.ussd.dao.LookUpDAO;

public class RAGDefaultsCache {
	private static String weekStartDate;
	private static String weekEndDate;
	private static String nextOfferAvailableDate;
	private static LocalDate dateTime;
	private static final String ERROR_OCCURED = "Error occured at :: ";

	private static final Logger LOGGER = Logger.getLogger(RAGDefaultsCache.class);

	private RAGDefaultsCache() {
		try {
			init();
		} catch (Exception ex) {
			LOGGER.error(ERROR_OCCURED, ex);
		}
	}

	public String getWeekStartDate() {
		LocalDate currentDate = LocalDate.now();
		if (!currentDate.equals(dateTime)) {
			try {
				init();
			} catch (Exception ex) {
				LOGGER.error(ERROR_OCCURED, ex);
			}
		}
		return weekStartDate;
	}

	public String getWeekEndDate() {
		LocalDate currentDate = LocalDate.now();
		if (!currentDate.equals(dateTime)) {
			try {
				init();
			} catch (Exception ex) {
				LOGGER.error(ERROR_OCCURED, ex);
			}
		}
		return weekEndDate;
	}

	public String getNextOfferAvailableDate() {
		LocalDate currentDate = LocalDate.now();
		if (!currentDate.equals(dateTime)) {
			try {
				init();
			} catch (Exception ex) {
				LOGGER.error(ERROR_OCCURED, ex);
			}
		}
		return nextOfferAvailableDate;
	}

	public synchronized void reloadCache() {
		try {
			init();
		} catch (Exception ex) {
			LOGGER.error(ERROR_OCCURED, ex);
		}
	}

	public static RAGDefaultsCache instance() {
		return InstanceHolder.instance;
	}

	private static void init() throws Exception {
		LOGGER.info("RAGDefaultsCache Loading START");
		dateTime = LocalDate.now();
		LookUpDAO lookUpDAO = new LookUpDAO();
		List<String> ragValues = lookUpDAO.getRagDefaultValues();
		weekStartDate = ragValues.get(0);
		weekEndDate = ragValues.get(1);
		nextOfferAvailableDate = ragValues.get(2);
		LOGGER.info("RAGDefaultsCache Loading END");
	}

	private static class InstanceHolder {
		private static RAGDefaultsCache instance = new RAGDefaultsCache();
	}
}
