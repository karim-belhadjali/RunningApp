package com.example.runningapp.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.runningapp.R
import com.example.runningapp.other.Constants
import com.example.runningapp.other.Constants.ACTION_PAUSE_SERVICE
import com.example.runningapp.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.example.runningapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runningapp.other.Constants.ACTION_STOP_SERVICE
import com.example.runningapp.other.Constants.FASTEST_LOCATION_INTERVAL
import com.example.runningapp.other.Constants.LOCATION_UPDATE_INTERVAL
import com.example.runningapp.other.Constants.NOTIFICATION_CHANNEL_ID
import com.example.runningapp.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.runningapp.other.Constants.NOTIFICATION_ID
import com.example.runningapp.other.Constants.TIMER_UPDATE_INTERVAL
import com.example.runningapp.other.TrackingUtility
import com.example.runningapp.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.ArrayList
import javax.inject.Inject

//a list of cordinates that constitute one polyline
typealias Polyline = MutableList<LatLng>
// a list of many polylines
typealias Polylines = MutableList<Polyline>

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    var isFirstRun = true


    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var basedNotificationBuilder: NotificationCompat.Builder

    lateinit var curNotificationBuilder: NotificationCompat.Builder

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var notificationChannel: NotificationChannel


    private val timeRunInSeconds = MutableLiveData<Long>()

    companion object {
        val timeRunInMillis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        var serviceKilled = false

        // this line is equal to this but we replaced it with typealias -> val pathPoints = MutableLiveData<MutableList<MutableList<LatLng>>>()
        val pathPoints = MutableLiveData<Polylines>()
    }

    // THIS FUNCTION IS FOR RESTING ALL THE VALUES OR POSTING THE INITIAL VALUES WHEN THE SERVICE IS CALLED FOR THE FIRST TIME
    private fun postInitialValues() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
    }

    // IN THIS FUN WHEN THE SERVICE IS CREATING WE DECLARE OUR VARIABLES AND OBSERVE THE CHANGES ON THE TRACKING AND EXECUTING THA UPDATES BASED ON IT
    override fun onCreate() {
        super.onCreate()
        curNotificationBuilder = basedNotificationBuilder
        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        isTracking.observe(this, Observer {
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
        })
    }

    //THIS FUNCTION IS CALLED WHEN WE CALL THE SERVICE AND BASED ON THE ACTION PASSED INTO IT WE EXECUTE THE FUNCTION NEEDED
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.d("Resuming service...")
                        startTimer()
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("PAUSED service")
                    pauseService()

                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stoped service")
                    killService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimeStamp = 0L

    // THIS FUNCTION IS FOR STARTING THE STOPWATCH AND COUNTING THE TIME OF THE RUN AND POSTING THEM IN THE LIVEDATA
    private fun startTimer() {
        addEmptyPolyline()
        serviceKilled=false
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                // TIME DIFFRENCE BETWEEN NOW AND TIME STARTED
                lapTime = System.currentTimeMillis() - timeStarted
                //add the new laptime to the total time
                timeRunInMillis.postValue(timeRun + lapTime)
                if (timeRunInMillis.value!! >= lastSecondTimeStamp + 1000L) {
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimeStamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            timeRun += lapTime
        }
    }

    //THIS FUCNTION IS TO PAUSE THE SERVICE
    private fun pauseService() {
        isTracking.postValue(false)
        isTimerEnabled = true
    }

    //THIS FUNCTION IS TO STOP ALL THE SERVICE, DELETING ALL THE NOTIFICATION AND RESETTING ALL THE VALUES
    private fun killService() {
        serviceKilled = true
        isFirstRun = true
        pauseService()
        postInitialValues()
        stopForeground(true)
        stopSelf()
    }



    // THIS FUNCTION IS FOR UPDATING THE NOTIFICATION WITH THE BUTTON TO RESUME OR PAUSE THE RUN FROM THE NOTIFICATION
    private fun updateNotificationTrackingState(isTracking: Boolean) {
        val notificationActionText = if (isTracking) "Pause" else "Resume"
        val pendingIntent = if (isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, FLAG_UPDATE_CURRENT)
        } else {
            val resmueIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resmueIntent, FLAG_UPDATE_CURRENT)
        }


        // THIS CODE IS FOR CLEARING ALL THE ACTIONS FROM THE NOTIFICATION BEFORE UPDATING IT
        curNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(curNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }
        if (!serviceKilled) {
            curNotificationBuilder = basedNotificationBuilder
                .addAction(R.drawable.ic_pause_black_24dp, notificationActionText, pendingIntent)
            notificationManager.notify(NOTIFICATION_ID, curNotificationBuilder.build())
        }

    }

    // THIS FUNCTION IS FOR GETTING THE LOCATIONS UPDATES FROM THE GOOGLE MAP LIVE AND CALLING THE FUNCTION TO ADD THEM IN THE THE LISTS
    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hasLocationPermissions(this)) {
                val request = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallBack,
                    Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallBack)
        }
    }


    //we use this variable to add the new detected location to our list when we activate the Tracking service
    val locationCallBack = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)
            //the !! opreator test if the variable is null or not before we check the value of it
            if (isTracking.value!!) {
                result?.locations?.let {
                    for (location in it) {
                        addPathPoint(location)
                    }
                }
            }
        }
    }

    //THIS FUNCTION IS FOR EXTRACTING THE VALUE OF THE LOCATION AND ADDING THEM TO THE LAST INDEX OF THE LIST THAT CONSTITUTE A POLYLINE
    private fun addPathPoint(location: Location?) {
        location?.let {
            // this value represent the live location given to us by google map
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                // In here we're adding the postion giving to us to the last item in the polylines list which is a list of cordinates that consitute one polyline
                last().add(pos)
                pathPoints.postValue(this)
            }

        }
    }

    // ?: means if the value is null then it will execute the other one so in the end ?: is testing on nullabality
    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))


    // THIS WILL ALLOW
    private fun startForegroundService() {
            //THIS WILL START THE STOP WATCH AND CALCULATE THE RUNNING TIME AND SEND IT TO DISPLAY
            startTimer()
            //ENABLING OR DISABLING THE TRACKING TO LUNCH THE OBSERVERS AND START THE TRACKING AND DRAWING THE LINES AND ALSO CHANGING THE BUTTONS
            isTracking.postValue(true)


            //LAUNCHING THE NOTIFICATION WHEN STARTING THE SERVICE
            notificationManager.createNotificationChannel(notificationChannel)
            startForeground(NOTIFICATION_ID, basedNotificationBuilder.build())



                timeRunInSeconds.observe(this, Observer {
                    if (!serviceKilled) {
                        val notification = curNotificationBuilder
                            .setContentText(TrackingUtility.getFormattedStopWatchTime(it * 1000L))
                        notificationManager.notify(NOTIFICATION_ID, notification.build())
                    }
                })



    }




}