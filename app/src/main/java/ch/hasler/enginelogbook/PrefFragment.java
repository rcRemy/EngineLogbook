/*******************************************************************************
 * Copyright (c) 2017 Remy Hasler (Hasler Electronic Engineering).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Last change: 26.09.17
 ******************************************************************************/
package ch.hasler.enginelogbook;

import java.io.File;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class PrefFragment extends PreferenceFragment implements
        OnSharedPreferenceChangeListener {

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View lv = getView().findViewById(android.R.id.list);
        if (lv != null) lv.setPadding(0, 0, 0, 0);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
//		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

        // Define the settings file and mode to use by this settings fragment
        this.getPreferenceManager().setSharedPreferencesName("Default_Values");
        this.getPreferenceManager().setSharedPreferencesMode(
                Activity.MODE_PRIVATE);

        // inflate preference resource file
        addPreferencesFromResource(R.xml.preference);

        // Initialize preference summary labels
        EditTextPreference prefLimit = (EditTextPreference) findPreference("Limit");
        prefLimit.setSummary(String.valueOf(getPreferenceManager()
                .getSharedPreferences().getInt("Limit",
                        Integer.parseInt(getString(R.string.constant_limit))))
                + getString(R.string.txt_ml));

        EditTextPreference prefSize = (EditTextPreference) findPreference("Size");
        prefSize.setSummary(String.valueOf(getPreferenceManager()
                .getSharedPreferences().getInt("Size",
                        Integer.parseInt(getString(R.string.constant_size))))
                + getString(R.string.txt_ml));

        EditTextPreference prefConsumption = (EditTextPreference) findPreference("Consumption");
        prefConsumption
                .setSummary(String.valueOf(getPreferenceManager()
                        .getSharedPreferences()
                        .getInt("Consumption",
                                Integer.parseInt(getString(R.string.constant_consumption))))
                        + getString(R.string.txt_ml_5min));

        checkAndSetStoragePlace();

        ListPreference prefFiles = (ListPreference) findPreference("Files");
        String storage = getPreferenceManager().getSharedPreferences()
                .getString("Files", "external");
        // set default value for list preference dialog
        prefFiles.setValue(storage);
        prefFiles.setSummary(getActualFilepath(storage));

        Preference prefBuild = findPreference("Build");
        prefBuild.setSummary(getString(R.string.system_version) + " / Database " + DBAdapter.DATABASE_VERSION);

        Preference prefMotor = findPreference("Motor");
        DBAdapter prefDB = StartActivity.myDB;
        Cursor cEng = prefDB.getAllEngines(DBAdapter.KEY_MAKE);
        prefMotor.setSummary(String.valueOf(cEng.getCount()));
        cEng.close();

        Preference prefPsc = findPreference("PSC");
        Cursor cPsc = prefDB.getAllPsc(DBAdapter.KEY_MAKE);
        prefPsc.setSummary(String.valueOf(cPsc.getCount()));
        cPsc.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (NavUtils.getParentActivityName(getActivity()) != null) {
                    NavUtils.navigateUpFromSameTask(getActivity());
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        Preference pref = findPreference(key);
        StringBuilder sb = new StringBuilder();

        if (key.equals("Limit")) {
            sb.append(String.valueOf(sharedPreferences.getInt(key, 0)));
            sb.append(getString(R.string.txt_ml));
        }
        if (key.equals("Size")) {
            sb.append(String.valueOf(sharedPreferences.getInt(key, 0)));
            sb.append(getString(R.string.txt_ml));
        }
        if (key.equals("Consumption")) {
            sb.append(String.valueOf(sharedPreferences.getInt(key, 0)));
            sb.append(getString(R.string.txt_ml_5min));
        }
        if (key.equals("Files")) {
            checkAndSetStoragePlace();
            String fileString = getActualFilepath(sharedPreferences.getString(
                    key, "external"));
            sb.append(fileString);
        }
        // if (key.equals("Orientation")) {
        // if (sharedPreferences.getBoolean(key, false)) {
        // getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // } else {
        // getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        // }
        // }
        pref.setSummary(sb.toString());
    }

    private void checkAndSetStoragePlace() {
        ListPreference prefFiles = (ListPreference) findPreference("Files");
        String storage = getPreferenceManager().getSharedPreferences()
                .getString("Files", "external");
        Boolean mounted = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
        String[] storagePlaces = getResources().getStringArray(R.array.storage_places);
        String[] storageValues = getResources().getStringArray(R.array.storage_value);

        // for Versions below API 19 only 'internal' and 'external' storage available, no 'sdcard'
        if (Build.VERSION.SDK_INT < 19) {
            if (!mounted && !(storage.equals("internal"))) {
                prefFiles.setValue("internal");
                Toast.makeText(getActivity().getApplicationContext(),
                        getString(R.string.pref_no_sdcard), Toast.LENGTH_SHORT)
                        .show();
                prefFiles.setEntries(new String[]{storagePlaces[0]});
                prefFiles.setEntryValues(new String[]{storageValues[0]});
            } else {
                prefFiles.setEntries(new String[]{storagePlaces[0], storagePlaces[1]});
                prefFiles.setEntryValues(new String[]{storageValues[0], storageValues[1]});
            }
        } else { // for API level =>19
            int length = getActivity().getExternalFilesDirs(null).length;
            switch (length) {
                case 0: // length = 0 -> not mounted, no sd
                    if (!(storage.equals("internal"))) {
                        prefFiles.setValue("internal");
                        Toast.makeText(getActivity().getApplicationContext(),
                                getString(R.string.pref_no_sdcard), Toast.LENGTH_SHORT)
                                .show();
                    }
                    prefFiles.setEntries(new String[]{storagePlaces[0]});
                    prefFiles.setEntryValues(new String[]{storageValues[0]});
                    break;

                case 1: // length = 1 -> mounted, no sd
                    if (storage.equals("sdcard")) {
                        prefFiles.setValue("external");
                    }
                    prefFiles.setEntries(new String[]{storagePlaces[0], storagePlaces[1]});
                    prefFiles.setEntryValues(new String[]{storageValues[0], storageValues[1]});
                    break;
                case 2: // length = 2 -> mounted, sd
                    prefFiles.setEntries(new String[]{storagePlaces[0], storagePlaces[1], storagePlaces[2]});
                    prefFiles.setEntryValues(new String[]{storageValues[0], storageValues[1], storageValues[2]});
                    break;
            }

        }

    }

    private String getActualFilepath(String storagePlace) {
        File filePath;
        String[] arrayPlaces = getResources().getStringArray(
                R.array.storage_places);

        switch (storagePlace) {
            case "internal":
                return (arrayPlaces[0]);

            case "external":
                if (Build.VERSION.SDK_INT >= 19) {
                    File[] Dirs = getActivity().getExternalFilesDirs(null);
                    String[] sepStr = Dirs[0].toString().split("Android");
                    return (arrayPlaces[1] + "\n" + "/Android" + sepStr[1]);
                } else {
                    filePath = new File(Environment.getExternalStorageDirectory(),
                            getString(R.string.file_folder));
                    return (arrayPlaces[1] + "\n" + filePath.toString());
                }

            case "sdcard":
                if (Build.VERSION.SDK_INT >= 19) {
                    File[] Dirs = getActivity().getExternalFilesDirs(null);
                    if (Dirs.length > 1) {
                        String[] sepStr = Dirs[1].toString().split("Android");
                        return (arrayPlaces[2] + "\n" + "/Android" + sepStr[1]);
                    }
                }

            default:
                return ("");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }
}
