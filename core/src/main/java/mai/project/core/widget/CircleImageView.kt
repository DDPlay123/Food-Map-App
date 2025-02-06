package mai.project.core.widget

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Build
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.RelativeCornerSize
import com.google.android.material.shape.ShapeAppearanceModel
import mai.project.core.R
import mai.project.core.extensions.DP

/**
 * 自定義圓形圖片
 *
 * 這個 widget 主要用於顯示使用者的頭像。
 *
 * 使用方法：
 * ```
 * // 設定圖片邊框顏色
 * app:circleImageStrokeColor="@color/gray_D9D9D9"
 * // 設定圖片邊框寬度
 * app:circleImageStrokeWidth="2dp"
 * // 設定中心文字
 * app:circleImageCenterText="0"
 */
class CircleImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ShapeableImageView(context, attrs, defStyleAttr) {

    /**
     * 中心文字
     */
    var centerText: String = ""
        private set

    /**
     * 設定中心文字風格
     */
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = context.resources.getDimension(R.dimen.text_smaller)
    }

    /**
     * 設定圖片邊框顏色
     *
     * @param color 邊框顏色
     */
    fun setCircleImageStrokeColor(
        @ColorInt color: Int
    ) {
        strokeColor = ColorStateList.valueOf(color)
    }

    /**
     * 設定圖片邊框寬度
     *
     * @param strokeWidth 邊框寬度，單位為 dp
     */
    fun setCircleImageStrokeWidth(
        @Dimension(unit = Dimension.DP) strokeWidth: Int
    ) {
        setPadding(strokeWidth / 2, strokeWidth / 2, strokeWidth / 2, strokeWidth / 2)
        this.strokeWidth = strokeWidth.toFloat()
    }

    /**
     * 設定中心文字
     *
     * @param text 中心文字
     */
    fun setCircleImageCenterText(text: String) {
        centerText = text
        adjustTextSizeToFit()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (centerText.isNotEmpty()) {
            val xPos = width / 2f
            val yPos = height / 2f - (textPaint.descent() + textPaint.ascent()) / 2
            canvas.drawText(centerText, xPos, yPos, textPaint)
        }
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        adjustTextSizeToFit()
    }

    /**
     * 計算文字大小以適應 View
     */
    private fun adjustTextSizeToFit() {
        if (centerText.isEmpty()) return

        val horizontalPadding = paddingLeft + paddingRight
        val verticalPadding = paddingTop + paddingBottom
        val targetWidth = width - horizontalPadding
        val targetHeight = height - verticalPadding

        var minTextSize = 1f
        var maxTextSize = 200f
        val threshold = 0.5f

        while ((maxTextSize - minTextSize) > threshold) {
            val midTextSize = (minTextSize + maxTextSize) / 2
            textPaint.textSize = midTextSize

            val textBounds = Rect()
            textPaint.getTextBounds(centerText, 0, centerText.length, textBounds)
            val textWidth = textPaint.measureText(centerText)
            val textHeight = textBounds.height()

            if (textWidth <= targetWidth && textHeight <= targetHeight) {
                minTextSize = midTextSize + threshold
            } else {
                maxTextSize = midTextSize
            }
        }

        textPaint.textSize = minTextSize * 0.5f
    }

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.CircleImageView, 0, 0).apply {
            try {
                val avatarStrokeWidth =
                    getDimension(R.styleable.CircleImageView_circleImageStrokeWidth, 0f.DP)
                val avatarPadding = avatarStrokeWidth.toInt()
                val avatarStrokeColor = getColor(
                    R.styleable.CircleImageView_circleImageStrokeColor,
                    context.getColor(android.R.color.transparent)
                )
                val centerText =
                    getString(R.styleable.CircleImageView_circleImageCenterText) ?: ""

                val circleModel = ShapeAppearanceModel.builder()
                    .setAllCornerSizes(RelativeCornerSize(0.5f))
                    .build()

                setPadding(avatarPadding, avatarPadding, avatarPadding, avatarPadding)
                strokeColor = ColorStateList.valueOf(avatarStrokeColor)
                strokeWidth = avatarStrokeWidth
                this@CircleImageView.centerText = centerText
                shapeAppearanceModel = circleModel

                if (centerText.isNotEmpty()) {
                    adjustTextSizeToFit()
                }

                shapeAppearanceModel = circleModel
            } finally {
                recycle()
            }
        }
    }
}