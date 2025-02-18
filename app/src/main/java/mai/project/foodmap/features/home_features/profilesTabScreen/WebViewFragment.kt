package mai.project.foodmap.features.home_features.profilesTabScreen

import android.os.Bundle
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import mai.project.foodmap.base.BaseFragment
import mai.project.foodmap.databinding.FragmentWebViewBinding

@AndroidEntryPoint
class WebViewFragment : BaseFragment<FragmentWebViewBinding, Nothing>(
    bindingInflater = FragmentWebViewBinding::inflate
) {
    private val args by navArgs<WebViewFragmentArgs>()

    override fun FragmentWebViewBinding.initialize(savedInstanceState: Bundle?) {
        with(webView) {
            loadDataWithBaseURL(null, args.path, "text/html", "UTF-8", null)
        }
    }
}