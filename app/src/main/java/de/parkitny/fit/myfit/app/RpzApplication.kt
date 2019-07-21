package de.parkitny.fit.myfit.app

import android.app.Application
import android.arch.persistence.room.Room
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.devicon_typeface_library.DevIcon
import com.mikepenz.entypo_typeface_library.Entypo
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.foundation_icons_typeface_library.FoundationIcons
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.Iconics
import com.mikepenz.ionicons_typeface_library.Ionicons
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic
import com.mikepenz.meteocons_typeface_library.Meteoconcs
import com.mikepenz.octicons_typeface_library.Octicons
import com.mikepenz.pixeden_7_stroke_typeface_library.Pixeden7Stroke
import com.mikepenz.typeicons_typeface_library.Typeicons
import com.mikepenz.weather_icons_typeface_library.WeatherIcons
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.DiskLogAdapter
import com.orhanobut.logger.Logger
import com.tramsun.libs.prefcompat.Pref
import de.parkitny.fit.myfit.app.database.FitDB
import de.parkitny.fit.myfit.app.database.Initializer
import de.parkitny.fit.myfit.app.database.Migrations
import de.parkitny.fit.myfit.app.utils.FileLoggingTree
import de.parkitny.fit.myfit.app.utils.FitParams
import timber.log.Timber


/**
 * Created by Sebastian on 23.03.2016.
 */
class RpzApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Iconics.registerFont(GoogleMaterial())
        Iconics.registerFont(MaterialDesignIconic())
        Iconics.registerFont(FontAwesome())
        Iconics.registerFont(Octicons())
        Iconics.registerFont(Meteoconcs())
        Iconics.registerFont(CommunityMaterial())
        Iconics.registerFont(WeatherIcons())
        Iconics.registerFont(Typeicons())
        Iconics.registerFont(Entypo())
        Iconics.registerFont(DevIcon())
        Iconics.registerFont(FoundationIcons())
        Iconics.registerFont(Ionicons())
        Iconics.registerFont(Pixeden7Stroke())

        Pref.init(this)

        DB = Room
                .databaseBuilder(this, FitDB::class.java, "fitdb")
                .allowMainThreadQueries()
//                .fallbackToDestructiveMigration()
                .addMigrations(
                        Migrations.MIGRATION_9_10,
                        Migrations.MIGRATION_10_11)
                .build()

        if (!Pref.getBoolean(FitParams.InitDB, false)) {

            Initializer.initializeDatabase(this)

            Pref.putBoolean(FitParams.InitDB, true)
        }

        Logger.addLogAdapter(AndroidLogAdapter())
        Logger.addLogAdapter(DiskLogAdapter())

        if (BuildConfig.DEBUG) {
            Timber.plant(object : Timber.DebugTree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                    Logger.log(priority, tag, message, t)
                }
            })
            Timber.plant(FileLoggingTree(this))
        } else {
            Timber.plant(object : Timber.DebugTree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                    Logger.log(priority, tag, message, t)
                }
            })
        }
    }

    companion object {
        lateinit var DB: FitDB
    }
}
