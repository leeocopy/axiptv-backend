package com.matrix.iptv.di

import android.content.Context
import androidx.room.Room
import com.matrix.iptv.BuildConfig
import com.matrix.iptv.data.local.db.AppDatabase
import com.matrix.iptv.data.local.db.ProfileDao
import com.matrix.iptv.data.local.prefs.DataStoreManager
import com.matrix.iptv.data.local.prefs.SecurePrefs
import com.matrix.iptv.data.remote.DeviceStatusApi
import com.matrix.iptv.data.remote.RemoteDeviceStatusService
import com.matrix.iptv.data.repository.ProfileRepositoryImpl
import com.matrix.iptv.domain.repository.DeviceStatusRepository
import com.matrix.iptv.domain.repository.ProfileRepository
import com.matrix.iptv.data.validation.XtreamValidatorImpl
import com.matrix.iptv.domain.validation.XtreamValidator
import com.matrix.iptv.data.remote.FootballApi
import com.matrix.iptv.data.repository.FootballRepositoryImpl
import com.matrix.iptv.domain.repository.FootballRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds @Singleton
    abstract fun bindDeviceStatusRepository(impl: RemoteDeviceStatusService): DeviceStatusRepository

    @Binds @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds @Singleton
    abstract fun bindXtreamValidator(impl: XtreamValidatorImpl): XtreamValidator

    @Binds @Singleton
    abstract fun bindXtreamRepository(impl: com.matrix.iptv.data.repository.XtreamRepositoryImpl): com.matrix.iptv.domain.repository.XtreamRepository

    @Binds @Singleton
    abstract fun bindFootballRepository(impl: FootballRepositoryImpl): FootballRepository

    @Binds @Singleton
    abstract fun bindWatchHistoryRepository(impl: com.matrix.iptv.data.repository.WatchHistoryRepositoryImpl): com.matrix.iptv.domain.repository.WatchHistoryRepository

    @Binds @Singleton
    abstract fun bindSearchHistoryRepository(impl: com.matrix.iptv.data.repository.SearchHistoryRepositoryImpl): com.matrix.iptv.domain.repository.SearchHistoryRepository

    @Binds @Singleton
    abstract fun bindTmdbRepository(impl: com.matrix.iptv.data.repository.TmdbRepositoryImpl): com.matrix.iptv.domain.repository.TmdbRepository

    companion object {
        @Provides @Singleton
        fun provideDeviceStatusApi(): DeviceStatusApi {
            val logging = HttpLoggingInterceptor { message ->
                android.util.Log.d("AXIPTV_HTTP", message)
            }.apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            val client = OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()

            val json = Json { ignoreUnknownKeys = true }

            return Retrofit.Builder()
                .baseUrl(BuildConfig.AXIPTV_BACKEND_URL)
                .client(client)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(DeviceStatusApi::class.java)
        }

        @Provides @Singleton
        fun provideFootballApi(): FootballApi {
            val logging = HttpLoggingInterceptor { message ->
                android.util.Log.d("FOOTBALL_API", message)
            }.apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            val client = OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()

            val json = Json { ignoreUnknownKeys = true }

            return Retrofit.Builder()
                .baseUrl("https://sportapi7.p.rapidapi.com/")
                .client(client)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(FootballApi::class.java)
        }

        @Provides @Singleton
        fun provideAppConfigApi(): com.matrix.iptv.data.remote.AppConfigApi {
            val logging = HttpLoggingInterceptor { message ->
                android.util.Log.d("APP_CONFIG_API", message)
            }.apply { level = HttpLoggingInterceptor.Level.BODY }
            val client = OkHttpClient.Builder()
                .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()
            val json = Json { ignoreUnknownKeys = true }
            return Retrofit.Builder()
                .baseUrl(BuildConfig.AXIPTV_BACKEND_URL)
                .client(client)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(com.matrix.iptv.data.remote.AppConfigApi::class.java)
        }

        @Provides @Singleton
        fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build()

        @Provides @Singleton
        fun provideProfileDao(db: AppDatabase): ProfileDao = db.profileDao()

        @Provides @Singleton
        fun provideFavoriteDao(db: AppDatabase): com.matrix.iptv.data.local.db.FavoriteDao = db.favoriteDao()

        @Provides @Singleton
        fun provideStreamCacheDao(db: AppDatabase): com.matrix.iptv.data.local.db.StreamCacheDao = db.streamCacheDao()

        @Provides @Singleton
        fun provideWatchHistoryDao(db: AppDatabase): com.matrix.iptv.data.local.db.WatchHistoryDao = db.watchHistoryDao()

        @Provides @Singleton
        fun provideSearchHistoryDao(db: AppDatabase): com.matrix.iptv.data.local.db.SearchHistoryDao = db.searchHistoryDao()

        @Provides @Singleton
        fun provideDataStoreManager(@ApplicationContext context: Context): DataStoreManager =
            DataStoreManager(context)

        @Provides @Singleton
        fun provideSecurePrefs(@ApplicationContext context: Context): SecurePrefs =
            SecurePrefs(context)
            
        @Provides @Singleton
        fun provideTmdbApi(): com.matrix.iptv.data.remote.api.TmdbApi {
            val logging = HttpLoggingInterceptor { message ->
                android.util.Log.d("TMDB_API", message)
            }.apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            val client = OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()

            val json = Json { ignoreUnknownKeys = true }

            return Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/3/")
                .client(client)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(com.matrix.iptv.data.remote.api.TmdbApi::class.java)
        }
    }
}
