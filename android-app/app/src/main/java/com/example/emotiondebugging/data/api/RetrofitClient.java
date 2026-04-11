package com.example.emotiondebugging.data.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // Dùng 10.0.2.2 khi chạy trên Android Emulator (trỏ về localhost máy tính)
    private static final String BASE_URL = "http://10.0.2.2:3000/";

    private static Retrofit instance;

    public static Retrofit getInstance() {
        if (instance == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            instance = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return instance;
    }

    public static ProfileApiService getProfileApi() {
        return getInstance().create(ProfileApiService.class);
    }

    public static AdminApiService getAdminApi() {
        return getInstance().create(AdminApiService.class);
    }
}
