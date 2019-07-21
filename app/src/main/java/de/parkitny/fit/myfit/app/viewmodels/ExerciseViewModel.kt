package de.parkitny.fit.myfit.app.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import de.parkitny.fit.myfit.app.RpzApplication
import de.parkitny.fit.myfit.app.dao.ExerciseDao
import de.parkitny.fit.myfit.app.dao.ExerciseEmphasisDao
import de.parkitny.fit.myfit.app.entities.Exercise
import de.parkitny.fit.myfit.app.entities.ExerciseEmphasis

class ExerciseViewModel : ViewModel() {

    private var exerciseDao: ExerciseDao = RpzApplication.DB.exerciseDao()

    private var exerciseEmphasisDao: ExerciseEmphasisDao = RpzApplication.DB.exerciseEmphasisDao()

    private var exercise: MutableLiveData<Exercise> = MutableLiveData()

    fun selectExercise(exercise: Exercise) {
        this.exercise.value = exercise
    }

    fun getSelectedExercise(): LiveData<Exercise> {
        return exercise
    }

    fun getExercises(): LiveData<List<Exercise>> {
        return exerciseDao.allLive
    }

    fun getExerciseByIdLive(id: Long): LiveData<Exercise> {
        return exerciseDao.getExerciseByIdLive(id)
    }

    fun getExerciseById(id: Long): Exercise {
        return exerciseDao.getExerciseById(id)
    }

    fun getExerciseEmphasisById(id: Long): LiveData<List<ExerciseEmphasis>> {
        return exerciseEmphasisDao.getByExerciseIdLive(id)
    }

    fun insertExercise(exercise: Exercise): Long {
        return exerciseDao.insert(exercise)
    }

    fun insertExerciseEmphasis(exerciseEmphasis: ExerciseEmphasis) {
        exerciseEmphasisDao.insert(exerciseEmphasis)
    }

    fun deleteExerciseEmphasisById(id: Long) {
        exerciseEmphasisDao.deleteByExerciseId(id)
    }
}