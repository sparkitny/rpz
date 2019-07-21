package de.parkitny.fit.myfit.app.database

import android.content.Context
import de.parkitny.fit.myfit.app.R
import de.parkitny.fit.myfit.app.RpzApplication
import de.parkitny.fit.myfit.app.domain.RoutineHolder
import de.parkitny.fit.myfit.app.entities.Exercise
import de.parkitny.fit.myfit.app.entities.ExerciseConfiguration
import de.parkitny.fit.myfit.app.entities.ExerciseEmphasis
import de.parkitny.fit.myfit.app.entities.ExerciseType
import de.parkitny.fit.myfit.app.ui.common.Utils
import mu.KotlinLogging

/**
 * Created by Sebastian on 19.10.2017.
 */
class Initializer {

    companion object {

        private val exerciseIds = arrayOf(
                Pair(R.string.chest_press, "chest_press"),
                Pair(R.string.fly, "fly"),
                Pair(R.string.ab_roller, "ab_roller"),
                Pair(R.string.bizeps_curls, "bizeps_curls"),
                Pair(R.string.hyperextension, "hyperextension"),
                Pair(R.string.one_handed_row, "one_handed_row"),
                Pair(R.string.reverse_fly, "reverse_fly"),
                Pair(R.string.squats, "squats"),
                Pair(R.string.russian_twist, "russian_twist"),
                Pair(R.string.shoulder_press, "shoulder_press"),
                Pair(R.string.trizeps_extension, "trizeps_extension"),
                Pair(R.string.u_form, "u_form"),
                Pair(R.string.push_ups, "push_ups"),
                Pair(R.string.pull_ups, "pull_ups"),
                Pair(R.string.deadlift, "deadlift"),
                Pair(R.string.sit_ups, "sit_ups"),
                Pair(R.string.burpees, "burpess")
        )

        private val logger = KotlinLogging.logger { }

        private val exerciseDao = RpzApplication.DB.exerciseDao()
        private val exerciseEmphasisDao = RpzApplication.DB.exerciseEmphasisDao()

        fun initializeDatabase(context: Context) {

            exerciseIds.forEach { item -> getOrCreateExercise(item.second, item.first, context) }

            insertAphroditeWorkout(context)
        }

        fun insertAphroditeWorkout(context: Context) {

            val afrodieter = RoutineHolder()
            afrodieter.newRoutine("Afrodieter")

            val burpees = getOrCreateExercise("burpess", R.string.burpees, context)
            val squats = getOrCreateExercise("squats", R.string.squats, context)
            val situps = getOrCreateExercise("sit_ups", R.string.sit_ups, context)

            for (i in 50 downTo 10 step 10) {

                logger.debug("i " + i)

                val burpeeConfiguration = ExerciseConfiguration()

                burpeeConfiguration.exerciseType = ExerciseType.Repetition
                burpeeConfiguration.repetitions = i
                burpeeConfiguration.sets = 1

                afrodieter.addExercise(burpees.id, burpeeConfiguration)

                val squatsConfiguration = ExerciseConfiguration()

                squatsConfiguration.exerciseType = ExerciseType.Repetition
                squatsConfiguration.repetitions = i
                squatsConfiguration.sets = 1

                afrodieter.addExercise(squats.id, squatsConfiguration)

                val situpsConfiguration = ExerciseConfiguration()

                situpsConfiguration.exerciseType = ExerciseType.Repetition
                situpsConfiguration.repetitions = i
                situpsConfiguration.sets = 1

                afrodieter.addExercise(situps.id, situpsConfiguration)

                afrodieter.setRoutineRounds(1)
            }

            afrodieter.storeCompleteRoutine()
        }

        private fun getOrCreateExercise(key: String, resId: Int, context: Context): Exercise {

            var exercise = exerciseDao.getExerciseByHiddenKey(key)

            if (exercise == null) {

                val converter = Converters()

                val exerciseInfo = context.getString(resId)

                val splits = exerciseInfo.split("|")

                val name = splits[0].trim()
                val emphasisTokens = splits[1].trim().split(",")

                exercise = Exercise()
                exercise.name = name
                exercise.hiddenKey = key

                val exerciseId = exerciseDao.insert(exercise)

                for (emphasisTypeString in emphasisTokens) {

                    val emphasisType = converter.fromEmphasisString(emphasisTypeString.trim())

                    val exerciseEmphasis = ExerciseEmphasis()
                    exerciseEmphasis.emphasisType = emphasisType
                    exerciseEmphasis.exerciseId = exerciseId

                    exerciseEmphasisDao.insert(exerciseEmphasis)
                }

                exercise = exerciseDao.getExerciseById(exerciseId)

                exercise.info = Utils.getEmphasisTypeInfo(exerciseId, context)

                exerciseDao.insert(exercise)
            }

            return exercise
        }
    }
}