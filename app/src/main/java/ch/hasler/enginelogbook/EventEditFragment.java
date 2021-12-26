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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class EventEditFragment extends Fragment implements OnClickListener,
        TextWatcher {

    // identifier for DatePickerFragment
    private static final String DIALOG_DATE = "date";

    private static final int REQUEST_DATE = 0;

    private static final int FUEL_BY_TANK = 0;
    private static final int FUEL_BY_TIME = 1;
    private static final int FUEL_BY_AMOUNT = 2;

    private int mTanks = 0;
    private int mRuntime = 0;
    private int mFuelAmount = 0;
    private int mTankSize = 0;
    private int mConsumption = 125;
    private int mFuel = 0;
    private long mUnixTime, mMinDate, mMaxDate;
    private Button mDateButton;
    private DBAdapter mDB;
    private Bundle mBundle;

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
        Button b = (Button) rootView.findViewById(R.id.btnPlus);
        b.setOnClickListener(this);
        b = (Button) rootView.findViewById(R.id.btnMinus);
        b.setOnClickListener(this);
        mDateButton = (Button) rootView.findViewById(R.id.btnAddEventDate);
        mDateButton.setOnClickListener(this);
        EditText et = (EditText) rootView.findViewById(R.id.etEventRuntime);
        et.addTextChangedListener(this);
        et = (EditText) rootView.findViewById(R.id.etEventFuelamount);
        et.addTextChangedListener(this);

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
        getData();

        switch (getActivity().getIntent().getExtras().getInt("EventState")) {
            case StartActivity.MODE_EDIT:
                Cursor cursor = mDB.getEvent(getActivity().getIntent().getExtras()
                        .getLong("EventId"));
                int action = cursor.getInt(DBAdapter.COL_ACTION_ID);
                String[] actionArr = getResources().getStringArray(
                        R.array.engine_actions);

                if (cursor.moveToFirst()) {
                    ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
                    ab.setTitle(R.string.title_activity_motor_edit);
                    ab.setSubtitle(actionArr[action]);
                }
                cursor.close();

                fillData();
                break;
            default:
                getActivity().setTitle((R.string.title_activity_event_new));
                resetInputData();
                break;
        }

        setActionUI();
        setFuelUI();
        setPscUI();
    }
/*
    @Override
    public void onResume() {
        super.onResume();

        if (getActivity().getIntent().getExtras().getInt("EventState") == StartActivity.MODE_EDIT) {
            fillData();
            //getActivity().getActionBar().getTabAt(0).setText(R.string.menu_edit);
        } else {
            resetInputData();
        }
    }*/

    // Sets the visibility of the UI according to action spinner position
    private void setActionUI() {
        Spinner spinnerAction = (Spinner) getView().findViewById(
                R.id.spnEventAction);

        spinnerAction.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                LinearLayout llFuel = (LinearLayout) getView().findViewById(
                        R.id.fuellayout);
                LinearLayout llAction = (LinearLayout) getView().findViewById(
                        R.id.partlayout);
                LinearLayout llPsc = (LinearLayout) getView().findViewById(
                        R.id.psclayout);
                switch (position) {
                    case 0: // refuel
                        llFuel.setVisibility(View.VISIBLE);
                        llAction.setVisibility(View.GONE);
                        llPsc.setVisibility(View.GONE);
                        if (getActivity().getIntent().getExtras().getInt("EventState") == StartActivity.MODE_EDIT) {
                            setButtonClickable(mDateButton, false);
                        } else {
                            setButtonClickable(mDateButton, true);
                        }
                        break;
                    case 4: // psc set exchange
                        llFuel.setVisibility(View.GONE);
                        llAction.setVisibility(View.GONE);
                        llPsc.setVisibility(View.VISIBLE);
                        // set date in button on today, because exchange should only be today possible
                        Calendar c = Calendar.getInstance();
                        mUnixTime = c.getTimeInMillis();
                        updateDateButton(mUnixTime);
                        setButtonClickable(mDateButton, false);
                        break;
                    default: // part change/repair
                        llFuel.setVisibility(View.GONE);
                        llAction.setVisibility(View.VISIBLE);
                        llPsc.setVisibility(View.GONE);
                        if (getActivity().getIntent().getExtras().getInt("EventState") == StartActivity.MODE_EDIT) {
                            setButtonClickable(mDateButton, false);
                        } else {
                            setButtonClickable(mDateButton, true);
                        }
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // not used, but must be implemented
            }
        });

        if (getActivity().getIntent().getExtras().getInt("EventState") == StartActivity.MODE_EDIT) {
            spinnerAction.setEnabled(false);
        }
    }

    // Sets the visibility of the UI according to psc spinner position
    private void setPscUI() {
        // fills the spinner with all psc sets with same brand
        long engineId = getActivity().getIntent().getExtras().getLong("EngineId");
        Cursor cEng = mDB.getEngine(engineId);
        String engMake = cEng.getString(DBAdapter.COL_MAKE);
        Cursor cPsc = mDB.getAllPsc(DBAdapter.KEY_MAKE);
        ArrayList<SpinnerData> pscNames = new ArrayList<>();
        if (cPsc != null && cPsc.moveToFirst()) {
            do {
                long builtInEngId = mDB.getWherePscBuiltin(cPsc
                        .getLong(DBAdapter.COL_ROWID));
                // fill the spinner with all psc ids and names which are built in nowhere
                if ((builtInEngId == Integer.MAX_VALUE) && cPsc.getString(DBAdapter.COL_PSC_MAKE).equals(engMake)) {
                    pscNames.add(new SpinnerData(cPsc
                            .getLong(DBAdapter.COL_ROWID), cPsc
                            .getString(DBAdapter.COL_PSC_NAME)));
                }
            } while (cPsc.moveToNext());
        }
        cPsc.close();
        cEng.close();

        ArrayAdapter<SpinnerData> Adapter = new ArrayAdapter<>(
                getActivity(), android.R.layout.simple_spinner_item, pscNames);
        Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinnerPsc = (Spinner) getView().findViewById(R.id.spnEventPsc);
        spinnerPsc.setAdapter(Adapter);
    }

    // Sets the visibility of the UI according to fuel spinner position
    private void setFuelUI() {
        Spinner spnFuel = (Spinner) getView().findViewById(
                R.id.spnEventFuelBy);
        spnFuel.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                LinearLayout llTank = (LinearLayout) getView().findViewById(
                        R.id.fuelbytanklayout);
                LinearLayout llTime = (LinearLayout) getView().findViewById(
                        R.id.fuelbytimelayout);
                LinearLayout llAmount = (LinearLayout) getView().findViewById(
                        R.id.fuelbyamountlayout);
                switch (position) {
                    case FUEL_BY_TANK:
                        mFuel = calculateFuel(mTanks, FUEL_BY_TANK);
                        llTank.setVisibility(View.VISIBLE);
                        llTime.setVisibility(View.GONE);
                        llAmount.setVisibility(View.GONE);
                        break;
                    case FUEL_BY_TIME:
                        mFuel = calculateFuel(mRuntime, FUEL_BY_TIME);
                        llTank.setVisibility(View.GONE);
                        llTime.setVisibility(View.VISIBLE);
                        llAmount.setVisibility(View.GONE);
                        break;
                    case FUEL_BY_AMOUNT:
                        mFuel = calculateFuel(mFuelAmount, FUEL_BY_AMOUNT);
                        llTank.setVisibility(View.GONE);
                        llTime.setVisibility(View.GONE);
                        llAmount.setVisibility(View.VISIBLE);
                        break;
                    default:
                        Spinner spnFuel = (Spinner) getView().findViewById(R.id.spnEventFuelBy);
                        int refuelType = spnFuel.getSelectedItemPosition();
                        mFuel = calculateFuel(mTanks, refuelType);
                        llTank.setVisibility(View.VISIBLE);
                        llTime.setVisibility(View.GONE);
                        llAmount.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // not used, but must be implemented
            }
        });
    }

    // Gets data of actual motor (tank size, consumption) and sets date on today
    private void getData() {
        long engId = getActivity().getIntent().getExtras().getLong("EngineId");
        Cursor cursor = mDB.getEngine(engId);
        long lastExchangeTime = mDB.getEventLastPscExchange(engId);
        long purchaseDate = 0;
        if (cursor.moveToFirst()) {
            mTankSize = cursor.getInt(DBAdapter.COL_TANKSIZE);
            mConsumption = cursor.getInt(DBAdapter.COL_CONSUMPTION);
            purchaseDate = cursor.getLong(DBAdapter.COL_DATE);
        }
        cursor.close();

        // sets minimum date
        if (lastExchangeTime > purchaseDate) {
            mMinDate = lastExchangeTime;
        } else {
            mMinDate = purchaseDate;
        }

        // set date in button on today
        Calendar c = Calendar.getInstance();
        mUnixTime = c.getTimeInMillis();
        mMaxDate = mUnixTime;
        updateDateButton(mUnixTime);
    }

    // if in EDIT mode fill fields with db data
    private void fillData() {
        Cursor cEvent = mDB.getEvent(getActivity().getIntent().getExtras()
                .getLong("EventId"));
        if (cEvent.moveToFirst()) {
            mUnixTime = cEvent.getLong(DBAdapter.COL_EVENT_DATE);
            int action = cEvent.getInt(DBAdapter.COL_ACTION_ID);
            mFuelAmount = cEvent.getInt(DBAdapter.COL_EVENT_FUEL);
            String place = cEvent.getString(DBAdapter.COL_EVENT_PLACE);
            int part = cEvent.getInt(DBAdapter.COL_EVENT_PART);
            String notes = cEvent.getString(DBAdapter.COL_EVENT_NOTES);
            long engineId = getActivity().getIntent().getExtras().getLong("EngineId");

            Spinner spnAction = (Spinner) getView().findViewById(R.id.spnEventAction);
            AutoCompleteTextView etPlace = (AutoCompleteTextView) getView().findViewById(R.id.etEventPlace);
            Spinner spnFuelBy = (Spinner) getView().findViewById(R.id.spnEventFuelBy);
            EditText etFuel = (EditText) getView().findViewById(R.id.etEventFuelamount);
            Spinner spnPart = (Spinner) getView().findViewById(R.id.spnEventParts);
            EditText etNotes = (EditText) getView().findViewById(R.id.etEventNotes);

            spnFuelBy.setSelection(2);
            etFuel.setText(String.valueOf(mFuelAmount));
            mFuel = calculateFuel(mFuelAmount, FUEL_BY_AMOUNT);
            spnAction.setSelection(action);
            updateDateButton(mUnixTime);
            //setButtonClickable(mDateButton, false);
            etPlace.setText(place);
            spnPart.setSelection(part);
            etNotes.setText(notes);

            mMinDate = mDB.getEventExchangeBefore(engineId, mUnixTime);
            mMaxDate = mDB.getEventExchangeAfter(engineId, mUnixTime);
        }
        cEvent.close();
    }

    // Resets all input data fields to a default value
    private void resetInputData() {
        // set data in edit text (RunTime + FuelAmount)
        AutoCompleteTextView atv = (AutoCompleteTextView) getView()
                .findViewById(R.id.etEventPlace);
        atv.setText("");
        EditText et = (EditText) getView().findViewById(R.id.etEventRuntime);
        et.setText("");
        et = (EditText) getView().findViewById(R.id.etEventFuelamount);
        et.setText("");
        et = (EditText) getView().findViewById(R.id.etEventNotes);
        et.setText("");
        TextView tv = (TextView) getView().findViewById(R.id.etEventTanks);
        mTanks = 0;
        tv.setText(String.valueOf(mTanks));
        Spinner spn = (Spinner) getView().findViewById(R.id.spnEventFuelBy);
        spn.setSelection(FUEL_BY_TANK);
        // set date in button on today
        Calendar c = Calendar.getInstance();
        mUnixTime = c.getTimeInMillis();
        updateDateButton(mUnixTime);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // inflate the menu, this adds items to the action bar if it is present.
        inflater.inflate(R.menu.frag_add, menu);
        super.onCreateOptionsMenu(menu, inflater);
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

    // Adds or update event data into DB
    private void addEventData() {
        String place, notes;
        int fuel = 0;
        int action, part;
        long engineId, pscId;
        SpinnerData psc;

        Spinner spnAction = (Spinner) getView().findViewById(
                R.id.spnEventAction);
        AutoCompleteTextView tvPlace = (AutoCompleteTextView) getView()
                .findViewById(R.id.etEventPlace);
        Spinner spnPart = (Spinner) getView().findViewById(R.id.spnEventParts);
        EditText tvNotes = (EditText) getView()
                .findViewById(R.id.etEventNotes);
        Spinner spnPsc = (Spinner) getView().findViewById(R.id.spnEventPsc);

        engineId = getActivity().getIntent().getExtras().getLong("EngineId");
        action = spnAction.getSelectedItemPosition();

        // save fuel amount only if action = 0 (refuel)
        if (action == 0) {
            fuel = mFuel;
        }
        place = tvPlace.getText().toString();
        part = spnPart.getSelectedItemPosition();
        notes = tvNotes.getText().toString();

        // if action = 4 (exchange psc) then get pscId from spinner
        if (action == 4) {
            psc = (SpinnerData) spnPsc.getSelectedItem();
            if (psc != null) {
                pscId = psc.getId();
                mDB.updateEnginePscId(engineId, pscId);
                mDB.insertEvent(mUnixTime, engineId, action, fuel, place, part,
                        notes, pscId);
                Toast.makeText(getActivity(), R.string.toast_updated,
                        Toast.LENGTH_SHORT).show();
            } else {
                AlertDialog.Builder alert = new AlertDialog.Builder(
                        getActivity());
                alert.setTitle(R.string.dia_title_no_psc);
                alert.setMessage(R.string.dia_no_psc);
                alert.setCancelable(true);
                alert.setPositiveButton(R.string.dia_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                alert.show();
            }
        } else {
            Cursor cEng = mDB.getEngine(engineId);
            pscId = cEng.getLong(DBAdapter.COL_ENG_PSC_ID);

            if (getActivity().getIntent().getExtras().getInt("EventState") == StartActivity.MODE_EDIT) {
                long eventId = getActivity().getIntent().getExtras().getLong("EventId");
                mDB.updateEvent(eventId, mUnixTime, engineId, action, fuel, place, part, notes, pscId);
                getActivity().getIntent().putExtra("EventState", StartActivity.MODE_NEW);
                Toast.makeText(getActivity(), R.string.toast_updated,
                        Toast.LENGTH_SHORT).show();
            } else {
                mDB.insertEvent(mUnixTime, engineId, action, fuel, place, part,
                        notes, pscId);
                Toast.makeText(getActivity(), R.string.toast_saved,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    // returns fuel amount depending on input type (data can be tanks, time or fuel)
    private int calculateFuel(int data, int refuelType) {
        int fuel = 0;

        switch (refuelType) {
            // refuel by tanks
            case FUEL_BY_TANK:
                fuel = data * mTankSize;
                break;
            // refuel by time
            case FUEL_BY_TIME:
                // *100 and /100 is to increase resolution, because variable type is
                // integer
                fuel = ((data * 100) / 5 * mConsumption) / 100;
                break;
            // refuel by amount
            case FUEL_BY_AMOUNT:
                fuel = data;
                break;
            default:
                break;
        }

        // update text view with current fuel value
        TextView fuelAddedTxt = (TextView) getView().findViewById(
                R.id.lblNewTotal);
        String str = String.valueOf(fuel) + getString(R.string.txt_ml);
        fuelAddedTxt.setText(str);

        return fuel;
    }

    @Override
    public void onClick(View v) {
        TextView tvTanks = (TextView) getView()
                .findViewById(R.id.etEventTanks);
        switch (v.getId()) {
            // when (+) button is clicked
            case R.id.btnPlus:
                mTanks = mTanks + 1;
                tvTanks.setText(String.valueOf(mTanks));
                mFuel = calculateFuel(mTanks, FUEL_BY_TANK);
                break;
            // when (-) button is clicked
            case R.id.btnMinus:
                mTanks = mTanks - 1;
                tvTanks.setText(String.valueOf(mTanks));
                mFuel = calculateFuel(mTanks, FUEL_BY_TANK);
                break;
            // when date button is clicked
            case R.id.btnAddEventDate:
                FragmentManager fm = getActivity().getSupportFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(
                        mUnixTime, mMinDate, mMaxDate);
                dialog.setTargetFragment(EventEditFragment.this, REQUEST_DATE);
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
        DateFormat formattedDate = DateFormat.getDateTimeInstance(
                DateFormat.LONG, DateFormat.SHORT, Locale.getDefault());
                mDateButton.setText(formattedDate.format(date));
    }

    private void setButtonClickable(Button btn, boolean state) {
        if (state) {
            btn.setClickable(true);
            btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        } else {
            btn.setClickable(false);
            btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_black_18dp, 0, 0, 0);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        // not used, but must be implemented
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
        // not used, but must be implemented
    }

    @Override
    // is called when text in edit text views is changed (fuel amount, run time)
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        int data = 0;
        if (s.toString().length() != 0) {
            data = Integer.parseInt(s.toString());
        }
        Spinner spnFuel = (Spinner) getView().findViewById(R.id.spnEventFuelBy);
        int refuelType = spnFuel.getSelectedItemPosition();
        mFuel = calculateFuel(data, refuelType);
    }
}
