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

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

public class IntEditTextPreference extends EditTextPreference {

	public IntEditTextPreference(Context context) {
		super(context);
	}

	public IntEditTextPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public IntEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected String getPersistedString(String defaultReturnValue) {
		return String.valueOf(getPersistedInt(-1));
	}

	@Override
	protected boolean persistString(String value) {
		if (value.length() != 0) {
			return persistInt(Integer.valueOf(value));
		} else {
			return false;
		}
	}
}
