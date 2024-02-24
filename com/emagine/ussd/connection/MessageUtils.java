package com.emagine.ussd.connection;

import java.util.ArrayList;
import java.util.List;

public class MessageUtils {

    private MessageUtils() {
    }

    public static List<String> splitMessageText(
                    String messageText,
                    final String breakTagRegex,
                    final int maxMessageTextLength) {
        return splitMessageText(messageText, 0, breakTagRegex, maxMessageTextLength);
    }

    public static List<String> splitMessageText(String messageText, final int dataCoding,
        final String breakTagRegex, final int maxMessageTextLength) {

        final List<String> result = new ArrayList<>();

        if (messageText == null || messageText.trim().length() == 0) {
            return result;
        }

        final String[] fragments = split(messageText, breakTagRegex);

        if (fragments.length == 0) {
            return result;
        } else if (fragments.length == 1 && fragments[0].length() <= maxMessageTextLength) {
            result.add(fragments[0]);
            return result;
        }

        StringBuffer currentMessagePart = new StringBuffer();

        // iterate over the fragments
        for (int i = 0; i < fragments.length; ++i) {

            final String previousFragment = (i == 0 ? null : fragments[i - 1]);
            final String fragment = fragments[i];

            // if we can add this fragment to current message part then do so.
            if ((currentMessagePart.length() + fragment.length() + 1) <= maxMessageTextLength) {
                if (previousFragment != null && !Character.isWhitespace(previousFragment.charAt(previousFragment.length() - 1))) {
                    currentMessagePart.append(' ');
                }
                currentMessagePart.append(fragment);
            } else {
                // else if the fragment will fit into a message part
                if (fragment.length() <= maxMessageTextLength) {

                    // if there is a current part
                    if (currentMessagePart.length() > 0) {
                        // then add the current message part to the result list
                        result.add(currentMessagePart.toString());
                        // create a new message part
                        currentMessagePart = new StringBuffer();
                    }
                    // append this fragment into the message part
                    currentMessagePart.append(fragment);
                }
                // else the fragment is too big to place into a message part
                // and has to be broken down further.
                else {
                    // if the current message part is at the maximum length
                    if (currentMessagePart.length() == maxMessageTextLength) {
                        // add the current message part to the result list
                        result.add(currentMessagePart.toString());
                        // create a new message part
                        currentMessagePart = new StringBuffer();
                    }
                    // define a couple of variable to use for creating substrings
                    int start = 0;
                    int end = 0;
                    // we will iterate over each character in the fragment
                    for (int j = 0; j < fragment.length(); ++j) {

                        // if there is a current part
                        if (currentMessagePart.length() > 0) {
                            // then add the current message part to the result list
                            result.add(currentMessagePart.toString());
                            // create a new message part
                            currentMessagePart = new StringBuffer();
                        }
                        // if the current character is whitespace then remember this position
                        if (Character.isWhitespace(fragment.charAt(j))) {
                            end = j;
                        }
                        // if the current character position relative to the start position
                        // would result in a substring too long to fit in a part
                        // then append the substring from start to end positions.
                        if ((j - start + currentMessagePart.length()) >= maxMessageTextLength) {
                            if (start == end) {
                                // there have been no whitespace characters since the start
                                // so set the end to be here.
                                end = j;
                            }
                            currentMessagePart.append(fragment.substring(start, end));
                            start = end;
                        }
                    }

                    // if there is still a portion of the fragment left over then add it.
                    if (start < fragment.length()) {
                        // if there is a current part
                        if (currentMessagePart.length() > 0) {
                            // then add the current message part to the result list
                            result.add(currentMessagePart.toString());
                            // create a new message part
                            currentMessagePart = new StringBuffer();
                        }
                        currentMessagePart.append(fragment.substring(start));
                    }
                }
            }
        }
        result.add(currentMessagePart.toString());

        return result;
    }

    protected static String[] split(final String message, final String delimiter) {

        final ArrayList<String> fields = new ArrayList<String>();

        final int delimiterLength = delimiter.length();

        int begin = 0;
        int end = 0;

        String substring = null;

        do {
            end = message.indexOf(delimiter, begin);

            if (end < 0) {
                substring = message.substring(begin);
            } else {
                substring = message.substring(begin, end);
            }

            // Only add the field to the results if it is not empty and not whitespace.
            if (substring.trim().length() > 0) {
                fields.add(substring);
            }

            begin = end + delimiterLength;
        } while (end >= 0);

        return fields.toArray(new String[fields.size()]);
    }

}
