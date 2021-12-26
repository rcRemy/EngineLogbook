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

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.AppCompatActivity;

public class EngineEventActivity extends AppCompatActivity implements
        ActionBar.TabListener {

    private ViewPager mViewPager;
    private TabsPagerAdapter mAdapter;
    private ActionBar mActionBar;
    private DBAdapter mDB;
    private String[] mTabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_engine_event);

        if (StartActivity.myDB == null) {
            mDB = new DBAdapter(this);
            mDB.open();
        } else {
            mDB = StartActivity.myDB;
        }

        // initialization
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mActionBar = getSupportActionBar();
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mAdapter);
        mActionBar.setHomeButtonEnabled(false);
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // adding Tabs
        mTabs = getResources().getStringArray(R.array.tabs);
        for (String tab_name : mTabs) {
            mActionBar.addTab(mActionBar.newTab().setText(tab_name)
                    .setTabListener(this));
        }

        mViewPager
                .setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

                    @Override
                    public void onPageSelected(int position) {
                        // when swiping between pages, select the corresponding
                        // tab
                        getSupportActionBar().setSelectedNavigationItem(position);
                        // this line will force all pages to be loaded fresh
                        // when changing between fragments
                        mAdapter.notifyDataSetChanged();

/*                        if (position != 0) {
                            getIntent().putExtra("EventState", StartActivity.MODE_NEW);
                            getSupportActionBar().getTabAt(0).setText(mTabs[0]);
                        }*/
                    }
                });

        // set the activity title to the selected engine name
        Cursor cEng = mDB.getEngine(getIntent().getExtras().getLong("EngineId"));
        long pscId = cEng.getLong(DBAdapter.COL_ENG_PSC_ID);
        Cursor cPsc = mDB.getPsc(pscId);
        if (cEng.moveToFirst()) {
            mActionBar.setTitle(cEng.getString(DBAdapter.COL_NAME));
            mActionBar.setSubtitle(cPsc.getString(DBAdapter.COL_PSC_NAME));
        }
        cEng.close();
    }

/*    @Override
    public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        mViewPager.setCurrentItem(tab.getPosition());
        // this line will force all pages to be loaded fresh when changing
        // between fragments
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
    }*/

    void switchFragment(int target) {
        mViewPager.setCurrentItem(target);
    }

    @Override
    public void onTabSelected(Tab tab, android.support.v4.app.FragmentTransaction ft) {
        mViewPager.setCurrentItem(tab.getPosition());
        // this line will force all pages to be loaded fresh when changing
        // between fragments
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTabUnselected(Tab tab, android.support.v4.app.FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(Tab tab, android.support.v4.app.FragmentTransaction ft) {

    }
}
