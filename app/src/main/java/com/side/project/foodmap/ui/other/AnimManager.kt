package com.side.project.foodmap.ui.other

import android.content.Context
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.side.project.foodmap.R

enum class AnimState {
    Start, End, Repeat
}

class AnimManager(context: Context) {
    val smallToLarge: Animation = AnimationUtils.loadAnimation(context, R.anim.small_to_large)
    val largeToSmall: Animation = AnimationUtils.loadAnimation(context, R.anim.large_to_small)
    val largeToHide: Animation = AnimationUtils.loadAnimation(context, R.anim.large_to_hide)
    val hideToLarge: Animation = AnimationUtils.loadAnimation(context, R.anim.hide_to_large)

    val rotateOpen: Animation = AnimationUtils.loadAnimation(context, R.anim.rotate_open)
    val rotateClose: Animation = AnimationUtils.loadAnimation(context, R.anim.rotate_closs)

    val fromBottom: Animation = AnimationUtils.loadAnimation(context, R.anim.from_bottom)
    val toBottom: Animation = AnimationUtils.loadAnimation(context, R.anim.to_bottom)

    val fromTop: Animation = AnimationUtils.loadAnimation(context, R.anim.from_top)
    val toTop: Animation = AnimationUtils.loadAnimation(context, R.anim.to_top)

    val fromEnd: Animation = AnimationUtils.loadAnimation(context, R.anim.from_end)
    val toEnd: Animation = AnimationUtils.loadAnimation(context, R.anim.to_end)
}