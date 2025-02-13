package mai.project.core.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import coil.dispose
import mai.project.core.annotations.ImageType
import mai.project.core.utils.ImageLoaderUtil
import kotlin.math.sqrt

/**
 * 下拉自動隱藏的 ImageView
 */
class DownToCloseImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val imageView: AppCompatImageView
    private var velocityTracker: VelocityTracker? = null
    private var lastY: Float = 0f
    private var lastX: Float = 0f
    private val closeThreshold = 300f // 下拉超過此距離將隱藏
    private var backgroundAlpha = 1.0f
    private val touchSlop: Int = ViewConfiguration.get(context).scaledTouchSlop
    private val transparencyMultiplier: Float = 0.8f // 可調整透明度變化速度

    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDownDragActive = false

    init {
        setBackgroundColor(0xFF000000.toInt()) // 預設背景為黑色
        imageView = AppCompatImageView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            visibility = GONE // 預設為隱藏，當沒有圖片時不顯示
        }
        addView(imageView)
    }

    var onCloseListener: (() -> Unit)? = null

    fun setImage(any: Any) {
        ImageLoaderUtil.loadImage(
            imageView = imageView,
            resource = any,
            imageType = ImageType.DEFAULT
        )
        resetPosition() // 重置位置
        visibility = VISIBLE // 當有圖片時顯示容器
        imageView.visibility = VISIBLE
        backgroundAlpha = 1.0f
        setBackgroundColor(0xFF000000.toInt()) // 設定背景為完全不透明
    }

    fun clearImage() {
        imageView.dispose()
        imageView.visibility = GONE // 當沒有圖片時隱藏圖片
        visibility = GONE // 當沒有圖片時隱藏容器
    }

    private fun resetPosition() {
        imageView.translationY = 0f
        imageView.translationX = 0f
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                lastY = event.rawY
                lastX = event.rawX
                isDownDragActive = false
                velocityTracker = VelocityTracker.obtain()
                velocityTracker?.addMovement(event)
            }

            MotionEvent.ACTION_MOVE -> {
                val currentX = event.rawX
                val currentY = event.rawY
                val totalDy = currentY - initialTouchY
                val dx = currentX - lastX
                val dy = currentY - lastY

                // 檢查是否超過閾值
                if (!isDownDragActive) {
                    if (totalDy > touchSlop) {
                        isDownDragActive = true
                        // 啟動拖曳模式後，禁止父容器攔截後續事件
                        parent.requestDisallowInterceptTouchEvent(true)
                    } else {
                        // 尚未觸發下拉，返回 false 讓父容器（例如 ViewPager2）處理事件
                        return false
                    }
                }
                // 已進入拖曳模式後，任意方向都由 Item 處理
                imageView.translationX += dx
                imageView.translationY += dy
                lastX = currentX
                lastY = currentY
                velocityTracker?.addMovement(event)
                updateBackgroundOpacity()
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isDownDragActive) {
                    velocityTracker?.addMovement(event)
                    velocityTracker?.computeCurrentVelocity(1000)
                    val velocityY = velocityTracker?.yVelocity ?: 0f
                    velocityTracker?.recycle()
                    velocityTracker = null

                    if (imageView.translationY > closeThreshold || velocityY > 1000) {
                        // 下拉條件滿足，執行關閉動畫
                        imageView.animate().translationY(height.toFloat())
                            .setInterpolator(AccelerateDecelerateInterpolator())
                            .setDuration(300)
                            .withEndAction {
                                clearImage()
                                onCloseListener?.invoke()
                            }
                            .start()
                    } else {
                        // 未滿足關閉條件，恢復原始位置與背景
                        imageView.animate().translationX(0f)
                            .translationY(0f)
                            .setInterpolator(AccelerateDecelerateInterpolator())
                            .setDuration(300)
                            .withEndAction {
                                backgroundAlpha = 1.0f
                                updateBackgroundOpacity()
                            }
                            .start()
                    }
                }
                // 每次觸控結束後重置拖曳狀態
                isDownDragActive = false
            }
        }
        return true
    }

    private fun updateBackgroundOpacity() {
        val distance = sqrt(
            imageView.translationY * imageView.translationY + imageView.translationX * imageView.translationX
        ).coerceIn(0f, height.toFloat())
        backgroundAlpha = (1.0f - (distance / height).coerceIn(0f, 1f)) * transparencyMultiplier + 0.2f // 最低透明度為0.2
        val alphaInt = (backgroundAlpha * 255).toInt()
        setBackgroundColor((alphaInt shl 24) or 0x000000)
    }
}