package mai.project.foodmap.base

import androidx.viewbinding.ViewBinding
import mai.project.core.extensions.displayToast
import mai.project.core.extensions.openAppSettings
import mai.project.core.extensions.openGpsSettings
import mai.project.core.utils.Event
import mai.project.core.utils.GoogleMapUtil
import mai.project.foodmap.MainActivity
import mai.project.foodmap.R
import mai.project.foodmap.domain.state.NetworkResult
import mai.project.foodmap.domain.utils.handleResult
import mai.project.foodmap.features.dialogs_features.loading.LoadingDialogDirections
import mai.project.foodmap.features.dialogs_features.prompt.PromptDialogDirections
import mai.project.foodmap.features.dialogs_features.selector.SelectorBottomSheetDialogDirections
import mai.project.foodmap.features.dialogs_features.selector.SelectorModel
import mai.project.foodmap.features.dialogs_features.webView.WebViewDialogDirections

/**
 * 開啟/關閉 Loading Dialog
 *
 * @param cancelable 是否可以點擊關閉 Dialog
 */
fun <VB : ViewBinding, VM : BaseViewModel> BaseFragment<VB, VM>.navigateLoadingDialog(
    isOpen: Boolean,
    cancelable: Boolean = true
) {
    val currentDestId = navController.currentBackStackEntry?.destination?.id
    when {
        // 顯示 LoadingDialog (不重複導頁)
        isOpen && currentDestId != R.id.loadingDialog -> {
            navController.navigate(LoadingDialogDirections.actionGlobalToLoadingDialog(cancelable))
        }

        // 關閉 LoadingDialog
        !isOpen && currentDestId == R.id.loadingDialog -> {
            // 退回上一層
            navController.navigateUp()
        }

        else -> Unit
    }
}

/**
 * 開啟 Prompt Dialog
 *
 * @param requestCode 請求碼
 * @param title 標題
 * @param message 內文
 * @param confirmText 確認按鈕 (不顯示則為 Null)
 * @param cancelText 取消按鈕 (不顯示則為 Null)
 * @param enableInput 是否啟用輸入框
 * @param inputHint 輸入框提示文本 (不顯示則為 Null)
 */
fun <VB : ViewBinding, VM : BaseViewModel> BaseFragment<VB, VM>.navigatePromptDialog(
    requestCode: String,
    title: String,
    message: String,
    confirmText: String? = getString(R.string.word_confirm),
    cancelText: String? = getString(R.string.word_cancel),
    enableInput: Boolean = false,
    inputHint: String? = null
) {
    navigate(
        PromptDialogDirections.actionGlobalToPromptDialog(
            requestCode = requestCode,
            title = title,
            message = message,
            confirmText = confirmText,
            cancelText = cancelText,
            enableInput = enableInput,
            inputHint = inputHint
        )
    )
}

/**
 * 開啟 Selector Dialog
 *
 * @param requestCode 請求碼
 * @param title 標題
 * @param items 選項
 */
fun <VB : ViewBinding, VM : BaseViewModel> BaseFragment<VB, VM>.navigateSelectorDialog(
    requestCode: String,
    title: String,
    items: List<SelectorModel>
) {
    navigate(
        SelectorBottomSheetDialogDirections.actionGlobalToSelectorBottomSheetDialog(
            requestCode = requestCode,
            title = title,
            items = items.toTypedArray()
        )
    )
}

/**
 * 開啟 WebView Dialog
 *
 * @param path 路徑
 */
fun <VB : ViewBinding, VM : BaseViewModel> BaseFragment<VB, VM>.navigateWebViewDialog(
    path: String
) {
    navigate(
        WebViewDialogDirections.actionGlobalToWebViewDialog(
            path = path
        )
    )
}

/**
 * 處理 API 基礎回傳結果
 *
 * @param event API 回傳事件
 * @param needLoading 是否需要 Loading
 * @param workOnSuccess 成功後執行工作
 * @param workOnError 失敗後執行工作
 */
fun <VB : ViewBinding, VM : BaseViewModel, T> BaseFragment<VB, VM>.handleBasicResult(
    event: Event<NetworkResult<T>>,
    needLoading: Boolean = true,
    workOnSuccess: ((T?) -> Unit)? = null,
    workOnError: (() -> Unit)? = null
) {
    event.getContentIfNotHandled?.handleResult {
        onLoading = { if (needLoading) viewModel?.setLoading(true) }
        onSuccess = {
            if (needLoading) viewModel?.setLoading(false)
            workOnSuccess?.invoke(it)
        }
        onError = { _, msg ->
            if (needLoading) viewModel?.setLoading(false)
            displayToast(msg ?: "Unknown Error")
            workOnError?.invoke()
        }
    }
}

/**
 * 檢查定位權限 是否開啟
 *
 * @param googleMapUtil [GoogleMapUtil]
 */
fun <VB : ViewBinding, VM : BaseViewModel> BaseFragment<VB, VM>.checkLocationPermission(
    googleMapUtil: GoogleMapUtil
): Boolean {
    return when {
        !googleMapUtil.checkLocationPermission -> {
            with((activity as? MainActivity)) {
                this?.showSnackBar(
                    message = getString(R.string.sentence_location_permission_denied),
                    actionText = getString(R.string.word_confirm)
                ) { openAppSettings() }
            }
            false
        }

        else -> true
    }
}

/**
 * 檢查 GPS 是否開啟
 *
 * @param googleMapUtil [GoogleMapUtil]
 */
fun <VB : ViewBinding, VM : BaseViewModel> BaseFragment<VB, VM>.checkGPS(
    googleMapUtil: GoogleMapUtil
): Boolean {
    return when {
        !googleMapUtil.checkGPS -> {
            with((activity as? MainActivity)) {
                this?.showSnackBar(
                    message = getString(R.string.sentence_gps_not_open),
                    actionText = getString(R.string.word_confirm)
                ) { openGpsSettings() }
            }
            false
        }

        else -> true
    }
}

/**
 * 檢查定位權限 和 GPS 是否開啟
 */
fun <VB : ViewBinding, VM : BaseViewModel> BaseFragment<VB, VM>.checkLocationPermissionAndGPS(
    googleMapUtil: GoogleMapUtil
): Boolean {
    return when {
        !googleMapUtil.checkLocationPermission -> checkLocationPermission(googleMapUtil)

        !googleMapUtil.checkGPS -> checkGPS(googleMapUtil)

        else -> true
    }
}

/**
 * 檢查 GPS 是否開啟，並取得當前位置
 *
 * @param googleMapUtil [GoogleMapUtil]
 * @param onSuccess 成功後執行工作
 * @param onFailure 失敗後執行工作
 */
fun <VB : ViewBinding, VM : BaseViewModel> BaseFragment<VB, VM>.checkGPSAndGetCurrentLocation(
    googleMapUtil: GoogleMapUtil,
    onSuccess: (lat: Double, lng: Double) -> Unit,
    onFailure: () -> Unit
) {
    if (checkLocationPermissionAndGPS(googleMapUtil)) {
        googleMapUtil.getCurrentLocation(
            onSuccess = onSuccess,
            onFailure = { displayToast(getString(R.string.sentence_can_not_get_location)) }
        )
    } else {
        onFailure()
    }
}