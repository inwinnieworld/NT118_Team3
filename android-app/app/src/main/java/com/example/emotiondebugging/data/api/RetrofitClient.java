package com.example.emotiondebugging.data.api;

import com.example.emotiondebugging.BuildConfig;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // Dùng 10.0.2.2 khi chạy trên Android Emulator (trỏ về localhost máy tính)
    private static final String BASE_URL = normalizeBaseUrl(BuildConfig.API_BASE_URL);

    private static Retrofit instance;

    public static Retrofit getInstance() {
        if (instance == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build();

            instance = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return instance;
    }

    public static String resolveMediaUrl(String value) {
        if (value == null || value.trim().isEmpty()) return "";
        String url = value.trim();
        if (url.startsWith("http://") || url.startsWith("https://")
                || url.startsWith("file://") || url.startsWith("content://")) return url;
        return BASE_URL + (url.startsWith("/") ? url.substring(1) : url);
    }

    private static String normalizeBaseUrl(String value) {
        String url = value == null ? "" : value.trim();
        if (url.isEmpty()) url = "http://10.0.2.2:3000/";
        return url.endsWith("/") ? url : url + "/";
    }

    // --- CÁC HÀM GỌI API ĐÃ ĐƯỢC GỘP CHUNG ---

    // Luồng Auth (của Thắng)
    public static AuthApiService getAuthApiService() {
        return getInstance().create(AuthApiService.class);
    }

    // Luồng Profile (của Chính)
    public static ProfileApiService getProfileApi() {
        return getInstance().create(ProfileApiService.class);
    }

    // Luồng Admin (của Chính)
    public static AdminApiService getAdminApi() {
        return getInstance().create(AdminApiService.class);
    }

    // Luồng Git Journal
    public static GitJournalApiService getGitJournalApi() {
        return getInstance().create(GitJournalApiService.class);
    }

    // Luồng Community (của Chính)
    public static CommunityApiService getCommunityApi() {
        return getInstance().create(CommunityApiService.class);
    }

    public static QuestBuilderApiService getQuestBuilderApi() {
        return getInstance().create(QuestBuilderApiService.class);
    }
}
