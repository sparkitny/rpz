package de.parkitny.fit.myfit.app.events

import de.parkitny.fit.myfit.app.entities.ExerciseType

/**
 * Created by Sebastian on 01.04.2016.
 */
class TimeUpdateEvent {

    var totalTime: String? = null
    var exerciseTime: String? = null
    var countdownExerciseTime: String? = null
    var pauseTotalTime: String? = null
    var pauseExerciseTime: String? = null
    var afterExercisePauseTime: String? = null
    var exerciseType: ExerciseType? = null
}
