package com.side.project.foodmap.ui.fragment.launch

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.side.project.foodmap.R
import com.side.project.foodmap.data.local.User
import com.side.project.foodmap.databinding.FragmentLoginBinding
import com.side.project.foodmap.helper.displayShortToast
import com.side.project.foodmap.ui.activity.MainActivity
import com.side.project.foodmap.ui.viewModel.LoginViewModel
import com.side.project.foodmap.ui.other.AnimManager
import com.side.project.foodmap.util.Method.logE
import com.side.project.foodmap.util.Resource
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModel()
    private val animManager: AnimManager by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        doInitialize()
        setListener()
    }

    private fun doInitialize() {
        lifecycleScope.launchWhenCreated {
            viewModel.loginState.collect {
                when (it) {
                    is Resource.Loading -> {
                        logE("Login", "Loading")
                        binding.btnStart.startAnimation()
                    }
                    is Resource.Success -> {
                        logE("Login", "Success")
                        binding.btnStart.revertAnimation()
                        requireActivity().displayShortToast(getString(R.string.hint_login_success))
                        Intent(requireActivity(), MainActivity::class.java).also { intent ->
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                    }
                    is Resource.Error -> {
                        logE("Login", "Error:${it.message.toString()}")
                        binding.btnStart.revertAnimation()
                        requireActivity().displayShortToast(getString(R.string.hint_login_error))
                        setEditTextVisibility(true)
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun setListener() {
        binding.run {
            val anim = animManager.smallToLarge
            btnStart.setOnClickListener {
                if (edName.text.toString().trim().isEmpty()) {
                    edName.error = getString(R.string.hint_please_enter_name)
                    return@setOnClickListener
                }
                it.startAnimation(anim)
            }
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(p0: Animation?) {
                    setEditTextVisibility(false)
                }

                override fun onAnimationEnd(p0: Animation?) {
                    val user = User(
                        edName.text.toString().trim()
                    )
                    viewModel.anonymityLogin(user)
                }

                override fun onAnimationRepeat(p0: Animation?) {}
            })
        }
    }

    private fun setEditTextVisibility(isVisibility: Boolean) {
        binding.run {
            if (isVisibility) {
                edName.visibility = View.VISIBLE
                return
            }
            val anim = animManager.largeToHide
            edName.startAnimation(anim)
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(p0: Animation?) {}

                override fun onAnimationEnd(p0: Animation?) {
                    edName.visibility = View.GONE
                }

                override fun onAnimationRepeat(p0: Animation?) {}
            })
        }
    }
}