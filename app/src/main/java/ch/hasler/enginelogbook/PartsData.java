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

public class PartsData {
	private int mPartId;
	private int mPartNbr;
	private long mDate;
	private int mFuel;
	private String mNotes;

	// empty constructor
	public PartsData() {
	}

	// constructor
	public PartsData(int partId, int partNbr, long date, int fuel, String notes) {
		this.mPartId = partId;
		this.mPartNbr = partNbr;
		this.mDate = date;
		this.mFuel = fuel;
		this.mNotes = notes;
	}

	public int get_partid() {
		return mPartId;
	}

	public void set_partid(int partId) {
		this.mPartId = partId;
	}

	public int get_partnbr() {
		return mPartNbr;
	}

	public void set_partnbr(int partNbr) {
		this.mPartNbr = partNbr;
	}

	public long get_date() {
		return mDate;
	}

	public void set_date(long date) {
		this.mDate = date;
	}

	public int get_fuel() {
		return mFuel;
	}

	public void set_fuel(int fuel) {
		this.mFuel = fuel;
	}

	public String get_notes() {
		return mNotes;
	}

	public void set_notes(String notes) {
		this.mNotes = notes;
	}
}
