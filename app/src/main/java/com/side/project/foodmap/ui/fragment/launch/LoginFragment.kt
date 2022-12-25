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
import com.side.project.foodmap.util.tools.Method
import com.side.project.foodmap.util.RegisterLoginValidation
import com.side.project.foodmap.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment : BaseFragment<FragmentLoginBinding>(R.layout.fragment_login) {
    private val viewModel: LoginViewModel by viewModel()

    override fun FragmentLoginBinding.initialize() {
        viewModel.getUserAccountFromDataStore()
        viewModel.getUserPasswordFromDataStore()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        doInitialize()
        setListener()
    }

    private fun doInitialize() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                // 紀錄上次登入資料
                launch {
                    viewModel.userAccount.observe(viewLifecycleOwner) { binding.account = it }
                }
                launch {
                    viewModel.userPassword.observe(viewLifecycleOwner) { binding.password = it }
                }
                // 驗證輸入
                launch {
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
                // 登入
                launch {
                    viewModel.loginState.collect {
                        when (it) {
                            is Resource.Loading -> {
                                Method.logE("Login", "Loading")
                                mActivity.hideKeyboard()
                                dialog.showLoadingDialog(mActivity, false)
                                binding.edUsername.isFocusableInTouchMode = false
                                binding.edPassword.isFocusableInTouchMode = false
                            }
                            is Resource.Success -> {
                                if (it.data?.status == 0) {
                                    // 登入
                                    Method.logE("Login", "Success")
                                    viewModel.clearPublicData()
                                    if (binding.checkbox.isChecked) {
                                        viewModel.putUserAccount(binding.edUsername.text.toString().trim())
                                        viewModel.putUserPassword(it.message.toString())
                                    }
                                    viewModel.putDeviceId(mActivity.getDeviceId())
                                } else {
                                    // 註冊
                                    dialog.cancelLoadingDialog()
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
                                requireActivity().displayShortToast(it.message.toString())
                            }
                            else -> Unit
                        }
                    }
                }
                // 註冊
                launch {
                    viewModel.registerState.collect {
                        when (it) {
                            is Resource.Loading -> {
                                Method.logE("Register", "Loading")
                                mActivity.hideKeyboard()
                                dialog.showLoadingDialog(mActivity, false)
                                binding.edUsername.isFocusableInTouchMode = false
                                binding.edPassword.isFocusableInTouchMode = false
                            }
                            is Resource.Success -> {
                                Method.logE("Register", "Success")
                                dialog.cancelLoadingDialog()
                                viewModel.login(
                                    account = binding.edUsername.text.toString().trim(),
                                    password = binding.edPassword.text.toString().trim(),
                                    deviceId = mActivity.getDeviceId()
                                )
                            }
                            is Resource.Error -> {
                                Method.logE("Register", "Error:${it.message.toString()}")
                                dialog.cancelLoadingDialog()
                                binding.edUsername.isFocusableInTouchMode = true
                                binding.edPassword.isFocusableInTouchMode = true
                                requireActivity().displayShortToast(it.message.toString())
                            }
                            else -> Unit
                        }
                    }
                }
                // 取得使用者照片
                launch {
                    viewModel.getUserImageState.observe(viewLifecycleOwner) {
                        when (it) {
                            is Resource.Loading -> {
                                Method.logE("Get User Image", "Loading")
                                dialog.showLoadingDialog(mActivity, false)
                            }
                            is Resource.Success -> {
                                Method.logE("Get User Image", "Success")
                                dialog.cancelLoadingDialog()
                                it.data?.result?.let { result ->
                                    viewModel.putUserPicture(result.userImage)
                                    mActivity.start(MainActivity::class.java, true)
                                }
                            }
                            is Resource.Error -> {
                                Method.logE("Get User Image", "Error:${it.message.toString()}")
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
                        account = binding.edUsername.text.toString().trim(),
                        password = binding.edPassword.text.toString().trim(),
                        deviceId = mActivity.getDeviceId()
                    )
                }
            }
        }
    }

    private fun setListener() {
        binding.run {
            btnStart.setOnClickListener {
                val anim = animManager.smallToLarge
                it.setAnimClick(anim, AnimState.End) {
                    if (!requestLocationPermission())
                        return@setAnimClick
                    viewModel.login(
                        account = binding.edUsername.text.toString().trim(),
                        password = binding.edPassword.text.toString().trim(),
                        deviceId = mActivity.getDeviceId()
                    )
                }
            }

            edUsername.setOnFocusChangeListener { view, b ->
                if (b)
                    view.delayOnLifecycle(300) {
                        scrollView.post {
                            scrollView.scrollY = scrollView.bottom
                        }
                    }
            }

            edPassword.setOnFocusChangeListener { view, b ->
                if (b)
                    view.delayOnLifecycle(300) {
                        scrollView.post {
                            scrollView.scrollY = scrollView.bottom
                        }
                    }
            }
        }
    }
}