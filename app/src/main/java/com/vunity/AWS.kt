package com.vunity

import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CannedAccessControlList
import java.io.File


class AWS : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_home)

        val path = Environment.getExternalStorageDirectory().absolutePath
        Log.d("Files", "Path: $path")
        val f = File(path)
        val file = f.listFiles()
        Log.d("Files", "Size: " + file.size)
        for (i in file.indices) {
            Log.d("Files", "FileName:" + file[i].absolutePath)
        }
        val newFile = File("/storage/emulated/0/sample-video.mp4")

        AWSMobileClient.getInstance().initialize(this).execute()

        // KEY and SECRET are gotten when we create an IAM user above
        val credentials =
            BasicAWSCredentials("AKIAQXVJ7GKFF4Z4QS5T", "kIpmr8jcIOvmYpgrdVQDJKUaNdg5nQbeT6ROkOH/")
        val s3Client = AmazonS3Client(credentials)

        val transferUtility = TransferUtility.builder()
            .context(applicationContext)
            .awsConfiguration(AWSMobileClient.getInstance().configuration)
            .s3Client(s3Client)
            .build()

        // "Bunny" will be the folder that contains the file
        val uploadObserver = transferUtility.upload(
            "Bunny/" + "sample.mp4", newFile,
            CannedAccessControlList.PublicRead
        )

        uploadObserver.setTransferListener(object : TransferListener {

            override fun onStateChanged(id: Int, state: TransferState) {
                if (TransferState.COMPLETED == state) {
                    // Handle a completed upload.
                    Log.e("onStateChanged", state.toString())
                }
            }

            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                val percentDonef = bytesCurrent.toFloat() / bytesTotal.toFloat() * 100
                val percentDone = percentDonef.toInt()
                Log.e("onProgressChanged", "$percentDonef  $percentDone")
            }

            override fun onError(id: Int, ex: Exception) {
                // Handle errors
                Log.e("onError", ex.toString())
            }
        })

        // If your upload does not trigger the onStateChanged method inside your
        // TransferListener, you can directly check the transfer state as shown here.
        if (TransferState.COMPLETED == uploadObserver.state) {
            // Handle a completed upload.
            Log.e("TransferState", uploadObserver.state.toString())
        }
    }
}