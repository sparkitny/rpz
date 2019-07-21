package de.parkitny.fit.myfit.app.events

/**
 * Created by Sebastian on 02.04.2016.
 */
class NextExerciseEvent {

    var nextExerciseTime: Long = 0
    var isForceUpdate = false

    constructor(forceUpdate: Boolean) {

        this.isForceUpdate = forceUpdate
        this.nextExerciseTime = System.currentTimeMillis()
    }

    constructor() {

        this.nextExerciseTime = System.currentTimeMillis()
    }
}
