/**
 *
 */
package com.emagine.ussd.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * @author udaykapavarapu
 *
 */
public class SplitMessageUtils {

    private static final Logger LOG = Logger.getLogger(SplitMessageUtils.class);
    private static final Pattern PATTERN = Pattern.compile("^\\*(\\d+)(?:\\*(.*))?#$");
    private final String code;
    private final String paramString;
    private final List<String> params;

    public SplitMessageUtils(String message) {
        LOG.debug("Into Split Message Utils Constructor");
        Matcher matcher = PATTERN.matcher(message);
        if (matcher.find()) {
            code = matcher.group(1);
            paramString = matcher.group(2);
        } else {
            code = null;
            paramString = message;
        }
        if (paramString == null) {
            params = new ArrayList<>();
        } else {
            params = Arrays.asList(paramString.split("\\*"));
        }
        LOG.debug("Code::" + code + ", ParamString::" + paramString + ",Params List:::" + params);
    }

    public String getCode() {
        return code;
    }

    public String getParams() {
        return paramString;
    }

    public String getParam(int index) {
        return params.get(index - 1);
    }

}
