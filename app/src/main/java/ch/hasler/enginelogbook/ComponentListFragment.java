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

import android.app.AlertDialog;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ComponentListFragment extends Fragment implements View.OnClickListener {

    private DBAdapter mDB;
    private FloatingActionButton mFab;

    private int componentCode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        //getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

        View rootView = inflater.inflate(R.layout.activity_psc_start, container,
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

        updateListView(DBAdapter.KEY_MAKE);

        ListView myList = (ListView) getView().findViewById(R.id.listViewMotor);
        myList.setEmptyView(getView().findViewById(android.R.id.empty));
        registerForContextMenu(myList);
        registerListClickCallback(myList);

        mFab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        mFab.setOnClickListener(this);

        switch (getActivity().getIntent().getExtras().getInt("Fragment")) {
            case StartActivity.FRAGMENT_SHAFT:
                getActivity().setTitle(getString(R.string.title_activity_shaft_list));
                componentCode = DBAdapter.CRANKSHAFT;
                break;
            case StartActivity.FRAGMENT_CARBURETOR:
                getActivity().setTitle(getString(R.string.title_activity_carburetor_list));
                componentCode = DBAdapter.CARBURETOR;
                break;
            case StartActivity.FRAGMENT_PLATE:
                getActivity().setTitle(getString(R.string.title_activity_plate_list));
                componentCode = DBAdapter.END_PLATE;
                break;
            case StartActivity.FRAGMENT_HEADER:
                getActivity().setTitle(getString(R.string.title_activity_header_list));
                componentCode = DBAdapter.UNDER_HEAD;
                break;
        }
    }

/*    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.frag_pcc_list, menu);
    }*/

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        // set context menu title to selected psc name
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Cursor cursor = mDB.getComponent(info.id);
        String name = cursor.getString(DBAdapter.COL_COMPONENT_NAME);
        menu.setHeaderTitle(name);
        getActivity().getMenuInflater().inflate(R.menu.ctx_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (NavUtils.getParentActivityName(getActivity()) != null) {
                    NavUtils.navigateUpFromSameTask(getActivity());
                }
                return true;
/*            case R.id.pcc_menu_add:
                Cursor c = mDB.getAllEngines(DBAdapter.KEY_MAKE);
                if (c.getCount() > 0) {
                    Intent intent = new Intent(getActivity(), PscEditActivity.class);
                    intent.putExtra("State", StartActivity.MODE_NEW);
                    startActivity(intent);
                } else {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(
                            getActivity());
                    builder1.setTitle(R.string.dia_title_no_engine);
                    builder1.setMessage(R.string.dia_no_engine);
                    builder1.setCancelable(true);
                    builder1.setPositiveButton(R.string.dia_ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                }
                return true;*/
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                Cursor c = mDB.getAllEngines(DBAdapter.KEY_MAKE);
                if (c.getCount() > 0) {
                    Intent intent = new Intent(getActivity(), PscEditActivity.class);
                    intent.putExtra("State", StartActivity.MODE_NEW);
                    startActivity(intent);
                } else {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(
                            getActivity());
                    builder1.setTitle(R.string.dia_title_no_engine);
                    builder1.setMessage(R.string.dia_no_engine);
                    builder1.setCancelable(true);
                    builder1.setPositiveButton(R.string.dia_ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                }
                break;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        Cursor cursor = mDB.getComponent(info.id);

        switch (item.getItemId()) {
            case R.id.ctxmenu_edit:
                Intent intent = new Intent(getActivity(), PscEditActivity.class);
                intent.putExtra("State", StartActivity.MODE_EDIT);
                intent.putExtra("ComponentId", info.id);
                startActivity(intent);
                return true;
            case R.id.ctxmenu_delete:
                String name = cursor.getString(DBAdapter.COL_COMPONENT_NAME);
                if (mDB.getWherePscBuiltin(info.id) == Integer.MAX_VALUE) {
                    showDeleteDialog(name, info.id);
                } else {
                    showDeleteNotPossibleDialog(name);
                }

                return true;
        }

        cursor.close();
        return super.onContextItemSelected(item);
    }

    private void showDeleteNotPossibleDialog(String name) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        // alert.setIcon(R.drawable.ic_piston);
        alert.setTitle(name);
        alert.setMessage(R.string.dia_delete_not_poss);
        alert.setPositiveButton(R.string.dia_ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
        alert.show();
    }

    private void showDeleteDialog(String name, long id) {
        final long idDB = id;
        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        // alert.setIcon(R.drawable.ic_piston);
        alert.setTitle(name);
        alert.setMessage(R.string.dia_delete_conf);
        alert.setPositiveButton(R.string.dia_yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Toast.makeText(getActivity().getApplicationContext(),
                                R.string.toast_delete, Toast.LENGTH_SHORT)
                                .show();
                        mDB.deletePsc(idDB);
                        mDB.cleanupEvents();
                        updateListView(DBAdapter.KEY_MAKE);
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
                        ComponentEventActivity.class);
                intent.putExtra("State", StartActivity.UTILITY_EVENT);
                intent.putExtra("PscId", idInDB);
                startActivity(intent);
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void updateListView(String sortOrder) {
        ListView myList = (ListView) getView().findViewById(R.id.listViewMotor);
        Cursor listCursor = mDB.getAllPsc(sortOrder);
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
            View retView = inflater.inflate(R.layout.item_componentlist, parent,
                    false);

            return retView;
        }

        @Override
        public void bindView(View view, Context context, Cursor c) {

            // sets content of the text views in the motor item view
            TextView tv = (TextView) view.findViewById(R.id.tvItemComponentName);
            tv.setText(c.getString(DBAdapter.COL_COMPONENT_NAME));

            int initialFuel = c.getInt(DBAdapter.COL_COMPONENT_FUEL);
            long componentId = c.getLong(DBAdapter.COL_ROWID);
            long engId = mDB.getWherePscBuiltin(componentId);
            String engName;
            if (engId == Integer.MAX_VALUE) {
                engName = getString(R.string.txt_none);
            } else {
                Cursor engCursor = mDB.getEngine(engId);
                engName = engCursor.getString(DBAdapter.COL_NAME);
                engCursor.close();
            }

            tv = (TextView) view.findViewById(R.id.tvItemComponentFuel);
            int conrodFuelValue = mDB.getEventPscPartFuel(componentId, componentCode);
            if (mDB.getEventPscLastPartchange(componentId, componentCode) == 0) {
                conrodFuelValue = conrodFuelValue + initialFuel;
            }
            tv.setText(conrodFuelValue + getString(R.string.txt_ml));
            // sets the fuel text color according to its value
            int limits = c.getInt(DBAdapter.COL_PSC_LIMITS);

            tv = (TextView) view.findViewById(R.id.tvItemComponentBrands);
            tv.setText(c.getString(DBAdapter.COL_COMPONENT_MAKE));

            tv = (TextView) view.findViewById(R.id.tvItemComponentBuiltIn);
            tv.setText(engName);

            TextView notes = (TextView) view.findViewById(R.id.tvItemComponentNotes);
            String text = c.getString(DBAdapter.COL_COMPONENT_NOTES);

            // show notes only when something is in it
            if (text.length() == 0) {
                notes.setVisibility(GONE);
            } else {
                notes.setText(""+componentCode);
                notes.setVisibility(VISIBLE);
            }
        }
    }
}
