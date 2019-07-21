package de.parkitny.fit.myfit.app.events

import de.parkitny.fit.myfit.app.entities.ExerciseType

/**
 * Created by Sebastian on 10.03.2016.
 */
class BroadcastExerciseEvent {

    var exerciseConfigurationId: Long = -1
    var orderId = -1
    var message: String? = null
    var time: String? = null
    var workoutName: String? = null
    var exerciseName: String? = null
    var exerciseType: ExerciseType? = null
    var currentRound: Int = 0
    var totalRounds: Int = 0
    var currentSet: Int = 0
    var totalSets: Int = 0
}
