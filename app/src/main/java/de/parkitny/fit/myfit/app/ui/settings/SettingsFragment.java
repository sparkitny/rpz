package de.parkitny.fit.myfit.app.ui.settings;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;

import com.onurciner.toastox.ToastOXDialog;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;
import com.tramsun.libs.prefcompat.Pref;

import de.parkitny.fit.myfit.app.BuildConfig;
import de.parkitny.fit.myfit.app.R;
import de.parkitny.fit.myfit.app.ui.RpzNavDrawer;
import de.parkitny.fit.myfit.app.ui.common.Bar;
import de.parkitny.fit.myfit.app.ui.common.Utils;
import de.parkitny.fit.myfit.app.utils.FitParams;
import tech.linjiang.pandora.Pandora;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * A simple {@link Fragment} subclass.
 */
@Bar(Position = 4)
public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

    private Preference themePreference, namePreference;

    public SettingsFragment() {
    }

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {

        setPreferencesFromResource(R.xml.fragment_preferences, rootKey);

        themePreference = findPreference("chosen_theme");

        if (themePreference != null) {
            themePreference.setOnPreferenceChangeListener(this);
        }

        namePreference = findPreference("name");

        if (namePreference != null) {
            namePreference.setOnPreferenceChangeListener(this);
            namePreference.setSummary(Pref.getString("name", ""));
        }

        if (!BuildConfig.DEBUG) {

            Preference debugPreference = findPreference(getString(R.string.debug));
            debugPreference.setVisible(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Utils.broadcastTitle(getString(R.string.action_settings), getString(R.string.configure_it));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {

        if (preference.getKey().equals("chosen_theme")) {
            new ToastOXDialog.Build(getActivity())
                    .setTitle(R.string.hint)
                    .setContent(R.string.theme_choose_text)
                    .setPositiveText(R.string.ok_option)
                    .setPositiveTextColor(Color.WHITE)
                    .setPositiveBackgroundColor(Utils.getThemedColor(getActivity(), R.attr.myPrim))
                    .setIcon(R.drawable.ic_info_outline_black_18dp)
                    .onPositive(toastOXDialog -> {
                        restart();
                        toastOXDialog.dismiss();
                    }).show();
        }

        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {

        if (preference.getKey().equals(FitParams.MenuStyle)) {

            new ToastOXDialog.Build(getActivity())
                    .setTitle(R.string.hint)
                    .setContent(R.string.menu_style_title)
                    .setPositiveText(R.string.ok_option)
                    .setPositiveTextColor(Color.WHITE)
                    .setPositiveBackgroundColor(Utils.getThemedColor(getActivity(), R.attr.myPrim))
                    .setIcon(R.drawable.ic_info_outline_black_18dp)
                    .onPositive(toastOXDialog -> {
                        restart();
                        toastOXDialog.dismiss();
                    }).show();
        }
        if (preference.getKey().equals(FitParams.About)) {
            Utils.showAbout(getContext(), null);
        }
        if (preference.getKey().equals(FitParams.ResetOptions)) {
            FitParams.resetShowcase();
        }
        if (BuildConfig.DEBUG) {
            if (preference.getKey().equals(getString(R.string.debug))) {
                Pandora.get().open();
            }
        }

        return super.onPreferenceTreeClick(preference);
    }

    private void restart() {

        Context context = getContext();

        Intent intent = new Intent(context, RpzNavDrawer.class);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).finish();
        }

        Runtime.getRuntime().exit(0);
    }
}
