package com.side.project.foodmap.ui.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.side.project.foodmap.R
import com.side.project.foodmap.databinding.DialogPromptBinding
import com.side.project.foodmap.databinding.DialogPromptSearchBinding
import com.side.project.foodmap.databinding.FragmentProfilesBinding
import com.side.project.foodmap.helper.*
import com.side.project.foodmap.ui.activity.launch.LoginActivity
import com.side.project.foodmap.ui.fragment.other.BaseFragment
import com.side.project.foodmap.ui.other.AnimState
import com.side.project.foodmap.ui.viewModel.MainViewModel
import com.side.project.foodmap.util.RegisterLoginValidation
import com.side.project.foodmap.util.tools.Method
import com.side.project.foodmap.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.io.FileNotFoundException

class ProfilesFragment : BaseFragment<FragmentProfilesBinding>(R.layout.fragment_profiles) {
    private val viewModel: MainViewModel by activityViewModel()

    override fun FragmentProfilesBinding.initialize() {
        binding.paddingTop = mActivity.getStatusBarHeight()
        binding.vm = viewModel
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (result.data != null) {
                val imageUri = result.data?.data as Uri
                try {
                    val inputStream = context?.contentResolver?.openInputStream(imageUri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    val encodedImage = Method.encodeImage(bitmap) as String
                    viewModel.setUserImage(encodedImage)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        doInitialize()
        setListener()
    }

    private fun doInitialize() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                // 驗證密碼輸入
                launch {
                    viewModel.validation.collect { validation ->
                        if (validation.password is RegisterLoginValidation.Failed)
                            withContext(Dispatchers.Main) {
                                requireActivity().displayShortToast(getString(validation.password.messageID))
                            }
                    }
                }
                // 修改密碼
                launch {
                    viewModel.setPasswordState.collect {
                        when (it) {
                            is Resource.Loading -> {
                                Method.logE("Set Password", "Loading")
                                mActivity.hideKeyboard()
                                dialog.showLoadingDialog(mActivity, false)
                            }
                            is Resource.Success -> {
                                Method.logE("Set Password", "Success")
                                dialog.cancelAllDialog()
                                requireActivity().displayShortToast(getString(R.string.hint_set_password_success))
                            }
                            is Resource.Error -> {
                                Method.logE("Set Password", "Error:${it.message.toString()}")
                                dialog.cancelLoadingDialog()
                                requireActivity().displayShortToast(it.message.toString())
                            }
                            else -> Unit
                        }
                    }
                }
                // 登出
                launch {
                    viewModel.logoutState.collect {
                        when (it) {
                            is Resource.Loading -> {
                                Method.logE("Logout", "Loading")
                                dialog.showLoadingDialog(mActivity, false)
                            }
                            is Resource.Success -> {
                                Method.logE("Logout", "Success")
                                dialog.cancelLoadingDialog()
                                requireActivity().displayShortToast(getString(R.string.hint_logout_success))
                                mActivity.start(LoginActivity::class.java, true)
                            }
                            is Resource.Error -> {
                                Method.logE("Logout", "Error:${it.message.toString()}")
                                dialog.cancelLoadingDialog()
                                requireActivity().displayShortToast(getString(R.string.hint_error))
                            }
                            else -> Unit
                        }
                    }
                }
                // 刪除帳號
                launch {
                    viewModel.deleteAccountState.collect {
                        when (it) {
                            is Resource.Loading -> {
                                Method.logE("Delete Account", "Loading")
                                dialog.showLoadingDialog(mActivity, false)
                            }
                            is Resource.Success -> {
                                Method.logE("Delete Account", "Success")
                                dialog.cancelLoadingDialog()
                                requireActivity().displayShortToast(getString(R.string.hint_delete_account_success))
                                mActivity.start(LoginActivity::class.java, true)
                            }
                            is Resource.Error -> {
                                Method.logE("Delete Account", "Error:${it.message.toString()}")
                                dialog.cancelLoadingDialog()
                                requireActivity().displayShortToast(getString(R.string.hint_error))
                            }
                            else -> Unit
                        }
                    }
                }
                // 設定大頭照
                launch {
                    viewModel.setUserImageState.collect {
                        when (it) {
                            is Resource.Loading -> {
                                Method.logE("Set User Image", "Loading")
                                dialog.showLoadingDialog(mActivity, false)
                            }
                            is Resource.Success -> {
                                Method.logE("Set User Image", "Success")
                                dialog.cancelLoadingDialog()
                            }
                            is Resource.Error -> {
                                Method.logE("Set User Image", "Error:${it.message.toString()}")
                                dialog.cancelLoadingDialog()
                                requireActivity().displayShortToast(getString(R.string.hint_error))
                            }
                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    private fun setListener() {
        val anim = animManager.smallToLarge
        binding.run {
            tvSetPassword.setOnClickListener {
                displaySetPasswordDialog()
            }

            tvLogout.setOnClickListener {
                displayLogoutDialog()
            }

            btnDeleteAccount.setOnClickListener {
                it.setAnimClick(anim, AnimState.Start) {
                    displayDeleteAccountDialog()
                }
            }

            imgUserImage.setOnClickListener {
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                    this.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                    pickImage.launch(this)
                }
            }
        }
    }

    private fun displaySetPasswordDialog() {
        val dialogBinding = DialogPromptSearchBinding.inflate(layoutInflater)
        dialog.showCenterDialog(mActivity, true, dialogBinding, true).let {
            dialogBinding.run {
                imgSearchIcon.setImageResource(R.drawable.ic_key)
                edSearch.transformationMethod = PasswordTransformationMethod.getInstance()
                titleText = getString(R.string.hint_prompt_set_password)
                tvCancel.setOnClickListener { dialog.cancelCenterDialog() }
                tvConfirm.setOnClickListener {
                    viewModel.setPassword(edSearch.text.toString().trim())
                    dialog.cancelCenterDialog()
                }
            }
        }
    }

    private fun displayLogoutDialog() {
        val dialogBinding = DialogPromptBinding.inflate(layoutInflater)
        dialog.showCenterDialog(mActivity, true, dialogBinding, false).let {
            dialogBinding.run {
                dialogBinding.run {
                    showIcon = true
                    imgPromptIcon.setImageResource(R.drawable.ic_logout)
                    titleText = getString(R.string.hint_prompt_logout_title)
                    tvCancel.setOnClickListener { dialog.cancelCenterDialog() }
                    tvConfirm.setOnClickListener {
                        viewModel.logout()
                        dialog.cancelCenterDialog()
                    }
                }
            }
        }
    }

    private fun displayDeleteAccountDialog() {
        val dialogBinding = DialogPromptBinding.inflate(layoutInflater)
        dialog.showCenterDialog(mActivity, true, dialogBinding, false).let {
            dialogBinding.run {
                dialogBinding.run {
                    showIcon = true
                    imgPromptIcon.setImageResource(R.drawable.ic_error)
                    titleText = getString(R.string.hint_prompt_delete_account_title)
                    subTitleText = getString(R.string.hint_prompt_delete_account_subtitle)
                    tvCancel.setOnClickListener { dialog.cancelCenterDialog() }
                    tvConfirm.setOnClickListener {
                        viewModel.deleteAccount()
                        dialog.cancelCenterDialog()
                    }
                }
            }
        }
    }
}