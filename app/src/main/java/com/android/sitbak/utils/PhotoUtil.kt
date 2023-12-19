package com.android.sitbak.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.github.florent37.viewtooltip.BuildConfig
import java.io.File
import java.io.IOException
import java.net.URISyntaxException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PhotoUtil {
    internal var activity: Activity? = null
    internal var fragment: Fragment? = null

    var photoIntentCallBack: MediaIntentCallBack

    var imageFileName = ""
    private var mImageFileLocation = ""

    constructor(activity: Activity, photoIntentCallBack: MediaIntentCallBack) {
        this.activity = activity
        this.photoIntentCallBack = photoIntentCallBack

    }

    constructor(fragment: Fragment, photoIntentCallBack: MediaIntentCallBack) {
        this.fragment = fragment
        this.photoIntentCallBack = photoIntentCallBack

    }


    fun cameraImage(context: Context) {

        val callCameraApplicationIntent = Intent()
        callCameraApplicationIntent.action = MediaStore.ACTION_IMAGE_CAPTURE

        if (activity != null) {
            try {
                callCameraApplicationIntent.putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    FileProvider.getUriForFile(
                        activity!!,
                        "${BuildConfig.APPLICATION_ID}.fileprovider",
                        createImageFile()
                    )
                )
            } catch (e: IOException) {
                Log.e("IOException", "" + e)
                e.printStackTrace()
            }

            activity!!.startActivityForResult(
                callCameraApplicationIntent,
                ACTIVITY_START_CAMERA_APP
            )

        } else {
            try {

                callCameraApplicationIntent.putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    FileProvider.getUriForFile(
                        context.applicationContext,
                        "${BuildConfig.APPLICATION_ID}.fileprovider",
                        createImageFile()
                    )
                )
                fragment!!.startActivityForResult(
                    callCameraApplicationIntent,
                    ACTIVITY_START_CAMERA_APP
                )
            } catch (e: IOException) {
                Log.e("IOException", "" + e)
                e.printStackTrace()
            }

        }
    }

    fun launchRecordVideIntent(context: Context) {

        val callCameraApplicationIntent = Intent()
        callCameraApplicationIntent.putExtra(
            MediaStore.EXTRA_DURATION_LIMIT,
            30
        )
        callCameraApplicationIntent.action = MediaStore.ACTION_VIDEO_CAPTURE

        if (activity != null) {
            try {
                callCameraApplicationIntent.putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    FileProvider.getUriForFile(
                        activity!!,
                        "${BuildConfig.APPLICATION_ID}.fileprovider",
                        createVideoFile()
                    )
                )
            } catch (e: IOException) {
                Log.e("IOException", "" + e)
                e.printStackTrace()
            }

            activity!!.startActivityForResult(
                callCameraApplicationIntent,
                ACTIVITY_START_VIDEO_RECORDER
            )

        } else {
            try {
                callCameraApplicationIntent.putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    FileProvider.getUriForFile(
                        context.applicationContext,
                        "${BuildConfig.APPLICATION_ID}.fileprovider",
                        createVideoFile()
                    )
                )
                fragment!!.startActivityForResult(
                    callCameraApplicationIntent,
                    ACTIVITY_START_VIDEO_RECORDER
                )
            } catch (e: IOException) {
                Log.e("IOException", "" + e)
                e.printStackTrace()
            }

        }
    }

    @Throws(IOException::class)
    internal fun createImageFile(): File {

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        imageFileName = "IMAGE_" + timeStamp + "_"
        val storageDirectory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

        val image = File.createTempFile(imageFileName, ".jpg", storageDirectory)

        mImageFileLocation = Uri.fromFile(image).toString()

        return image
    }

    @Throws(IOException::class)
    internal fun createVideoFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        imageFileName = "VIDEO_" + timeStamp + "_"
        val storageDirectory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

        val image = File.createTempFile(imageFileName, ".mp4", storageDirectory)

        mImageFileLocation = Uri.fromFile(image).toString()

        return image
    }

    fun selectImageFromGallery() {

        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        if (fragment != null)
            fragment!!.startActivityForResult(photoPickerIntent, ACTIVITY_START_GALLERY)
        else
            activity!!.startActivityForResult(photoPickerIntent, ACTIVITY_START_GALLERY)
    }

    fun selectImageVideoFromGallery() {

        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/* video/*"
        if (fragment != null)
            fragment!!.startActivityForResult(photoPickerIntent, ACTIVITY_START_GALLERY)
        else
            activity!!.startActivityForResult(photoPickerIntent, ACTIVITY_START_GALLERY)
    }


    fun selectMultipleImageFromGallery() {

        val photoPickerIntent = Intent(Intent.ACTION_GET_CONTENT)
        photoPickerIntent.type = "image/*"
        photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        if (fragment != null)
            fragment!!.startActivityForResult(photoPickerIntent, ACTIVITY_START_GALLERY)
        else
            activity!!.startActivityForResult(photoPickerIntent, ACTIVITY_START_GALLERY)
    }

    fun selectMultipleImageVideosFromGallery() {

        val photoPickerIntent = Intent(Intent.ACTION_GET_CONTENT)
        photoPickerIntent.type = "image/* video/*"
        photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        if (fragment != null)
            fragment!!.startActivityForResult(photoPickerIntent, ACTIVITY_START_GALLERY)
        else
            activity!!.startActivityForResult(photoPickerIntent, ACTIVITY_START_GALLERY)
    }


    fun selectVideoFromGallery() {
        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "video/*"
        photoPickerIntent.action = Intent.ACTION_GET_CONTENT
        if (fragment != null)
            fragment!!.startActivityForResult(photoPickerIntent, ACTIVITY_START_GALLERY)
        else
            activity!!.startActivityForResult(photoPickerIntent, ACTIVITY_START_GALLERY)
    }

    fun handleGalleryIntent(context: Context, data: Intent) {

        val uri = data.data

        var imagePath: String? = null
        try {
            //  val file = pickedExistingPicture(context, uri)
            val file = getPath(context, uri)
            imagePath = file.toString()
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }

        try {
            if (imagePath != null) {
                photoIntentCallBack.onPhotoIntentSuccess(imagePath)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun handleMultpleImagesGalleryIntent(context: Context, data: Intent) {
        val imagesPathList = ArrayList<String>()
        try {
            val clipData = data.clipData
            if (clipData != null && clipData.itemCount > 0) {
                // In-Case of More than 1 Image is Selected
                for (i in 0 until clipData.itemCount) {
                    val item = clipData.getItemAt(i)
                    val itemUri = item.uri
                    try {
                        val imagePath = getPath(context, itemUri)
                        if (imagePath != null && imagePath.isNotEmpty())
                            imagesPathList.add(imagePath)
                    } catch (e: URISyntaxException) {
                        e.printStackTrace()
                    }
                }
            } else if (data.data != null) {
                // In-Case of Only 1 Image is Selected
                val uri = data.data
                var imagePath: String? = null
                try {
                    //  val file = pickedExistingPicture(context, uri)
                    val file = getPath(context, uri)
                    imagePath = file.toString()
                    imagesPathList.add(imagePath)
                } catch (e: URISyntaxException) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        photoIntentCallBack.onMultipleImagesSuccess(imagesList = imagesPathList)
    }


    fun handleAttachmentIntent(context: Context, data: Intent) {
        val uri = data.data

        var type = ""

        val attachment_type = (context as Activity).contentResolver?.getType(uri!!)

        type = if (attachment_type?.contains("image")!!) {
            "image"
        } else
            "video"

        var imagePath: String? = null
        try {
            //  val file = pickedExistingPicture(context, uri)
            val file = getPath(context, uri)
            imagePath = file.toString()
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }

        try {
            if (imagePath != null) {
                photoIntentCallBack.onMediaIntentSuccess(imagePath, type)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun handleCameraIntent(context: Context) {

        Log.d("KadiImage Path", "KadiImage Path :$mImageFileLocation")
        val myUri = Uri.parse(mImageFileLocation)
        var imagePath: String? = null
        try {
            imagePath = getPath(context, myUri)
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }


        try {

            if (imagePath != null) {
                photoIntentCallBack.onPhotoIntentSuccess(imagePath)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun handleVideoRecorderIntent(context: Context) {

        Log.d("KadiImage Path", "KadiImage Path :$mImageFileLocation")
        val myUri = Uri.parse(mImageFileLocation)
        var imagePath: String? = null
        try {
            imagePath = getPath(context, myUri)
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }


        try {

            if (imagePath != null) {
                photoIntentCallBack.onMediaIntentSuccess(imagePath, "video")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    companion object {

        val ACTIVITY_START_CAMERA_APP = 100
        val ACTIVITY_START_GALLERY = 200
        val ACTIVITY_START_VIDEO_RECORDER = 300

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is ExternalStorageProvider.
         */
        fun isExternalStorageDocument(uri: Uri): Boolean {
            return "com.android.externalstorage.documents" == uri.authority
        }

        /*
     * Gets the file path of the given Uri.
     */
        @SuppressLint("NewApi")
        @Throws(URISyntaxException::class)
        fun getPath(context: Context, paramUri: Uri?): String? {
            var uri = paramUri
            val needToCheckUri = Build.VERSION.SDK_INT >= 19
            var selection: String? = null
            var selectionArgs: Array<String>? = null
            // Uri is different in versions after KITKAT (Android 4.4), we need to
            // deal with different Uris.
            if (needToCheckUri && DocumentsContract.isDocumentUri(
                    context.applicationContext,
                    uri
                )
            ) {
                when {
                    isExternalStorageDocument(uri!!) -> {
                        val docId = DocumentsContract.getDocumentId(uri)
                        val split =
                            docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    }
                    isDownloadsDocument(uri) -> {
                        val id = DocumentsContract.getDocumentId(uri)
                        uri = ContentUris.withAppendedId(
                            Uri.parse("content://com.android.providers.downloads.documents/document"),
                            java.lang.Long.valueOf(id)
                        )
                    }
                    isMediaDocument(uri) -> {
                        val docId = DocumentsContract.getDocumentId(uri)
                        val split =
                            docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        val type = split[0]
                        if ("image" == type) {
                            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        } else if ("video" == type) {
                            uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        } else if ("audio" == type) {
                            uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        }
                        selection = "_id=?"
                        selectionArgs = arrayOf(split[1])
                    }
                }
            }
            if ("content".equals(uri!!.scheme!!, ignoreCase = true)) {
                val projection = arrayOf(MediaStore.Images.Media.DATA)
                val cursor: Cursor?
                try {
                    cursor = context.contentResolver.query(
                        uri,
                        projection,
                        selection,
                        selectionArgs,
                        null
                    )
                    val columnIndex = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    if (cursor.moveToFirst()) {
                        return cursor.getString(columnIndex)
                    }
                } catch (e: Exception) {
                    Log.d("Exception Throw", e.message.toString())
                }

            } else if ("file".equals(uri.scheme!!, ignoreCase = true)) {
                return uri.path
            }
            return null
        }


        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is DownloadsProvider.
         */
        fun isDownloadsDocument(uri: Uri): Boolean {
            return "com.android.providers.downloads.documents" == uri.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is MediaProvider.
         */
        fun isMediaDocument(uri: Uri): Boolean {
            return "com.android.providers.media.documents" == uri.authority
        }


    }
}

interface MediaIntentCallBack {
    fun onPhotoIntentSuccess(imageUri: String)
    fun onMediaIntentSuccess(Uri: String, type: String)
    fun onMultipleImagesSuccess(imagesList: ArrayList<String>)
}