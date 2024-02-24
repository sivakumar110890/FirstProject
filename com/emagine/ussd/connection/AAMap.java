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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.emagine.ussd.model.UserInfo;

public class AAMap {

    private Map<Integer, UserInfo> airtimeAdvanceMap = new ConcurrentHashMap<>();

    /**
     * @return
     */
    public static AAMap instance() {
        return InstanceHolder.instance;
    }

    /**
     * @param key
     * @return
     */
    public UserInfo get(Integer key) {
        return this.airtimeAdvanceMap.get(key);
    }

    /**
     * @param key
     * @param value
     */
    public void put(Integer key, UserInfo value) {
        this.airtimeAdvanceMap.put(key, value);
    }

    /**
     * @param key
     */
    public void remove(Integer key) {
        this.airtimeAdvanceMap.remove(key);
    }

    private static class InstanceHolder {
        private static AAMap instance = new AAMap();
    }
}
