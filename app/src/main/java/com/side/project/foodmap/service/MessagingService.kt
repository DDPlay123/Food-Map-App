package com.side.project.foodmap.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.side.project.foodmap.util.tools.Method

class MessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Method.logE("Firebase Token", token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Method.logE("Firebase Message", message.notification?.body.toString())
    }
}