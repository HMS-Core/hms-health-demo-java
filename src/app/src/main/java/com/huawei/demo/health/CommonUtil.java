package com.huawei.demo.health;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;
import android.widget.TextView;

import com.huawei.hms.common.ApiException;
import com.huawei.hms.hihealth.HiHealthStatusCodes;

/**
 * 功能描述
 *
 * @since 2020-09-19
 */
class CommonUtil {
    // Line separators for the display on the UI
    private static final String SPLIT = "*******************************" + System.lineSeparator();

    /**
     * Printout failure exception error code and error message
     *
     * @param  tag activity log tag
     * @param e   Exception object
     * @param api Interface name
     * @param logInfoView  Text View object
     */
    static void printFailureMessage(String tag, Exception e, String api, TextView logInfoView) {
        String errorCode = e.getMessage();
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(errorCode);
        if (e instanceof ApiException) {
            int eCode = ((ApiException) e).getStatusCode();
            String errorMsg = HiHealthStatusCodes.getStatusCodeMessage(eCode);
            logger(api + " failure " + eCode + ":" + errorMsg, tag, logInfoView);
            return;
        } else if (isNum.matches()) {
            String errorMsg = HiHealthStatusCodes.getStatusCodeMessage(Integer.parseInt(errorCode));
            logger(api + " failure " + errorCode + ":" + errorMsg, tag, logInfoView);
            return;
        } else {
            logger(api + " failure " + errorCode, tag, logInfoView);
        }
        logger(SPLIT, tag, logInfoView);
    }

    /**
     * Send the operation result logs to the logcat and TextView control on the UI
     *
     * @param string indicating the log string
     * @param  tag activity log tag
     * @param logInfoView  Text View object
     */
    static void logger(String string, String tag, TextView logInfoView) {
        Log.i(tag, string);
        logInfoView.append(string + System.lineSeparator());
        int offset = logInfoView.getLineCount() * logInfoView.getLineHeight();
        if (offset > logInfoView.getHeight()) {
            logInfoView.scrollTo(0, offset - logInfoView.getHeight());
        }
    }

}
