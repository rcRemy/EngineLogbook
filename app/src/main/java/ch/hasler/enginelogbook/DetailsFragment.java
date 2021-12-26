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
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

public class DetailsFragment extends Fragment {

	private DBAdapter mDB;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);

		View rootView = inflater.inflate(R.layout.fragment_details, container,
				false);

		if (StartActivity.myDB == null) {
			mDB = new DBAdapter(getActivity());
			mDB.open();
		} else {
			mDB = StartActivity.myDB;
		}

		populateDetails(rootView);

		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// hide soft keyboard
		final InputMethodManager imm = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
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

	private void populateDetails(View v) {
		Cursor cEng = mDB.getEngine(getActivity().getIntent().getExtras()
				.getLong("ID"));
		if (cEng.moveToFirst()) {
			String name = cEng.getString(DBAdapter.COL_NAME);
			String make = cEng.getString(DBAdapter.COL_MAKE);
			long saledate = cEng.getLong(DBAdapter.COL_DATE);
			long pscId = cEng.getLong(DBAdapter.COL_ENG_PSC_ID);
			String pscName = "None";
			int limits = cEng.getInt(DBAdapter.COL_LIMITS);
			if (pscId != Integer.MAX_VALUE) {
				Cursor cPsc = mDB.getPsc(pscId);
				pscName = cPsc.getString(DBAdapter.COL_PSC_NAME);
				limits = cPsc.getInt(DBAdapter.COL_PSC_LIMITS);
				cPsc.close();
			}
			int status = cEng.getInt(DBAdapter.COL_STATUS);
			int consumption = cEng.getInt(DBAdapter.COL_CONSUMPTION);
			String compression = cEng.getString(DBAdapter.COL_COMPRESSION);
			String plug = cEng.getString(DBAdapter.COL_PLUG);
			String clutch = cEng.getString(DBAdapter.COL_CLUTCH);
			String shoe = cEng.getString(DBAdapter.COL_SHOE);
			String spring = cEng.getString(DBAdapter.COL_SPRING);
			String tension = cEng.getString(DBAdapter.COL_TENSION);
			String play = cEng.getString(DBAdapter.COL_AXIALPLAY);
			String clearance = cEng.getString(DBAdapter.COL_CLEARANCE);
			String notes = cEng.getString(DBAdapter.COL_NOTES);

			TextView tvName = (TextView) v.findViewById(R.id.tvDetailName);
			TextView tvMake = (TextView) v.findViewById(R.id.tvDetailMake);
			TextView tvDate = (TextView) v.findViewById(R.id.tvDetailDate);
			TextView tvFuel = (TextView) v.findViewById(R.id.tvDetailFuel);
			TextView tvStatus = (TextView) v.findViewById(R.id.tvDetailStatus);
			TextView tvPscName = (TextView) v.findViewById(R.id.tvDetailPsc);
			TextView tvLimit = (TextView) v.findViewById(R.id.tvDetailLimit);
			TextView tvConsumption = (TextView) v
					.findViewById(R.id.tvDetailConsumption);
			TextView tvCompression = (TextView) v
					.findViewById(R.id.tvDetailCompression);
			TextView tvPlug = (TextView) v.findViewById(R.id.tvDetailPlug);
			TextView tvClutch = (TextView) v.findViewById(R.id.tvDetailClutch);
			TextView tvShoe = (TextView) v.findViewById(R.id.tvDetailShoe);
			TextView tvSpring = (TextView) v.findViewById(R.id.tvDetailSpring);
			TextView tvTension = (TextView) v
					.findViewById(R.id.tvDetailTension);
			TextView tvPlay = (TextView) v.findViewById(R.id.tvDetailPlay);
			TextView tvClearance = (TextView) v
					.findViewById(R.id.tvDetailClearance);
			TextView tvNotes = (TextView) v.findViewById(R.id.tvDetailNotes);

			tvName.setText(name);
			tvMake.setText(make);
			tvDate.setText(unixTimeToString(saledate));
			int initialFuel = cEng.getInt(cEng
					.getColumnIndex(DBAdapter.KEY_FUEL));
			long motorID = cEng.getLong(cEng
					.getColumnIndex(DBAdapter.KEY_ROWID));
			int totalFuel = mDB.getEventSumFuel(motorID);
			totalFuel = totalFuel + initialFuel;
			tvFuel.setText(String.valueOf(totalFuel));

			tvStatus.setText(getResources().getStringArray(
					R.array.engine_status)[status]);
			tvPscName.setText(pscName);
			tvLimit.setText(String.valueOf(limits));
			tvConsumption.setText(String.valueOf(consumption));
			tvCompression.setText(compression);
			tvPlug.setText(plug);
			tvClutch.setText(clutch);
			tvShoe.setText(shoe);
			tvSpring.setText(spring);
			tvTension.setText(tension);
			tvPlay.setText(play);
			tvClearance.setText(clearance);
			tvNotes.setText(notes);
		}
		cEng.close();
	}

	private String unixTimeToString(long timeMS) {
		Date date = new Date(timeMS);
		DateFormat formattedDate = DateFormat.getDateInstance(
				DateFormat.DEFAULT, Locale.getDefault());
		return formattedDate.format(date);
	}
}
