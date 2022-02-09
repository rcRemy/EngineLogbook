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

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

// Github Test 1

public class StartActivity extends AppCompatActivity {

    public static final int MODE_NEW = 0;
    public static final int MODE_EDIT = 1;
    // public static final int UTILITY_ENGINE = 2;
    public static final int UTILITY_EVENT = 3;
    public static final int FRAGMENT_PSC = 4;
    public static final int FRAGMENT_SHAFT = 5;
    public static final int FRAGMENT_CARBURETOR = 6;
    public static final int FRAGMENT_PLATE = 7;
    public static final int FRAGMENT_HEADER = 8;

    static DBAdapter myDB;

    protected int getLayoutResId() {
        // return R.layout.activity_fragment_container;
        return R.layout.activity_masterdetail;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("StartActivity", "onCreate");
        // setContentView(R.layout.activity_fragment_container);
        setContentView(getLayoutResId());

        // only to rename the database from the test versions
        // if (getDatabasePath("MyDb").exists()) {
        // File pathDB = getDatabasePath("MyDb");
        // pathDB.renameTo(getDatabasePath(DBAdapter.DATABASE_NAME));
        // }
        // if (getDatabasePath("MyDb-journal").exists()) {
        // File pathDB = getDatabasePath("MyDb-journal");
        // pathDB.renameTo(getDatabasePath(DBAdapter.DATABASE_NAME +
        // "-journal"));
        // }

        openDB();

        // check permission for external storage during runtime, needs to be done for target sdk api levels >=23 (Marshmallow)
        if (Build.VERSION.SDK_INT >= 23) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        // only used for update to database 26 and 27
        myDB.updateTo26();
        myDB.updateTo27();

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);

        if (fragment == null) {
            fragment = new StartFragment();
            fm.beginTransaction().add(R.id.fragmentContainer, fragment)
                    .commit();
        }
    }


    private void openDB() {
        myDB = new DBAdapter(this);
        // myDB = DBAdapter.getInstance(this);
        myDB.open();
    }

    private void closeDB() {
        myDB.close();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeDB();
    }
}
