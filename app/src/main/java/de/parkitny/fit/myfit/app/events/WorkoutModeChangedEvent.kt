package de.parkitny.fit.myfit.app.events

/**
 * Created by Sebastian on 29.06.2016.
 */
class WorkoutModeChangedEvent {

    private val timeStamp: Long

    init {
        timeStamp = System.currentTimeMillis()
    }
}
