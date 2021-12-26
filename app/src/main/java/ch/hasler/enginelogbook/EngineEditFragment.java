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
 * Last change: 30.07.2019
 ******************************************************************************/
package ch.hasler.enginelogbook;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class EngineEditFragment extends Fragment implements OnClickListener {

    private static final String DIALOG_PURCHASE_DATE = "purchaseDate";
    private static final int REQUEST_DATE = 0;
    private long mPurchaseUnixTime;
    private Button mDateButton;
    private String mOldName;
    private DBAdapter mDB;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
//        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

        View rootView = inflater.inflate(R.layout.fragment_engine_edit,
                container, false);

        mDateButton = (Button) rootView.findViewById(R.id.btnEditDate);
        mDateButton.setOnClickListener(this);

        if (StartActivity.myDB == null) {
            mDB = new DBAdapter(getActivity());
            mDB.open();
        } else {
            mDB = StartActivity.myDB;
        }

        // get list of all different plug names entered in event DB for auto
        // fill
        ArrayList<String> plugNames = mDB
                .getEngineArrayNameByKey(DBAdapter.KEY_PLUG);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, plugNames);
        AutoCompleteTextView atv = (AutoCompleteTextView) rootView
                .findViewById(R.id.etEngEditPlug);
        atv.setAdapter(adapter);

        // get list of all different make names entered in event DB for auto
        // fill
        ArrayList<String> makeNames = mDB.getAllMakes();
        adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, makeNames);
        atv = (AutoCompleteTextView) rootView.findViewById(R.id.etEngEditMake);
        atv.setAdapter(adapter);

        // get list of all different clutch names entered in event DB for auto
        // fill
        ArrayList<String> clutchTypes = mDB
                .getEngineArrayNameByKey(DBAdapter.KEY_CLUTCH);
        adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, clutchTypes);
        atv = (AutoCompleteTextView) rootView
                .findViewById(R.id.etEngEditClutch);
        atv.setAdapter(adapter);

        // get list of all different spring names entered in event DB for auto
        // fill
        ArrayList<String> springNames = mDB
                .getEngineArrayNameByKey(DBAdapter.KEY_SPRING);
        adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, springNames);
        atv = (AutoCompleteTextView) rootView
                .findViewById(R.id.etEngEditSpring);
        atv.setAdapter(adapter);

        // get list of all different shoe names entered in event DB for auto
        // fill
        ArrayList<String> shoeNames = mDB
                .getEngineArrayNameByKey(DBAdapter.KEY_SHOE);
        adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, shoeNames);
        atv = (AutoCompleteTextView) rootView.findViewById(R.id.etEngEditShoe);
        atv.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        switch (getActivity().getIntent().getExtras().getInt("State")) {
            case StartActivity.MODE_EDIT:
                Cursor cursor = mDB.getEngine(getActivity().getIntent().getExtras()
                        .getLong("EngineId"));
                if (cursor.moveToFirst()) {
                    getActivity().setTitle(
                            getString(R.string.title_activity_motor_edit) + " "
                                    + cursor.getString(DBAdapter.COL_NAME));
                }
                cursor.close();

                fillData();
                break;
            case StartActivity.UTILITY_EVENT:
                fillData();
                break;
            default:
                getActivity().setTitle((R.string.title_activity_motor_new));
                loadDefaultValues();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        fillData();
    }

    // if in EDIT mode fill fields with db data
    private void fillData() {
        Cursor cEng = mDB.getEngine(getActivity().getIntent().getExtras()
                .getLong("EngineId"));
        if (cEng.moveToFirst()) {
            mOldName = cEng.getString(DBAdapter.COL_NAME);
            String make = cEng.getString(DBAdapter.COL_MAKE);
            mPurchaseUnixTime = cEng.getLong(DBAdapter.COL_DATE);
            int fuel = cEng.getInt(DBAdapter.COL_FUEL);
            // int status = cEng.getInt(DBAdapter.COL_STATUS);
            int tanksize = cEng.getInt(DBAdapter.COL_TANKSIZE);
            int consumption = cEng.getInt(DBAdapter.COL_CONSUMPTION);
            String compression = cEng.getString(DBAdapter.COL_COMPRESSION);
            String plug = cEng.getString(DBAdapter.COL_PLUG);
            String clutch = cEng.getString(DBAdapter.COL_CLUTCH);
            String shoe = cEng.getString(DBAdapter.COL_SHOE);
            String spring = cEng.getString(DBAdapter.COL_SPRING);
            String tension = cEng.getString(DBAdapter.COL_TENSION);
            String play = cEng.getString(DBAdapter.COL_AXIALPLAY);
            String clearance = cEng.getString(DBAdapter.COL_CLEARANCE);
            long pscId = cEng.getLong(DBAdapter.COL_ENG_PSC_ID);
            Cursor cPsc = mDB.getPsc(pscId);
            int limits = cPsc.getInt(DBAdapter.COL_PSC_LIMITS);
            int status = cPsc.getInt(DBAdapter.COL_PSC_STATUS);
            String notes = cEng.getString(DBAdapter.COL_NOTES);

            EditText nameTxt = (EditText) getView().findViewById(
                    R.id.etEngEditName);
            AutoCompleteTextView makeTxt = (AutoCompleteTextView) getView()
                    .findViewById(R.id.etEngEditMake);
            EditText fuelTxt = (EditText) getView().findViewById(
                    R.id.etEngEditFuel);
            Spinner statusSpn = (Spinner) getView()
                    .findViewById(R.id.spnStatus);
            EditText tanksizeTxt = (EditText) getView().findViewById(
                    R.id.etEngEditTanksize);
            EditText consumptionTxt = (EditText) getView().findViewById(
                    R.id.etEngEditConsumption);
            EditText compressionTxt = (EditText) getView().findViewById(
                    R.id.etEngEditCompression);
            AutoCompleteTextView plugTxt = (AutoCompleteTextView) getView()
                    .findViewById(R.id.etEngEditPlug);
            AutoCompleteTextView clutchTxt = (AutoCompleteTextView) getView()
                    .findViewById(R.id.etEngEditClutch);
            AutoCompleteTextView shoeTxt = (AutoCompleteTextView) getView()
                    .findViewById(R.id.etEngEditShoe);
            AutoCompleteTextView springTxt = (AutoCompleteTextView) getView()
                    .findViewById(R.id.etEngEditSpring);
            EditText tensionTxt = (EditText) getView().findViewById(
                    R.id.etEngEditTension);
            EditText playTxt = (EditText) getView().findViewById(
                    R.id.etEngEditPlay);
            EditText clearanceTxt = (EditText) getView().findViewById(
                    R.id.etEngEditClearance);
            EditText limitTxt = (EditText) getView().findViewById(
                    R.id.etEngEditLimit);
            EditText notesTxt = (EditText) getView().findViewById(
                    R.id.etEngEditNotes);

            nameTxt.setText(mOldName);
            makeTxt.setText(make);
            fuelTxt.setText(String.valueOf(fuel));
            updateDateButton(mPurchaseUnixTime);
            statusSpn.setSelection(status);
            tanksizeTxt.setText(String.valueOf(tanksize));
            consumptionTxt.setText(String.valueOf(consumption));
            compressionTxt.setText(compression);
            plugTxt.setText(plug);
            clutchTxt.setText(clutch);
            shoeTxt.setText(shoe);
            springTxt.setText(spring);
            tensionTxt.setText(tension);
            playTxt.setText(play);
            clearanceTxt.setText(clearance);
            limitTxt.setText(String.valueOf(limits));
            notesTxt.setText(notes);
        }
        cEng.close();
    }

    private void loadDefaultValues() {
        // loading default values into limit and tank size field
        SharedPreferences pref = getActivity().getSharedPreferences(
                "Default_Values", Activity.MODE_PRIVATE);
        EditText tankSizeTxt = (EditText) getView().findViewById(
                R.id.etEngEditTanksize);
        EditText limitTxt = (EditText) getView().findViewById(
                R.id.etEngEditLimit);
        EditText consumptionTxt = (EditText) getView().findViewById(
                R.id.etEngEditConsumption);
        limitTxt.setText(String.valueOf(pref.getInt("Limit",
                Integer.parseInt(getString(R.string.constant_limit)))));
        tankSizeTxt.setText(String.valueOf(pref.getInt("Size",
                Integer.parseInt(getString(R.string.constant_size)))));
        consumptionTxt.setText(String.valueOf(pref.getInt("Consumption",
                Integer.parseInt(getString(R.string.constant_consumption)))));

        // set date in button on today
        Calendar c = Calendar.getInstance();
        mPurchaseUnixTime = c.getTimeInMillis();
        updateDateButton(mPurchaseUnixTime);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.motor_edit, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (NavUtils.getParentActivityName(getActivity()) != null) {
                    NavUtils.navigateUpFromSameTask(getActivity());
                }
                return true;
            case R.id.fragedit_save:
                EditText nameTxt = (EditText) getView().findViewById(
                        R.id.etEngEditName);
                EditText makeTxt = (EditText) getView().findViewById(
                        R.id.etEngEditMake);
                String name = nameTxt.getText().toString();
                String make = makeTxt.getText().toString();

                ArrayList<String> engMakes = new ArrayList<>();
                Cursor c = mDB.getAllEngines(DBAdapter.KEY_MAKE);
                String existingMake;
                if (c != null && c.moveToFirst()) {
                    do {
                        existingMake = c.getString(DBAdapter.COL_MAKE);
                        if ((make.equalsIgnoreCase(existingMake)) && !(make.equals(existingMake))) {
                            engMakes.add(existingMake.toLowerCase());
                        }
                    } while (c.moveToNext());
                }
                c.close();

                ArrayList<String> engNames = new ArrayList<>();
                c = mDB.getAllEngines(DBAdapter.KEY_NAME);
                if (c != null && c.moveToFirst()) {
                    do {
                        engNames.add(c.getString(DBAdapter.COL_NAME));
                    } while (c.moveToNext());
                }
                c.close();
                engNames.remove(mOldName);
                if (engNames.contains(name) || (engMakes.contains(make.toLowerCase()))) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(
                            getActivity());
                    alert.setTitle(R.string.dia_title_multiple_name);
                    alert.setMessage(R.string.dia_multiple_name);
                    alert.setCancelable(true);
                    alert.setPositiveButton(R.string.dia_ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                    alert.show();
                } else if ((make.length() == 0) || (name.length() == 0)) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(
                            getActivity());
                    alert.setTitle(R.string.dia_title_no_make_name);
                    alert.setMessage(R.string.dia_no_make_name);
                    alert.setCancelable(true);
                    alert.setPositiveButton(R.string.dia_ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                    alert.show();
                } else {
                    addOrUpdateData();
                    switch (getActivity().getIntent().getExtras().getInt("State")) {
                        case StartActivity.MODE_EDIT:
                        case StartActivity.MODE_NEW:
                            getActivity().finish();
                            break;
                        case StartActivity.UTILITY_EVENT:
                            // set the activity title to the selected engine name
                            Cursor cEng = mDB.getEngine(getActivity().getIntent().getExtras().getLong("EngineId"));
                            long pscId = cEng.getLong(DBAdapter.COL_ENG_PSC_ID);
                            Cursor cPsc = mDB.getPsc(pscId);
                            if (cEng.moveToFirst()) {
                                ActionBar ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
                                ab.setTitle(cEng.getString(DBAdapter.COL_NAME));
                                ab.setSubtitle(cPsc.getString(DBAdapter.COL_PSC_NAME));
                                mOldName = cEng.getString(DBAdapter.COL_NAME);
                            }
                            cEng.close();
                        default:
                            break;
                    }
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addOrUpdateData() {
        String nameString, makeString, compressionString, plugString, clutchString, shoeString, springString, tensionString, playString, clearanceString, notesString;
        int fuelInt = 0;
        int statusInt = 0;
        int tankSizeInt = 0;
        int consumptionInt = 0;
        int limitsInt = 0;
        long pscId, shaftId, carbId, plateId, headerId;

        EditText nameTxt = (EditText) getView()
                .findViewById(R.id.etEngEditName);
        AutoCompleteTextView makeTxt = (AutoCompleteTextView) getView()
                .findViewById(R.id.etEngEditMake);
        EditText fuelTxt = (EditText) getView()
                .findViewById(R.id.etEngEditFuel);
        Spinner statusSpn = (Spinner) getView().findViewById(R.id.spnStatus);
        EditText tankSizeTxt = (EditText) getView().findViewById(
                R.id.etEngEditTanksize);
        EditText consumptionTxt = (EditText) getView().findViewById(
                R.id.etEngEditConsumption);
        EditText compressionTxt = (EditText) getView().findViewById(
                R.id.etEngEditCompression);
        AutoCompleteTextView plugTxt = (AutoCompleteTextView) getView()
                .findViewById(R.id.etEngEditPlug);
        AutoCompleteTextView clutchTxt = (AutoCompleteTextView) getView()
                .findViewById(R.id.etEngEditClutch);
        AutoCompleteTextView shoeTxt = (AutoCompleteTextView) getView()
                .findViewById(R.id.etEngEditShoe);
        AutoCompleteTextView springTxt = (AutoCompleteTextView) getView()
                .findViewById(R.id.etEngEditSpring);
        EditText tensionTxt = (EditText) getView().findViewById(
                R.id.etEngEditTension);
        EditText playTxt = (EditText) getView()
                .findViewById(R.id.etEngEditPlay);
        EditText clearanceTxt = (EditText) getView().findViewById(
                R.id.etEngEditClearance);
        EditText limitTxt = (EditText) getView().findViewById(
                R.id.etEngEditLimit);
        EditText notesTxt = (EditText) getView().findViewById(
                R.id.etEngEditNotes);

        // Fill fields with data
        nameString = nameTxt.getText().toString();
        makeString = makeTxt.getText().toString();

        // check if something is inserted in edittext
        if (fuelTxt.getText().toString().length() != 0) {
            // Get String
            fuelInt = Integer.parseInt(fuelTxt.getText().toString());
        }

        statusInt = statusSpn.getSelectedItemPosition();

        if (tankSizeTxt.getText().toString().length() != 0) {
            tankSizeInt = Integer.parseInt(tankSizeTxt.getText().toString());
        }
        if (consumptionTxt.getText().toString().length() != 0) {
            consumptionInt = Integer.parseInt(consumptionTxt.getText()
                    .toString());
        }

        compressionString = compressionTxt.getText().toString();
        plugString = plugTxt.getText().toString();
        clutchString = clutchTxt.getText().toString();
        shoeString = shoeTxt.getText().toString();
        springString = springTxt.getText().toString();
        tensionString = tensionTxt.getText().toString();
        playString = playTxt.getText().toString();
        clearanceString = clearanceTxt.getText().toString();

        if (limitTxt.getText().toString().length() != 0) {
            limitsInt = Integer.parseInt(limitTxt.getText().toString());
        }

        notesString = notesTxt.getText().toString();

        // Save new or update data set in DB
        switch (getActivity().getIntent().getExtras().getInt("State")) {
            case StartActivity.MODE_NEW:
                Toast.makeText(getActivity(), R.string.toast_saved,
                        Toast.LENGTH_SHORT).show();

                pscId = mDB.insertPsc(nameString + " "
                                + getString(R.string.dia_oem), makeString, fuelInt,
                        mPurchaseUnixTime, statusInt, false, limitsInt, "");
                shaftId = mDB.insertComponent(nameString + " "
                                + getString(R.string.dia_oem), makeString, fuelInt,
                        mPurchaseUnixTime, "",DBAdapter.CRANKSHAFT);
                carbId = mDB.insertComponent(nameString + " "
                            + getString(R.string.dia_oem), makeString, fuelInt,
                    mPurchaseUnixTime, "",DBAdapter.CARBURETOR);
                plateId = mDB.insertComponent(nameString + " "
                                + getString(R.string.dia_oem), makeString, fuelInt,
                        mPurchaseUnixTime, "",DBAdapter.END_PLATE);
                headerId = mDB.insertComponent(nameString + " "
                                + getString(R.string.dia_oem), makeString, fuelInt,
                        mPurchaseUnixTime, "",DBAdapter.UNDER_HEAD);

                mDB.insertEngine(nameString, makeString, fuelInt,
                        mPurchaseUnixTime, statusInt, tankSizeInt, consumptionInt,
                        compressionString, plugString, springString, tensionString,
                        playString, clearanceString, shoeString, clutchString,
                        limitsInt, notesString, pscId, shaftId, carbId, plateId, headerId);
                break;
            case StartActivity.MODE_EDIT:
            case StartActivity.UTILITY_EVENT:
                Toast.makeText(getActivity(), R.string.toast_updated,
                        Toast.LENGTH_SHORT).show();
                long engId = getActivity().getIntent().getExtras().getLong("EngineId");
                Cursor cEng = mDB.getEngine(engId);
                pscId = cEng.getLong(DBAdapter.COL_ENG_PSC_ID);
                mDB.updateEngine(engId, nameString, makeString, fuelInt,
                        mPurchaseUnixTime, statusInt, tankSizeInt, consumptionInt,
                        compressionString, plugString, springString, tensionString,
                        playString, clearanceString, shoeString, clutchString,
                        limitsInt, notesString, pscId);
                mDB.updatePscLimit(pscId, limitsInt);
                mDB.updatePscStatus(pscId, statusInt);
                break;
            default:
                break;
        }
    }

    private String unixTimeToString(long timeMS) {
        DateFormat formattedDate = DateFormat.getDateInstance(
                DateFormat.DEFAULT, Locale.getDefault());
        Date date = new Date(timeMS);

        return formattedDate.format(date);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnEditDate:
                FragmentManager fm = getActivity().getSupportFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(
                        mPurchaseUnixTime, 0, Long.MAX_VALUE);
                dialog.setTargetFragment(EngineEditFragment.this, REQUEST_DATE);
                dialog.show(fm, DIALOG_PURCHASE_DATE);
                break;
            default:
                break;
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;
        if (requestCode == REQUEST_DATE) {
            mPurchaseUnixTime = data.getLongExtra(
                    DatePickerFragment.EXTRA_DATE, 0);
            updateDateButton(mPurchaseUnixTime);
        }
    }

    private void updateDateButton(long unixTime) {
        Date date = new Date(unixTime);
        DateFormat formattedDate = DateFormat.getDateTimeInstance(
                DateFormat.LONG, DateFormat.SHORT, Locale.getDefault());
        mDateButton.setText(formattedDate.format(date));
    }
}
