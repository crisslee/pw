package com.coomix.app.all.data;

import android.util.Log;
import com.coomix.app.all.Constant;
import com.coomix.app.all.manager.DomainManager;
import com.google.gson.GsonBuilder;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by ly on 2017/5/17.
 */
public class DataEngine {

    private static final int DEFAULT_TIMEOUT = 10;

    private static CommunityApi mCommunityApi;
    private static CommunityPicupApi mCommunityPicupApi;
    private static AllOnlineMainApi mAllOnlineMainApi;
    private static FirstHostApi firstHostApi;
    private static AdverApi adverApi;
    private static GoogleAddressApi googleAddressApi;
    private static ConfigApi configApi;
    private static GoomeLogApi goomeLogApi;
    private static UploadInfoApi uploadInfoApi;
    private static CardApi cardApi;

    private static OkHttpClient.Builder getOkHttpClientBuilder() {
        OkHttpClient.Builder builder;
        DecryptInterceptor decrypt = new DecryptInterceptor();
        if (Constant.IS_DEBUG_MODE) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(String message) {
                    Log.d("OKHTTP:", message);
                }
            });
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(logging)
                .addInterceptor(decrypt);
        } else {
            builder = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(decrypt);
        }
        return builder;
    }

    private static <V> V getRetrofitApi(final String host, final Class<V> v) {
        OkHttpClient.Builder builder = getOkHttpClientBuilder();
        Retrofit retrofit = new Retrofit.Builder()
            .client(builder.build())
            .baseUrl(host)
            .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().serializeNulls().create()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build();
        return retrofit.create(v);
    }

    public static AllOnlineMainApi getAllMainApi() {
        if (mAllOnlineMainApi == null) {
            synchronized (DataEngine.class) {
                // init instance
                if (mAllOnlineMainApi == null) {
                    mAllOnlineMainApi = getRetrofitApi(DomainManager.getInstance().getCartrackingHost(), AllOnlineMainApi.class);
                }
            }
        }
        return mAllOnlineMainApi;
    }


    public static CommunityApi getCommunityApi() {
        if (mCommunityApi == null) {
            synchronized (DataEngine.class) {
                // init instance
                if (mCommunityApi == null) {
                    mCommunityApi = getRetrofitApi(DomainManager.getInstance().getCommunityHost(), CommunityApi.class);
                }
            }
        }
        return mCommunityApi;
    }

    public static CommunityPicupApi getCommunityPicupApi() {
        if (mCommunityPicupApi == null) {
            synchronized (DataEngine.class) {
                // init instance
                if (mCommunityPicupApi == null) {
                    mCommunityPicupApi = getRetrofitApi(DomainManager.getInstance().getCommunityPicup(), CommunityPicupApi.class);
                }
            }
        }
        return mCommunityPicupApi;
    }

    public static FirstHostApi getFirstHostApi() {
        if (firstHostApi == null) {
            synchronized (DataEngine.class) {
                // init instance
                if (firstHostApi == null) {
                    firstHostApi = getRetrofitApi(DomainManager.getInstance().getFirstHost(), FirstHostApi.class);
                }
            }
        }
        return firstHostApi;
    }

    public static AdverApi getAdverApi() {
        if (adverApi == null) {
            synchronized (DataEngine.class) {
                // init instance
                if (adverApi == null) {
                    adverApi = getRetrofitApi(DomainManager.getInstance().getAdverHost(), AdverApi.class);
                }
            }
        }
        return adverApi;
    }

    public static GoogleAddressApi getGoogleAddressApi() {
        if (googleAddressApi == null) {
            synchronized (DataEngine.class) {
                // init instance
                if (googleAddressApi == null) {
                    googleAddressApi = getRetrofitApi(DomainManager.getInstance().getGoogleAddressHost(), GoogleAddressApi.class);
                }
            }
        }
        return googleAddressApi;
    }

    public static ConfigApi getConfigApi() {
        if (configApi == null) {
            synchronized (DataEngine.class) {
                // init instance
                if (configApi == null) {
                    configApi = getRetrofitApi(DomainManager.getInstance().getConfigHost(), ConfigApi.class);
                }
            }
        }
        return configApi;
    }

    public static GoomeLogApi getGoomeLogApi() {
        if (goomeLogApi == null) {
            synchronized (DataEngine.class) {
                // init instance
                if (goomeLogApi == null) {
                    goomeLogApi = getRetrofitApi(DomainManager.getInstance().getGoomeLogHost(), GoomeLogApi.class);
                }
            }
        }
        return goomeLogApi;
    }

    public static UploadInfoApi getUploadInfoApi() {
        if (uploadInfoApi == null) {
            synchronized (DataEngine.class) {
                // init instance
                if (uploadInfoApi == null) {
                    uploadInfoApi = getRetrofitApi(DomainManager.getInstance().getUploadInfoHost(), UploadInfoApi.class);
                }
            }
        }
        return uploadInfoApi;
    }

    public static CardApi getCardApi() {
        if (cardApi == null) {
            synchronized (DataEngine.class) {
                // init instance
                if (cardApi == null) {
                    cardApi = getRetrofitApi(DomainManager.getInstance().getCardHost(), CardApi.class);
                }
            }
        }
        return cardApi;
    }
}
