package com.coomix.app.all.util;

import android.text.TextUtils;

/**
 * @author Crazy24k@gmail.com
 */
public class StringUtil {

    /**
     * 是否为空
     */
    public static boolean isTrimEmpty(String s) {
        return s == null || "".equals(s.trim());
    }

    /**
     * 通过{n},格式化.
     */
    public static String format(String src, Object... objects) {
        int k = 0;
        for (Object obj : objects) {
            src = src.replace("{" + k + "}", obj.toString());
            k++;
        }
        return src;
    }

    /**
     * 格式化显示银行卡，每四位一个空格
     */
    public static final String formatBankCard(String cardNum) {
        if (isTrimEmpty(cardNum)) {
            return "";
        } else {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < cardNum.length(); i++) {
                if (i % 4 == 0) {
                    builder.append(' ');
                }
                builder.append(cardNum.charAt(i));
            }
            return builder.toString();
        }
    }

    public static boolean isPhoneValid(String phone) {
        String PhonePattern = "^1[3|4|5|6|7|8|9][0-9]\\d{8}$";
        if (TextUtils.isEmpty(phone)) {
            return false;
        }
        return phone.matches(PhonePattern);
    }

    public static boolean isBankcardValid(String bankCardNum) {
        if (bankCardNum == null || bankCardNum.length() > 19 || bankCardNum.length() < 16) {
            return false;
        } else {
            int[] cardNoArr = new int[bankCardNum.length()];
            for (int i = 0; i < bankCardNum.length(); i++) {
                cardNoArr[i] = Integer.valueOf(String.valueOf(bankCardNum.charAt(i)));
            }
            for (int i = cardNoArr.length - 2; i >= 0; i -= 2) {
                cardNoArr[i] <<= 1;
                cardNoArr[i] = cardNoArr[i] / 10 + cardNoArr[i] % 10;
            }
            int sum = 0;
            for (int i = 0; i < cardNoArr.length; i++) {
                sum += cardNoArr[i];
            }
            return sum % 10 == 0;
        }
    }

    public static boolean isIdcardValid(String idCardNum) {
        return TextUtils.isEmpty(IdCardUtil.IDCardValidate(idCardNum));
    }
}
