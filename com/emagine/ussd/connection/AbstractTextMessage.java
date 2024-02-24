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

import java.util.ArrayList;

/**
 * @author udaykapavarapu
 */
public abstract class AbstractTextMessage {

    protected static String encodeString(final String value) {

        if (value == null) {
            return null;
        }

        final StringBuilder s = new StringBuilder();
        final int valueLength = value.length();
        int ch = 0;
        for (int i = 0; i < valueLength; ++i) {
            ch = value.charAt(i);
            if ('\r' == ch) {
                s.append("\\r");
            } else if ('\n' == ch) {
                s.append("\\n");
            } else if ('\t' == ch) {
                s.append("\\t");
            } else if ('\\' == ch) {
                s.append("\\\\");
            } else {
                s.append((char) ch);
            }
        }
        return s.toString();
    }

    protected static String decodeString(final String value) {

        final StringBuilder s = new StringBuilder();
        final int valueLength = value.length();
        int ch = 0;
        for (int i = 0; i < valueLength; ++i) {
            ch = value.charAt(i);
            if ('\\' == ch) {
                ++i;
                if (i < valueLength) {
                    ch = value.charAt(i);
                    if ('n' == ch) {
                        s.append('\n');
                    } else if ('t' == ch) {
                        s.append('\t');
                    } else if ('r' == ch) {
                        s.append('\r');
                    } else if ('\\' == ch) {
                        s.append('\\');
                    } else {
                        s.append('\\');
                        s.append((char) ch);
                    }
                } else {
                    s.append('\\');
                }
            } else {
                s.append((char) ch);
            }
        }
        return s.toString();
    }

    /**
     * @return
     */
    public abstract String serializeToString();

    /**
     *
     */
    public String toString() {
        return serializeToString();
    }

    protected String[] split(final String record, final String delimiter) {

        final ArrayList<String> fields = new ArrayList<>();

        final int delimiterLength = delimiter.length();

        int begin = 0;
        int end = 0;

        do {
            end = record.indexOf(delimiter, begin);
            if (end < 0) {
                fields.add(record.substring(begin));
            } else {
                fields.add(record.substring(begin, end));
            }
            begin = end + delimiterLength;
        } while (end >= 0);

        return fields.toArray(new String[fields.size()]);
    }
}
