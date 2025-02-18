package mai.project.foodmap.features.home_features.mapTabScreen.dialog

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import mai.project.core.annotations.Direction
import mai.project.core.extensions.DP
import mai.project.core.widget.recyclerView_decorations.SpacesItemDecoration
import mai.project.foodmap.base.BaseDialog
import mai.project.foodmap.databinding.DialogClustersBinding

@AndroidEntryPoint
class ClustersDialog : BaseDialog<DialogClustersBinding, Nothing>(
    bindingInflater = DialogClustersBinding::inflate
) {
    private val args by navArgs<ClustersDialogArgs>()

    private val clusterAdapter by lazy { ClusterAdapter() }

    override fun DialogClustersBinding.initialize(savedInstanceState: Bundle?) {
        with(rvRestaurants) {
            addItemDecoration(
                SpacesItemDecoration(
                    direction = Direction.VERTICAL,
                    space = 8.DP
                )
            )
            adapter = clusterAdapter
        }

        clusterAdapter.setItems(args.clusters.toList())
    }

    override fun DialogClustersBinding.setListener() {
        clusterAdapter.onItemClick = {
            setFragmentResult(
                args.requestCode,
                bundleOf(ClustersCallback.ARG_ITEM_CLICK to ClustersCallback.OnItemClick(it))
            )
            dismiss()
        }
    }
}