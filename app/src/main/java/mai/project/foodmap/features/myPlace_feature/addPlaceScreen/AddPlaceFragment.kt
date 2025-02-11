package mai.project.foodmap.features.myPlace_feature.addPlaceScreen

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener
import com.google.android.gms.maps.GoogleMap.OnCameraMoveListener
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import mai.project.core.Configs
import mai.project.core.extensions.DP
import mai.project.core.extensions.displayToast
import mai.project.core.extensions.hideKeyboard
import mai.project.core.extensions.onClick
import mai.project.core.extensions.openAppSettings
import mai.project.core.extensions.openGpsSettings
import mai.project.core.utils.GoogleMapUtil
import mai.project.foodmap.MainActivity
import mai.project.foodmap.R
import mai.project.foodmap.base.BaseFragment
import mai.project.foodmap.databinding.FragmentAddPlaceBinding
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class AddPlaceFragment : BaseFragment<FragmentAddPlaceBinding, AddPlaceViewModel>(
    bindingInflater = FragmentAddPlaceBinding::inflate
), OnMapReadyCallback, OnMapLoadedCallback, OnCameraMoveListener, OnCameraIdleListener {
    override val viewModel by viewModels<AddPlaceViewModel>()

    @Inject
    lateinit var googleMapUtil: GoogleMapUtil

    private lateinit var mapFragment: SupportMapFragment

    private lateinit var myMap: GoogleMap

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    // 中心點位置
    private var targetCameraPosition: CameraPosition? = null

    override fun FragmentAddPlaceBinding.initialize(savedInstanceState: Bundle?) {
        mapFragment = childFragmentManager.findFragmentById(R.id.mapHost) as SupportMapFragment
        mapFragment.getMapAsync(this@AddPlaceFragment)

        bottomSheetBehavior = BottomSheetBehavior.from(binding.layoutSelector.root)
        with(bottomSheetBehavior) {
            skipCollapsed = true
            state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    override fun FragmentAddPlaceBinding.destroy() {
        if (::myMap.isInitialized) with(myMap) {
            clear()
            setOnMapLoadedCallback(null)
            setOnCameraMoveListener(null)
            setOnCameraIdleListener(null)
        }
    }

    override fun FragmentAddPlaceBinding.setListener() {
        imgBack.onClick { popBackStack() }

        imgMyLocation.onClick {
            if (checkLocationPermission()) getMyLocationAndMove()
        }

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        layoutSelector.tvCurrentAddress.isVisible = true
                        layoutSelector.groupSearch.isVisible = false
                    }

                    BottomSheetBehavior.STATE_EXPANDED -> {
                        layoutSelector.tvCurrentAddress.isVisible = false
                        layoutSelector.groupSearch.isVisible = true
                    }

                    BottomSheetBehavior.STATE_SETTLING, BottomSheetBehavior.STATE_DRAGGING -> {
                        root.hideKeyboard
                    }

                    else -> Unit
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val height = bottomSheet.height.toFloat()
                val offset = height * slideOffset

                targetCameraPosition?.let {
                    when (bottomSheetBehavior.state) {
                        BottomSheetBehavior.STATE_DRAGGING, BottomSheetBehavior.STATE_SETTLING -> {
                            setMapsPaddingFromBottom(offset)
                            googleMapUtil.updateCamera(myMap, it)
                        }

                        else -> Unit
                    }
                }
            }
        })
    }

    /**
     * 設定地圖與底部的偏移
     */
    private fun setMapsPaddingFromBottom(offset: Float) {
        val maxMapsPaddingBottom = 1f
        binding.layoutMap.setPadding(0, 0, 0, (offset * maxMapsPaddingBottom).roundToInt())
    }

    override fun onMapReady(maps: GoogleMap) {
        if (checkLocationPermission()) {
            myMap = maps.apply {
                googleMapUtil.doInitializeGoogleMap(this)
                googleMapUtil.setCompassLocation(
                    mapFragment = mapFragment,
                    marginTop = 45.DP,
                    marginLeft = 90.DP
                )
                setOnMapLoadedCallback(this@AddPlaceFragment)
                setOnCameraMoveListener(this@AddPlaceFragment)
                setOnCameraIdleListener(this@AddPlaceFragment)
            }
        }
    }

    override fun onMapLoaded() {
        getMyLocationAndMove()
    }

    override fun onCameraMove() {
        targetCameraPosition = myMap.cameraPosition
    }

    override fun onCameraIdle() {
        targetCameraPosition = myMap.cameraPosition
    }

    /**
     * 檢查定位權限 是否開啟
     */
    private fun checkLocationPermission(): Boolean {
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
     */
    private fun checkGPS(): Boolean {
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
     * 取得當前定位並移動到該點
     */
    private fun getMyLocationAndMove() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        if (checkGPS()) {
            googleMapUtil.getCurrentLocation(
                onSuccess = { lat, lng -> initLocation(lat, lng) },
                onFailure = { displayToast(getString(R.string.sentence_can_not_get_location)) }
            )
        } else {
            initLocation(Configs.DEFAULT_LATITUDE, Configs.DEFAULT_LONGITUDE)
        }
    }

    /**
     * 初始化位置
     */
    private fun initLocation(lat: Double, lng: Double) {
        if (::myMap.isInitialized) {
            googleMapUtil.animateCamera(
                map = myMap,
                latLng = LatLng(lat, lng),
                zoomLevel = 15f
            )
        }
    }
}