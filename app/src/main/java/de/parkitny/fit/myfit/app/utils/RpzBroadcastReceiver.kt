package de.parkitny.fit.myfit.app.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.parkitny.fit.myfit.app.events.NextExerciseEvent
import org.greenrobot.eventbus.EventBus

class RpzBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        when (intent.action) {
            ACTION_NEXT -> EventBus.getDefault().post(NextExerciseEvent(true))
        }
    }

    companion object {
        const val ACTION_NEXT = "nextExercise"
    }
}
