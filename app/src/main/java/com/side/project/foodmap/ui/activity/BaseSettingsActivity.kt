package com.side.project.foodmap.ui.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.alexvasilkov.gestures.Settings
import com.side.project.foodmap.util.gestureViewsSetting.SettingsController
import com.side.project.foodmap.util.gestureViewsSetting.SettingsMenu

/**
 * 手勢縮放用基本設定
 */
abstract class BaseSettingsActivity : BaseActivity() {
    private val settingsMenu: SettingsMenu = SettingsMenu()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsMenu.onRestoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        settingsMenu.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        settingsMenu.onCreateOptionsMenu(menu!!)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (settingsMenu.onOptionsItemSelected(item)) {
            onSettingsChanged()
            true
        } else
            super.onOptionsItemSelected(item)
    }

    protected fun getSettingsController(): SettingsController = settingsMenu

    protected abstract fun onSettingsChanged()

    protected open fun setDefaultSettings(settings: Settings) {
        settingsMenu.setValuesFrom(settings)
        invalidateOptionsMenu()
    }
}