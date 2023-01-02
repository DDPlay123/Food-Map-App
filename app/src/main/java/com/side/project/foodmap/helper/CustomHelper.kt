package com.side.project.foodmap.helper

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.view.View
import android.view.animation.Animation
import com.side.project.foodmap.data.remote.Location
import com.side.project.foodmap.ui.other.AnimState

fun String.getLocation(): Location =
    when (this) {
        "基隆市" -> Location(25.127658594632145, 121.73880131220862)
        "台北市" -> Location(25.032870233377594, 121.56551195464739)
        "新北市" -> Location(25.016524483439696, 121.46436724737164)
        "桃園縣" -> Location(24.99354770140831, 121.3012168941898)
        "新竹市" -> Location(24.81345635343065, 120.96672247472294)
        "新竹縣" -> Location(24.839204290743627, 121.01695914190239)
        "苗栗縣" -> Location(24.5599560653154, 120.82164417070744)
        "台中市" -> Location(24.148261609165665, 120.67538379113937)
        "彰化縣" -> Location(24.05179682571012, 120.51617302325565)
        "南投縣" -> Location(23.961219714201068, 120.97224443533372)
        "雲林縣" -> Location(23.709326466471225, 120.43139848309468)
        "嘉義市" -> Location(23.480528070232477, 120.44981089689183)
        "嘉義縣" -> Location(23.453030245426042, 120.25583672988114)
        "台南市" -> Location(22.999840313529003, 120.22915752907181)
        "高雄市" -> Location(22.634638513658054, 120.33558849608472)
        "屏東縣" -> Location(22.552204686630112, 120.54961279760572)
        "台東縣" -> Location(22.797797671300437, 121.07136781572237)
        "花蓮縣" -> Location(23.98570356313552, 121.60394991898482)
        "宜蘭縣" -> Location(24.759374889498634, 121.75406693440107)
        "澎湖縣" -> Location(23.57114662370971, 119.57738150256455)
        "金門縣" -> Location(24.454081200925675, 118.38356473451705)
        "連江縣" -> Location(26.160449323852774, 119.95066870788101)
        else -> Location(0.00, 0.00)
    }

fun View.setAnimClick(
    animation: Animation,
    state: AnimState = AnimState.Start,
    work: (() -> Unit)
) {
    this.let {
        it.startAnimation(animation)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {
                if (state == AnimState.Start) work()
            }

            override fun onAnimationEnd(p0: Animation?) {
                if (state == AnimState.End) work()
            }

            override fun onAnimationRepeat(p0: Animation?) {
                if (state == AnimState.Repeat) work()
            }
        })
    }
}

inline fun SpannableStringBuilder.withSpan(
    vararg spans: Any,
    action: SpannableStringBuilder.() -> Unit
): SpannableStringBuilder {
    val from = length
    action()

    for (span in spans) {
        setSpan(span, from, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
    return this
}