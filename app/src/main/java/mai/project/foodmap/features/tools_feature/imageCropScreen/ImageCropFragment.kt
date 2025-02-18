package mai.project.foodmap.features.tools_feature.imageCropScreen

import android.os.Bundle
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.canhub.cropper.CropImageView
import dagger.hilt.android.AndroidEntryPoint
import mai.project.core.extensions.displayToast
import mai.project.core.extensions.onClick
import mai.project.core.utils.Method
import mai.project.foodmap.R
import mai.project.foodmap.base.BaseFragment
import mai.project.foodmap.databinding.FragmentImageCropBinding

@AndroidEntryPoint
class ImageCropFragment : BaseFragment<FragmentImageCropBinding, Nothing>(
    bindingInflater = FragmentImageCropBinding::inflate
) {
   private val args by navArgs<ImageCropFragmentArgs>()

    override fun FragmentImageCropBinding.initialize(savedInstanceState: Bundle?) {
        with(cropImageView) {
            setImageUriAsync(args.imagePath.toUri())
            if (args.isCircle) {
                cropShape = CropImageView.CropShape.OVAL
                setAspectRatio(1, 1)
            }
            guidelines = CropImageView.Guidelines.ON
        }
    }

    override fun FragmentImageCropBinding.setListener() {
        imgBack.onClick { popBackStack() }

        imgChecked.onClick {
            val cropped = cropImageView.getCroppedImage()
            val base64 = Method.encodeImage(cropped)
            cropped?.recycle()
            if (base64 != null) {
                setFragmentResult(
                    args.requestCode,
                    bundleOf(args.requestCode to base64.toString())
                )
                navigateUp()
            } else {
                displayToast(message = getString(R.string.sentence_crop_image_failed))
                navigateUp()
            }
        }
    }
}