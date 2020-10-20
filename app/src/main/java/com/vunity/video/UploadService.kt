package com.vunity.video

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vunity.R
import com.vunity.general.Constants.SUCCESS_NOTIFICATION_ID
import com.vunity.general.Constants.UPLOAD_NOTIFICATION_ID
import com.vunity.general.Home
import com.vunity.server.RetrofitClient
import com.vunity.user.ErrorMsgDto
import com.vunity.user.ResDto
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File


class UploadService : Service() {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    lateinit var notificationManager: NotificationManager
    lateinit var notificationBuilder: NotificationCompat.Builder
    lateinit var notification: Notification
    lateinit var imagePath: String
    lateinit var videoPath: String
    lateinit var textField: String
    lateinit var fileName: String

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        createNotificationChannel()
        if (intent.extras != null) {
            imagePath = intent.extras?.get("imagePath") as String
            videoPath = intent.extras?.get("videoPath") as String
            textField = intent.extras?.get("textField") as String
        }
        uploadVideo()
        val pendingIntent: PendingIntent =
            Intent(this, Home::class.java).let { notificationIntent ->
                notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                PendingIntent.getActivity(
                    this,
                    0,
                    notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationBuilder = NotificationCompat.Builder(this, getString(R.string.channel_id))

        notificationBuilder.setOngoing(true)
            .setContentTitle("Videos are uploading, please wait...")
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setContentIntent(pendingIntent)
            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
            .setProgress(100, 0, false)

        notification = notificationBuilder.build()
        notificationManager.notify(UPLOAD_NOTIFICATION_ID, notification)
        startForeground(UPLOAD_NOTIFICATION_ID, notification)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true)
        }
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        stopForeground(true)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                getString(R.string.channel_id),
                getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)!!
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun uploadVideo() {
        val newFile = File(videoPath)
        AWSMobileClient.getInstance().initialize(this).execute()

        // KEY and SECRET are gotten when we create an IAM user above
        val credentials =
            BasicAWSCredentials(getString(R.string.access_key), getString(R.string.secret_key))
        val s3Client = AmazonS3Client(credentials)
        val transferUtility = TransferUtility.builder()
            .context(applicationContext)
            .awsConfiguration(AWSMobileClient.getInstance().configuration)
            .s3Client(s3Client)
            .build()

        // "videos" will be the folder that contains the file
        fileName = System.currentTimeMillis().toString() + ".mp4"
        val uploadObserver = transferUtility.upload(
            "videos/$fileName", newFile,
            CannedAccessControlList.PublicRead
        )

        uploadObserver.setTransferListener(object : TransferListener {

            override fun onStateChanged(id: Int, state: TransferState) {
                if (TransferState.COMPLETED == state) {
                    createVideo()
                }
            }

            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                val percentDonef = bytesCurrent.toFloat() / bytesTotal.toFloat() * 100
                notificationBuilder.setProgress(100, percentDonef.toInt(), false)
                notification = notificationBuilder.build()
                notificationManager.notify(UPLOAD_NOTIFICATION_ID, notification)
            }

            override fun onError(id: Int, ex: Exception) {
                notifier("Video not uploaded", ex.toString())
            }
        })

        // If your upload does not trigger the onStateChanged method inside your
        // TransferListener, you can directly check the transfer state as shown here.
        if (TransferState.COMPLETED == uploadObserver.state) {
            // Handle a completed upload.
            Log.e("TransferState", uploadObserver.state.toString())
        }
    }

    fun createVideo() {
        try {
            val imageFile = File(imagePath)
            val imageReqBody = RequestBody.create(MediaType.parse("video/*"), imageFile)
            val imagePart: MultipartBody.Part =
                MultipartBody.Part.createFormData("video-thumb", imageFile.name, imageReqBody)
            val jsonAdapter: JsonAdapter<ReqVideoBody> =
                moshi.adapter(ReqVideoBody::class.java)
            val oldBody: ReqVideoBody? = jsonAdapter.fromJson(textField)
            val newBody = oldBody?.copy(content = fileName)
            val json: String = jsonAdapter.toJson(newBody)
            val text = RequestBody.create(MediaType.parse("text/plain"), json)

            val create = RetrofitClient.videoClient.addVideo(thumbnail = imagePart, text = text)
            create.enqueue(object : Callback<ResDto> {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(call: Call<ResDto>, response: Response<ResDto>) {
                    if (response.code() == 200) {
                        when (response.body()?.status) {
                            200 -> {
                                notifier("Video uploaded", "Video has been uploaded successfully")
                            }
                            else -> {
                                notifier("Video not uploaded", response.message())
                            }
                        }

                    } else if (response.code() == 422 || response.code() == 400) {
                        try {
                            val moshi: Moshi = Moshi.Builder().build()
                            val adapter: JsonAdapter<ErrorMsgDto> =
                                moshi.adapter(ErrorMsgDto::class.java)
                            val errorResponse =
                                adapter.fromJson(response.errorBody()!!.string())
                            if (errorResponse != null) {
                                if (errorResponse.status == 400) {
                                    notifier("Video not uploaded", errorResponse.message)
                                } else {
                                    notifier("Video not uploaded", errorResponse.message)
                                }
                            } else {
                                notifier(
                                    "Video not uploaded",
                                    getString(R.string.msg_something_wrong)
                                )
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            notifier("Video not uploaded", getString(R.string.msg_something_wrong))
                        }
                    } else {
                        notifier("Video not uploaded", response.message())
                    }
                }

                override fun onFailure(call: Call<ResDto>, t: Throwable) {
                    notifier("Video not uploaded", getString(R.string.msg_something_wrong))
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun notifier(title: String, subText: String) {
        stopSelf()
        stopForeground(true)
        val intent = Intent(applicationContext, Home::class.java)
        val contentIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, getString(R.string.channel_id))
        builder.setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setOngoing(false)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setContentTitle(title)
            .setContentText(subText)
            .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_SOUND)
            .setContentIntent(contentIntent)
            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification: Notification = builder.build()
        notificationManager.notify(SUCCESS_NOTIFICATION_ID, notification)
    }
}