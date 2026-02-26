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
import com.matrix.iptv.domain.validation.XtreamValidator
import com.matrix.iptv.data.validation.XtreamValidatorImpl
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
        fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build()

        @Provides @Singleton
        fun provideProfileDao(db: AppDatabase): ProfileDao = db.profileDao()

        @Provides @Singleton
        fun provideDataStoreManager(@ApplicationContext context: Context): DataStoreManager =
            DataStoreManager(context)

        @Provides @Singleton
        fun provideSecurePrefs(@ApplicationContext context: Context): SecurePrefs =
            SecurePrefs(context)
    }
}
