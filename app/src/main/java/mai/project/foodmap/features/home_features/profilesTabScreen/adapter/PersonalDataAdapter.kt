package mai.project.foodmap.features.home_features.profilesTabScreen.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import mai.project.core.annotations.ImageType
import mai.project.core.extensions.inflateBinding
import mai.project.core.extensions.onClick
import mai.project.core.utils.ImageLoaderUtil
import mai.project.core.utils.Method
import mai.project.foodmap.databinding.ItemPersonalDataBinding

class PersonalDataAdapter : RecyclerView.Adapter<ViewHolder>() {

    private data class Model(val userImage: String, val username: String)

    private var item: Model = Model(userImage = "", username = "")

    var onImageClick: (() -> Unit)? = null

    fun submitModel(userImage: String, username: String) {
        this.item = Model(userImage = userImage, username = username)
        notifyItemChanged(0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        PersonalDataViewHolder.from(parent)

    override fun getItemCount(): Int = 1

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is PersonalDataViewHolder) holder.bind(item, onImageClick)
    }

    private class PersonalDataViewHolder(
        private val binding: ItemPersonalDataBinding,
    ) : ViewHolder(binding.root) {

        fun bind(
            item: Model,
            onImageClick: (() -> Unit)?
        ) = with(binding) {
            ImageLoaderUtil.loadImage(
                imageView = imgAvatar,
                resource = Method.decodeImage(item.userImage) ?: item.userImage,
                imageType = ImageType.PERSON
            )
            tvUsername.text = item.username.ifEmpty { "Not Found User..." }

            imgAvatar.onClick { onImageClick?.invoke() }
        }

        companion object {
            fun from(parent: ViewGroup) = PersonalDataViewHolder(
                parent.inflateBinding(ItemPersonalDataBinding::inflate)
            )
        }
    }
}