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
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class PscEventsFragment extends Fragment implements View.OnClickListener {

    private DBAdapter mDB;
    private FloatingActionButton mFab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View rootView = inflater.inflate(R.layout.fragment_events, container,
                false);

        if (StartActivity.myDB == null) {
            mDB = new DBAdapter(getActivity());
            mDB.open();
        } else {
            mDB = StartActivity.myDB;
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        updateListView();

        ListView myList = (ListView) getView().findViewById(R.id.listViewEvent);
        myList.setEmptyView(getView().findViewById(android.R.id.empty));

        registerForContextMenu(myList);

        final InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);

        mFab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        mFab.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateListView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // inflate the menu, this adds items to the action bar if it is present.
        inflater.inflate(R.menu.frag_events, menu);
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
/*            case R.id.fragevent_add:
                long pscId = getActivity().getIntent().getExtras().getLong("PscId");
                Intent intent = new Intent(getActivity(), PscEventEditActivity.class);
                intent.putExtra("EventState", StartActivity.MODE_NEW);
                intent.putExtra("PscId", pscId);
                startActivity(intent);
                return true;*/
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        // set context menu title to selected event action
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Cursor cursor = mDB.getEvent(info.id);
        int action = cursor.getInt(DBAdapter.COL_ACTION_ID);
        String[] actionArr = getResources().getStringArray(
                R.array.engine_actions);
        menu.setHeaderTitle(actionArr[action]);
        getActivity().getMenuInflater().inflate(R.menu.ctx_pscevent, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();

        switch (item.getItemId()) {
/*            case R.id.ctxpsc_edit:
                long pscId = getActivity().getIntent().getExtras().getLong("PscId");
                Intent intent = new Intent(getActivity(), PscEventEditActivity.class);
                intent.putExtra("EventState", StartActivity.MODE_EDIT);
                intent.putExtra("EventId", info.id);
                intent.putExtra("PscId", pscId);
                startActivity(intent);
                return true;*/
            case R.id.ctxpsc_delete:
                mDB.deleteEvent(info.id);
                updateListView();
                return true;
        }

        // cursor.close();
        return super.onContextItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                long pscId = getActivity().getIntent().getExtras().getLong("PscId");
                Intent intent = new Intent(getActivity(), PscEventEditActivity.class);
                intent.putExtra("EventState", StartActivity.MODE_NEW);
                intent.putExtra("PscId", pscId);
                startActivity(intent);
                break;
        }
    }

    @SuppressWarnings("deprecation")
    private void updateListView() {
        ListView myList = (ListView) getView().findViewById(R.id.listViewEvent);
        Cursor listCursor = mDB.getEventPsc(getActivity().getIntent()
                .getExtras().getLong("PscId"));
        getActivity().startManagingCursor(listCursor);
        myList.setAdapter(new MyListAdapter(getActivity(), listCursor));
    }

    // own custom list adapter class, allows to handle each entry different
    private class MyListAdapter extends ResourceCursorAdapter {

        @SuppressWarnings("deprecation")
        public MyListAdapter(Context context, Cursor cursor) {
            super(context, R.layout.item_eventlist, cursor);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            // sets content of the text views in the engine event view
            TextView dateTxt = (TextView) view.findViewById(R.id.tvItemEventDate);
            long unixTime = cursor.getLong(DBAdapter.COL_EVENT_DATE);
            Date date = new Date(unixTime);
            DateFormat formattedDate = DateFormat.getDateTimeInstance(
                    DateFormat.DEFAULT, DateFormat.SHORT, Locale.getDefault());
            dateTxt.setText(formattedDate.format(date));

            TextView place = (TextView) view.findViewById(R.id.tvItemEventPlace);
            place.setText(cursor.getString(DBAdapter.COL_EVENT_PLACE));

            TextView action = (TextView) view.findViewById(R.id.tvItemEventAction);
            String[] actionArr = getResources().getStringArray(
                    R.array.engine_actions);
            int actionKind = cursor.getInt(DBAdapter.COL_ACTION_ID);
            action.setText(actionArr[actionKind]);

            TextView info = (TextView) view.findViewById(R.id.tvItemEventInfo);
            switch (actionKind) {
                case 0: // refuel
                    int fuelValue = cursor.getInt(DBAdapter.COL_EVENT_FUEL);
                    info.setText(fuelValue + getString(R.string.txt_ml));
                    break;
                case 4: // psc exchange
                    long engIdX = cursor.getLong(DBAdapter.COL_ENGINE_ID);
                    Cursor engCX = mDB.getEngine(engIdX);
                    info.setText(engCX.getString(DBAdapter.COL_PSC_NAME));
                    engCX.close();
                    break;
                default: // in any other case
                    String[] partArr = getResources().getStringArray(
                            R.array.engine_parts);
                    info.setText(partArr[cursor.getInt(DBAdapter.COL_EVENT_PART)]);
                    break;
            }

            TextView engine = (TextView) view.findViewById(R.id.tvItemEngineInfo);
            long engineId = cursor.getLong(DBAdapter.COL_ENGINE_ID);
            Cursor engC = mDB.getEngine(engineId);
            if (engineId != Integer.MAX_VALUE) {
                engine.setText(getString(R.string.txt_action_in) + engC.getString(DBAdapter.COL_NAME));
            } else {
                engine.setText(getString(R.string.txt_engine_destroyed));
            }
            engC.close();

            TextView psc = (TextView) view.findViewById(R.id.tvItemPscInfo);
            psc.setVisibility(GONE);

            TextView notes = (TextView) view.findViewById(R.id.tvItemEventNotes);
            String text = cursor.getString(DBAdapter.COL_EVENT_NOTES);
            if (text.length() == 0) { // show notes only when something is in it
                notes.setVisibility(GONE);
            } else {
                notes.setText(text);
                notes.setVisibility(VISIBLE);
            }

            // set Lock image to invisible
            ImageView lockView = (ImageView) view.findViewById(R.id.imageViewLock);
            lockView.setVisibility(GONE);
/*            // alternating the background color
            int pos = cursor.getPosition();
            switch (pos % 2) {
                case 0:
                    view.setBackgroundColor(ContextCompat.getColor(context,
                            R.color.bg_transparent_white));
                    break;
                default:
                    view.setBackgroundColor(ContextCompat.getColor(context,
                            R.color.bg_transparent_black));
                    break;
            }*/

        }
    }
}
