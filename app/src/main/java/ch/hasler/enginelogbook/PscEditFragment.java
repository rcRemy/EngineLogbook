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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class PscEditFragment extends Fragment implements OnClickListener {

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

        View rootView = inflater.inflate(R.layout.fragment_psc_edit, container,
                false);

        mDateButton = (Button) rootView.findViewById(R.id.btnPscEditDate);
        mDateButton.setOnClickListener(this);

        if (StartActivity.myDB == null) {
            mDB = new DBAdapter(getActivity());
            mDB.open();
        } else {
            mDB = StartActivity.myDB;
        }

        // get list of all different engine makes entered in the DB for spinner
        ArrayList<String> makes = mDB.getAllMakes();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, makes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = (Spinner) rootView.findViewById(R.id.spnPscEditMake);
        spinner.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        switch (getActivity().getIntent().getExtras().getInt("State")) {
            case StartActivity.MODE_EDIT:
                Cursor cursor = mDB.getPsc(getActivity().getIntent().getExtras()
                        .getLong("PscId"));
                if (cursor.moveToFirst()) {
                    getActivity().setTitle(
                            getString(R.string.title_activity_motor_edit) + " "
                                    + cursor.getString(DBAdapter.COL_PSC_NAME));
                }
                cursor.close();

                fillData();
                break;
            case StartActivity.UTILITY_EVENT:
                Cursor cEvent = mDB.getPsc(getActivity().getIntent().getExtras()
                        .getLong("ID"));
                cEvent.close();

                fillData();
                break;
            default:
                getActivity().setTitle((R.string.title_activity_psc_new));
                loadDefaultValues();
                break;
        }

        // disable the header option
        CheckBox headerCheck = (CheckBox) getView().findViewById(
                R.id.cbPscEditHeader);
        headerCheck.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        fillData();
    }

    // if in EDIT mode fill fields with db data
    private void fillData() {
        Cursor cursor = mDB.getPsc(getActivity().getIntent().getExtras()
                .getLong("PscId"));
        if (cursor.moveToFirst()) {
            mOldName = cursor.getString(DBAdapter.COL_PSC_NAME);
            String make = cursor.getString(DBAdapter.COL_PSC_MAKE);
            mPurchaseUnixTime = cursor.getLong(DBAdapter.COL_PSC_DATE);
            int status = cursor.getInt(DBAdapter.COL_PSC_STATUS);
            boolean header = cursor.getInt(DBAdapter.COL_PSC_HEADER) > 0;
            int limits = cursor.getInt(DBAdapter.COL_PSC_LIMITS);
            int fuel = cursor.getInt(DBAdapter.COL_PSC_FUEL);
            String notes = cursor.getString(DBAdapter.COL_PSC_NOTES);

            EditText nameTxt = (EditText) getView().findViewById(
                    R.id.etPscEditName);
            Spinner makeSpn = (Spinner) getView().findViewById(
                    R.id.spnPscEditMake);
            Spinner statusSpn = (Spinner) getView().findViewById(
                    R.id.spnPscEditStatus);
            CheckBox headerCheck = (CheckBox) getView().findViewById(
                    R.id.cbPscEditHeader);
            EditText fuelTxt = (EditText) getView().findViewById(
                    R.id.etPscEditFuel);
            EditText notesTxt = (EditText) getView().findViewById(
                    R.id.etPscEditNotes);
            EditText limitsTxt = (EditText) getView().findViewById(
                    R.id.etPscEditLimits);

            nameTxt.setText(mOldName);
            makeSpn.setSelection(getIndex(makeSpn, make));
            updateDateButton(mPurchaseUnixTime);
            statusSpn.setSelection(status);
            headerCheck.setChecked(header);
            notesTxt.setText(notes);
            limitsTxt.setText(String.valueOf(limits));
            fuelTxt.setText(String.valueOf(fuel));
        }
        cursor.close();
    }

    private int getIndex(Spinner spinner, String myString) {
        int index = 0;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).equals(myString)) {
                index = i;
            }
        }
        return index;
    }

    private void loadDefaultValues() {
        // loading default values into limit and tank size field
        SharedPreferences pref = getActivity().getSharedPreferences(
                "Default_Values", Activity.MODE_PRIVATE);
        EditText limitTxt = (EditText) getView().findViewById(
                R.id.etPscEditLimits);
        limitTxt.setText(String.valueOf(pref.getInt("Limit",
                Integer.parseInt(getString(R.string.constant_limit)))));

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
                    Intent intent = NavUtils.getParentActivityIntent(getActivity());
                    intent.putExtra("Fragment", StartActivity.FRAGMENT_PSC);
                    NavUtils.navigateUpTo(getActivity(), intent);
                    //NavUtils.navigateUpFromSameTask(getActivity());
                }
                return true;
            case R.id.fragedit_save:
                EditText nameTxt = (EditText) getView().findViewById(
                        R.id.etPscEditName);
                Spinner makeSpn = (Spinner) getView().findViewById(
                        R.id.spnPscEditMake);
                String name = nameTxt.getText().toString();
                String make = makeSpn.getItemAtPosition(
                        makeSpn.getSelectedItemPosition()).toString();
                ArrayList<String> pscNames = new ArrayList<String>();
                Cursor c = mDB.getAllPsc(DBAdapter.KEY_PSC_NAME);
                if (c != null && c.moveToFirst()) {
                    do {
                        pscNames.add(c.getString(DBAdapter.COL_PSC_NAME));
                    } while (c.moveToNext());
                }
                c.close();
                pscNames.remove(mOldName);
                if (pscNames.contains(name)) {
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
                } else if ((make.equals("")) || (name.length() == 0)) {
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
                    addupdateData();
                    switch (getActivity().getIntent().getExtras().getInt("State")) {
                        case StartActivity.MODE_EDIT:
                            getActivity().finish();
                            break;
                        case StartActivity.UTILITY_EVENT:
                            // set the activity subtitle to the selected engine name
                            Cursor cPsc = mDB.getPsc(getActivity().getIntent().getExtras().getLong("PscId"));
                            if (cPsc.moveToFirst()) {
                                ActionBar ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
                                ab.setSubtitle(cPsc.getString(DBAdapter.COL_PSC_NAME));
                                mOldName = cPsc.getString(DBAdapter.COL_NAME);
                            }
                            cPsc.close();
                        default:
                            break;
                    }
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addupdateData() {
        String name, make, notes;
        int fuel = 0;
        int status = 0;
        int limits = 0;
        boolean header = false;

        EditText nameTxt = (EditText) getView()
                .findViewById(R.id.etPscEditName);
        Spinner makeSpn = (Spinner) getView().findViewById(R.id.spnPscEditMake);
        Spinner statusSpn = (Spinner) getView().findViewById(
                R.id.spnPscEditStatus);
        CheckBox headerCheck = (CheckBox) getView().findViewById(
                R.id.cbPscEditHeader);
        EditText notesTxt = (EditText) getView().findViewById(
                R.id.etPscEditNotes);
        EditText limitsTxt = (EditText) getView().findViewById(
                R.id.etPscEditLimits);
        EditText fuelTxt = (EditText) getView()
                .findViewById(R.id.etPscEditFuel);

        // Fill fields with data
        name = nameTxt.getText().toString();
        make = makeSpn.getItemAtPosition(makeSpn.getSelectedItemPosition())
                .toString();
        status = statusSpn.getSelectedItemPosition();
        header = headerCheck.isChecked();
        notes = notesTxt.getText().toString();
        if (limitsTxt.getText().toString().length() != 0) {
            limits = Integer.parseInt(limitsTxt.getText().toString());
        }
        if (fuelTxt.getText().toString().length() != 0) {
            fuel = Integer.parseInt(fuelTxt.getText().toString());
        }

        // Save new or update data set in DB
        switch (getActivity().getIntent().getExtras().getInt("State")) {
            case StartActivity.MODE_NEW:
                Toast.makeText(getActivity(), R.string.toast_saved,
                        Toast.LENGTH_SHORT).show();
                mDB.insertPsc(name, make, fuel, mPurchaseUnixTime, status, header,
                        limits, notes);
                break;
            case StartActivity.MODE_EDIT:
            case StartActivity.UTILITY_EVENT:
                Toast.makeText(getActivity(), R.string.toast_updated,
                        Toast.LENGTH_SHORT).show();
                mDB.updatePsc(getActivity().getIntent().getExtras().getLong("PscId"),
                        name, make, fuel, mPurchaseUnixTime, status, header,
                        limits, notes);
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
            case R.id.btnPscEditDate:
                FragmentManager fm = getActivity().getSupportFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(
                        mPurchaseUnixTime, 0, Long.MAX_VALUE);
                dialog.setTargetFragment(PscEditFragment.this, REQUEST_DATE);
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
