package com.example.emotiondebugging.data.api;

import com.example.emotiondebugging.utils.ApiConstants;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // ⚠️ CHỈNH SỬA: BASE_URL được quản lý tập trung tại ApiConstants.java
    private static final String BASE_URL = ApiConstants.BASE_URL;

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

    // Luồng AI Chat (Dr.Bug)
    public static AiChatApiService getAiChatApi() {
        return getInstance().create(AiChatApiService.class);
    }
}
