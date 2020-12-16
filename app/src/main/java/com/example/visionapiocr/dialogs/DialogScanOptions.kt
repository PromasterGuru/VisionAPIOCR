package com.example.visionapiocr.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.visionapiocr.databinding.DialogScanOptionsBinding
import com.example.visionapiocr.interfaces.IOptionSelected

class DialogScanOptions : DialogFragment() {
    private lateinit var binding: DialogScanOptionsBinding
    private lateinit var selectedOption: IOptionSelected

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogScanOptionsBinding.inflate(layoutInflater)

        selectedOption = requireActivity() as IOptionSelected

        binding.tvCamera.setOnClickListener {
            selectedOption.selectOption(0)
            dismiss()
        }

        binding.tvChoose.setOnClickListener {
            selectedOption.selectOption(1)
            dismiss()
        }

        return binding.root
    }


}