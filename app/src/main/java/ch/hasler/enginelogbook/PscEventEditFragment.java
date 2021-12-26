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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class PscEventEditFragment extends Fragment implements OnClickListener {

    // identifier for DatePickerFragment
    private static final String DIALOG_DATE = "date";

    private static final int REQUEST_DATE = 0;

    private int mTanks = 0;
    private long mUnixTime, mPurchaseDate, mNow;
    private Button mDateButton;
    private DBAdapter mDB;
    private Bundle mBundle;

    private static int CONROD = 1;
    private static int PISTON = 4;
    private static int SLEEVE = 5;
    private static int UNDERHEAD = 6;
    private static int PISTONROD = 8;
    private static int PISTONRODCLIPS = 9;
    private static int REPLACE = 1;
    private static int REPLACE_USED = 2;
    private static int REPAIR = 3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);
//        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

        View rootView = inflater.inflate(R.layout.fragment_addevent, container,
                false);

        if (mDB == null) {
            mDB = new DBAdapter(getActivity());
            mDB.open();
        }

        // setup the listeners
        mDateButton = (Button) rootView.findViewById(R.id.btnAddEventDate);
        mDateButton.setOnClickListener(this);

        // sets action spinner list
        ArrayList<SpinnerData> spnActions = new ArrayList<>();
        String[] actions = getResources()
                .getStringArray(R.array.engine_actions);
        spnActions.add(new SpinnerData(REPLACE, actions[REPLACE]));
        spnActions.add(new SpinnerData(REPLACE_USED, actions[REPLACE_USED]));
        spnActions.add(new SpinnerData(REPAIR, actions[REPAIR]));
        ArrayAdapter<SpinnerData> actAdapter = new ArrayAdapter<>(
                getActivity(), android.R.layout.simple_spinner_item, spnActions);
        actAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = (Spinner) rootView.findViewById(R.id.spnEventAction);
        spinner.setAdapter(actAdapter);

        // sets parts spinner list
        ArrayList<SpinnerData> spnParts = new ArrayList<>();
        String[] Parts = getResources().getStringArray(R.array.engine_parts);
        spnParts.add(new SpinnerData(CONROD, Parts[CONROD]));
        spnParts.add(new SpinnerData(PISTON, Parts[PISTON]));
        spnParts.add(new SpinnerData(SLEEVE, Parts[SLEEVE]));
        spnParts.add(new SpinnerData(PISTONROD, Parts[PISTONROD]));
        spnParts.add(new SpinnerData(PISTONRODCLIPS, Parts[PISTONRODCLIPS]));
        long pscId = getActivity().getIntent().getExtras().getLong("PscId");
        Cursor cPsc = mDB.getPsc(pscId);
        if (cPsc.getInt(DBAdapter.COL_PSC_HEADER) > 0) {
            spnParts.add(new SpinnerData(UNDERHEAD, Parts[UNDERHEAD]));
        }
        cPsc.close();
        ArrayAdapter<SpinnerData> partAdapter = new ArrayAdapter<SpinnerData>(
                getActivity(), android.R.layout.simple_spinner_item, spnParts);
        partAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner = (Spinner) rootView.findViewById(R.id.spnEventParts);
        spinner.setAdapter(partAdapter);

        // get list of all different places entered in event DB for auto fill
        ArrayList<String> places = mDB.getEventPlaces();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, places);
        AutoCompleteTextView textView = (AutoCompleteTextView) rootView
                .findViewById(R.id.etEventPlace);
        textView.setAdapter(adapter);
        textView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(arg1.getApplicationWindowToken(), 0);
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mBundle = savedInstanceState;

        switch (getActivity().getIntent().getExtras().getInt("EventState")) {
/*            case StartActivity.MODE_EDIT:
                Cursor cursor = mDB.getEvent(getActivity().getIntent().getExtras()
                        .getLong("EventId"));
                int action = cursor.getInt(DBAdapter.COL_ACTION_ID);
                String[] actionArr = getResources().getStringArray(
                        R.array.engine_actions);

                if (cursor.moveToFirst()) {
                    ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
                    ab.setTitle(R.string.title_activity_motor_edit);
                    ab.setSubtitle(actionArr[action]);
                    cursor.close();
                }

                fillData();
                break;*/
            default:
                getActivity().setTitle((R.string.title_activity_event_new));
                resetInputData();
                break;
        }

        getData();
        setActionUI();
    }

    @Override
    public void onResume() {
        super.onResume();

        resetInputData();
    }

    // Sets the visibility of the UI according to action spinner position
    private void setActionUI() {
        LinearLayout llFuel = (LinearLayout) getView().findViewById(
                R.id.fuellayout);
        LinearLayout llAction = (LinearLayout) getView().findViewById(
                R.id.partlayout);
        LinearLayout llPsc = (LinearLayout) getView().findViewById(
                R.id.psclayout);

        llFuel.setVisibility(View.GONE);
        llAction.setVisibility(View.VISIBLE);
        llPsc.setVisibility(View.GONE);
        setButtonClickable(mDateButton, false);
    }

    // Sets date on today
    private void getData() {
        Cursor cPsc = mDB.getPsc(getActivity().getIntent().getExtras()
                .getLong("PscId"));
        if (cPsc.moveToFirst()) {
            mPurchaseDate = cPsc.getLong(DBAdapter.COL_DATE);
        }
        cPsc.close();

        // set date in button on today
        Calendar c = Calendar.getInstance();
        mUnixTime = c.getTimeInMillis();
        mNow = mUnixTime;
        updateDateButton(mUnixTime);
    }

    // Resets all input data fields to a default value
    private void resetInputData() {
        // set data in edit text (RunTime + FuelAmount)
        AutoCompleteTextView actv = (AutoCompleteTextView) getView()
                .findViewById(R.id.etEventPlace);
        actv.setText("");
        EditText et = (EditText) getView().findViewById(R.id.etEventRuntime);
        et.setText("");
        et = (EditText) getView().findViewById(R.id.etEventFuelamount);
        et.setText("");
        et = (EditText) getView().findViewById(R.id.etEventNotes);
        et.setText("");
        TextView tv = (TextView) getView().findViewById(R.id.etEventTanks);
        mTanks = 0;
        tv.setText(String.valueOf(mTanks));
        // set date in button on today
        Calendar c = Calendar.getInstance();
        mUnixTime = c.getTimeInMillis();
        updateDateButton(mUnixTime);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // inflate the menu, this adds items to the action bar if it is present.
        inflater.inflate(R.menu.frag_add, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (NavUtils.getParentActivityName(getActivity()) != null) {
                    Intent intent = NavUtils.getParentActivityIntent(getActivity());
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    NavUtils.navigateUpTo(getActivity(), intent);
                }
                return true;
            case R.id.fragadd_save:
                Toast.makeText(getActivity(), R.string.toast_updated,
                        Toast.LENGTH_SHORT).show();
                addEventData();
                getActivity().finish();
                return true;
/*            case R.id.fragadd_reset:
                Toast.makeText(getActivity(), R.string.toast_input_reset,
                        Toast.LENGTH_SHORT).show();
                resetInputData();
                return true;*/
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Adds event data into DB
    private void addEventData() {
        String place, notes;
        int fuel = 0;
        int part = 0;
        SpinnerData sdAction, sdPart;
        int action;
        long engineId, pscId;

        Spinner actionSpn = (Spinner) getView().findViewById(
                R.id.spnEventAction);
        AutoCompleteTextView placeTxt = (AutoCompleteTextView) getView()
                .findViewById(R.id.etEventPlace);
        Spinner partSpn = (Spinner) getView().findViewById(R.id.spnEventParts);
        EditText notesTxt = (EditText) getView()
                .findViewById(R.id.etEventNotes);

        pscId = getActivity().getIntent().getExtras().getLong("PscId");

        sdAction = (SpinnerData) actionSpn.getSelectedItem();
        action = (int) sdAction.getId();

        engineId = Integer.MAX_VALUE;
        place = placeTxt.getText().toString();

        sdPart = (SpinnerData) partSpn.getSelectedItem();
        part = (int) sdPart.getId();

        notes = notesTxt.getText().toString();

        mDB.insertEvent(mUnixTime, engineId, action, fuel, place, part, notes,
                pscId);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // when date button is clicked
            case R.id.btnAddEventDate:
                FragmentManager fm = getActivity().getSupportFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(
                        mUnixTime, mPurchaseDate, mNow);
                dialog.setTargetFragment(PscEventEditFragment.this, REQUEST_DATE);
                dialog.show(fm, DIALOG_DATE);
                break;
            default:
                break;
        }
    }

    @Override
    // is called by DatePickerFragment with result
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;
        if (requestCode == REQUEST_DATE) {
            mUnixTime = data.getLongExtra(DatePickerFragment.EXTRA_DATE, 0);
            updateDateButton(mUnixTime);
        }
    }

    // Updates date button text to given date
    private void updateDateButton(long unixTime) {
        Date date = new Date(unixTime);
        DateFormat formattedDate = DateFormat.getDateInstance(
                DateFormat.DEFAULT, Locale.getDefault());
        mDateButton.setText(formattedDate.format(date));
    }

    private void setButtonClickable(Button btn, boolean state) {
        if (state) {
            btn.setClickable(true);
            btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        } else {
            btn.setClickable(false);
            btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_idle_lock, 0, 0, 0);
        }
    }
}
