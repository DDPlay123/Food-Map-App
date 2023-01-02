package com.side.project.foodmap.ui.fragment.launch

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.side.project.foodmap.R
import com.side.project.foodmap.databinding.DialogPromptBinding
import com.side.project.foodmap.databinding.FragmentLoginBinding
import com.side.project.foodmap.helper.*
import com.side.project.foodmap.ui.activity.MainActivity
import com.side.project.foodmap.ui.fragment.other.BaseFragment
import com.side.project.foodmap.ui.viewModel.LoginViewModel
import com.side.project.foodmap.ui.other.AnimState
import com.side.project.foodmap.util.RegisterLoginValidation
import com.side.project.foodmap.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class LoginFragment : BaseFragment<FragmentLoginBinding>(R.layout.fragment_login) {
    private val viewModel: LoginViewModel by activityViewModel()

    override fun FragmentLoginBinding.initialize() {
        binding?.vm = viewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        doInitialize()
        setListener()
    }

    private fun doInitialize() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                // 驗證輸入
                launch {
                    viewModel.validation.collect { validation ->
                        // 帳號錯誤
                        if (validation.username is RegisterLoginValidation.Failed)
                            withContext(Dispatchers.Main) {
                                binding?.edUsername?.apply {
                                    error = getString(validation.username.messageID)
                                }
                            }
                        // 密碼錯誤
                        if (validation.password is RegisterLoginValidation.Failed)
                            withContext(Dispatchers.Main) {
                                binding?.edPassword?.apply {
                                    error = getString(validation.password.messageID)
                                }
                            }
                    }
                }
                // 登入
                launch {
                    viewModel.loginFlow.collect {
                        when (it) {
                            is Resource.Loading -> {
                                mActivity.hideKeyboard()
                                dialog.showLoadingDialog(mActivity, false)
                                binding?.edUsername?.isFocusableInTouchMode = false
                                binding?.edPassword?.isFocusableInTouchMode = false
                            }
                            is Resource.Success -> {
                                if (it.data?.status == 0) {
                                    // 登入
                                    viewModel.getUserImage()
                                } else {
                                    // 註冊
                                    dialog.cancelLoadingDialog()
                                    binding?.edUsername?.isFocusableInTouchMode = true
                                    binding?.edPassword?.isFocusableInTouchMode = true
                                    registerPrompt()
                                }
                            }
                            is Resource.Error -> {
                                dialog.cancelLoadingDialog()
                                binding?.edUsername?.isFocusableInTouchMode = true
                                binding?.edPassword?.isFocusableInTouchMode = true
                                requireActivity().displayShortToast(it.message.toString())
                            }
                            else -> Unit
                        }
                    }
                }
                // 註冊
                launch {
                    viewModel.registerFlow.collect {
                        when (it) {
                            is Resource.Loading -> {
                                mActivity.hideKeyboard()
                                dialog.showLoadingDialog(mActivity, false)
                                binding?.edUsername?.isFocusableInTouchMode = false
                                binding?.edPassword?.isFocusableInTouchMode = false
                            }
                            is Resource.Success -> {
                                viewModel.login(
                                    username = binding?.edUsername?.text.toString().trim(),
                                    password = binding?.edPassword?.text.toString().trim(),
                                    deviceId = mActivity.getDeviceId()
                                )
                            }
                            is Resource.Error -> {
                                dialog.cancelLoadingDialog()
                                binding?.edUsername?.isFocusableInTouchMode = true
                                binding?.edPassword?.isFocusableInTouchMode = true
                                requireActivity().displayShortToast(it.message.toString())
                            }
                            else -> Unit
                        }
                    }
                }
                // 取得使用者照片
                launch {
                    viewModel.getUserImageFlow.collect {
                        when (it) {
                            is Resource.Success -> {
                                dialog.cancelLoadingDialog()
                                it.data?.result?.let {
                                    // 是否記住帳號密碼
                                    if (binding?.checkbox?.isChecked == true) {
                                        viewModel.putUserAccount(binding?.edUsername?.text.toString().trim())
                                        viewModel.putUserPassword(binding?.edPassword?.text.toString().trim())
                                    }
                                    mActivity.start(MainActivity::class.java, true)
                                }
                            }
                            is Resource.Error -> {
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

    private fun registerPrompt() {
        val bindingPrompt = DialogPromptBinding.inflate(layoutInflater)
        dialog.cancelAllDialog()
        dialog.showCenterDialog(mActivity, true, bindingPrompt, false).let {
            bindingPrompt.run {
                showIcon = true
                imgPromptIcon.setImageResource(R.drawable.ic_error)
                titleText = getString(R.string.hint_register_prompt_title)
                subTitleText = getString(R.string.hint_register_prompt_subtitle)
                tvCancel.setOnClickListener { dialog.cancelCenterDialog() }
                tvConfirm.setOnClickListener {
                    dialog.cancelCenterDialog()
                    viewModel.register(
                        username = binding?.edUsername?.text.toString().trim(),
                        password = binding?.edPassword?.text.toString().trim(),
                        deviceId = mActivity.getDeviceId()
                    )
                }
            }
        }
    }

    private fun setListener() {
        binding?.run {
            btnStart.setOnClickListener {
                val anim = animManager.smallToLarge
                it.setAnimClick(anim, AnimState.End) {
                    if (!mActivity.requestLocationPermission() || !mActivity.checkMyDeviceGPS())
                        return@setAnimClick
                    viewModel.login(
                        username = binding?.edUsername?.text.toString().trim(),
                        password = binding?.edPassword?.text.toString().trim(),
                        deviceId = mActivity.getDeviceId()
                    )
                }
            }
        }
    }
}