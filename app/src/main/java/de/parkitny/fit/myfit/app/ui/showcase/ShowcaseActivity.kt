package de.parkitny.fit.myfit.app.ui.showcase

import android.content.Intent
import android.os.Bundle
import com.shashank.sony.fancywalkthroughlib.FancyWalkthroughActivity
import com.shashank.sony.fancywalkthroughlib.FancyWalkthroughCard
import de.parkitny.fit.myfit.app.R
import de.parkitny.fit.myfit.app.ui.RpzNavDrawer

class ShowcaseActivity : FancyWalkthroughActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configureCardList()
    }

    private fun configureCardList() {

        val pages = ArrayList<FancyWalkthroughCard>()

        pages.add(createWelcomeCard())
        pages.add(createExerciseCard())
        pages.add(createWorkoutCard())

        pages.forEach {

            it.backgroundColor = android.R.color.background_dark
            it.titleColor = android.R.color.white
            it.descriptionColor = android.R.color.white
        }

        setOnboardPages(pages)

        setImageBackground(R.mipmap.world)
    }

    private fun createWelcomeCard(): FancyWalkthroughCard {

        return FancyWalkthroughCard(
                R.string.welcome,
                R.string.welcome_description)
    }

    private fun createExerciseCard(): FancyWalkthroughCard {

        return FancyWalkthroughCard(
                R.string.exercises,
                R.string.welcome_exercise
        )
    }

    private fun createWorkoutCard(): FancyWalkthroughCard {

        return FancyWalkthroughCard(
                R.string.workouts,
                R.string.welcome_workouts
        )
    }

    override fun onFinishButtonPressed() {

        val intent = Intent(this, RpzNavDrawer::class.java)
        startActivity(intent)
    }
}
