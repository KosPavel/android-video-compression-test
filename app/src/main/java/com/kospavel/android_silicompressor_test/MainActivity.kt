package com.kospavel.android_silicompressor_test

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject


class MainActivity : AppCompatActivity() {

    private val vm: TestViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnRecord.setOnClickListener {
            dispatchTakeVideoIntent()
        }

        btnOpenGallery.setOnClickListener {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "*/*"
//            photoPickerIntent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
            photoPickerIntent.putExtra(Intent.EXTRA_MIME_TYPES,"video/*")
            startActivityForResult(photoPickerIntent, REQUEST_VIDEO_GALLERY)
        }

        vm.recordedUri.observe(this, {
            vvRaw.setVideoURI(it)
            btnStartRecorded.setOnClickListener {
                vvRaw.start()
            }
        })

        vm.compressedUri.observe(this, {
            vvCompressed.setVideoURI(it)
            Toast.makeText(
                this,
                "compressed type: ${contentResolver.getType(it)}",
                Toast.LENGTH_SHORT
            ).show()
            btnStartCompressed.setOnClickListener {
                vvCompressed.start()
            }
        })

        vm.toastMessages.observe(this, {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        })
    }

    private fun dispatchTakeVideoIntent() {
        Intent(MediaStore.ACTION_VIDEO_CAPTURE).also { takeVideoIntent ->
            takeVideoIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_VIDEO_CAPTURE -> {
                    data?.data?.let { contentUri ->
                        val type = contentResolver.getType(contentUri)
                        vm.setRecorderUri(contentUri, type)
                    }
                }
                REQUEST_VIDEO_GALLERY -> {
                    data?.data?.let { contentUri ->
                        val type = contentResolver.getType(contentUri)
                        vm.setRecorderUri(contentUri, type)
                    }
                }
            }
        }
    }

    companion object {
        const val REQUEST_VIDEO_CAPTURE = 1
        const val REQUEST_VIDEO_GALLERY = 2
    }

}