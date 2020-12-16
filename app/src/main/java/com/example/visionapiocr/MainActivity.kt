package com.example.visionapiocr

import android.R
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.forEach
import androidx.fragment.app.FragmentTransaction
import com.example.visionapiocr.databinding.ActivityMainBinding
import com.example.visionapiocr.dialogs.DialogScanOptions
import com.example.visionapiocr.interfaces.IOptionSelected
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextRecognizer
import com.iceteck.silicompressorr.FileUtils
import com.iceteck.silicompressorr.SiliCompressor
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(), IOptionSelected {
    private lateinit var binding: ActivityMainBinding
    private lateinit var imageUri: Uri
    private lateinit var bitmap: Bitmap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.icScanner.setOnClickListener {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            fragmentTransaction.add(R.id.content, DialogScanOptions()).addToBackStack(null).commit()
        }
    }

    private fun scanReceipt(bitmap: Bitmap) {
        val textRecognizer = TextRecognizer.Builder(applicationContext).build()
        if (!textRecognizer.isOperational) {
            Toast.makeText(this, "Unable to scan receipt, please try again!", Toast.LENGTH_LONG)
                .show()
        } else {
            val frame = Frame.Builder().setBitmap(bitmap).build()
            val items = textRecognizer.detect(frame)
            val sb = StringBuilder()
            items.forEach { _, value ->
                sb.append(value.value)
                sb.append("\n")
            }
            binding.tvResult.text = sb.toString()
        }
    }

    private fun captureReceipt(code: Int) {
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val file = File(this.getExternalFilesDir(intent.type), createImageFile() + ".jpg")
        try {
            imageUri = Uri.fromFile(file)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            startActivityForResult(intent, code)
        } catch (e: ActivityNotFoundException) {
            Log.d(this.javaClass.simpleName, e.message.toString())
        }
    }

    private fun chooseReceipt(code: Int) {
        val choosePictureIntent = Intent(Intent.ACTION_GET_CONTENT)
        try {
            choosePictureIntent.type = "image/*"
            startActivityForResult(choosePictureIntent, code)
        } catch (e: ActivityNotFoundException) {
            Log.d(this.javaClass.simpleName, e.message.toString())
        }
    }

    override fun selectOption(option: Int) {
        when (option) {
            0 -> captureReceipt(option)
            1 -> chooseReceipt(option)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == 1 && resultCode == RESULT_OK) {
                imageUri = data!!.data!!
            } else {
                imageUri = compressReceipt(imageUri)
            }
            binding.icScanner.setImageURI(imageUri)
            bitmap = (binding.icScanner.drawable as BitmapDrawable).bitmap
            scanReceipt(bitmap)
        } catch (ex: Exception) {
            Toast.makeText(this, ex.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun createImageFile(): String {
        return "IMAGE_${SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())}"
    }

    private fun compressReceipt(path: Uri): Uri {
        lateinit var uri: Uri
        try {
            uri = Uri.fromFile(
                File(
                    SiliCompressor.with(this).compress(
                        FileUtils.getPath(this, path),
                        File(this.cacheDir, "temp")
                    )
                )
            )
        } catch (ex: Exception) {
            Log.d(this.javaClass.simpleName, ex.message.toString())
        }
        return uri
    }

}