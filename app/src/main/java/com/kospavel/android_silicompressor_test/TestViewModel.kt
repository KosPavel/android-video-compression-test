package com.kospavel.android_silicompressor_test

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iceteck.silicompressorr.SiliCompressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class TestViewModel(private val context: Context) : ViewModel() {

    private val _recordedUri = MutableLiveData<Uri>()
    val recordedUri: LiveData<Uri> = _recordedUri

    private val _compressedUri = MutableLiveData<Uri>()
    val compressedUri: LiveData<Uri> = _compressedUri

    private val _toastMessages = MutableLiveData<String>()
    val toastMessages: LiveData<String> = _toastMessages

    fun setRecorderUri(uri: Uri, type: String?) {
        viewModelScope.launch {
            createFileFromUri(uri, type)
        }
        _recordedUri.postValue(uri)
    }

    private suspend fun createFileFromUri(uri: Uri, type: String?) {

        //открытие inputstream и создание файла из него
        val fileInputStream = context.contentResolver.openInputStream(uri)
        val file = fileInputStream?.let { inputStreamToFile(it) }
        val fileSize = (file?.length() ?: 0).toFloat()
//        _toastMessages.postValue("file size: ${fileSize / 1000000} MB, type: $type")

        file?.let {
            val params = getVideoParameters(it.toUri())
            _toastMessages.postValue("raw height: ${params.second}, width: ${params.first}")
        }

        viewModelScope.launch {
            file?.let { recordedFile ->
                val recordedFileUri = recordedFile.absoluteFile.toUri()
                val compressedFile = compressUsingSili(recordedFileUri, 480, 320)
                Log.i("qwerty", "compressed path = ${compressedFile.path}")
                val params = getVideoParameters(compressedFile.toUri())
                _toastMessages.postValue("compressed height: ${params.second}, width: ${params.first}")

                val compressedSize = compressedFile.length().toFloat()
//                _toastMessages.postValue("compressed size: ${compressedSize / 1000000} MB")
                _compressedUri.postValue(compressedFile.toUri())
            }
        }
    }

    private fun inputStreamToFile(input: InputStream): File {
        input.use {
            val file = File(context.cacheDir, "recordedFile.mp4")
            FileOutputStream(file).use { output ->
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (it.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                }
                output.flush()
            }
            return file
        }
    }

    private suspend fun compressUsingSili(videoUri: Uri, width: Int = 0, height: Int = 0, bitrate: Int = 0): File {
        return withContext(Dispatchers.IO) {
            File(SiliCompressor.with(context).compressVideo(
                videoUri,
                context.cacheDir.path,
                width,
                height,
                bitrate
            ))
        }
    }

    private fun getVideoParameters(uri: Uri): Pair<String?, String?> {
        val mediaRetriever = MediaMetadataRetriever()
        mediaRetriever.setDataSource(context, uri)
        val height = mediaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
        val width = mediaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
        return Pair(width, height)
    }

}