package com.mapr.utils;

/**
 * Created by aravi on 10/15/17.
 */
public class GeneralUtils {
    public static String getInvokingClassName() {
        return new Throwable().getStackTrace()[1].getClassName();
    }
}
