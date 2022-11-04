package com.side.project.foodmap.ui.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.side.project.foodmap.R
import com.side.project.foodmap.databinding.DialogPromptBinding
import com.side.project.foodmap.ui.other.DialogManager
import com.side.project.foodmap.ui.other.NetworkConnection
import org.koin.android.ext.android.inject

abstract class BaseActivity : AppCompatActivity() {
    lateinit var mActivity: BaseActivity
    lateinit var dialog: DialogManager
    private val networkConnection: NetworkConnection by inject()

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level <= TRIM_MEMORY_BACKGROUND)
            System.gc()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivity = this
        dialog = DialogManager.instance(mActivity)

        checkNetWork()
    }

    private fun checkNetWork() {
        networkConnection.observe(this) { isConnect ->
            if (!isConnect) {
                val binding = DialogPromptBinding.inflate(layoutInflater)
                dialog.cancelAllDialog()
                dialog.showCenterDialog(false, binding, false).let {
                    binding.run {
                        showIcon = true
                        hideCancel = true
                        imgPromptIcon.setImageResource(R.drawable.ic_wifi_off)
                        titleText = getString(R.string.hint_internet_error_title)
                        subTitleText = getString(R.string.hint_internet_error_subtitle)
                        tvConfirm.setOnClickListener { dialog.cancelCenterDialog() }
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) finish()
        }
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