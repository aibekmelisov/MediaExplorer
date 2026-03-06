package com.example.mediaexplorer.data.remote;

import com.example.mediaexplorer.BuildConfig;
import com.example.mediaexplorer.utils.Constants;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // Singleton экземпляр Retrofit
    private static Retrofit retrofit;

    // Создание и получение экземпляра Retrofit
    public static Retrofit getRetrofit() {
        if (retrofit == null) {

            // Логирование HTTP-запросов (для отладки)
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // HTTP клиент OkHttp
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            // Настройка Retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.TMDB_BASE_URL) // базовый URL TMDB API
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create()) // JSON → Java
                    .build();
        }
        return retrofit;
    }

    // Получение интерфейса API (сетевых методов)
    public static TmdbApiService getApiService() {
        return getRetrofit().create(TmdbApiService.class);
    }

    // Получение API-ключа TMDB
    public static String apiKey() {
        return BuildConfig.TMDB_API_KEY;
    }
}