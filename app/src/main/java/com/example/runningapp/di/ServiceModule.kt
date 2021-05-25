package com.example.runningapp.di

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.runningapp.R
import com.example.runningapp.other.Constants
import com.example.runningapp.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    // THIS WILL INJECT THE LOCATION PROVIDER THAT LET US TRACK THE USER AND RETURN TO US THE VALUES OF LOCATIONS
    @ServiceScoped
    @Provides
    fun provideFusedLocationProviderClient(
        @ApplicationContext app: Context
    ) = FusedLocationProviderClient(app)


    // THIS WILL INJECT THE PENDING INTENT THA CONNECT US TO THE TRACKING FRAGMENT WHEN WE CLICK ON THE NOTIFICATION
    @ServiceScoped
    @Provides
    fun provideMainActivityPendingIntent(
        @ApplicationContext app: Context
    ) = PendingIntent.getActivity(
        app,
        0,
        Intent(app, MainActivity::class.java).also {
            it.action = Constants.ACTION_SHOW_TRACKING_FRAGMENT
        },
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    // THIS WILL INJECT THE NOTIFICATION BUILDER THAT ALLOW US TO BUILD THE TRACKING NOTIFICATION
    @ServiceScoped
    @Provides
    fun provideBasedNotificationBuilder(
        @ApplicationContext app: Context,
        pendingIntent: PendingIntent
    ) = NotificationCompat.Builder(app, Constants.NOTIFICATION_CHANNEL_ID)
        .setAutoCancel(false)
        .setOngoing(true)
        .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
        .setContentTitle("Running App")
        .setContentText("00:00:00")
        .setContentIntent(pendingIntent)

    //LAUNCHING THE NOTIFICATION WHEN STARTING THE SERVICE
    @ServiceScoped
    @Provides
    fun provideNotificationManager(
        @ApplicationContext app: Context
    )=  app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    //LAUNCHING THE NOTIFICATION WHEN STARTING THE SERVICE
    @ServiceScoped
    @Provides
    fun provideNotificationChannel(
        notificationManager: NotificationManager
    )=NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_ID,
            Constants.NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )



}