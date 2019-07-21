package de.parkitny.fit.myfit.app.utils

import de.parkitny.fit.myfit.app.entities.Exercise
import de.parkitny.fit.myfit.app.entities.ExerciseConfiguration
import de.parkitny.fit.myfit.app.entities.ExerciseType
import de.parkitny.fit.myfit.app.entities.Routine
import de.parkitny.fit.myfit.app.ui.common.Utils

/**
 * Created by sebas on 04.12.2015.
 */
class ExerciseConfigurationView {

    /**
     * The id of the [ExerciseConfiguration]
     */
    var configId: Long = 0

    /**
     * The id of the [Exercise]
     */
    var exerciseId: Long = 0

    /**
     * The id of the routine
     */
    var routineId: Long = 0

    /**
     * The firstLine of the [Exercise]
     */
    var exerciseName: String? = null

    /**
     * the order of the [ExerciseConfiguration] within the [Routine]
     */
    var orderId: Int = 0

    /**
     * The number of repetitions
     */
    var repetitions: Int = 0

    /**
     * the duration of the [ExerciseConfiguration]
     */
    var duration: Long = 0

    /**
     * the [ExerciseType] of the [ExerciseConfiguration]
     */
    var exerciseType: ExerciseType? = null

    /**
     * the numbers of sets of the [ExerciseConfiguration]
     */
    var sets: Int = 0

    /**
     * the pause in ms
     */
    var pause: Long = 0

    /**
     * The current weight of the [Exercise]
     */
    var weight: Float = 0.toFloat()

    /**
     * id used to identify [ExerciseConfiguration] of the same set
     */
    var globalId: String? = null

    val readableString: String
        get() = if (exerciseType == ExerciseType.Repetition) {
            String.format("%s %s", repetitions, exerciseName)
        } else {
            String.format("%s %s", Utils.formatPeriod(duration), exerciseName)
        }
}
