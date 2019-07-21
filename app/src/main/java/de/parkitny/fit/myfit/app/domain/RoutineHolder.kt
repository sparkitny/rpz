package de.parkitny.fit.myfit.app.domain

import de.parkitny.fit.myfit.app.RpzApplication
import de.parkitny.fit.myfit.app.dao.ExerciseConfigurationDao
import de.parkitny.fit.myfit.app.dao.RoutineDao
import de.parkitny.fit.myfit.app.entities.Exercise
import de.parkitny.fit.myfit.app.entities.ExerciseConfiguration
import de.parkitny.fit.myfit.app.entities.Routine
import timber.log.Timber
import java.util.*

/**
 * Created by Sebastian on 05.10.2017.
 */
class RoutineHolder {
    /**
     * the id of the [Routine]
     */
    private var routineId: Long = 0
    /**
     * The [Routine]
     */
    private var routine: Routine? = null
    /**
     * the [ExerciseConfiguration]s of this [Routine]
     */
    private var exerciseConfigurations: MutableList<ExerciseConfiguration> = ArrayList()
    /**
     * The [RoutineDao] used to access the database
     */
    private val routineDao: RoutineDao = RpzApplication.DB.routineDao()
    /**
     * ths [ExerciseConfigurationDao] used to read/write the [ExerciseConfiguration]s
     */
    private val exerciseConfigurationDao: ExerciseConfigurationDao = RpzApplication.DB.exerciseConfigurationDao()

    /**
     * Returns the firstLine of the [Routine]
     *
     * @return
     */
    /**
     * Sets the firstLine of the [Routine]
     *
     * @param name
     */
    var routineName: String
        get() = routine!!.name
        set(name) {
            routine!!.name = name
        }

    var routinePause: Long
        get() = routine!!.setPause
        set(pause) {
            routine!!.setPause = pause
        }

    /**
     * @return the rounds of the [Routine]
     */
    val rountineRounds: Int
        get() = routine!!.rounds

    val numberOfExercises: Int
        get() = exerciseConfigurations.size

    constructor() {
        this.routineId = -1
    }

    constructor(routineId: Long) {

        this.routineId = routineId

        routine = routineDao.getRoutineById(this.routineId)

        exerciseConfigurations = exerciseConfigurationDao.getByRoutineId(this.routineId)
    }

    /**
     * Stores the [Routine] and the [ExerciseConfiguration]s
     */
    fun storeCompleteRoutine() {

        storeRoutine()

        exerciseConfigurationDao.insert(exerciseConfigurations)
    }

    fun storeRoutine() {

        this.routineId = routineDao.insert(routine)
    }

    fun newRoutine(name: String): Long {

        routine = Routine()
        routine!!.name = name
        routineId = routineDao.insert(routine)
        routine = routineDao.getRoutineById(routineId)

        return routineId
    }

    fun deleteCompleteRoutine() {
        exerciseConfigurationDao.deleteAllByRoutineId(routineId)
        routineDao.delete(routine)
    }

    /**
     * Sets the number of rounds
     *
     * @param rounds
     */
    fun setRoutineRounds(rounds: Int) {
        routine!!.rounds = rounds
    }

    /**
     * @return the maximal number of the exercises
     */
    private fun calculateMax(): Int {

        var max = -1

        for (item in exerciseConfigurations) {
            if (item.order > -1) max = item.order
        }

        return max
    }

    /**
     * Adds a given [ExerciseConfiguration] at the end of the [Routine]
     *
     * @param exerciseId            the id of the [Exercise]
     * @param exerciseConfiguration the [ExerciseConfiguration]
     */
    fun addExercise(exerciseId: Long, exerciseConfiguration: ExerciseConfiguration) {

        val max = calculateMax() + 1

        exerciseConfiguration.exerciseId = exerciseId
        exerciseConfiguration.order = max
        exerciseConfiguration.routineId = routineId
        exerciseConfiguration.pause = routine!!.setPause

        exerciseConfigurations.add(exerciseConfiguration)

        sort()
    }

    fun removeExerciseById(id: Long) {

        val exerciseConfiguration = exerciseConfigurationDao.getById(id)

        if (exerciseConfiguration != null) {
            exerciseConfigurationDao.delete(exerciseConfiguration)
        }

        exerciseConfigurations = exerciseConfigurationDao.getByRoutineId(routineId)

        for (i in exerciseConfigurations.indices) {

            if (exerciseConfigurations[i].id == id) {
                exerciseConfigurations.removeAt(i)
            }
        }

        renumberOrders()
        storeCompleteRoutine()
    }

    private fun renumberOrders() {
        for (i in exerciseConfigurations.indices) {

            val current = exerciseConfigurations[i]
            current.order = i
        }
    }

    fun getExerciseConfiguration(id: Long): ExerciseConfiguration? {

        for (exerciseConfiguration in exerciseConfigurations) {
            if (id == exerciseConfiguration.id) return exerciseConfiguration
        }

        return null
    }

    fun updatePause(pause: Long) {

        for (exerciseConfiguration in exerciseConfigurations) {
            if (exerciseConfiguration.canOverwritePause) {
                Timber.d("Update pause of %s in routine %s",
                        exerciseConfiguration.exerciseId,
                        exerciseConfiguration.routineId)
                exerciseConfiguration.pause = pause
            }
        }

        routinePause = pause
        storeCompleteRoutine()
    }

    /**
     * Sorts the list of exercises
     */
    private fun sort() {

        val comparator = { p1: ExerciseConfiguration, p2: ExerciseConfiguration -> p1.order.compareTo(p2.order) }

        val temp = ArrayList(exerciseConfigurations)

        Collections.sort(temp, comparator)

        exerciseConfigurations = temp
    }

    fun copy(suffix: String): Routine {

        val routine = Routine()
        routine.name = this.routine!!.name + " " + suffix
        routine.rounds = rountineRounds
        routine.archived = this.routine!!.archived
        routine.setPause = routinePause

        return routine
    }
}
