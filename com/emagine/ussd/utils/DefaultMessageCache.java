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
package com.emagine.ussd.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.emagine.ussd.dao.LookUpDAO;
import com.emagine.ussd.exception.USSDPluginException;
import com.emagine.ussd.model.DefaultMessageDTO;

/**
 *
 */
public class DefaultMessageCache {
    private static final Logger LOGGER = Logger.getLogger(DefaultMessageCache.class);
    private final Map<Integer, DefaultMessageDTO> cache;

    private DefaultMessageCache() {
        cache = new HashMap<>();
        try {
            init();
        } catch (Exception ex) {
            LOGGER.error("Error occured at ==> ", ex);
        }
    }

    /**
     * @return
     */
    public static DefaultMessageCache instance() {
        return InstanceHolder.instance;
    }

    /**
     * @param key
     * @return
     */
    public DefaultMessageDTO get(int key) {
        return cache.get(key);
    }

    /**
     * @throws Exception
     */
    public void init() throws Exception {
        LookUpDAO dao = new LookUpDAO();
        Map<Integer, DefaultMessageDTO> cacheMap = dao.getDefaultMessagesMap();
        if (null != cacheMap) {
            if (null != cache) {
                cache.clear();
                cache.putAll(cacheMap);
            }
        } else {
            throw new USSDPluginException ("Could not find any DefaultMessage data from database");
        }
    }

    private static class InstanceHolder {
        private static DefaultMessageCache instance = new DefaultMessageCache();
    }
}
