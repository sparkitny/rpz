package de.parkitny.fit.myfit.app.ui.routine;

import java.util.List;

import de.parkitny.fit.myfit.app.RpzApplication;
import de.parkitny.fit.myfit.app.dao.ExerciseConfigurationDao;
import de.parkitny.fit.myfit.app.dao.RoutineDao;
import de.parkitny.fit.myfit.app.domain.RoutineHolder;
import de.parkitny.fit.myfit.app.entities.ExerciseConfiguration;
import de.parkitny.fit.myfit.app.entities.Routine;

public class RoutineHelper {
    /**
     * Makes a deep copy of the given {@link Routine}
     *
     * @param id the id of the {@link Routine}
     * @param suffix
     */
    public static Routine copyRoutine(long id, String suffix) {

        RoutineDao routineDao = RpzApplication.DB.routineDao();
        ExerciseConfigurationDao exerciseConfigurationDao = RpzApplication.DB.exerciseConfigurationDao();

        RoutineHolder routineHolder = new RoutineHolder(id);
        Routine routineCopy = routineHolder.copy(suffix);

        long routineId = routineDao.insert(routineCopy);

        List<ExerciseConfiguration> exerciseConfigurations = exerciseConfigurationDao.getByRoutineId(id);

        for (ExerciseConfiguration exerciseConfiguration : exerciseConfigurations) {

            ExerciseConfiguration exerciseConfiguration1 = exerciseConfiguration.copy();

            exerciseConfiguration1.routineId = routineId;
            exerciseConfigurationDao.insert(exerciseConfiguration1);
        }

        return routineCopy;
    }
}
