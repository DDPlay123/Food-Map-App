package mai.project.foodmap.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import mai.project.foodmap.R
import mai.project.foodmap.base.BaseActivity
import mai.project.foodmap.databinding.ActivityMainBinding

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding, SharedViewModel>(
    bindingInflater = ActivityMainBinding::inflate
) {
    override val viewModel by viewModels<SharedViewModel>()

    /**
     * 導航控制器
     */
    private lateinit var navController: NavController

    /**
     * 導航基底 Fragment
     */
    private lateinit var navHostFragment: NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        doInitialization()
    }

    /**
     * 初始化
     */
    private fun doInitialization() {
        navHostFragment = supportFragmentManager.findFragmentById(R.id.navigationHost) as NavHostFragment
        navController = navHostFragment.navController

        if (viewModel.isLogin) {
            navController.setGraph(R.navigation.nav_main)
        } else {
            navController.setGraph(R.navigation.nav_auth)
        }
    }
}