package mai.project.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.widget.ImageView
import androidx.core.graphics.drawable.IconCompat
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.decode.VideoFrameDecoder
import coil.disk.DiskCache
import coil.load
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.size.ViewSizeResolver
import coil.transform.CircleCropTransformation
import coil.transform.Transformation
import coil.transition.CrossfadeTransition
import coil.util.DebugLogger
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.qualifiers.ApplicationContext
import mai.project.core.BuildConfig
import mai.project.core.R
import mai.project.core.annotations.ImageType
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 圖片讀取工具
 *
 * @property preloadImage 預加載圖片。
 * @property loadImage 讀取圖片。
 * @property asyncLoadIcon 非同步讀取圖片並轉換為 Icon。
 * @property getBitmapImage 取得 Bitmap 圖片。
 */
@Singleton
class ImageLoaderUtil @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * 自定義圖片讀取器
     */
    val imageLoader: ImageLoader

    /**
     * 預加載圖片。當 imageView 為 null 時，只預加載圖片；當非 null 時，設定大小與 imageView 一致。
     *
     * @param imageView 圖片元件
     * @param url 圖片網址
     */
    fun preloadImage(
        imageView: ImageView? = null,
        url: String,
    ) {
        Timber.d("Preloading image URL: $url")
        val request = ImageRequest.Builder(context).data(url)
            .apply {
                // 設定圖片大小與 imageView 一致
                imageView?.let { size(ViewSizeResolver(it)) }
                // 加載完成後將保存到記憶體空間，避免下次再次發送請求
                memoryCachePolicy(CachePolicy.ENABLED)
            }.build()
        imageLoader.enqueue(request)
    }

    /**
     * 讀取圖片
     *
     * @param imageView 圖片元件
     * @param resource 圖片資源
     * @param imageType 圖片類型
     * @param transformation 圖片轉換
     */
    fun loadImage(
        imageView: ImageView,
        resource: Any,
        @ImageType
        imageType: Int = ImageType.DEFAULT,
        transformation: Transformation? = null
    ) {
        imageView.load(resource, imageLoader) {
            size(1000)
            error(getDrawableResource(imageType, true))
            placeholder(getDrawableResource(imageType, false))
            transitionFactory(CrossfadeTransition.Factory())
            transformation?.let { transformations(it) }
        }
    }

    /**
     * 非同步讀取圖片並轉換為 Icon
     *
     * - 主要用於取得 Notification 的 Icon
     *
     * @param imageUrl 圖片網址
     * @param setIcon 設定 Icon
     */
    fun asyncLoadIcon(
        imageUrl: String?,
        setIcon: (IconCompat?) -> Unit
    ) {
        if (imageUrl.isNullOrEmpty())
            setIcon(null)
        else {
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .transformations(CircleCropTransformation())
                .target { drawable ->
                    setIcon(IconCompat.createWithBitmap((drawable as BitmapDrawable).bitmap))
                }
                .listener(object : ImageRequest.Listener {
                    override fun onError(request: ImageRequest, result: ErrorResult) {
                        setIcon(null)
                    }
                })
                .build()
            imageLoader.enqueue(request)
        }
    }

    /**
     * 取得 Bitmap 圖片
     *
     * @param resource 圖片資源
     * @param size 圖片大小
     */
    suspend fun getBitmapImage(
        resource: Any,
        size: Int = 1000
    ): Bitmap? {
        return try {
            val request = ImageRequest.Builder(context).data(resource).size(size).build()
            (imageLoader.execute(request).drawable as BitmapDrawable).bitmap
        } catch (e: Exception) {
            Timber.e(message = "getBitmapImage", t = e)
            FirebaseCrashlytics.getInstance().recordException(e)
            null
        }
    }

    /**
     * 初始化圖片讀取器
     */
    private fun initializeImageLoader(): ImageLoader {
        val builder = ImageLoader.Builder(context)
            // 設定 memory cache
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25) // 設定大小為可用記憶體的 1/4
                    .strongReferencesEnabled(true) // 開啟強引用，避免圖片閃爍
                    .build()
            }
            // 開啟 memory cache 策略
            .memoryCachePolicy(CachePolicy.ENABLED)
            // 設定 disk cache
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache")) // 設定緩存路徑
                    .maxSizePercent(0.1) // 設定大小為可用記憶體的 1/10
                    .build()
            }
            // 開啟 disk cache 策略
            .diskCachePolicy(CachePolicy.ENABLED)
            .crossfade(true)
            .apply {
                components {
                    if (Build.VERSION.SDK_INT >= 28) {
                        add(ImageDecoderDecoder.Factory())
                    } else {
                        add(
                            GifDecoder.Factory()
                        )
                    }
                    add(VideoFrameDecoder.Factory())
                    add(SvgDecoder.Factory())
                }
            }
        if (BuildConfig.DEBUG) builder.logger(DebugLogger())
        return builder.build()
    }

    /**
     * 取得圖片資源
     *
     * @param imageType 圖片類型
     * @param isError 是否為錯誤圖片
     */
    private fun getDrawableResource(
        @ImageType
        imageType: Int,
        isError: Boolean
    ): Int {
        return when (imageType) {
            ImageType.NONE -> 0

            ImageType.PERSON ->
                R.drawable.img_user

            else ->
                if (isError) R.drawable.vector_image_not_supported else R.drawable.vector_image_search
        }
    }

    init {
        imageLoader = initializeImageLoader()
    }
}