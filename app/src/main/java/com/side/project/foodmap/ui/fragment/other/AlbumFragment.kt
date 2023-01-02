package com.side.project.foodmap.ui.fragment.other

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.side.project.foodmap.R
import com.side.project.foodmap.databinding.FragmentAlbumBinding
import com.side.project.foodmap.helper.delayOnLifecycle
import com.side.project.foodmap.helper.getStatusBarHeight
import com.side.project.foodmap.helper.setAnimClick
import com.side.project.foodmap.ui.adapter.other.AlbumAdapter
import com.side.project.foodmap.ui.other.AnimState
import com.side.project.foodmap.util.Constants
import com.side.project.foodmap.util.Constants.download_permission
import com.side.project.foodmap.util.tools.DownloadImage
import com.side.project.foodmap.util.tools.Method

class AlbumFragment : BaseDialogFragment<FragmentAlbumBinding>(R.layout.fragment_album) {
    private lateinit var photoIdList: List<String>
    private var position = -1

    lateinit var onDismissListener: (() -> Unit)

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener.invoke()
    }

    override fun FragmentAlbumBinding.initialize() {
        binding?.paddingTop = mActivity.getStatusBarHeight()
        arguments?.let {
            val jsonString = it.getString(Constants.ALBUM_IMAGE_RESOURCE, "")
            val type = object : TypeToken<List<String>>() {}.type
            photoIdList = Gson().fromJson(jsonString, type)
            position = it.getInt(Constants.IMAGE_POSITION, 3)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        doInitialize()
        setListener()
    }

    private fun doInitialize() {
        binding?.run {
            if (::photoIdList.isInitialized && position >= 0) {
                val albumAdapter = AlbumAdapter(this@AlbumFragment, photoIdList)
                total = photoIdList.size
                now = position
                vpAlbum.apply {
                    this.delayOnLifecycle(50) {
                        adapter = albumAdapter
                        offscreenPageLimit = 5
                        setCurrentItem(position, false)
                    }

                    registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                        override fun onPageSelected(position: Int) {
                            super.onPageSelected(position)
                            now = position
                            this@AlbumFragment.position = position
                        }
                    })
                }
            }
        }
    }

    private fun setListener() {
        val anim = animManager.smallToLarge
        binding?.run {
            imgBack.setOnClickListener {
                it.setAnimClick(anim, AnimState.Start) {
                    dismiss()
                }
            }

            imgDownload.setOnClickListener {
                if (!Method.requestPermission(mActivity, *download_permission))
                    return@setOnClickListener
                DownloadImage.downloadImage(
                    mActivity, "http://kkhomeserver.ddns.net:33000/api/place/get_html_photo/${photoIdList[position]}"
                )
            }
        }
    }
}