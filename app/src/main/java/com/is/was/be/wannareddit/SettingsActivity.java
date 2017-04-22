package com.is.was.be.wannareddit;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.is.was.be.wannareddit.data.DataUtility;
import com.is.was.be.wannareddit.data.ForRedditProvider;
import com.is.was.be.wannareddit.data.ListColumns;

import java.util.ArrayList;

/**
 * Created by hyeryungpark on 4/9/17.
 */

public class SettingsActivity extends AppCompatPreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {

            /* Show the Up button in the action bar.
               Then "Up" action is explicitly set in the Fragment's onOptionsItemSelected()
               as Explicit Intent
            */
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {

        // We only have one Preference for this version of the app
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener =
            new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object value) {
                    String uiStr = value.toString();

                    if (preference instanceof ListPreference){
                        ListPreference l = (ListPreference)preference;
                        int ix = l.findIndexOfValue(uiStr);
                        if (ix >= 0){
                            preference.setSummary(l.getEntries()[ix]);
                        }
                    }
                    return true;
                }
    };

    private static void bindPreferenceToValue(Preference preferenceToSaveNow){
        preferenceToSaveNow.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preferenceToSaveNow,
                PreferenceManager.getDefaultSharedPreferences(preferenceToSaveNow.getContext()
                ).getString(preferenceToSaveNow.getKey(), ""));
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {

        final static private String TAG = "GeneralPreference";
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            ArrayList<String> holder = new ArrayList<>();

            ListPreference localPref = (ListPreference) findPreference(getString(R.string.pref_subrdd_key));

            Uri uri = ForRedditProvider.MainContract.CONTENT_URI;

            Cursor cursor = getActivity().getContentResolver().query(uri,
                    new String[]{ListColumns.SUBREDDITNAME}, null, null, DataUtility.sortOrder);

            while (cursor.moveToNext()){
                String name = cursor.getString(0);
                holder.add(name);
            }

            final CharSequence[] entries = holder.toArray(new CharSequence[holder.size()]);
            final CharSequence[] savedValues = holder.toArray(new CharSequence[holder.size()]);
            localPref.setEntries(entries);
            localPref.setEntryValues(savedValues);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceToValue(localPref);
            // TimeFence Preference
            ListPreference tfPref = (ListPreference) findPreference(getString(R.string.pref_timefence_key));
            bindPreferenceToValue(tfPref);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();

            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), MainActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

}
