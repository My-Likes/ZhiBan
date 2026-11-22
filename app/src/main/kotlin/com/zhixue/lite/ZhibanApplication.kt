package com.zhixue.lite

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.zhixue.lite.core.data.repository.TokenRefreshManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ZhibanApplication : Application(), ImageLoaderFactory {

    @Inject
    lateinit var tokenRefreshManager: TokenRefreshManager

    override fun onCreate() {
        super.onCreate()
        tokenRefreshManager.register()
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader(this).newBuilder()
            .crossfade(true)
            .respectCacheHeaders(false)
            .build()
    }
}