package mai.project.foodmap.features.home_features.homeTabScreen

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Looper
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.viewModels
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import mai.project.core.extensions.checkMultiplePermissions
import mai.project.core.extensions.displayToast
import mai.project.core.extensions.hasGPS
import mai.project.core.extensions.launchAndRepeatStarted
import mai.project.core.extensions.onClick
import mai.project.core.extensions.openAppSettings
import mai.project.core.extensions.openGpsSettings
import mai.project.core.extensions.requestMultiplePermissions
import mai.project.core.utils.Event
import mai.project.foodmap.MainActivity
import mai.project.foodmap.R
import mai.project.foodmap.base.BaseFragment
import mai.project.foodmap.databinding.FragmentHomeTabBinding
import mai.project.foodmap.domain.models.RestaurantResult
import mai.project.foodmap.domain.state.NetworkResult
import timber.log.Timber

@AndroidEntryPoint
class HomeTabFragment : BaseFragment<FragmentHomeTabBinding, HomeTabViewModel>(
    bindingInflater = FragmentHomeTabBinding::inflate
) {
    override val viewModel by viewModels<HomeTabViewModel>()

    override val useActivityOnBackPressed: Boolean = true

    private lateinit var locationPermissionLauncher: ActivityResultLauncher<Array<String>>

    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationPermissionLauncher = requestMultiplePermissions(
            allPermissions = locationPermissions,
            needAllPermissions = false,
            onGranted = ::handleLocationPermissionGranted,
            onDenied = ::handleLocationPermissionDenied
        )
    }


    override fun FragmentHomeTabBinding.initialize(savedInstanceState: Bundle?) {
        locationPermissionLauncher.launch(locationPermissions)
    }

    override fun FragmentHomeTabBinding.setObserver() = with(viewModel) {
        launchAndRepeatStarted(
            // Loading
            { isLoading.collect { navigateLoadingDialog(it, false) } },
            // 人氣餐廳卡片資訊
            { drawCardResult.collect(::handleDrawCardResult) }
        )
    }

    override fun FragmentHomeTabBinding.setListener() {
        tvLocation.onClick(anim = true) {

        }

        clTextSearch.onClick {

        }

        imgImageSearch.onClick {

        }

        imgVoiceSearch.onClick {

        }

        tvPopular.onClick(anim = true) {

        }

        imgRefresh.onClick(anim = true) { handleLocationPermissionGranted() }

        cardMore.onClick {

        }
    }

    /**
     * 處理定位權限請求成功結果
     */
    private fun handleLocationPermissionGranted() {
        when {
            !checkMultiplePermissions(locationPermissions, false) ->
                handleLocationPermissionDenied()

            !requireContext().hasGPS -> {
                with((activity as? MainActivity)) {
                    this?.showSnackBar(
                        message = getString(R.string.sentence_gps_not_open),
                        actionText = getString(R.string.word_confirm)
                    ) { openGpsSettings() }
                }
            }

            else -> loadData()
        }
    }

    /**
     * 處理定位權限請求失敗結果
     */
    private fun handleLocationPermissionDenied() {
        with((activity as? MainActivity)) {
            this?.showSnackBar(
                message = getString(R.string.sentence_location_permission_denied),
                actionText = getString(R.string.word_confirm)
            ) { openAppSettings() }
        }
    }

    /**
     * 載入資料
     */
    private fun loadData() {
        getCurrentLocation(
            onSuccess = { lat, lng ->
                viewModel.getDrawCard(lat, lng, true)
            },
            onFailure = {
                displayToast(getString(R.string.sentence_can_not_get_location))
            }
        )
    }

    /**
     * 取得當前的座標經緯度
     */
    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(
        onSuccess: (lat: Double, lng: Double) -> Unit,
        onFailure: () -> Unit
    ) {
        if (!checkMultiplePermissions(locationPermissions, false)) {
            handleLocationPermissionDenied()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                onSuccess(location.latitude, location.longitude)
            } else {
                // 如果 lastLocation 為 null，請求新的位置更新
                val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        locationResult.locations.lastOrNull()?.let { latestLocation ->
                            onSuccess(latestLocation.latitude, latestLocation.longitude)
                            // 取得一次即移除更新
                            fusedLocationClient.removeLocationUpdates(this)
                        } ?: onFailure()
                    }
                }
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            }
        }.addOnFailureListener {
            Timber.e(message = "取得經緯度失敗", t = it)
            onFailure()
        }
    }

    /**
     * 處理人氣餐廳卡片結果
     */
    private fun handleDrawCardResult(
        event: Event<NetworkResult<List<RestaurantResult>>>
    ) {
        handleBasicResult(event,
            workOnSuccess = {

            },
            workOnError = {

            }
        )
    }
}