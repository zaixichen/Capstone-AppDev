package cn.edu.sjtu.Yuki666.kotlinChatter

import android.app.Activity
import android.content.ComponentName
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import cn.edu.sjtu.Yuki666.kotlinChatter.ChattStore.postChatt
import cn.edu.sjtu.Yuki666.kotlinChatter.databinding.ActivityPostBinding

class PostViewState : ViewModel() {
    var enableSend = true
    var imageUri: Uri? = null
    var videoUri: Uri? = null
    var videoIcon = android.R.drawable.presence_video_online
}

class PostActivity : AppCompatActivity() {

    private lateinit var view: ActivityPostBinding
    private val viewState: PostViewState by viewModels()
    private lateinit var forCropResult: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = ActivityPostBinding.inflate(layoutInflater)
        setContentView(view.root)

        view.videoButton.setImageResource(viewState.videoIcon)
        viewState.imageUri?.let { view.previewImage.display(it) }

        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            results.forEach {
                if (!it.value) {
                    toast("${it.key} access denied")
                    finish()
                }
            }
        }.launch(arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.READ_MEDIA_IMAGES,
            android.Manifest.permission.READ_MEDIA_VIDEO
        ))

        val cropIntent = initCropIntent()
        val forPickedResult =
            registerForActivityResult(ActivityResultContracts.GetContent(), fun(uri: Uri?) {
                uri?.let {
                    if (it.toString().contains("video")) {
                        viewState.videoUri = it
                        viewState.videoIcon = android.R.drawable.presence_video_busy
                        view.videoButton.setImageResource(viewState.videoIcon)
                    } else {
                        val inStream = contentResolver.openInputStream(it) ?: return
                        viewState.imageUri = mediaStoreAlloc("image/jpeg")
                        viewState.imageUri?.let { destUri ->
                            val outStream = contentResolver.openOutputStream(destUri) ?: return
                            val buffer = ByteArray(8192)
                            var read: Int
                            while (inStream.read(buffer).also { read = it } != -1) {
                                outStream.write(buffer, 0, read)
                            }
                            outStream.flush()
                            outStream.close()
                            inStream.close()
                        }
                        doCrop(cropIntent)
                    }
                } ?: run { Log.d("Pick media", "failed") }
            })
        view.albumButton.setOnClickListener {
            forPickedResult.launch("*/*")
        }

        forCropResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.data.let {
                        viewState.imageUri?.run {
                            if (!toString().contains("ORIGINAL")) {
                                contentResolver.delete(this, null, null)
                            }
                        }
                        viewState.imageUri = it
                        viewState.imageUri?.let { uri -> view.previewImage.display(uri) }
                    }
                } else {
                    Log.d("Crop", result.resultCode.toString())
                }
            }

        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            toast("Device has no camera!")
            return
        }

        val forTakePictureResult =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success) {
                    doCrop(cropIntent)
                } else {
                    Log.d("TakePicture", "failed")
                }
            }
        view.cameraButton.setOnClickListener {
            viewState.imageUri = mediaStoreAlloc("image/jpeg")
            forTakePictureResult.launch(viewState.imageUri)
        }

        val forCaptureVideoResult =
            registerForActivityResult(ActivityResultContracts.CaptureVideo()) {
                viewState.videoIcon = android.R.drawable.presence_video_busy
                view.videoButton.setImageResource(viewState.videoIcon)
            }
        view.videoButton.setOnClickListener {
            viewState.videoUri = mediaStoreAlloc("video/mp4")
            forCaptureVideoResult.launch(viewState.videoUri)
        }
    }

    private fun initCropIntent(): Intent? {
        val intent = Intent("com.android.camera.action.CROP")
        intent.type = "image/*"
        val listofCroppers = packageManager.queryIntentActivities(intent, 0)
        if (listofCroppers.size == 0) {
            toast("Device does not support image cropping")
            return null
        }

        intent.component = ComponentName(
            listofCroppers[0].activityInfo.packageName,
            listofCroppers[0].activityInfo.name
        )

        intent.putExtra("outputX", 500)
            .putExtra("outputY", 500)
            .putExtra("aspectX", 1)
            .putExtra("aspectY", 1)
            .putExtra("scale", true)
            .putExtra("crop", true)
            .putExtra("return-data", true)

        return intent
    }

    private fun doCrop(intent: Intent?) {
        intent ?: run {
            viewState.imageUri?.let { view.previewImage.display(it) }
            return
        }

        viewState.imageUri?.let {
            intent.data = it
            forCropResult.launch(intent)
        }
    }

    private fun mediaStoreAlloc(mediaType: String): Uri? {
        val values = ContentValues()
        values.put(MediaStore.MediaColumns.MIME_TYPE, mediaType)
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)

        return contentResolver.insert(
            if (mediaType.contains("video"))
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            else
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.post_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_send).isEnabled = viewState.enableSend
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_send) {
            viewState.enableSend = false
            submitChatt()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun submitChatt() {
        val chatt = Chatt(
            username = view.usernameTextView.text.toString(),
            message = view.messageTextView.text.toString()
        )

        postChatt(applicationContext, chatt, viewState.imageUri, viewState.videoUri) { msg ->
            runOnUiThread {
                toast(msg)
            }
            finish()
        }
    }
}
