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

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

public class ExcelDialogFragment extends DialogFragment {
	public static ExcelDialogFragment newInstance() {
		ExcelDialogFragment fragment = new ExcelDialogFragment();
		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		File sd = Environment.getExternalStorageDirectory();
		File xlsFile = new File(sd, getString(R.string.filename_xls));

		View v = getActivity().getLayoutInflater().inflate(
				R.layout.dialog_text, null);
		TextView tv = (TextView) v.findViewById(R.id.tvDiaInfo);
		StringBuffer str = new StringBuffer();

		if (xlsFile.exists()) {
			str.append(getString(R.string.dia_xls_exist));
			tv.setText(Html.fromHtml(str.toString()));
			final AlertDialog d = new AlertDialog.Builder(getActivity())
					.setView(v).setTitle(R.string.dia_title_excel)
					.setPositiveButton(R.string.dia_ok, new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							sendResult(Activity.RESULT_OK);
						}
					})
					// do nothing, just dismiss dialog
					.setNegativeButton(R.string.dia_cancel, null).create();
			d.show();
			return d;
		} else {
			str.append(getString(R.string.dia_xls_not_exist));
			tv.setText(Html.fromHtml(str.toString()));
			final AlertDialog d = new AlertDialog.Builder(getActivity())
					.setView(v).setTitle(R.string.dia_title_excel)
					.setPositiveButton(R.string.dia_ok, new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							sendResult(Activity.RESULT_OK);
						}
					}).create();
			d.show();
			return d;
		}
	}

	private void sendResult(int resultCode) {
		if (getTargetFragment() == null)
			return;

		Intent i = new Intent();
		getTargetFragment().onActivityResult(getTargetRequestCode(),
				resultCode, i);
	}
}
