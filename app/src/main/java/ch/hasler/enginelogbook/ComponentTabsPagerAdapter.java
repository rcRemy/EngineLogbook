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

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class ComponentTabsPagerAdapter extends FragmentStatePagerAdapter {

    public ComponentTabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int index) {

        switch (index) {
            case 0:
                // Events fragment activity
                return new ComponentEventsFragment();
            case 1:
                // Details fragment activity
                return new ComponentEditFragment();
        }

        return null;
    }

    //	simulate a "setOffscreenPageLimit(0)"
    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 3;
    }

}
