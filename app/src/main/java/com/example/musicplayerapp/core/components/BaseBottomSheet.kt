package com.example.musicplayerapp.core.components

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.viewbinding.ViewBinding
import com.example.musicplayerapp.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.shape.MaterialShapeDrawable

class BaseBottomSheet<T: ViewBinding>(
    private val bindingInflater : ((LayoutInflater, ViewGroup?, Boolean) -> T),
    private val onViewCreated: ((T, BaseBottomSheet<T>) -> Unit)? = null
) : BottomSheetDialogFragment() {

    private var _binding: T? = null
    private val binding get() = _binding!!

    override fun getTheme(): Int = R.style.AppBottomSheet
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = true
    }

    override fun onStart() {
        super.onStart()
        dialog?.setCanceledOnTouchOutside(true) // 🔥 ensure outside click works
        val bottomSheet = dialog?.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet
        )
        val bg = bottomSheet?.background

        if (bg is MaterialShapeDrawable) {
            bg.fillColor = ColorStateList.valueOf(Color.YELLOW)
        }

        ViewCompat.setBackgroundTintList(view ?: return, null)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = bindingInflater.invoke(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onViewCreated?.invoke(binding,this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}