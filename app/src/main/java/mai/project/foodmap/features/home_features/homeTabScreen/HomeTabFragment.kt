package mai.project.foodmap.features.home_features.homeTabScreen

import android.Manifest
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import mai.project.core.extensions.displayToast
import mai.project.core.extensions.hasGPS
import mai.project.core.extensions.onClick
import mai.project.core.extensions.openAppSettings
import mai.project.core.extensions.openGpsSettings
import mai.project.core.extensions.requestMultiplePermissions
import mai.project.foodmap.MainActivity
import mai.project.foodmap.R
import mai.project.foodmap.base.BaseFragment
import mai.project.foodmap.databinding.FragmentHomeTabBinding

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationPermissionLauncher = requestMultiplePermissions(
            allPermissions = locationPermissions,
            onGranted = ::handleLocationPermissionGranted,
            onDenied = ::handleLocationPermissionDenied
        )
    }

    override fun FragmentHomeTabBinding.initialize(savedInstanceState: Bundle?) {
        locationPermissionLauncher.launch(locationPermissions)
    }

    override fun FragmentHomeTabBinding.setListener() {
        clTextSearch.onClick {

        }

        imgImageSearch.onClick {

        }

        imgVoiceSearch.onClick {

        }
    }

    /**
     * 處理定位權限請求成功結果
     */
    private fun handleLocationPermissionGranted() {
        if (!requireContext().hasGPS) {
            with((activity as? MainActivity)) {
                this?.showSnackBar(
                    message = getString(R.string.sentence_gps_not_open),
                    actionText = getString(R.string.word_confirm)
                ) { openGpsSettings() }
            }
        } else {
            // TODO
            displayToast("成功")
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
}