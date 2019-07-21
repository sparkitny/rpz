package de.parkitny.fit.myfit.app.events

import android.os.Bundle

/**
 * Created by Sebastian on 31.03.2016.
 */
data class StartFragmentEvent(val fragmentType: FragmentType, val bundle: Bundle? = null)
