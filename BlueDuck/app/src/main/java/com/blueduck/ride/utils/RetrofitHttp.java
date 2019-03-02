package com.blueduck.ride.utils;


import com.blueduck.ride.utils.converter.JsonConverterFactory;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Network request class
 * 网络请求类
 */
public class RetrofitHttp {

//    public static final String BASE_URL = "http://192.168.100.246:10171/BlueDuck/";//本地
    public static final String BASE_URL = "https://backend.flywild.io/blueduck/";//服务器
    public static final String GOOGLE_DIRECTIONS_URL = "https://maps.googleapis.com/maps/api/directions/";

    public static synchronized Retrofit getRetrofit(int converterType){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(createFactory(converterType))
                .build();
        return retrofit;
    }

    public static synchronized Retrofit getGoogleRetrofit(int converterType){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GOOGLE_DIRECTIONS_URL)
                .client(client)
                .addConverterFactory(createFactory(converterType))
                .build();
        return retrofit;
    }

    public static final OkHttpClient client = new OkHttpClient.Builder().
            connectTimeout(60, TimeUnit.SECONDS).
            readTimeout(60, TimeUnit.SECONDS).
            writeTimeout(60, TimeUnit.SECONDS).build();

    private static Converter.Factory createFactory(int converterType){
        if (converterType == 0){
            return JsonConverterFactory.create();
        }else{
            return GsonConverterFactory.create();
        }
    }
}
