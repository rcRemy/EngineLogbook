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
 * Last change: 15.08.2019
 ******************************************************************************/
package ch.hasler.enginelogbook;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOException;
import java.io.InputStream;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class StartFragment extends Fragment implements NavigationView.OnNavigationItemSelectedListener, OnClickListener {

    private DBAdapter mDB;
    private String mSortorder;
    private DrawerLayout mDrawerLayout;
    private NavigationView mDrawer;
    private ActionBarDrawerToggle mToggle;
    private FloatingActionButton mFab;

    private static int CONROD = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        View rootView = inflater.inflate(R.layout.activity_start, container,
                false);

        if (StartActivity.myDB == null) {
            mDB = new DBAdapter(getActivity());
            mDB.open();
        } else {
            mDB = StartActivity.myDB;
        }

        SharedPreferences pref = getActivity().getSharedPreferences(
                "Default_Values", Activity.MODE_PRIVATE);
        mSortorder = pref.getString("Sortorder", DBAdapter.KEY_MAKE);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        updateListView(mSortorder);

        ListView myList = (ListView) getView().findViewById(R.id.listViewMotor);
        myList.setEmptyView(getView().findViewById(android.R.id.empty));
        registerForContextMenu(myList);
        registerListClickCallback(myList);


        mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout, R.string.open, R.string.close);

        mDrawer = (NavigationView) getActivity().findViewById(R.id.left_drawer);
        mDrawer.setNavigationItemSelectedListener(this);

        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        mFab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        mFab.setOnClickListener(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.activity_start, menu);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        // set context menu title to selected motor name
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Cursor cursor = mDB.getEngine(info.id);
        String name = cursor.getString(DBAdapter.COL_NAME);
        menu.setHeaderTitle(name);
        getActivity().getMenuInflater().inflate(R.menu.ctx_menu, menu);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Intent intent = new Intent(getActivity(), PscListActivity.class);
        switch (item.getItemId()) {
            case R.id.settingsmenu_show_pcc:
                intent.putExtra("Fragment", StartActivity.FRAGMENT_PSC);
                startActivity(intent);
                return true;
            case R.id.settingsmenu_show_shaft:
                intent.putExtra("Fragment", StartActivity.FRAGMENT_SHAFT);
                startActivity(intent);
                return true;
            case R.id.settingsmenu_show_carburetor:
                intent.putExtra("Fragment", StartActivity.FRAGMENT_CARBURETOR);
                startActivity(intent);
                return true;
            case R.id.settingsmenu_show_plate:
                intent.putExtra("Fragment", StartActivity.FRAGMENT_PLATE);
                startActivity(intent);
                return true;
            case R.id.settingsmenu_show_header:
                intent.putExtra("Fragment", StartActivity.FRAGMENT_HEADER);
                startActivity(intent);
                return true;
            case R.id.settingsmenu_settings:
                Intent prefIntent = new Intent(getActivity(), PrefActivity.class);
                startActivity(prefIntent);
                return true;
            case R.id.settingsmenu_import_export:
                Intent intentImpEx = new Intent(getActivity(),
                        ImportExportActivity.class);
                startActivity(intentImpEx);
                return true;
            case R.id.settingsmenu_info:
                new AlertDialog.Builder(getActivity())
                        .setIcon(R.drawable.piston_log_72)
                        .setTitle(R.string.dia_info_title)
                        .setMessage(getInfoText() + "\n" + getString(R.string.system_version) + "\n\n" + getPrivacyText())
                        //.setMessage(Html.fromHtml(getString(R.string.dia_info) + "<br/>" + getString(R.string.system_version) + "<br/>" + getInfoText()))
                        .setNegativeButton(getString(R.string.send_feedback),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        sendMail();
                                    }

                                })
                        .setPositiveButton(R.string.dia_ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                    }

                                })
/*                        .setNeutralButton(getString(R.string.privacy_policy),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        openBrowser();
                                    }

                                })*/
                        .show();
                return true;
            case R.id.settingsmenu_help:
                openBrowser();
                return true;
        }
        return false;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                Intent intent = new Intent(getActivity(), EngineEditActivity.class);
                intent.putExtra("State", StartActivity.MODE_NEW);
                startActivity(intent);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // enables the navigation toggle button
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
/*            case R.id.menu_add:
                Intent intent = new Intent(getActivity(), EngineEditActivity.class);
                intent.putExtra("State", StartActivity.MODE_NEW);
                startActivity(intent);
                return true;*/
            case R.id.menu_sort:
                SharedPreferences prefsort = getActivity().getSharedPreferences(
                        "Default_Values", Activity.MODE_PRIVATE);
                String[] sortorder = {DBAdapter.KEY_MAKE, DBAdapter.KEY_NAME,
                        DBAdapter.KEY_STATUS};
                String[] orderbyitem = {getString(R.string.hint_make),
                        getString(R.string.hint_name),
                        getString(R.string.hint_status)};
                SharedPreferences.Editor editor = prefsort.edit();
                int pos = 0;
                while (!mSortorder.equals(sortorder[pos])) {
                    pos++;
                }
                if (pos == (sortorder.length - 1)) {
                    pos = 0;
                    mSortorder = sortorder[pos];
                } else {
                    pos++;
                    mSortorder = sortorder[pos];
                }
                editor.putString("Sortorder", mSortorder);
                editor.commit();
                updateListView(mSortorder);
                Toast.makeText(getActivity(),
                        getString(R.string.toast_sortby) + orderbyitem[pos],
                        Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();

        switch (item.getItemId()) {
            case R.id.ctxmenu_edit:
                Intent intent = new Intent(getActivity(), EngineEditActivity.class);
                intent.putExtra("State", StartActivity.MODE_EDIT);
                intent.putExtra("EngineId", info.id);
                startActivity(intent);
                return true;
            case R.id.ctxmenu_delete:
                Cursor engCursor = mDB.getEngine(info.id);
                String name = engCursor.getString(DBAdapter.COL_NAME);
                long pscId = engCursor.getLong(DBAdapter.COL_ENG_PSC_ID);
                showDeleteDialog(name, info.id, pscId);
                engCursor.close();
                return true;
        }

        return super.onContextItemSelected(item);
    }

    @SuppressLint("InflateParams")
    private void showDeleteDialog(String name, final long engId, final long pscId) {
        LayoutInflater li = LayoutInflater.from(getActivity());
        View deleteView = li.inflate(R.layout.dialog_engine_delete, null);
        Cursor pscCursor = mDB.getPsc(pscId);
        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        // alert.setIcon(R.drawable.ic_piston);
        alert.setTitle(name);
        alert.setView(deleteView);
        TextView info = (TextView) deleteView
                .findViewById(R.id.tvDiaEngDelInfo);
        TextView info2line = (TextView) deleteView
                .findViewById(R.id.tvDiaEngDelInfo2line);
        info.setText(R.string.dia_delete_conf);
        info2line.setText(R.string.dia_delete_psc);
        final CheckBox cb = (CheckBox) deleteView.findViewById(R.id.cbDiaEngDelName);
        cb.setText(pscCursor.getString(DBAdapter.COL_PSC_NAME));
        cb.setChecked(true);
        alert.setPositiveButton(R.string.dia_yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Toast.makeText(getActivity().getApplicationContext(),
                                R.string.toast_delete, Toast.LENGTH_SHORT)
                                .show();
                        mDB.deleteEngine(engId);
                        if (cb.isChecked()) {
                            mDB.deletePsc(pscId);
                        }
                        mDB.cleanupEvents();
                        updateListView(mSortorder);
                    }
                });
        alert.setNegativeButton(R.string.dia_no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        alert.show();
    }

    // listener for short item click

    private void registerListClickCallback(ListView myList) {
        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked,
                                    int position, long idInDB) {
                Intent intent = new Intent(getActivity(),
                        EngineEventActivity.class);
                intent.putExtra("State", StartActivity.UTILITY_EVENT);
                intent.putExtra("EventState", StartActivity.MODE_NEW);
                intent.putExtra("EngineId", idInDB);
                startActivity(intent);
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void updateListView(String sortorder) {
        ListView myList = (ListView) getView().findViewById(R.id.listViewMotor);
        Cursor listCursor = mDB.getAllEngines(sortorder);
        getActivity().startManagingCursor(listCursor);
        myList.setAdapter(new MyListAdapter(getActivity(), listCursor));
    }

    // own custom list adapter class, allows to handle each entry different
    private class MyListAdapter extends CursorAdapter {

        @SuppressWarnings("deprecation")
        public MyListAdapter(Context context, Cursor cursor) {
            super(context, cursor);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            // when the view will be created for first time,
            // we need to tell the adapters, how each item will look
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View retView = inflater.inflate(R.layout.item_enginelist, parent,
                    false);

            return retView;
        }

        @Override
        public void bindView(View view, Context context, Cursor cEng) {

            long pscId = cEng.getLong(DBAdapter.COL_ENG_PSC_ID);
            Cursor cPsc = mDB.getPsc(pscId);

            // sets content of the text views in the motor item view
            TextView tv = (TextView) view.findViewById(R.id.tvItemEngineName);
            tv.setText(cEng.getString(DBAdapter.COL_NAME));

            tv = (TextView) view.findViewById(R.id.tvItemEngineMake);
            tv.setText(cEng.getString(DBAdapter.COL_MAKE));

            tv = (TextView) view.findViewById(R.id.tvItemEngineStatus);
            String[] statusArr = getResources().getStringArray(
                    R.array.engine_status);
            tv.setText(statusArr[cPsc.getInt(DBAdapter.COL_PSC_STATUS)]);

            tv = (TextView) view.findViewById(R.id.tvItemEngineClutch);
            String clutchValue = cEng.getString(DBAdapter.COL_CLUTCH);
            tv.setText(clutchValue);

            tv = (TextView) view.findViewById(R.id.tvItemEngineSpring);
            String springValue = cEng.getString(DBAdapter.COL_SPRING);
            tv.setText(springValue);

            tv = (TextView) view.findViewById(R.id.tvItemEnginePlug);
            String plugValue = cEng.getString(DBAdapter.COL_PLUG);
            tv.setText(plugValue);

            tv = (TextView) view.findViewById(R.id.tvItemEngineBuiltIn);
            if (pscId == Integer.MAX_VALUE) {
                tv.setText(getString(R.string.txt_none));
            } else {
                tv.setText(cPsc.getString(DBAdapter.COL_PSC_NAME));
            }

            tv = (TextView) view.findViewById(R.id.tvItemEngineClearance);
            String clearanceValue = cEng.getString(DBAdapter.COL_CLEARANCE);
            tv.setText(clearanceValue);

            tv = (TextView) view.findViewById(R.id.tvItemEngineTension);
            String tensionValue = cEng.getString(DBAdapter.COL_TENSION);
            tv.setText(tensionValue);

            tv = (TextView) view.findViewById(R.id.tvItemEngineFuel);
/*            int initialFuel = 0;
              if (pscId != Integer.MAX_VALUE) {
                initialFuel = cPsc.getInt(DBAdapter.COL_PSC_FUEL);
            }*/
            int initialFuel = cEng.getInt(DBAdapter.COL_FUEL);
            long motorID = cEng.getLong(DBAdapter.COL_ROWID);
            int totalFuel = mDB.getEventSumFuel(motorID);
            totalFuel = totalFuel + initialFuel;
            tv.setText(totalFuel + getString(R.string.txt_ml));

            tv = (TextView) view.findViewById(R.id.tvItemEngineConrodFuel);
            if (pscId != Integer.MAX_VALUE) {
                int conrodFuelValue = mDB.getEventPscPartFuel(pscId, CONROD);
                if (mDB.getEventPscLastPartchange(pscId, CONROD) == 0) {
                    conrodFuelValue = conrodFuelValue + initialFuel;
                }
                tv.setText(conrodFuelValue + getString(R.string.txt_ml));

                // sets the fuel text color according to its value

                int limits = cPsc.getInt(DBAdapter.COL_PSC_LIMITS);
                if (conrodFuelValue > limits) {
                    tv.setTextColor(getResources().getColor(R.color.red));
                    tv = (TextView) view
                            .findViewById(R.id.lblItemEngineConrodFuel);
                    tv.setTextColor(getResources().getColor(R.color.red));
                } else {
                    tv.setTextColor(getResources().getColor(R.color.holo_green_dark));
                    tv = (TextView) view
                            .findViewById(R.id.lblItemEngineConrodFuel);
                    tv.setTextColor(getResources().getColor(R.color.holo_green_dark));
                }


                ProgressBar pb = (ProgressBar) view.findViewById(R.id.progressBar);
                pb.setProgress((int) ((double) conrodFuelValue / (double) limits * 100));
            } else {
                tv.setText(getString(R.string.txt_none));
            }

            TextView notes = (TextView) view.findViewById(R.id.tvItemEngineNotes);
            String text = cEng.getString(DBAdapter.COL_NOTES);
            String textPsc = cPsc.getString(DBAdapter.COL_PSC_NOTES);

            // show notes only when something is in it
            if ((text.length() == 0) && (textPsc.length() == 0)) {
                notes.setVisibility(GONE);
            } else {
                if ((text.length() > 0) && (textPsc.length() == 0)) {
                    notes.setText(text);
                }
                if ((text.length() == 0) && (textPsc.length() > 0)) {
                    notes.setText(getString(R.string.txt_psc_set) + textPsc);
                }
                if ((text.length() > 0) && (textPsc.length() > 0)) {
                    notes.setText(text + "\n" + getString(R.string.txt_psc_set) + textPsc);
                }
                notes.setVisibility(VISIBLE);
            }

/*            // alternating the background color
            int pos = cEng.getPosition();
            switch (pos % 2) {
                case 0:
                    view.setBackgroundColor(getResources().getColor(
                            R.color.bg_transparent_white));
                    break;
                default:
                    view.setBackgroundColor(getResources().getColor(
                            R.color.bg_transparent_black));
                    break;
            }*/
            cPsc.close();
        }
    }

    private void sendMail() {
        Intent i = new Intent(Intent.ACTION_SEND_MULTIPLE);

        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL,
                new String[]{getString(R.string.mail_hasler)});
        i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_subject));
        try {
            startActivity(Intent.createChooser(i,
                    getString(R.string.mail_title)));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getActivity().getApplicationContext(),
                    getString(R.string.mail_noclients), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void openBrowser() {
        Intent i = new Intent((Intent.ACTION_VIEW), Uri.parse(getString(R.string.url_help)));
        startActivity(i);
    }

    private String getPrivacyText() {
        String tContents = "";
        try {
            InputStream stream = getResources().openRawResource(R.raw.privacy);
            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();
            tContents = new String(buffer);
        } catch (IOException ex) {
        }
        return tContents;
    }

    private String getInfoText() {
        String tContents = "";
        try {
            InputStream stream = getResources().openRawResource(R.raw.info);
            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();
            tContents = new String(buffer);
        } catch (IOException ex) {
        }
        return tContents;
    }
}
