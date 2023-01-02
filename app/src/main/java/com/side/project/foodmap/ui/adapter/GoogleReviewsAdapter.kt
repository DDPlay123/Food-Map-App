package com.side.project.foodmap.ui.adapter

import android.text.TextUtils
import com.side.project.foodmap.R
import com.side.project.foodmap.data.remote.Review
import com.side.project.foodmap.databinding.ItemGoogleReviewsBinding
import com.side.project.foodmap.helper.display
import com.side.project.foodmap.helper.gone
import com.side.project.foodmap.ui.adapter.other.BaseRvListAdapter

class GoogleReviewsAdapter : BaseRvListAdapter<ItemGoogleReviewsBinding, Review>(R.layout.item_google_reviews) {

    lateinit var onItemClick: ((Review) -> Unit)

    override fun bind(item: Review, binding: ItemGoogleReviewsBinding, position: Int) {
        super.bind(item, binding, position)
        binding.apply {
            reviews = item

            tvReadMore.setOnClickListener {
                if (tvReadMore.text.toString() == tvReadMore.context.getString(R.string.hint_read_more)) {
                    tvComment.maxLines = Int.MAX_VALUE
                    tvComment.ellipsize = null
                    tvReadMore.text = tvReadMore.context.getString(R.string.hint_read_less)
                } else {
                    tvComment.maxLines = 4
                    tvComment.ellipsize = TextUtils.TruncateAt.END
                    tvReadMore.text = tvReadMore.context.getString(R.string.hint_read_more)
                }
            }

            tvComment.post {
                if (tvComment.lineCount > 4)
                    tvReadMore.display()
            }

            imgPicture.setOnClickListener { onItemClick.invoke(item) }
        }
    }
}