package de.parkitny.fit.myfit.app.ui.common;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.View;

import com.mikepenz.aboutlibraries.LibsBuilder;
import com.tramsun.libs.prefcompat.Pref;
import com.wooplr.spotlight.SpotlightView;

import org.greenrobot.eventbus.EventBus;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import cn.nekocode.badge.BadgeDrawable;
import de.parkitny.fit.myfit.app.R;
import de.parkitny.fit.myfit.app.RpzApplication;
import de.parkitny.fit.myfit.app.entities.EmphasisType;
import de.parkitny.fit.myfit.app.entities.ExerciseConfiguration;
import de.parkitny.fit.myfit.app.entities.ExerciseEmphasis;
import de.parkitny.fit.myfit.app.events.SetTitleEvent;
import de.parkitny.fit.myfit.app.events.ToastEvent;
import de.parkitny.fit.myfit.app.utils.FitParams;

/**
 * Created by sebas on 30.11.2015.
 */
public class Utils {

    private static final PeriodFormatter hourPeriodFormatter = new PeriodFormatterBuilder()
            .printZeroAlways()
            .minimumPrintedDigits(2)
            .appendHours()
            .appendLiteral("h ")
            .appendMinutes()
            .appendLiteral("m ")
            .appendSeconds()
            .appendLiteral("s")
            .toFormatter();
    private static final PeriodFormatter minutePeriodFormatter = new PeriodFormatterBuilder()
            .printZeroAlways()
            .minimumPrintedDigits(2)
            .appendMinutes()
            .appendLiteral("m ")
            .appendSeconds()
            .appendLiteral("s")
            .toFormatter();
    private static final String TAG = "Utils";

    /**
     * Returns the formatted time. If the time is greater than an hour it returns a formatted
     * string with hours. Else only minutes and seconds are returned
     *
     * @param time the time in milliseconds to be formatted
     * @return the formatted string
     */
    public static String formatPeriod(long time) {

        Period period = new Period(time);
        return period.toString(getPeriodFormatter(time));
    }

    /**
     * Returns a formatter including the hours or only seconds and minutes depending
     * on the elapsed time
     *
     * @param time the elapsed time
     * @return a formatter depending the elapsed time
     */
    public static PeriodFormatter getPeriodFormatter(long time) {

        if (time > 1000 * 60 * 60) {
            return hourPeriodFormatter;
        } else {
            return minutePeriodFormatter;
        }
    }

    /**
     * Sends an {@link ToastEvent} to show a message
     *
     * @param message the message to be shown
     */
    public static void toast(String message) {

        EventBus.getDefault().post(new ToastEvent(message));
    }

    /**
     * Returns a theme based color
     *
     * @param context        the context
     * @param colorAttribute the attribute id of the style
     * @return the id of the color
     */
    public static int getThemedColor(Context context, int colorAttribute) {

        int attributeId = getAttributeId(context, colorAttribute);

        return getColor(context, attributeId);
    }

    public static int getAttributeId(Context context, int ressourceId) {

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(new int[]{ressourceId});

        int result = typedArray.getResourceId(0, 0);
        typedArray.recycle();

        return result;
    }

    public static int getColor(Context context, int colorCode) {

        return ContextCompat.getColor(context, colorCode);
    }

    /**
     * Formats a given time in milliseconds to the string
     *
     * @param milliseconds the given time in milliseconds
     * @param context      the given {@link Context} used to format according to the {@link Locale} of
     *                     the users phone
     * @return the correctly formatted string
     */
    public static String getDateTimeString(long milliseconds, Context context) {

        java.text.DateFormat dateFormat = DateFormat.getDateFormat(context);
        java.text.DateFormat timeFormat = DateFormat.getTimeFormat(context);

        Date date = new Date(milliseconds);

        return String.format("%s %s",
                dateFormat.format(date),
                timeFormat.format(date));
    }

    /**
     * Formats a given time in milliseconds to the string of only the date
     *
     * @param milliseconds the given time in milliseconds
     * @param context      the given {@link Context} used to format according to the {@link Locale} of
     *                     the users phone
     * @return the correctly formatted string
     */
    public static String getDateString(long milliseconds, Context context) {
        java.text.DateFormat dateFormat = DateFormat.getDateFormat(context);

        Date date = new Date(milliseconds);

        return dateFormat.format(date);
    }

    /**
     * Shows the about dialog of Mike Penz about libraries
     *
     * @param context       the context needed
     * @param componentName the component firstLine
     */
    public static void showAbout(Context context, ComponentName componentName) {

//        try {
        new LibsBuilder()
                .withFields(R.string.class.getFields())
                // retrieve the current theme
//                    .withActivityTheme(
//                            context.getPackageManager().getApplicationInfo(componentName.getPackageName(), 0).theme
//                    )
                .withActivityTheme(Utils.getThemeId(context))
                .withAutoDetect(true)
                .withAboutIconShown(true)
                .withVersionShown(true)
                //                    .withAboutDescription("Testbeschreibung")
                .withAboutVersionShown(true)
                .withAboutAppName(context.getString(R.string.app_name))
                .withLicenseShown(true)
                .withActivityTitle(context.getString(R.string.about))
                .start(context);
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
    }

    public static int getThemeId(Context context) {

        String name = getTheme(context);

        switch (name) {
            case "Fitness":
                return R.style.Men;
            case "Yoga":
                return R.style.Women;
            default:
                return R.style.Women;
        }
    }

    public static String getTheme(Context context) {
        SharedPreferences preferencesCompat = PreferenceManager.getDefaultSharedPreferences(context);

        String themeName = preferencesCompat.getString("chosen_theme", "None");

        return themeName;
    }

    /**
     * Returns the number of voice ticks to be done before timed {@link ExerciseConfiguration}  ends
     *
     * @return the number of voice ticks
     */
    public static int numberVoiceTicks() {
        return 3;
    }

    /**
     * Broadcasts a given string as the title in the {@link Toolbar}
     *
     * @param title    the title to be shown in the toolbar
     * @param subtitle
     */
    public static void broadcastTitle(String title, String subtitle) {
        EventBus.getDefault().post(new SetTitleEvent(title, subtitle));
    }

    /**
     * Returns the userSizeView of the user
     *
     * @return the userSizeView of the user
     */
    public static double getUserSize() {

        double size = Pref.getDouble(FitParams.UserSize, 1.0);

        return size;
    }

    public static String calculateBmi(double weight, double height) {

        double bmi = weight / (height / 100 * height / 100);

        return String.format("%.2f", bmi);
    }

//    public static MaterialIntroView.Builder getIntro(Activity activity, int textId, View target, String key, ShapeType shapeType) {
//
//        MaterialIntroView.Builder builder = new MaterialIntroView.Builder(activity)
//                .setIdempotent(false)
//                .enableDotAnimation(true)
//                .enableIcon(false)
//                .setFocusGravity(FocusGravity.CENTER)
//                .setFocusType(Focus.MINIMUM)
//                .setDelayMillis(0)
//                .enableFadeAnimation(true)
//                .setInfoText(activity.getString(textId))
//                .setShape(shapeType)
//                .setTarget(target)
//                .setUsageId(key);
//
//        return builder;
//    }

    public static SpotlightView.Builder getSpotlight(Activity activity, View view, String title, String description) {

        return new SpotlightView.Builder(activity)
                .introAnimationDuration(100)
                .enableRevealAnimation(true)
                .performClick(true)
                .fadeinTextDuration(100)
                .headingTvColor(Color.parseColor("#eb273f"))
                .headingTvSize(32)
                .headingTvText(title)
                .subHeadingTvColor(Color.parseColor("#ffffff"))
                .subHeadingTvSize(16)
                .subHeadingTvText(description)
                .maskColor(Color.parseColor("#dc000000"))
                .target(view)
                .lineAnimDuration(100)
                .lineAndArcColor(Color.parseColor("#eb273f"))
                .dismissOnTouch(true)
                .dismissOnBackPress(true)
                .enableDismissAfterShown(true)
                .usageId(UUID.randomUUID().toString());
    }

    public static Drawable getBadgeNumber(Context context, int number) {

        return new BadgeDrawable.Builder()
                .type(BadgeDrawable.TYPE_NUMBER)
                .badgeColor(Utils.getThemedColor(context, R.attr.myAccent))
                .number(number)
                .padding(2, 2, 2, 2, 2)
                .build();
    }

    public static Drawable getBadgeRounds(Context context, int number) {

        return new BadgeDrawable.Builder()
                .type(BadgeDrawable.TYPE_WITH_TWO_TEXT_COMPLEMENTARY)
                .badgeColor(Utils.getThemedColor(context, R.attr.myAccent))
                .text1(context.getString(R.string.round))
                .text2(String.format("%s", number))
                .padding(2, 2, 2, 2, 2)
                .build();
    }

    public static String getEmphasisTypeName(Context context, EmphasisType emphasisType) {

        switch (emphasisType) {
            case ARMS:
                return context.getString(R.string.ARMS);
            case CORE:
                return context.getString(R.string.CORE);
            case LEGS:
                return context.getString(R.string.LEGS);
            case PULL:
                return context.getString(R.string.PULL);
            case PUSH:
                return context.getString(R.string.PUSH);
            case BREAST:
                return context.getString(R.string.BREAST);
            case SHOULDER:
                return context.getString(R.string.SHOULDER);
            case LOWER_BACK:
                return context.getString(R.string.LOWER_BACK);
            case UPPER_BACK:
                return context.getString(R.string.UPPER_BACK);
            default:
                return "Not implemented yet";
        }
    }

    public static String getEmphasisTypeInfo(long exerciseId, Context context) {

        List<ExerciseEmphasis> exerciseEmphases = RpzApplication.DB.exerciseEmphasisDao().getByExerciseId(exerciseId);

        StringBuilder result = new StringBuilder();

        if (exerciseEmphases.size() == 0) return "";

        for (ExerciseEmphasis exerciseEmphasis : exerciseEmphases) {

            result.append(Utils.getEmphasisTypeName(context, exerciseEmphasis.emphasisType)).append(", ");
        }

        return result.toString().substring(0, result.length() - 2);
    }

    public static SharedPreferences getUserLoginSharedPreferences(Context context) {
        return context.getSharedPreferences("user_login", Context.MODE_PRIVATE);
    }
}
