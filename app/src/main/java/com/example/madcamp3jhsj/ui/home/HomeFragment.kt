package com.example.madcamp3jhsj.ui.home

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.madcamp3jhsj.BuildConfig
import com.example.madcamp3jhsj.databinding.FragmentHomeBinding

import com.google.ai.client.generativeai.GenerativeModel
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var photoFile: File
    private lateinit var generativeModel: GenerativeModel
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val captureButton = binding.buttonCapture
        captureButton.setOnClickListener {
            openCamera()
        }
        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        generativeModel = GenerativeModel(
            // The Gemini 1.5 models are versatile and work with most use cases
            modelName = "gemini-1.5-flash",
            // Access your API key as a Build Configuration variable (see "Set up your API key" above)
            apiKey = BuildConfig.apiKey
        )
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            photoFile = createImageFile() // ✅ Ensure photoFile is initialized **before** using it.
            val photoURI: Uri = FileProvider.getUriForFile(
                requireContext(),
                "com.example.madcamp20250103.fileprovider",
                photoFile
            )
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        } catch (ex: IOException) {
            Log.e("DashboardFragment", "❌ Error creating image file: ${ex.localizedMessage}")
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File(storageDir, "JPEG_${timeStamp}.jpg").apply {
            try {
                createNewFile() // ✅ Ensure file is created
                Log.d("DashboardFragment", "✅ Image file created: ${absolutePath}")
            } catch (e: IOException) {
                Log.e("DashboardFragment", "❌ Failed to create image file: ${e.localizedMessage}")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            // ✅ 사진 촬영 성공 후 작업 수행
            processCapturedPhoto()
        } else {
            Log.e("HomeFragment", "❌ Image capture failed or cancelled")
        }
    }

    private fun processCapturedPhoto() {

    }

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
    }
}