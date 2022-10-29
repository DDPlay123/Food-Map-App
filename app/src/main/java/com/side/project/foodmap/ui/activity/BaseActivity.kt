package com.side.project.foodmap.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {
    lateinit var mActivity: BaseActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivity = this
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level <= TRIM_MEMORY_BACKGROUND)
            System.gc()
    }

    private val receiveResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val intent: Intent? = result.data
            }
        }

    fun startForResult(next: Class<*>, bundle: Bundle?) {
        Intent(
            applicationContext,
            next
        ).also { intent ->
            if (bundle == null)
                intent.putExtras(Bundle())
            else
                intent.putExtras(bundle)
            // jump activity
            receiveResult.launch(intent)
        }
    }

    fun start(next: Class<*>, bundle: Bundle?, finished: Boolean) {
        Intent(
            applicationContext,
            next
        ).also { intent ->
            if (bundle == null)
                intent.putExtras(Bundle())
            else
                intent.putExtras(bundle)
            // jump activity
            startActivity(intent)
            // close activity
            if (finished)
                this.finish()
        }
    }

    fun start(next: Class<*>) {
        this.start(next, null, false)
    }

    fun start(next: Class<*>, bundle: Bundle?) {
        this.start(next, bundle, false)
    }

    fun start(next: Class<*>, finished: Boolean) {
        this.start(next, null, finished)
    }
}