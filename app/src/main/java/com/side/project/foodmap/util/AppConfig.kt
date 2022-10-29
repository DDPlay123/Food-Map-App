package com.side.project.foodmap.util

import android.annotation.SuppressLint
import android.app.Application
import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.side.project.foodmap.R
import com.side.project.foodmap.di.firebaseModule
import com.side.project.foodmap.di.managerModule
import com.side.project.foodmap.di.repoModule
import com.side.project.foodmap.di.viewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

@Suppress("unused")
class AppConfig: Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@AppConfig)
            modules(listOf(
                viewModel,
                managerModule,
                firebaseModule,
                repoModule
            ))
        }
    }

    @SuppressLint("ResourceType")
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(applicationContext)
            .crossfade(true)
            .placeholder(R.raw.placeholder)
            .error(R.drawable.img_placeholder)
            .components {
                if (Build.VERSION.SDK_INT >= 28)
                    add(ImageDecoderDecoder.Factory())
                else
                    add(GifDecoder.Factory())
            }
            .build()
    }
}