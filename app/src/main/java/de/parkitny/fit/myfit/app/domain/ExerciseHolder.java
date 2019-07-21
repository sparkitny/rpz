package de.parkitny.fit.myfit.app.domain;

import java.util.ArrayList;
import java.util.List;

import de.parkitny.fit.myfit.app.RpzApplication;
import de.parkitny.fit.myfit.app.dao.ExerciseEmphasisDao;
import de.parkitny.fit.myfit.app.entities.ExerciseEmphasis;

/**
 * Created by Sebastian on 20.10.2017.
 */

public class ExerciseHolder {

    private final ExerciseEmphasisDao exerciseEmphasisDao = RpzApplication.DB.exerciseEmphasisDao();
    private long exerciseId;

    private List<ExerciseEmphasis> exerciseEmphases = new ArrayList<>();

    public ExerciseHolder(long exerciseId) {

        this.exerciseId = exerciseId;

        if (this.exerciseId > 0) {
            exerciseEmphases = exerciseEmphasisDao.getByExerciseId(this.exerciseId);
        }
    }

    public List<ExerciseEmphasis> getExerciseEmphases() {
        return exerciseEmphases;
    }

}
