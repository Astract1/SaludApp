package com.astract.saludapp

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CloudinaryManager {
    companion object {
        private var isInitialized = false

        fun init(context: Context) {
            if (!isInitialized) {
                val config = HashMap<String, String>()
                config["cloud_name"] = "dryqbjsoe"
                config["api_key"] = "773439922314385"
                config["api_secret"] = "Ns2cSvQ0EUp85C9XY9vttkXZJZA"

                MediaManager.init(context, config)
                isInitialized = true
            }
        }

        suspend fun uploadImage(uri: Uri): String = suspendCoroutine { continuation ->
            MediaManager.get().upload(uri).callback(object : UploadCallback {
                override fun onStart(requestId: String) {}

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val imageUrl = resultData["url"] as String
                    continuation.resume(imageUrl)
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    continuation.resumeWithException(Exception(error.description))
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            }).dispatch()
        }
    }
}
