package com.andongni.vcblearn.di

import android.content.Context
import com.andongni.vcblearn.data.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SettingModule {

    @Singleton
    @Provides
    fun provideSettingsRepository(
        @ApplicationContext context: Context,
    ) = SettingsRepository(context)
}