package com.side.project.foodmap.ui.fragment

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig.SINGLE
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.side.project.foodmap.R
import com.side.project.foodmap.databinding.DialogPromptBinding
import com.side.project.foodmap.databinding.DialogPromptSearchBinding
import com.side.project.foodmap.databinding.FragmentProfilesBinding
import com.side.project.foodmap.helper.*
import com.side.project.foodmap.ui.activity.ListActivity
import com.side.project.foodmap.ui.activity.launch.LoginActivity
import com.side.project.foodmap.ui.fragment.other.BaseFragment
import com.side.project.foodmap.ui.other.AnimState
import com.side.project.foodmap.ui.viewModel.MainViewModel
import com.side.project.foodmap.util.Constants
import com.side.project.foodmap.util.RegisterLoginValidation
import com.side.project.foodmap.util.Resource
import com.side.project.foodmap.util.tools.CoilEngine
import com.side.project.foodmap.util.tools.Method
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.io.File
import java.io.FileNotFoundException

class ProfilesFragment : BaseFragment<FragmentProfilesBinding>(R.layout.fragment_profiles) {
    private val viewModel: MainViewModel by activityViewModel()

    override fun FragmentProfilesBinding.initialize() {
        binding?.paddingTop = mActivity.getStatusBarHeight()
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
                    viewModel.setPasswordFlow.collect {
                        when (it) {
                            is Resource.Loading -> {
                                mActivity.hideKeyboard()
                                dialog.showLoadingDialog(mActivity, false)
                            }
                            is Resource.Success -> {
                                dialog.cancelAllDialog()
                                requireActivity().displayShortToast(getString(R.string.hint_set_password_success))
                            }
                            is Resource.Error -> {
                                dialog.cancelLoadingDialog()
                                requireActivity().displayShortToast(it.message.toString())
                            }
                            else -> Unit
                        }
                    }
                }
                // 登出
                launch {
                    viewModel.logoutFlow.collect {
                        when (it) {
                            is Resource.Loading -> dialog.showLoadingDialog(mActivity, false)
                            is Resource.Success -> {
                                dialog.cancelLoadingDialog()
                                requireActivity().displayShortToast(getString(R.string.hint_logout_success))
                                mActivity.start(LoginActivity::class.java, true)
                            }
                            is Resource.Error -> {
                                dialog.cancelLoadingDialog()
                                requireActivity().displayShortToast(getString(R.string.hint_error))
                            }
                            else -> Unit
                        }
                    }
                }
                // 刪除帳號
                launch {
                    viewModel.deleteAccountFlow.collect {
                        when (it) {
                            is Resource.Loading -> dialog.showLoadingDialog(mActivity, false)
                            is Resource.Success -> {
                                dialog.cancelLoadingDialog()
                                requireActivity().displayShortToast(getString(R.string.hint_delete_account_success))
                                mActivity.start(LoginActivity::class.java, true)
                            }
                            is Resource.Error -> {
                                dialog.cancelLoadingDialog()
                                requireActivity().displayShortToast(getString(R.string.hint_error))
                            }
                            else -> Unit
                        }
                    }
                }
                // 設定大頭照
                launch {
                    viewModel.setUserImageFlow.collect {
                        when (it) {
                            is Resource.Loading -> dialog.showLoadingDialog(mActivity, false)
                            is Resource.Success -> {
                                viewModel.getUserPictureFromDataStore()
                                dialog.cancelLoadingDialog()
                            }
                            is Resource.Error -> requireActivity().displayShortToast(getString(R.string.hint_error))
                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    private fun setListener() {
        val anim = animManager.smallToLarge
        binding?.run {
            tvBlackList.setOnClickListener {
                Bundle().also { b ->
                    b.putString(Constants.LIST_TYPE, Constants.ListType.BLACK_LIST.name)
                    b.putString(Constants.KEYWORD, "")
                    b.putInt(Constants.DISTANCE, 1000)
                    mActivity.start(ListActivity::class.java, b)
                }
            }

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
                if (!mActivity.requestCameraPermission())
                    return@setOnClickListener
                pictureSelector()
            }
        }
    }

    private fun pictureSelector() {
        PictureSelector.create(this)
            .openGallery(SelectMimeType.ofImage())
            .setMaxSelectNum(1)
            .setCameraImageFormat(PictureMimeType.PNG)
            .setImageEngine(CoilEngine())
            .setImageSpanCount(3)
            .setSelectionMode(SINGLE)
            .setRecyclerAnimationMode(R.anim.layout_animation_random)
            .isPageStrategy(true)
            .isCameraRotateImage(true)
            .isGif(false)
            .isFastSlidingSelect(true)
            .isDirectReturnSingle(true)
            .forResult(object: OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: ArrayList<LocalMedia>?) {
                    val imageFile = File(result?.first()?.realPath ?: "")
                    try {
                        cropImage.launch(
                            CropImageContractOptions(
                                uri = imageFile.toUri(),
                                cropImageOptions = CropImageOptions(
                                    outputRequestHeight = 1000,
                                    outputRequestWidth = 1000,
                                    maxCropResultWidth = 4000,
                                    maxCropResultHeight = 4000,
                                    aspectRatioX = 1,
                                    aspectRatioY = 1,
                                    initialCropWindowPaddingRatio = 0f,
                                    fixAspectRatio = true,
                                )
                            )
                        )
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    }
                }

                override fun onCancel() {
                    mActivity.displayShortToast(getString(R.string.hint_crop_cancel))
                }
            })
    }

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uriContent: Uri = result.uriContent as Uri
            val inputStream = context?.contentResolver?.openInputStream(uriContent)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val encodedImage = Method.encodeImage(bitmap) as String
            viewModel.setUserImage(encodedImage)
        } else
            pictureSelector()
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