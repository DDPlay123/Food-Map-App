package mai.project.foodmap.features.restaurant_feature.searchDialog

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import mai.project.core.Configs
import mai.project.core.annotations.Direction
import mai.project.core.extensions.DP
import mai.project.core.extensions.displayToast
import mai.project.core.extensions.getColorCompat
import mai.project.core.extensions.getDrawableCompat
import mai.project.core.extensions.launchAndRepeatStarted
import mai.project.core.extensions.onClick
import mai.project.core.extensions.openAppSettings
import mai.project.core.utils.Event
import mai.project.core.utils.GoogleMapUtil
import mai.project.core.widget.recyclerView_decorations.DividerItemDecoration
import mai.project.core.widget.recyclerView_decorations.SpacesItemDecoration
import mai.project.foodmap.MainActivity
import mai.project.foodmap.R
import mai.project.foodmap.base.BaseBottomSheetDialog
import mai.project.foodmap.databinding.DialogBottomSheetSearchBinding
import mai.project.foodmap.domain.models.SearchRestaurantResult
import mai.project.foodmap.domain.state.NetworkResult
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class SearchBottomSheetDialog : BaseBottomSheetDialog<DialogBottomSheetSearchBinding, SearchViewModel>(
    bindingInflater = DialogBottomSheetSearchBinding::inflate
) {
    override val viewModel by viewModels<SearchViewModel>()

    private val args by navArgs<SearchBottomSheetDialogArgs>()

    @Inject
    lateinit var googleMapUtil: GoogleMapUtil

    private val searchAdapter by lazy { SearchAdapter() }

    private val audioPermission = Manifest.permission.RECORD_AUDIO

    private val audioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startSpeechRecognition()
        } else {
            with((activity as? MainActivity)) {
                this?.showSnackBar(
                    message = getString(R.string.sentence_audio_permission_denied),
                    actionText = getString(R.string.word_confirm)
                ) { openAppSettings() }
            }
        }
    }

    private val speechRecognizerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.let { text ->
                binding.edSearch.setText(text)
            }
        }
    }

    private val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            // 搜尋的項目無法滑動
            if (viewHolder.itemViewType == SearchAdapter.SEARCH) {
                return 0
            }
            return makeMovementFlags(0, ItemTouchHelper.LEFT)
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean = false // 不處理拖動

        override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float = .7f

        override fun onSwiped(
            viewHolder: RecyclerView.ViewHolder,
            direction: Int
        ) {
            val position = viewHolder.bindingAdapterPosition
            val list = searchAdapter.currentList.toMutableList()
            viewModel.deleteSearchRecord(list[position])
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            val itemView = viewHolder.itemView
            val itemHeight = itemView.bottom - itemView.top

            if (dX < 0) {
                // 畫紅色背景
                val p = Paint().apply { color = getColorCompat(R.color.error) }
                val background = RectF(
                    itemView.right + dX,
                    itemView.top.toFloat(),
                    itemView.right.toFloat(),
                    itemView.bottom.toFloat()
                )
                c.drawRect(background, p)

                // 取得 icon
                val icon = getDrawableCompat(R.drawable.vector_delete) ?: return

                // 計算露出區域寬度（正值）
                val uncoveredWidth = -dX
                // 將 icon 置中於該區域
                val iconWidth = icon.intrinsicWidth
                val iconHeight = icon.intrinsicHeight
                val iconLeft = itemView.right + dX + (uncoveredWidth - iconWidth) / 2
                val iconTop = itemView.top + (itemHeight - iconHeight) / 2
                val iconRight = iconLeft + iconWidth
                val iconBottom = iconTop + iconHeight

                icon.setBounds(
                    iconLeft.toInt(),
                    iconTop,
                    iconRight.toInt(),
                    iconBottom
                )
                icon.draw(c)
            }
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // 使用 super 拿到底層的 BottomSheetDialog
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        // 當 dialog 顯示時，設定行為
        dialog.setOnShowListener {
            // 1. 找到底部 sheet container
            val bottomSheet = dialog
                .findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
                ?: return@setOnShowListener

            // 2. 把 container 高度改成全螢幕
            bottomSheet.layoutParams = bottomSheet.layoutParams.apply {
                height = ViewGroup.LayoutParams.MATCH_PARENT
            }

            // 3. 取得 Behavior，設為展開並跳過半折疊
            BottomSheetBehavior.from(bottomSheet).apply {
                skipCollapsed = true
                isFitToContents = true
                state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        return dialog
    }

    override fun DialogBottomSheetSearchBinding.initialize(savedInstanceState: Bundle?) {
        sbDistance.max = Configs.MAX_SEARCH_DISTANCE - Configs.MIN_SEARCH_DISTANCE

        with(rvResults) {
            addItemDecoration(
                SpacesItemDecoration(
                    direction = Direction.VERTICAL,
                    space = 20.DP,
                    sideSpace = 10.DP
                )
            )
            addItemDecoration(
                DividerItemDecoration(
                    context = requireContext(),
                    direction = Direction.VERTICAL,
                    dividerHeight = 1.DP,
                    marginTop = 10.DP,
                    marginLeft = 40.DP,
                    marginRight = 40.DP,
                    dividerDrawableRes = R.drawable.bg_divider
                )
            )
            ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(this)
            adapter = searchAdapter
        }

        // 先取得經緯度，在設定文字
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main.immediate) {
            val myPlaceId = viewModel.myPlaceId.first()
            viewModel.myPlaceList.first().find { it.placeId == myPlaceId }
                ?.let { place ->
                    viewModel.searchLatLng = LatLng(place.lat, place.lng)
                    edSearch.setText(args.keyword)
                }
                ?: run {
                    googleMapUtil.getCurrentLocation(
                        onSuccess = { lat, lng ->
                            viewModel.searchLatLng = LatLng(lat, lng)
                            edSearch.setText(args.keyword)
                        },
                        onFailure = {
                            edSearch.setText(args.keyword)
                        }
                    )
                }
        }
    }

    override fun DialogBottomSheetSearchBinding.setObserver() = with(viewModel) {
        launchAndRepeatStarted(
            // 搜尋的距離
            { searchDistance.collect(::handleSearchDistanceState) },
            // 關鍵資搜尋
            { searchFlow.debounce(300L).distinctUntilChanged().collect(::handleSearchFlow) },
            // 搜尋的結果
            { searchRestaurantsResult.collect(::handleSearchRestaurantsResult) },
            // 列表資料
            { restaurantList.collect(::handleRestaurantList) }
        )
    }

    override fun DialogBottomSheetSearchBinding.setListener() {
        tvClear.onClick(anim = true) { viewModel.clearAllSearchRecord() }

        imgVoiceSearch.onClick {
            if (ContextCompat.checkSelfPermission(requireContext(), audioPermission) == PackageManager.PERMISSION_GRANTED) {
                audioPermissionLauncher.launch(audioPermission)
            } else {
                startSpeechRecognition()
            }
        }

        edSearch.doAfterTextChanged { viewModel.setSearchKeyword(it?.trim().toString()) }

        searchAdapter.onKeywordItemClick = {
            viewModel.addNewSearchRecord(it)
            navController.navigate(
                SearchBottomSheetDialogDirections.actionSearchBottomSheetDialogToRestaurantListFragment(
                    lat = viewModel.searchLatLng.latitude.toFloat(),
                    lng = viewModel.searchLatLng.longitude.toFloat(),
                    keyword = it.name,
                    distance = viewModel.searchDistance.value
                )
            )
        }

        searchAdapter.onRestaurantItemClick = {
            viewModel.addNewSearchRecord(it)
            navController.navigate(
                SearchBottomSheetDialogDirections.actionSearchBottomSheetDialogToRestaurantDetailFragment(
                    placeId = it.placeId,
                    name = it.name
                )
            )
        }

        sbDistance.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = Configs.MIN_SEARCH_DISTANCE + progress
                viewModel.setSearchDistance(value.takeIf { it >= Configs.MIN_SEARCH_DISTANCE } ?: Configs.MIN_SEARCH_DISTANCE)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                handleSearchFlow(edSearch.text?.trim().toString())
            }
        })
    }

    /**
     * 開始語音辨識
     */
    private fun startSpeechRecognition() {
        with(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)) {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.sentence_start_speak))
            runCatching {
                speechRecognizerLauncher.launch(this)
            }.onFailure {
                Timber.e(t = it, message = "無法使用語音辨識")
                FirebaseCrashlytics.getInstance().recordException(it)
                displayToast(message = getString(R.string.sentence_not_support_speech_recognizer))
            }
        }
    }

    /**
     * 處理搜尋的距離狀態
     */
    private fun handleSearchDistanceState(distance: Int) = with(binding) {
        sbDistance.progress = distance - Configs.MIN_SEARCH_DISTANCE
        tvKilometer.text = if (distance == Configs.MIN_SEARCH_DISTANCE)
            getString(R.string.word_nearby)
        else
            getString(R.string.format_kilometer, distance)
    }

    /**
     * 處理關鍵字搜尋
     */
    private fun handleSearchFlow(keyword: String) {
        if (keyword.isNotEmpty()) {
            viewModel.searchRestaurants(keyword)
        } else {
            viewModel.getSearchRecords()
        }
    }

    /**
     * 處理搜尋的結果
     */
    private fun handleSearchRestaurantsResult(
        event: Event<NetworkResult<List<SearchRestaurantResult>>>
    ) {
        handleBasicResult(
            event = event,
            workOnSuccess = { data ->
                data?.let { list ->
                    if (list.isNotEmpty()) {
                        viewModel.setRestaurantList(list)
                    } else {
                        displayToast(message = getString(R.string.sentence_not_found_search))
                        viewModel.getSearchRecords()
                    }
                }
            },
            workOnError = { viewModel.getSearchRecords() }
        )
    }

    /**
     * 處理餐廳的列表資料
     */
    private fun handleRestaurantList(
        list: List<SearchRestaurantResult>
    ) = with(binding) {
        searchAdapter.submitList(list)
        val showGroupHistory = list.isNotEmpty() && !list.first().isSearch
        groupHistory.isVisible = showGroupHistory
    }
}