package com.side.project.foodmap.util.gestureViewsSetting

import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.StringRes
import com.alexvasilkov.android.commons.state.InstanceState
import com.alexvasilkov.android.commons.state.InstanceStateManager
import com.alexvasilkov.gestures.Settings
import com.alexvasilkov.gestures.Settings.Fit
import com.alexvasilkov.gestures.internal.GestureDebug
import com.alexvasilkov.gestures.views.interfaces.GestureView
import com.side.project.foodmap.R

class SettingsMenu : SettingsController {
    companion object {
        private const val OVERSCROLL = 32f
        private const val SLOW_ANIMATIONS = 1500L
    }

    private enum class GravityType(val gravity: Int) {
        CENTER(Gravity.CENTER), TOP(Gravity.TOP), BOTTOM(Gravity.BOTTOM), START(Gravity.START), END(Gravity.END),
        TOP_START(Gravity.TOP or Gravity.START), BOTTOM_END(Gravity.BOTTOM or Gravity.END);

        companion object {
            fun find(gravity: Int): GravityType? {
                for (type in values())
                    if (type.gravity == gravity)
                        return type
                return null
            }
        }
    }

    @InstanceState
    private var isPanEnabled = true
    @InstanceState
    private var isZoomEnabled = true
    @InstanceState
    private var isRotationEnabled = false
    @InstanceState
    private var isRestrictRotation = false
    @InstanceState
    private var isOverscrollEnabled = false
    @InstanceState
    private var isOverZoomEnabled = true
    @InstanceState
    private var isFillViewport = true
    @InstanceState
    private var fitMethod = Fit.INSIDE
    @InstanceState
    private var boundsType = Settings.Bounds.NORMAL
    @InstanceState
    private var gravity = Gravity.CENTER
    @InstanceState
    private var isSlow = false

    fun setValuesFrom(settings: Settings) {
        isPanEnabled = settings.isPanEnabled
        isZoomEnabled = settings.isZoomEnabled
        isRotationEnabled = settings.isRotationEnabled
        isRestrictRotation = settings.isRestrictRotation
        isFillViewport = settings.isFillViewport
        fitMethod = settings.fitMethod
        boundsType = settings.boundsType
        gravity = settings.gravity
    }

    fun onSaveInstanceState(outState: Bundle?) {
        InstanceStateManager.saveInstanceState(this, outState)
    }

    fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        InstanceStateManager.restoreInstanceState(this, savedInstanceState)
    }

    fun onCreateOptionsMenu(menu: Menu) {
        addBoolMenu(menu, isPanEnabled, R.string.menu_enable_pan)
        addBoolMenu(menu, isZoomEnabled, R.string.menu_enable_zoom)
        addBoolMenu(menu, isRotationEnabled, R.string.menu_enable_rotation)
        addBoolMenu(menu, isRestrictRotation, R.string.menu_restrict_rotation)
        addBoolMenu(menu, isOverscrollEnabled, R.string.menu_enable_overscroll)
        addBoolMenu(menu, isOverZoomEnabled, R.string.menu_enable_overZoom)
        addBoolMenu(menu, isFillViewport, R.string.menu_fill_viewport)
        addSubMenu(menu, Fit.values(), fitMethod, R.string.menu_fit_method)
        addSubMenu(menu, Settings.Bounds.values(), boundsType, R.string.menu_bounds_type)
        addSubMenu(menu, GravityType.values(), GravityType.find(gravity)!!, R.string.menu_gravity)
        addBoolMenu(menu, isSlow, R.string.menu_enable_slow)
        addBoolMenu(menu, GestureDebug.isDrawDebugOverlay(), R.string.menu_enable_overlay)
    }

    private fun addBoolMenu(menu: Menu, checked: Boolean, @StringRes titleId: Int) {
        val item = menu.add(Menu.NONE, titleId, 0, titleId)
        item.isCheckable = true
        item.isChecked = checked
    }

    private fun <T> addSubMenu(menu: Menu, items: Array<T>, selected: T, @StringRes titleId: Int) {
        val sub = menu.addSubMenu(titleId)
        sub.setGroupCheckable(Menu.NONE, true, true)

        for (i in items.indices) {
            val item = sub.add(Menu.NONE, titleId, i, items[i].toString())
            item.isCheckable = true
            item.isChecked = items[i] === selected
        }
    }

    fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.string.menu_enable_pan -> isPanEnabled = !isPanEnabled
            R.string.menu_enable_zoom -> isZoomEnabled = !isZoomEnabled
            R.string.menu_enable_rotation -> isRotationEnabled = !isRotationEnabled
            R.string.menu_restrict_rotation -> isRestrictRotation = !isRestrictRotation
            R.string.menu_enable_overscroll -> isOverscrollEnabled = !isOverscrollEnabled
            R.string.menu_enable_overZoom -> isOverZoomEnabled = !isOverZoomEnabled
            R.string.menu_fill_viewport -> isFillViewport = !isFillViewport
            R.string.menu_fit_method -> fitMethod = Fit.values()[item.order]
            R.string.menu_bounds_type -> boundsType = Settings.Bounds.values()[item.order]
            R.string.menu_gravity -> gravity = GravityType.values()[item.order].gravity
            R.string.menu_enable_slow -> isSlow = !isSlow
            R.string.menu_enable_overlay -> GestureDebug.setDrawDebugOverlay(!GestureDebug.isDrawDebugOverlay())
            else -> return false
        }
        return true
    }

    override fun apply(view: GestureView) {
        val context = (view as View).context
        val overscroll = if (isOverscrollEnabled) OVERSCROLL else 0f
        val overZoom = if (isOverZoomEnabled) Settings.OVERZOOM_FACTOR else 1f

        view.controller.settings
            .setPanEnabled(isPanEnabled)
            .setZoomEnabled(isZoomEnabled)
            .setDoubleTapEnabled(isZoomEnabled)
            .setRotationEnabled(isRotationEnabled)
            .setRestrictRotation(isRestrictRotation)
            .setOverscrollDistance(context, overscroll, overscroll)
            .setOverzoomFactor(overZoom)
            .setFillViewport(isFillViewport)
            .setFitMethod(fitMethod)
            .setBoundsType(boundsType)
            .setGravity(gravity).animationsDuration = if (isSlow) SLOW_ANIMATIONS else Settings.ANIMATIONS_DURATION
    }
}