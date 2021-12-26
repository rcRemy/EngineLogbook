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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.TimePicker;
import android.widget.ViewSwitcher;

public class DatePickerFragment extends DialogFragment implements View.OnClickListener {
    public static final String EXTRA_DATE = "ch.hasler.enginelogbook.date";
    public static final String EXTRA_MINDATE = "ch.hasler.enginelogbook.mindate";
    public static final String EXTRA_MAXDATE = "ch.hasler.enginelogbook.maxdate";

    private long mUnixGmt, mMinimumGmt, mMaximumGmt;
    private int mYear, mMonth, mDay, mHour, mMinute;
    private ViewSwitcher switcher;
    private Button btnDate, btnTime;

    public static DatePickerFragment newInstance(long unixTime, long minDate, long maxDate) {
        Bundle args = new Bundle();
        args.putLong(EXTRA_DATE, unixTime);
        args.putLong(EXTRA_MINDATE, minDate);
        args.putLong(EXTRA_MAXDATE, maxDate);

        DatePickerFragment fragment = new DatePickerFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mUnixGmt = getArguments().getLong(EXTRA_DATE);
        mMinimumGmt = getArguments().getLong(EXTRA_MINDATE);
        mMaximumGmt = getArguments().getLong(EXTRA_MAXDATE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mMaximumGmt);
        final int maxHour = calendar.get(calendar.HOUR_OF_DAY);
        final int maxMinute = calendar.get(calendar.MINUTE);

        calendar.setTimeInMillis(mUnixGmt);
        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        mDay = calendar.get(Calendar.DAY_OF_MONTH);
        mHour = calendar.get(Calendar.HOUR_OF_DAY);
        mMinute = calendar.get(Calendar.MINUTE);

        calendar.setTimeInMillis(mMinimumGmt);
        final int minHour = calendar.get(calendar.HOUR_OF_DAY);
        final int minMinute = calendar.get(calendar.MINUTE);

        final View v = getActivity().getLayoutInflater().inflate(
                R.layout.dialog_datetime, null);

        final DatePicker datePicker = (DatePicker) v
                .findViewById(R.id.dialog_date_datePicker);
        datePicker.init(mYear, mMonth, mDay, new OnDateChangedListener() {

            @Override
            public void onDateChanged(DatePicker view, int year, int month,
                                      int day) {
                mUnixGmt = new GregorianCalendar(year, month, day, mHour, mMinute)
                        .getTimeInMillis();
                // set to actual hour/minute to distinguish between events with
                // same date
                mYear = year;
                mMonth = month;
                mDay = day;
                if (mMinimumGmt > mUnixGmt) {
                    mHour = minHour;
                    mMinute = minMinute + 1;
                    mUnixGmt = new GregorianCalendar(mYear, mMonth, mDay, mHour, mMinute).getTimeInMillis();
                }

                DateFormat formattedDate = DateFormat.getDateInstance(
                        DateFormat.DEFAULT, Locale.getDefault());
                btnDate = (Button) v.findViewById(R.id.dialog_btn_date);
                btnDate.setText(formattedDate.format(mUnixGmt));


                // Update argument to preserve selected value on rotation
                getArguments().putLong(EXTRA_DATE, mUnixGmt);
            }
        });
        datePicker.setMinDate(mMinimumGmt);
//        datePicker.setMaxDate(mMaximumGmt);

        final TimePicker timePicker = (TimePicker) v.findViewById(R.id.dialog_date_timePicker);
        timePicker.setIs24HourView(android.text.format.DateFormat.is24HourFormat(getActivity()));
        timePicker.setCurrentHour(mHour);
        timePicker.setCurrentMinute(mMinute);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int hour, int minute) {
                mUnixGmt = new GregorianCalendar(mYear, mMonth, mDay, hour, minute).getTimeInMillis();
                if (mMinimumGmt > mUnixGmt) {
                    mHour = minHour;
                    mMinute = minMinute + 1;
                    mUnixGmt = new GregorianCalendar(mYear, mMonth, mDay, mHour, mMinute).getTimeInMillis();
                } else if (mMaximumGmt < mUnixGmt) {
                    mHour = maxHour;
                    mMinute = maxMinute - 1;
                    mUnixGmt = new GregorianCalendar(mYear, mMonth, mDay, mHour, mMinute).getTimeInMillis();
                } else {
                    mHour = hour;
                    mMinute = minute;
                }

                DateFormat formattedTime = DateFormat.getTimeInstance(
                        DateFormat.SHORT, Locale.getDefault());
                btnTime = (Button) v.findViewById(R.id.dialog_btn_time);
                btnTime.setText(formattedTime.format(mUnixGmt));
            }
        });

        switcher = (ViewSwitcher) v.findViewById(R.id.switcher);
        btnDate = (Button) v.findViewById(R.id.dialog_btn_date);
        btnTime = (Button) v.findViewById(R.id.dialog_btn_time);
        btnDate.setOnClickListener(this);
        btnTime.setOnClickListener(this);
        btnDate.setClickable(false);
        btnTime.setClickable(true);
        DateFormat formattedDate = DateFormat.getDateInstance(
                DateFormat.DEFAULT, Locale.getDefault());
        btnDate.setText(formattedDate.format(mUnixGmt));
        DateFormat formattedTime = DateFormat.getTimeInstance(
                DateFormat.SHORT, Locale.getDefault());
        btnTime.setText(formattedTime.format(mUnixGmt));

        final AlertDialog d = new AlertDialog.Builder(getActivity()).setView(v)
                .setTitle(R.string.dia_date)
                .setPositiveButton(R.string.dia_ok, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        sendResult(Activity.RESULT_OK);
                    }
                })
                // do nothing, just dismiss dialog
                .setNegativeButton(R.string.dia_cancel, null)
                // a listener is later declared, so dialog is not dismissed
                .setNeutralButton(R.string.dia_today, null).create();

        d.show();
        d.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // set date of today in Date Picker
                        Calendar c = Calendar.getInstance();
                        datePicker.updateDate(c.get(Calendar.YEAR),
                                c.get(Calendar.MONTH),
                                c.get(Calendar.DAY_OF_MONTH));
                        // set first to 0 to force onTimeChanged
                        timePicker.setCurrentHour(0);
                        timePicker.setCurrentMinute(0);
                        timePicker.setCurrentHour(c.get(Calendar.HOUR_OF_DAY));
                        timePicker.setCurrentMinute(c.get(Calendar.MINUTE));
                    }
                });
        return d;
    }

    private void sendResult(int resultCode) {
        if (getTargetFragment() == null)
            return;

        Intent i = new Intent();
        i.putExtra(EXTRA_DATE, mUnixGmt);

        getTargetFragment().onActivityResult(getTargetRequestCode(),
                resultCode, i);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.dialog_btn_time:
                btnDate.setClickable(true);
                btnTime.setClickable(false);
                switcher.showNext();
                break;
            case R.id.dialog_btn_date:
                btnDate.setClickable(false);
                btnTime.setClickable(true);
                switcher.showPrevious();
                break;
        }
    }
}
