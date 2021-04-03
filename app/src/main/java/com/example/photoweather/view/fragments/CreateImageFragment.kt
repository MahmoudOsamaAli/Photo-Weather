package com.example.photoweather.view.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.photoweather.viewModel.BaseViewModel
import com.example.photoweather.R
import com.example.photoweather.databinding.FragmentTakeImageBinding
import com.example.photoweather.network.models.Status
import com.example.photoweather.utils.AppUtils
import com.example.photoweather.utils.ImageUtil
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

class CreateImageFragment : Fragment() {

    private lateinit var viewModel: BaseViewModel
    private lateinit var binding: FragmentTakeImageBinding
    private var dialog: ProgressDialog = ProgressDialog()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_take_image, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[BaseViewModel::class.java]
        init()
    }

    private fun init() {
        requestTakeImage()
        dialog.isCancelable = false
        setToolBarConfig()
        setClickListeners()
    }

    private fun setToolBarConfig() {
        try {
            (activity as AppCompatActivity).setSupportActionBar(binding.createImageToolbar.toolbar)
            (activity as AppCompatActivity).supportActionBar?.title = ""
            (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
            binding.createImageToolbar.tvToolbarTitle.text =
                resources.getString(R.string.create_image)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.i(TAG, "updateItemsRV: " + e.message)
        }
    }

    private fun setClickListeners() {
        binding.takeAnotherImg.setOnClickListener { requestTakeImage() }

        binding.tokenImage.setOnClickListener {
            viewModel.selectedImageURL.value = viewModel.newImagePath
            val displayDlg = DisplayImageDialog()
            displayDlg.show(requireActivity().supportFragmentManager, displayDlg.tag)
        }

        binding.saveImage.setOnClickListener {
            if (!viewModel.newImagePath.isNullOrEmpty()) {
                AppUtils.makeToast(requireContext(), resources.getString(R.string.image_saved))
                viewModel.getAllImages()
                requireActivity().findNavController(R.id.nav_host_fragment)
                    .navigate(R.id.from_create_image_to_home)
            } else {
                AppUtils.makeToast(
                    requireContext(),
                    resources.getString(R.string.please_take_image_first)
                )
            }
        }
    }

    private fun requestTakeImage() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(requireContext().packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    ImageUtil.createImageFile()
                } catch (ex: IOException) {
                    AppUtils.makeToast(
                        requireContext(),
                        resources.getString(R.string.error_creatinr_file)
                    )
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    viewModel.newImagePath = photoFile.absolutePath
                    val photoURI: Uri = FileProvider.getUriForFile(
                        requireContext(),
                        "com.example.photoweather.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            MainScope().launch { handleResult() }
        } else {
            viewModel.deleteImage()
        }
    }

    private suspend fun handleResult() {
            MainScope().async { dialog.show(requireActivity().supportFragmentManager, dialog.tag) }.await()
            dialog.addText(resources.getString(R.string.getting_weather_conditions))
            viewModel.getWeatherConditions().observe(viewLifecycleOwner, {
                it?.let { resource ->
                    if (resource.status == Status.SUCCESS) {
                        viewModel.weatherConditions = resource.data
                        MainScope().launch { createImage() }
                    }else if (resource.status == Status.ERROR){
                        viewModel.deleteImage()
                        dialog.dismiss()
                        AppUtils.makeToast(requireContext() , resources.getString(R.string.failed_to_get_weather))
                    }
                }
            })
    }

    private suspend fun createImage(){
        dialog.addText(resources.getString(R.string.creating_weather_image))
        val drawTextToBitmap = ImageUtil.drawTextToBitmap(
            viewModel.weatherConditions,
            viewModel.newImagePath!!,
            requireContext()
        )
        binding.tokenImage.setImageBitmap(drawTextToBitmap)
        binding.tokenImage.visibility = View.VISIBLE
        dialog.dismiss()
        scanFile()
    }

    private fun scanFile() {
        /** Refresh scanning external storage to display newly
         * added file in case a usb cable plugged in and reviewing DCIM directory from a PC**/
        val scanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val contentUri = Uri.parse("file://${viewModel.newImagePath}")
        scanIntent.data = contentUri
        requireContext().sendBroadcast(scanIntent)
    }

    override fun onResume() {
        super.onResume()
        AppUtils.statusCheck(requireActivity())
    }

    companion object {
        private const val TAG = "TakeImageFragment"
        private const val REQUEST_IMAGE_CAPTURE = 1
    }
}