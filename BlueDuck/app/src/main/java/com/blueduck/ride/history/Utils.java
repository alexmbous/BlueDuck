package com.blueduck.ride.history;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;

import com.blueduck.ride.utils.CommonSharedValues;
import com.blueduck.ride.utils.CommonUtils;
import com.google.android.gms.maps.model.LatLng;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Utils {

    public static String dateFormat(String timestamp){
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy");
        long startingDate = Long.valueOf(timestamp) * 1000;
        return sdf.format(new Date(startingDate));
    }

    public static String hourMinuteFormat(String date){
        String time = null;
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
        long startTime = Long.parseLong(date) * 1000;
        String timeStr = sdf.format(new Date(startTime));
        if (CommonUtils.getAmOrPm(startTime) == 0) {
            time = timeStr + " " + "AM";
        } else {
            time = timeStr + " " + "PM";
        }
        return time;
    }

    public static String hourMinuteFormat(long timestamp){
        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
        return sdf.format(new Date(timestamp));
    }

    public static Uri getGoogleStaticMapURL(
            Integer width,
            Integer height,
            Integer scale,
            String path,
            Double startLat,
            Double startLon,
            Double endLat,
            Double endLon ){
        return Uri.parse ("https://maps.googleapis.com/maps/api/staticmap")
                .buildUpon()
                .appendQueryParameter("size",String.format(Locale.ENGLISH,"%dx%d",width,height))
                .appendQueryParameter("scale",String.format(Locale.ENGLISH,"%d",scale))
                .appendQueryParameter("format","png")
                .appendQueryParameter("maptype","roadmap")
                .appendQueryParameter("path",String.format("weight:5|color:0x2DBAB4ff|enc:%s",path))
                .appendQueryParameter("style","feature:poi|visibility:off")
                .appendQueryParameter("markers",String.format(Locale.ENGLISH,"color:0x2DBAB4ff|label:S|%f,%f",startLat,startLon))
                .appendQueryParameter("markers",String.format(Locale.ENGLISH,"color:0x2DBAB4ff|label:F|%f,%f",endLat,endLon))
                // TODO eventually need to pass in a list of other points that will represent holds on the ride
                .appendQueryParameter("key",CommonSharedValues.MAP_KEY)
                .build();
    }

    public static String getAddressFromLatLng(Context context, LatLng latLng) {
        try {
            Geocoder geo = new Geocoder(context.getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geo.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses.isEmpty()) {
                return "Address Unknown";
            } else {
                if (addresses.size() > 0) {
                    String houseNumber = addresses.get(0).getSubThoroughfare() != null ? addresses.get(0).getSubThoroughfare() : "";
                    String street = addresses.get(0).getThoroughfare() != null ? addresses.get(0).getThoroughfare() : "";
                    return String.format("%s %s", houseNumber, street);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Address Unknown";
        }
        return "Address Unknown";
    }

    public static String formatValue(String distance, int decimalPlaces){
        if(distance==null || distance.equals("")) {
            return "0";
        }
        String retValue;
        BigDecimal distanceFormatted =  new BigDecimal(distance).setScale(decimalPlaces,BigDecimal.ROUND_HALF_UP);
        if(distanceFormatted.intValue()<999.0){
            retValue = distanceFormatted.toString();
        }
        else {
            retValue = format(distanceFormatted.doubleValue());
        }
        return retValue;
    }

    public static String format(Double number){
        String[] suffix = new String[]{"k","m","b","t"};
        int size = (number.intValue() != 0) ? (int) Math.log10(number) : 0;
        if (size >= 3){
            while (size % 3 != 0) {
                size = size - 1;
            }
        }
        double notation = Math.pow(10, size);
        return (size >= 3) ? (+(Math.round((number / notation) * 10) / 10.0d) + suffix[(size / 3) - 1]) : (+number + "");
    }

}
