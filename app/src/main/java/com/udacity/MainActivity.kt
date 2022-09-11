package com.udacity

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action
    private val NOTIFICATION_ID = 0
    private var fileName = ""
    private var downloadStatus = ""
    private lateinit var radioGroup: RadioGroup
    private lateinit var radioButton: RadioButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        radioGroup = findViewById(R.id.radio_parent)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        custom_button.setOnClickListener {

            val selectedItem: Int = radioGroup.checkedRadioButtonId
            // if select no radio button
            if (radioGroup.checkedRadioButtonId == -1) {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.toast_msg),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                custom_button.setLoadingBTNState(ButtonState.Loading)
                radioButton = findViewById(selectedItem)
                onRadioButtonClicked(radioButton)
            }
        }
        createChannel(
            getString(R.string.download_notification_channel_id),
            getString(R.string.notification_description)
        )
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager

            notificationManager = ContextCompat.getSystemService(
                application,
                NotificationManager::class.java
            ) as NotificationManager

            val query: DownloadManager.Query = DownloadManager.Query()
            query.setFilterById(id!!) // filter your download due download Id
            val cursor: Cursor = downloadManager.query(query)
            if (cursor.moveToFirst()) {
                var status = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                status = cursor.getInt(status)
                cursor.close()
                when (status) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        downloadStatus = getString(R.string.success)
                    }
                    DownloadManager.STATUS_FAILED -> downloadStatus = getString(R.string.fail)
                }

                //send the Notification
                notificationManager.sendNotification(
                    application.getString(R.string.notification_description),
                    applicationContext
                )
            }
        }
    }

    private fun download(url: String) {
        val request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    companion object {
        private const val URL_UDACITY =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter"
        private const val URL_GLIDE =
            "https://github.com/bumptech/glide"
        private const val URL_RETROFIT =
            "https://github.com/square/retrofit"
    }

    private fun onRadioButtonClicked(view: View) {
        // Check which radio button was clicked
        when (view.id) {
            R.id.radio_glide -> {
                download(URL_GLIDE)
                fileName = getString(R.string.glideTitle)
            }
            R.id.radio_udacity -> {
                download(URL_UDACITY)
                fileName = getString(R.string.udacityTitle)
            }
            R.id.radio_retrofit -> {
                download(URL_RETROFIT)
                fileName = getString(R.string.retrofitTitle)
            }
        }
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
                .apply {
                    setShowBadge(false)
                }

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = getString(R.string.notification_description)

            val notificationManager = applicationContext.getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    fun NotificationManager.sendNotification(messageBody: String, applicationContext: Context) {
        val contentIntent = Intent(applicationContext, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            applicationContext,
            NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val resultIntent = Intent(applicationContext, DetailActivity::class.java)
        resultIntent.putExtra("fileName", fileName)
        resultIntent.putExtra("status", downloadStatus)
        pendingIntent = TaskStackBuilder.create(applicationContext).run {
            // Add the intent, which inflates the back stack
            addNextIntentWithParentStack(resultIntent)
            // Get the PendingIntent containing the entire back stack
            getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val builder = NotificationCompat.Builder(
            applicationContext,
            applicationContext.getString(R.string.download_notification_channel_id)
        )
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(
                applicationContext
                    .getString(R.string.notification_title)
            )
            .setContentText(messageBody)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_assistant_black_24dp,
                applicationContext.getString(R.string.check_status),
                pendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
        notify(NOTIFICATION_ID, builder.build())
    }
}