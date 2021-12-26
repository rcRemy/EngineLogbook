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
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

public class PartsFragment extends Fragment {
	private DBAdapter mDB;
	private String[] mParts;
	private ExpandableListView mExpListView;
	private ArrayList<PartsData> mDataListGroup;
	private ArrayList<ArrayList<PartsData>> mDataListChild;

	// private static int PLUG = 0;
	private static int CONROD = 1;
	// private static int FRONTBEARING = 2;
	// private static int REARBEARING = 3;
	private static int PISTON = 4;
	private static int SLEEVE = 5;
	private static int UNDERHEAD = 6;
	// private static int CRANKSHAFT = 7;
	private static int PISTONROD = 8;
	private static int PISTONRODCLIPS = 9;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		long engineId = getActivity().getIntent().getExtras().getLong("EngineId");
		if (StartActivity.myDB == null) {
			mDB = new DBAdapter(getActivity());
			mDB.open();
		} else {
			mDB = StartActivity.myDB;
		}
		mDataListGroup = new ArrayList<>();
		mDataListChild = new ArrayList<>();

		mParts = getResources().getStringArray(R.array.engine_parts);

		Cursor cEng = mDB.getEngine(engineId);
		long pscId = cEng.getLong(DBAdapter.COL_ENG_PSC_ID);
		ArrayList<Integer> pscPart = new ArrayList<>();
		if (pscId != Integer.MAX_VALUE) {
			pscPart.add(CONROD);
			pscPart.add(PISTON);
			pscPart.add(SLEEVE);
			pscPart.add(PISTONROD);
			pscPart.add(PISTONRODCLIPS);
			Cursor cPsc = mDB.getPsc(pscId);
			if (cPsc.getInt(DBAdapter.COL_PSC_HEADER) > 0) {
				pscPart.add(UNDERHEAD);
			}
		}

		for (int x = 0; x < mParts.length; x++) {
			// get all of the data of a part group for child data
			ArrayList<PartsData> DataList;
			if (pscPart.contains(x)) {
				DataList = mDB.getPscPartsDataList(getActivity(), pscId, x);
			} else {
				DataList = mDB.getPartsDataList(getActivity(), engineId, x);
			}
			mDataListChild.add(DataList);

			PartsData item;
			// get the data of newest part for group title
			item = DataList.get(0);
			mDataListGroup.add(item);
		}
		cEng.close();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// get the list view
		mExpListView = (ExpandableListView) getView().findViewById(
				R.id.elvParts);
		ExpandableListAdapter adapter = new ExpandableListAdapter(
				mDataListGroup, mDataListChild);
		mExpListView.setAdapter(adapter);

		// hide soft keyboard
		final InputMethodManager imm = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		View rootView = inflater.inflate(R.layout.fragment_parts, container,
				false);

		return rootView;
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

	private class ExpandableListAdapter extends BaseExpandableListAdapter {

		// header titles array
		private ArrayList<PartsData> _listDataHeader;
		// child data list array
		private ArrayList<ArrayList<PartsData>> _listDataChild;

		DateFormat formatedDate = DateFormat.getDateInstance(
				DateFormat.DEFAULT, Locale.getDefault());

		public ExpandableListAdapter(ArrayList<PartsData> dataList,
				ArrayList<ArrayList<PartsData>> listChildData) {
			this._listDataHeader = dataList;
			this._listDataChild = listChildData;
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			ArrayList<PartsData> alPd = this._listDataChild.get(groupPosition);
			return alPd.get(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public View getChildView(int groupPosition, final int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {

			final PartsData childData = (PartsData) getChild(groupPosition,
					childPosition);

			if (convertView == null) {
				LayoutInflater infalInflater = (LayoutInflater) getActivity()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = infalInflater.inflate(
						R.layout.item_partslist_child, null);
			}

			TextView tv = (TextView) convertView
					.findViewById(R.id.tvItemPartchildNbr);
			tv.setText(getString(R.string.txt_parts_nbr)
					+ String.valueOf(childData.get_partnbr()));

			tv = (TextView) convertView.findViewById(R.id.tvItemPartchildFuel);
			tv.setText(String.valueOf(childData.get_fuel())
					+ getString(R.string.txt_ml));

			tv = (TextView) convertView.findViewById(R.id.tvItemPartchildDate);
			tv.setText(formatedDate.format(childData.get_date()));

			tv = (TextView) convertView.findViewById(R.id.tvItemPartchildNotes);
			// show notes only when something is in it
			if (childData.get_notes().length() == 0) {
				tv.setVisibility(View.GONE);
			} else {
				tv.setText(childData.get_notes());
				tv.setVisibility(View.VISIBLE);
			}
			convertView.setBackgroundColor(0x8833b5e5);
			return convertView;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return this._listDataChild.get(groupPosition).size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return this._listDataHeader.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return this._listDataHeader.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(
						R.layout.item_partslist, null);
			}

			// configure the view for this data
			PartsData d = (PartsData) getGroup(groupPosition);

			TextView tv = (TextView) convertView
					.findViewById(R.id.tvItemPartsPart);
			tv.setText(mParts[d.get_partid()]);

			tv = (TextView) convertView.findViewById(R.id.tvItemPartsChanges);
			tv.setText(getString(R.string.txt_parts_nbr) + d.get_partnbr());

			tv = (TextView) convertView.findViewById(R.id.tvItemPartsFuel);
			tv.setText(d.get_fuel() + getString(R.string.txt_ml));

			tv = (TextView) convertView.findViewById(R.id.lblItemPartsDate);
			Date date = new Date(d.get_date());
			tv.setText(formatedDate.format(date));

/*			// alternating the background color
			switch (groupPosition % 2) {
			case 0:
				convertView.setBackgroundColor(0x22ffffff);
				break;
			default:
				convertView.setBackgroundColor(0x22000000);
				break;
			}*/
			return convertView;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}
	}
}
