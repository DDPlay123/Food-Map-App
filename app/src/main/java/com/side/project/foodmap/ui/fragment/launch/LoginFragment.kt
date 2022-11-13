package com.side.project.foodmap.ui.fragment.launch

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.api.user.LoginReq
import com.side.project.foodmap.databinding.DialogPromptBinding
import com.side.project.foodmap.databinding.FragmentLoginBinding
import com.side.project.foodmap.helper.displayShortToast
import com.side.project.foodmap.helper.hideKeyboard
import com.side.project.foodmap.helper.setAnimClick
import com.side.project.foodmap.ui.activity.MainActivity
import com.side.project.foodmap.ui.fragment.BaseFragment
import com.side.project.foodmap.ui.viewModel.LoginViewModel
import com.side.project.foodmap.ui.other.AnimManager
import com.side.project.foodmap.ui.other.AnimState
import com.side.project.foodmap.util.Method
import com.side.project.foodmap.util.RegisterLoginValidation
import com.side.project.foodmap.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment : BaseFragment<FragmentLoginBinding>(R.layout.fragment_login) {
    private val viewModel: LoginViewModel by viewModel()
    private val animManager: AnimManager by inject()

    override fun FragmentLoginBinding.initialize() {
        binding.vm = viewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        doInitialize()
        setListener()
    }

    private fun doInitialize() {
        // 驗證輸入
        lifecycleScope.launchWhenCreated {
            viewModel.validation.collect { validation ->
                // 帳號錯誤
                if (validation.account is RegisterLoginValidation.Failed)
                    withContext(Dispatchers.Main) {
                        binding.edUsername.apply {
                            error = getString(validation.account.messageID)
                        }
                    }
                // 密碼錯誤
                if (validation.password is RegisterLoginValidation.Failed)
                    withContext(Dispatchers.Main) {
                        binding.edPassword.apply {
                            error = getString(validation.password.messageID)
                        }
                    }
            }
        }

        // 登入/註冊
        lifecycleScope.launchWhenCreated {
            viewModel.loginState.collect {
                when (it) {
                    is Resource.Loading -> {
                        Method.logE("Login", "Loading")
                        mActivity.hideKeyboard()
                        dialog.showLoadingDialog(false)
                        binding.edUsername.isFocusableInTouchMode = false
                        binding.edPassword.isFocusableInTouchMode = false
                    }
                    is Resource.Success -> {
                        dialog.cancelLoadingDialog()
                        if (it.data?.status == 0) {
                            // 登入
                            Method.logE("Login", "Success")
                            if (binding.checkbox.isChecked) {
                                viewModel.putUserAccount(binding.edUsername.text.toString().trim())
                                viewModel.putUserPassword(binding.edPassword.text.toString().trim())
                            }
                            mActivity.start(MainActivity::class.java, true)
                        } else {
                            // 註冊
                            Method.logE("Login", "to Register")
                            binding.edUsername.isFocusableInTouchMode = true
                            binding.edPassword.isFocusableInTouchMode = true
                            registerPrompt()
                        }
                    }
                    is Resource.Error -> {
                        Method.logE("Login", "Error:${it.message.toString()}")
                        dialog.cancelLoadingDialog()
                        binding.edUsername.isFocusableInTouchMode = true
                        binding.edPassword.isFocusableInTouchMode = true
                        requireActivity().displayShortToast(getString(R.string.hint_login_error))
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun registerPrompt() {
        val bindingPrompt = DialogPromptBinding.inflate(layoutInflater)
        dialog.cancelAllDialog()
        dialog.showCenterDialog(true, bindingPrompt, false).let {
            bindingPrompt.run {
                showIcon = true
                imgPromptIcon.setImageResource(R.drawable.ic_error)
                titleText = getString(R.string.hint_register_prompt_title)
                subTitleText = getString(R.string.hint_register_prompt_subtitle)
                tvCancel.setOnClickListener { dialog.cancelCenterDialog() }
                tvConfirm.setOnClickListener {
                    // TODO(直接註冊或先選圖片)
                }
            }
        }
    }

    private fun setListener() {
        binding.btnStart.setOnClickListener {
            val anim = animManager.smallToLarge
            it.setAnimClick(anim, AnimState.End) {
                if (!requestPermission())
                    return@setAnimClick
                viewModel.login(LoginReq(
                    username = binding.edUsername.text.toString().trim(),
                    password = binding.edPassword.text.toString().trim(),
                    deviceId = getDeviceId()
                ))
            }
        }
    }

    private fun requestPermission(): Boolean {
        if (!Method.requestPermission(mActivity, *permission)) {
            mActivity.displayShortToast(getString(R.string.hint_not_location_permission))
            return false
        }
        return true
    }

    @SuppressLint("HardwareIds")
    private fun getDeviceId(): String =
        Settings.Secure.getString(context?.contentResolver, Settings.Secure.ANDROID_ID)
}