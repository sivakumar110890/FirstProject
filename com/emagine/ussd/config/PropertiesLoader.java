package com.emagine.ussd.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class PropertiesLoader {

    private static final Logger LOGGER = Logger.getLogger(PropertiesLoader.class);
    private static final String PROPERTIES_FILENAME = "/config/ussd.properties";
    private static Properties ussdProperties = null;

    // These keys are defined in camconsumers.properties file...

    public static void loadUSSDProperties() throws Exception {
        String userDir = System.getProperty("user.dir");
        File aFile = new File(userDir + PROPERTIES_FILENAME);
        if (aFile.exists()) {
            try(InputStream inputStream = new FileInputStream(aFile)) {
                ussdProperties = new Properties();
                ussdProperties.load(inputStream);
                LOGGER.debug("loadUSSDProperties method :: loaded the property file");
            } catch (Exception e) {
                LOGGER.error("loadUSSDProperties method :: Error Loading enba Properties From File: " + aFile.getName());
                throw e;
            } 
        } else {
            throw new FileNotFoundException(aFile.getAbsoluteFile().getName() + " Is NOT Found");
        }
    }

    public static String getValue(String propertyKey) throws Exception {
        try {
            if (ussdProperties == null) {
                LOGGER.info("getValue method :: First Call to get a value from ussd.properties file. Should see this line only once...");
                loadUSSDProperties();
            }
        } catch (Exception e) {
            LOGGER.error("getValue :: Mostly ussd.properties File is NOT there. Error Getting the Value For Key: " + propertyKey);
            throw e;
        }
        return ussdProperties.getProperty(propertyKey);
    }

    public static int getIntValue(String cmpPropertyKey) throws Exception {
        String stringValue = getValue(cmpPropertyKey);
        if (stringValue != null && stringValue.trim().length() != 0) {
            return (new Integer(stringValue)).intValue();
        }
        return 0;
    }
    
    public static boolean getBooleanValue(String cmpPropertyKey) throws Exception {
		String stringValue = getValue(cmpPropertyKey);
		if (stringValue != null && stringValue.trim().length() != 0) {
			return (new Boolean(stringValue)).booleanValue();
		}
		return false;
	}

    public static String reloadUSSDProperties() throws Exception {
        try {
            ussdProperties = null;
            loadUSSDProperties();
            return "SUCCESS";
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }
}