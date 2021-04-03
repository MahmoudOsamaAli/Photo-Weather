package com.example.photoweather.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.photoweather.R
import com.example.photoweather.databinding.FragmentShareOptionsDialogBinding
import com.example.photoweather.utils.DialogDismiss
import com.example.photoweather.viewModel.BaseViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ShareOptionsDialog : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentShareOptionsDialogBinding
    private lateinit var listener :DialogDismiss

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_share_options_dialog, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init(){
        binding.shareViaFacebook.setOnClickListener {
            listener.onDialogDismiss(binding.shareViaFacebook.id)
            dismiss()
        }
        binding.shareViaTwitter.setOnClickListener {
            listener.onDialogDismiss(binding.shareViaTwitter.id)
            dismiss()
        }
    }

    fun setListener(listener: DialogDismiss) {
        this.listener = listener
    }
}