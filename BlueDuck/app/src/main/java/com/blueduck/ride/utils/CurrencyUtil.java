package com.blueduck.ride.utils;

import java.text.DecimalFormat;

public class CurrencyUtil {

    public static String convertToTwoDecimalPlaces(double value) {
        DecimalFormat twoPlaces = new DecimalFormat("0.00");
        return twoPlaces.format(value);
    }
}
