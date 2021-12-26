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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HeaderFooter;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class ImportExportFragment extends PreferenceFragment implements
        OnSharedPreferenceChangeListener, OnPreferenceClickListener {

    private DBAdapter mDB;
    private File mActualFilepath;
    private String[] mForbiddenChars = {":", "/", "\\", "*", "?", "[", "]"};

    private static int CONROD = 1;
    private static int PISTON = 4;
    private static int SLEEVE = 5;
    private static int UNDERHEAD = 6;
    private static int PISTONROD = 8;
    private static int PISTONRODCLIPS = 9;

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View lv = getView().findViewById(android.R.id.list);
        if (lv != null) lv.setPadding(0, 0, 0, 0);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
//		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

        // Define the settings file and mode to use by this settings fragment
        this.getPreferenceManager().setSharedPreferencesName("Default_Values");
        this.getPreferenceManager().setSharedPreferencesMode(
                Activity.MODE_PRIVATE);

        mDB = StartActivity.myDB;

        // create folder on external or internal storage
        String storage = getPreferenceManager().getSharedPreferences()
                .getString("Files", "external");

        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)
                && (storage.equals("external"))) {
            storage = "internal";
            Toast.makeText(getActivity().getApplicationContext(),
                    getString(R.string.pref_no_sdcard), Toast.LENGTH_SHORT)
                    .show();
        }

        switch (storage) {
            case "internal":
                mActualFilepath = new File(getActivity().getFilesDir().getParent(),
                        getString(R.string.file_folder));
                break;

            case "external":
                if (Build.VERSION.SDK_INT >= 19) {
                    File[] Dirs = getActivity().getExternalFilesDirs(null);
                    mActualFilepath = Dirs[0];
                } else {
                    mActualFilepath = new File(
                            Environment.getExternalStorageDirectory(),
                            getString(R.string.file_folder));
                }
                break;
            case "sdcard":
                if (Build.VERSION.SDK_INT >= 19) {
                    File[] Dirs = getActivity().getExternalFilesDirs(null);
                    if (Dirs.length > 1) {
                        mActualFilepath = Dirs[1];
                    }
                }
                break;
            default:
                mActualFilepath = new File(
                        Environment.getExternalStorageDirectory(),
                        getString(R.string.file_folder));
        }

        mActualFilepath.mkdir();

        // inflate preference resource file
        addPreferencesFromResource(R.xml.importexport);

        initSummaryLabels();

        // set click listener
        findPreference("Backup").setOnPreferenceClickListener(this);
        findPreference("Restore").setOnPreferenceClickListener(this);
        findPreference("Excel").setOnPreferenceClickListener(this);
        findPreference("Send").setOnPreferenceClickListener(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // inflate the menu, this adds items to the action bar if it is present.
        inflater.inflate(R.menu.frag_import_export, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (NavUtils.getParentActivityName(getActivity()) != null) {
                    NavUtils.navigateUpFromSameTask(getActivity());
                }
                return true;
            case R.id.fragimportexport_info:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(
                        getActivity());
                builder1.setTitle(R.string.hint_info);
                builder1.setMessage(R.string.impex_dia_info);
                builder1.setCancelable(true);
                builder1.setPositiveButton(R.string.dia_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        Preference pref = findPreference(key);
        StringBuilder sb = new StringBuilder();

        if (key.equals("Email")) {
            sb.append(sharedPreferences.getString(key, ""));
        }
        pref.setSummary(sb.toString());
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        File backupFile = new File(mActualFilepath,
                getString(R.string.filename_backup));
        File excelFile = new File(mActualFilepath,
                getString(R.string.filename_xls));

        String key = preference.getKey();
        // if Backup is pressed
        if (key.equals("Backup")) {
            if (!backupFile.exists()) {
                saveDBBackup();
                initSummaryLabels();
            } else {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(
                        getActivity());
                builder1.setTitle(R.string.impex_diatitle_createbackup);
                builder1.setMessage(R.string.impex_dia_backup_exists);
                builder1.setCancelable(true);
                builder1.setPositiveButton(R.string.dia_yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                saveDBBackup();
                                initSummaryLabels();
                                dialog.cancel();
                            }
                        });
                builder1.setNegativeButton(R.string.dia_no,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();
                return true;
            }
        }
        // if Restore is pressed
        if (key.equals("Restore")) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(
                    getActivity());
            builder1.setTitle(R.string.impex_diatitle_restorebackup);
            builder1.setCancelable(true);
            if (backupFile.exists()) {
                builder1.setMessage(R.string.impex_dia_restore);
                builder1.setPositiveButton(R.string.dia_yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                restoreDB();
                                dialog.cancel();
                            }
                        });
                builder1.setNegativeButton(R.string.dia_no,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
            } else {
                builder1.setMessage(R.string.impex_dia_no_backup_file);
                builder1.setPositiveButton(R.string.dia_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
            }

            AlertDialog alert11 = builder1.create();
            alert11.show();
            return true;
        }
        // if Excel is pressed
        if (key.equals("Excel")) {
            if (!excelFile.exists()) {
                exportDBtoXLS();
                initSummaryLabels();
            } else {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(
                        getActivity());
                builder1.setTitle(R.string.impex_diatitle_createexcel);
                builder1.setMessage(R.string.impex_dia_xls_exists);
                builder1.setCancelable(true);
                builder1.setPositiveButton(R.string.dia_yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                exportDBtoXLS();
                                initSummaryLabels();
                                dialog.cancel();
                            }
                        });
                builder1.setNegativeButton(R.string.dia_no,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();
                return true;
            }
        }
        // if Send Mail is pressed
        if (key.equals("Send")) {
            if (backupFile.exists() || excelFile.exists()) {
                sendMail();
            } else {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(
                        getActivity());
                builder1.setTitle(R.string.mail_title);
                builder1.setMessage(R.string.mail_text_nofiles);
                builder1.setCancelable(true);
                builder1.setPositiveButton(R.string.dia_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();
                return true;
            }
        }
        return false;
    }

    private void saveDBBackup() {
        FileChannel src = null;
        FileChannel dst = null;
        try {
            if (mActualFilepath.canWrite()) {
                String backupFile = getString(R.string.filename_backup);
                File currentDB = getActivity().getDatabasePath(
                        DBAdapter.DATABASE_NAME);
                File backupFilePath = new File(mActualFilepath, backupFile);

                if (currentDB.exists()) {
                    src = new FileInputStream(currentDB).getChannel();
                    dst = new FileOutputStream(backupFilePath).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    Toast.makeText(
                            getActivity().getApplicationContext(),
                            getString(R.string.toast_backup)
                                    + backupFilePath.toString(),
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity().getApplicationContext(),
                        "no save", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
        } finally {
            if (src != null) {
                try {
                    src.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (dst != null) {
                try {
                    dst.close();
                } catch (IOException e) {

                    e.printStackTrace();
                }
            }
        }
    }

    private void restoreDB() {
        FileChannel src = null;
        FileChannel dst = null;
        try {
            if (mActualFilepath.canWrite()) {
                String backupFile = getString(R.string.filename_backup);
                File currentDB = getActivity().getDatabasePath(
                        DBAdapter.DATABASE_NAME);
                File backupFilePath = new File(mActualFilepath, backupFile);

                if (currentDB.exists() && backupFilePath.exists()) {
                    src = new FileInputStream(backupFilePath).getChannel();
                    dst = new FileOutputStream(currentDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    Toast.makeText(getActivity().getApplicationContext(),
                            getString(R.string.toast_restore),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(),
                            getString(R.string.toast_restore_failed),
                            Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
        } finally {
            if (src != null) {
                try {
                    src.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (dst != null) {
                try {
                    dst.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void initSummaryLabels() {
        // Initialize preference summary label.
        EditTextPreference summaryEmail = (EditTextPreference) findPreference("Email");
        summaryEmail.setSummary(getPreferenceManager().getSharedPreferences()
                .getString("Email", getString(R.string.pref_summary_no_mail)));

        DateFormat formattedDate = DateFormat.getDateTimeInstance(
                DateFormat.DEFAULT, DateFormat.SHORT, Locale.getDefault());
        String storage = getPreferenceManager().getSharedPreferences()
                .getString("Files", "external");
        String storageStr ="";
        switch (storage){
            case "internal":
                storageStr = getResources().getStringArray(
                        R.array.storage_places)[0];
                break;
            case "external":
                storageStr = getResources().getStringArray(
                        R.array.storage_places)[1];
                break;
            case "sdcard":
                storageStr = getResources().getStringArray(
                        R.array.storage_places)[2];
                break;
        }
        Preference summaryRestore = findPreference("Restore");
        // File filePath = Environment.getExternalStorageDirectory();
        File backupFile = new File(mActualFilepath,
                getString(R.string.filename_backup));
        if (backupFile.exists()) {
            summaryRestore.setSummary(storageStr + "\n" + getString(R.string.filename_backup)
                    + "\n" + formattedDate.format(backupFile.lastModified()));
        } else {
            summaryRestore.setSummary(getString(R.string.pref_summary_no_file));
        }

        Preference summaryExcel = findPreference("Excel");
        File excelFile = new File(mActualFilepath,
                getString(R.string.filename_xls));
        if (excelFile.exists()) {
            summaryExcel.setSummary(storageStr + "\n" + getString(R.string.filename_xls) + "\n"
                    + formattedDate.format(excelFile.lastModified()));
        } else {
            summaryExcel.setSummary(getString(R.string.pref_summary_no_file));
        }

        Preference summarySend = findPreference("Send");
        StringBuffer str = new StringBuffer();
        if (backupFile.exists()) {
            str.append(getString(R.string.filename_backup));
            str.append("\n");
        }
        if (excelFile.exists()) {
            str.append(getString(R.string.filename_xls));
        }
        if (!(backupFile.exists() || excelFile.exists())) {
            str.append(getString(R.string.pref_summary_no_file));
        }
        summarySend.setSummary(str.toString());
    }

    private boolean exportDBtoXLS() {

        // check if available and not read only
        // if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
        // Log.w("FileUtils", "Storage not available or read only");
        // return false;
        // }

        boolean success = false;

        // New Workbook
        Workbook wb = new HSSFWorkbook();

        // Get current Date and Time
        Date currentDate = new Date(System.currentTimeMillis());
        DateFormat formattedDate = DateFormat.getDateInstance(
                DateFormat.DEFAULT, Locale.getDefault());

        Cell c;
        Sheet s;

        // Bold Fond
        Font boldBig = wb.createFont();
        boldBig.setBoldweight(Font.BOLDWEIGHT_BOLD);
        boldBig.setFontHeightInPoints((short) 20);

        Font boldSmall = wb.createFont();
        boldSmall.setBoldweight(Font.BOLDWEIGHT_BOLD);
        boldSmall.setFontHeightInPoints((short) 10);

        // Cell styles for header row
        CellStyle csHeaderBigLimeBorderTopBottomLeft = wb.createCellStyle();
        csHeaderBigLimeBorderTopBottomLeft.setFillForegroundColor(HSSFColor.LIME.index);
        csHeaderBigLimeBorderTopBottomLeft.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        csHeaderBigLimeBorderTopBottomLeft.setBorderTop(CellStyle.BORDER_THIN);
        csHeaderBigLimeBorderTopBottomLeft.setTopBorderColor(IndexedColors.BLACK.getIndex());
        csHeaderBigLimeBorderTopBottomLeft.setBorderBottom(CellStyle.BORDER_THIN);
        csHeaderBigLimeBorderTopBottomLeft.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        csHeaderBigLimeBorderTopBottomLeft.setBorderLeft(CellStyle.BORDER_THIN);
        csHeaderBigLimeBorderTopBottomLeft.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        csHeaderBigLimeBorderTopBottomLeft.setFont(boldBig);

        CellStyle csHeaderBigLimeBorderTopBottom = wb.createCellStyle();
        csHeaderBigLimeBorderTopBottom.setFillForegroundColor(HSSFColor.LIME.index);
        csHeaderBigLimeBorderTopBottom.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        csHeaderBigLimeBorderTopBottom.setBorderTop(CellStyle.BORDER_THIN);
        csHeaderBigLimeBorderTopBottom.setTopBorderColor(IndexedColors.BLACK.getIndex());
        csHeaderBigLimeBorderTopBottom.setBorderBottom(CellStyle.BORDER_THIN);
        csHeaderBigLimeBorderTopBottom.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        csHeaderBigLimeBorderTopBottom.setFont(boldBig);

        CellStyle csHeaderBigLimeBorderTopBottomRight = wb.createCellStyle();
        csHeaderBigLimeBorderTopBottomRight.setFillForegroundColor(HSSFColor.LIME.index);
        csHeaderBigLimeBorderTopBottomRight.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        csHeaderBigLimeBorderTopBottomRight.setBorderTop(CellStyle.BORDER_THIN);
        csHeaderBigLimeBorderTopBottomRight.setTopBorderColor(IndexedColors.BLACK.getIndex());
        csHeaderBigLimeBorderTopBottomRight.setBorderBottom(CellStyle.BORDER_THIN);
        csHeaderBigLimeBorderTopBottomRight.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        csHeaderBigLimeBorderTopBottomRight.setBorderRight(CellStyle.BORDER_THIN);
        csHeaderBigLimeBorderTopBottomRight.setRightBorderColor(IndexedColors.BLACK.getIndex());
        csHeaderBigLimeBorderTopBottomRight.setAlignment(CellStyle.ALIGN_RIGHT);
        csHeaderBigLimeBorderTopBottomRight.setFont(boldBig);

        CellStyle csHeaderSmallBlueBorderTopBottomLeft = wb.createCellStyle();
        csHeaderSmallBlueBorderTopBottomLeft.setFillForegroundColor(HSSFColor.AQUA.index);
        csHeaderSmallBlueBorderTopBottomLeft.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        csHeaderSmallBlueBorderTopBottomLeft.setBorderTop(CellStyle.BORDER_THIN);
        csHeaderSmallBlueBorderTopBottomLeft.setTopBorderColor(IndexedColors.BLACK.getIndex());
        csHeaderSmallBlueBorderTopBottomLeft.setBorderBottom(CellStyle.BORDER_THIN);
        csHeaderSmallBlueBorderTopBottomLeft.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        csHeaderSmallBlueBorderTopBottomLeft.setBorderLeft(CellStyle.BORDER_THIN);
        csHeaderSmallBlueBorderTopBottomLeft.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        csHeaderSmallBlueBorderTopBottomLeft.setFont(boldSmall);

        CellStyle csHeaderSmallBlueBorderTopBottom = wb.createCellStyle();
        csHeaderSmallBlueBorderTopBottom.setFillForegroundColor(HSSFColor.AQUA.index);
        csHeaderSmallBlueBorderTopBottom.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        csHeaderSmallBlueBorderTopBottom.setBorderTop(CellStyle.BORDER_THIN);
        csHeaderSmallBlueBorderTopBottom.setTopBorderColor(IndexedColors.BLACK.getIndex());
        csHeaderSmallBlueBorderTopBottom.setBorderBottom(CellStyle.BORDER_THIN);
        csHeaderSmallBlueBorderTopBottom.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        csHeaderSmallBlueBorderTopBottom.setFont(boldSmall);

        CellStyle csHeaderSmallBlueBorderTopBottomRight = wb.createCellStyle();
        csHeaderSmallBlueBorderTopBottomRight.setFillForegroundColor(HSSFColor.AQUA.index);
        csHeaderSmallBlueBorderTopBottomRight.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        csHeaderSmallBlueBorderTopBottomRight.setBorderTop(CellStyle.BORDER_THIN);
        csHeaderSmallBlueBorderTopBottomRight.setTopBorderColor(IndexedColors.BLACK.getIndex());
        csHeaderSmallBlueBorderTopBottomRight.setBorderBottom(CellStyle.BORDER_THIN);
        csHeaderSmallBlueBorderTopBottomRight.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        csHeaderSmallBlueBorderTopBottomRight.setBorderRight(CellStyle.BORDER_THIN);
        csHeaderSmallBlueBorderTopBottomRight.setRightBorderColor(IndexedColors.BLACK.getIndex());
        csHeaderSmallBlueBorderTopBottomRight.setFont(boldSmall);

        CellStyle csBorderLeft = wb.createCellStyle();
        // TEST TEST TEST TEST TEST TEST
        csBorderLeft.setWrapText(true);
        csBorderLeft.setVerticalAlignment(CellStyle.VERTICAL_TOP);
        csBorderLeft.setBorderLeft(CellStyle.BORDER_THIN);
        csBorderLeft.setLeftBorderColor(IndexedColors.BLACK.getIndex());

        CellStyle csBorderRight = wb.createCellStyle();
        // TEST TEST TEST TEST TEST TEST
        csBorderRight.setWrapText(true);
        csBorderRight.setVerticalAlignment(CellStyle.VERTICAL_TOP);
        csBorderRight.setBorderRight(CellStyle.BORDER_THIN);
        csBorderRight.setRightBorderColor(IndexedColors.BLACK.getIndex());

        CellStyle csBorderBottomLeft = wb.createCellStyle();
        // TEST TEST TEST TEST TEST TEST
        csBorderBottomLeft.setWrapText(true);
        csBorderBottomLeft.setVerticalAlignment(CellStyle.VERTICAL_TOP);
        csBorderBottomLeft.setBorderBottom(CellStyle.BORDER_THIN);
        csBorderBottomLeft.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        csBorderBottomLeft.setBorderLeft(CellStyle.BORDER_THIN);
        csBorderBottomLeft.setLeftBorderColor(IndexedColors.BLACK.getIndex());

        CellStyle csBorderBottomRight = wb.createCellStyle();
        // TEST TEST TEST TEST TEST TEST
        csBorderBottomRight.setWrapText(true);
        csBorderBottomRight.setVerticalAlignment(CellStyle.VERTICAL_TOP);
        csBorderBottomRight.setBorderBottom(CellStyle.BORDER_THIN);
        csBorderBottomRight.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        csBorderBottomRight.setBorderRight(CellStyle.BORDER_THIN);
        csBorderBottomRight.setRightBorderColor(IndexedColors.BLACK.getIndex());

        CellStyle csBorderBottom = wb.createCellStyle();
        // TEST TEST TEST TEST TEST TEST
        csBorderBottom.setWrapText(true);
        csBorderBottom.setVerticalAlignment(CellStyle.VERTICAL_TOP);
        csBorderBottom.setBorderBottom(CellStyle.BORDER_THIN);
        csBorderBottom.setBottomBorderColor(IndexedColors.BLACK.getIndex());

        // New Sheet
        Cursor cEng = mDB.getAllEngines(DBAdapter.KEY_MAKE);
        ArrayList<Sheet> sheets = new ArrayList<>();
        if (cEng != null && cEng.moveToFirst()) {
            for (int x = 0; x < cEng.getCount(); x++) {
                // create a sheet for every motor
                String name = cEng.getString(DBAdapter.COL_NAME);
                // replace not allowed characters :/\*?[]
                for (String str : mForbiddenChars) {
                    name = name.replace(str, " ");
                }
                sheets.add(wb.createSheet((x + 1) + ". " + name));
                s = sheets.get(x);

                // Setup the Page margins - Left, Right, Top and Bottom
                s.setMargin(Sheet.LeftMargin, 0.25);
                s.setMargin(Sheet.RightMargin, 0.25);
                s.setMargin(Sheet.TopMargin, 0.75);
                s.setMargin(Sheet.BottomMargin, 0.75);
                s.setHorizontallyCenter(true);

                // Setup the Header and Footer Margins
                HSSFPrintSetup ps = (HSSFPrintSetup) s.getPrintSetup();
                ps.setPaperSize(PrintSetup.A4_PAPERSIZE);
                ps.setFitWidth((short) 1);
                ps.setHeaderMargin(0.25);
                ps.setFooterMargin(0.25);

                // Set Header Information
                Header header = s.getHeader();
                header.setLeft(getString(R.string.app_name));
                // header.setCenter(HSSFHeader.font("Arial", "Bold") +
                // HSSFHeader.fontSize((short) 14)
                // + "SAMPLE ORDER");
                header.setRight(formattedDate.format(currentDate));

                // Set Footer Information with Page Numbers
                Footer footer = s.getFooter();
                footer.setRight(getString(R.string.xls_page)
                        + HeaderFooter.page() + getString(R.string.xls_of)
                        + HeaderFooter.numPages());

                // 1. row
                Row row = s.createRow(0);
                c = row.createCell(0);
                c.setCellValue(cEng.getString(DBAdapter.COL_NAME));
                c.setCellStyle(csHeaderBigLimeBorderTopBottomLeft);
                c = row.createCell(1);
                c.setCellStyle(csHeaderBigLimeBorderTopBottom);
                c = row.createCell(2);
                c.setCellStyle(csHeaderBigLimeBorderTopBottom);
                c = row.createCell(3);
                c.setCellStyle(csHeaderBigLimeBorderTopBottomRight);
                c.setCellValue(cEng.getString(DBAdapter.COL_MAKE));

                s.setColumnWidth(0, (10 * 600));
                s.setColumnWidth(1, (10 * 600));
                s.setColumnWidth(2, (10 * 600));
                s.setColumnWidth(3, (10 * 600));

                // 2. row
                row = s.createRow(1);
                c = row.createCell(0);
                c.setCellValue(getString(R.string.txt_psc_set));
                c.setCellStyle(csBorderBottomLeft);
                Cursor cPsc = mDB.getPsc(cEng
                        .getLong(DBAdapter.COL_ENG_PSC_ID));
                c = row.createCell(1);
                c.setCellValue(cPsc.getString(DBAdapter.COL_PSC_NAME));
                c.setCellStyle(csBorderBottomRight);
                c = row.createCell(2);
                c.setCellValue(getString(R.string.hint_date) + ":");
                c.setCellStyle(csBorderBottom);
                c = row.createCell(3);
                Date date = new Date(cEng.getLong(DBAdapter.COL_DATE));
                c.setCellValue(formattedDate.format(date));
                c.setCellStyle(csBorderBottomRight);

                // 3. row
                row = s.createRow(2);
                c = row.createCell(0);
                c.setCellValue(getString(R.string.hint_compression) + ":");
                c.setCellStyle(csBorderBottomLeft);
                c = row.createCell(1);
                c.setCellValue(cEng.getString(DBAdapter.COL_COMPRESSION));
                c.setCellStyle(csBorderBottomRight);
                c = row.createCell(2);
                c.setCellValue(getString(R.string.hint_plug) + ":");
                c.setCellStyle(csBorderBottom);
                c = row.createCell(3);
                c.setCellValue(cEng.getString(DBAdapter.COL_PLUG));
                c.setCellStyle(csBorderBottomRight);

                // 4. row
                row = s.createRow(3);
                c = row.createCell(0);
                c.setCellValue(getString(R.string.hint_status) + ":");
                c.setCellStyle(csBorderBottomLeft);
                c = row.createCell(1);
                String[] statusArr = getResources().getStringArray(
                        R.array.engine_status);
                c.setCellValue(statusArr[cEng.getInt(DBAdapter.COL_STATUS)]);
                c.setCellStyle(csBorderBottomRight);
                c = row.createCell(2);
                c.setCellValue(getString(R.string.txt_fuel_used));
                c.setCellStyle(csBorderBottom);
                c = row.createCell(3);
                int initialFuel = cEng.getInt(DBAdapter.COL_FUEL);
                long motorID = cEng.getLong(DBAdapter.COL_ROWID);
                int totalFuel = mDB.getEventSumFuel(motorID);
                totalFuel = totalFuel + initialFuel;
                c.setCellValue(totalFuel + getString(R.string.txt_ml));
                c.setCellStyle(csBorderBottomRight);

                // 5. row
                row = s.createRow(4);
                c = row.createCell(0);
                c.setCellValue(getString(R.string.hint_notes) + ":");
                c.setCellStyle(csBorderBottomLeft);
                c = row.createCell(1);
                c.setCellValue(cEng.getString(DBAdapter.COL_NOTES));
                c.setCellStyle(csBorderBottom);
                c = row.createCell(2);
                c.setCellStyle(csBorderBottom);
                c = row.createCell(3);
                c.setCellStyle(csBorderBottomRight);

                // 7. row, header for clutch rows
                row = s.createRow(6);
                c = row.createCell(0);
                c.setCellValue(getString(R.string.hint_clutch));
                c.setCellStyle(csHeaderSmallBlueBorderTopBottomLeft);
                c = row.createCell(1);
                c.setCellStyle(csHeaderSmallBlueBorderTopBottom);
                c = row.createCell(2);
                c.setCellStyle(csHeaderSmallBlueBorderTopBottom);
                c = row.createCell(3);
                c.setCellStyle(csHeaderSmallBlueBorderTopBottomRight);

                // 8. row
                row = s.createRow(7);
                c = row.createCell(0);
                c.setCellValue(getString(R.string.hint_clutch) + ":");
                c.setCellStyle(csBorderBottomLeft);
                c = row.createCell(1);
                c.setCellValue(cEng.getString(DBAdapter.COL_CLUTCH));
                c.setCellStyle(csBorderBottomRight);
                c = row.createCell(2);
                c.setCellValue(getString(R.string.hint_tension) + ":");
                c.setCellStyle(csBorderBottom);
                c = row.createCell(3);
                c.setCellValue(cEng.getString(DBAdapter.COL_TENSION));
                c.setCellStyle(csBorderBottomRight);

                // 9. row
                row = s.createRow(8);
                c = row.createCell(0);
                c.setCellValue(getString(R.string.hint_shoe) + ":");
                c.setCellStyle(csBorderBottomLeft);
                c = row.createCell(1);
                c.setCellValue(cEng.getString(DBAdapter.COL_SHOE));
                c.setCellStyle(csBorderBottomRight);
                c = row.createCell(2);
                c.setCellValue(getString(R.string.hint_play) + ":");
                c.setCellStyle(csBorderBottom);
                c = row.createCell(3);
                c.setCellValue(cEng.getString(DBAdapter.COL_AXIALPLAY));
                c.setCellStyle(csBorderBottomRight);

                // 10. row
                row = s.createRow(9);
                c = row.createCell(0);
                c.setCellValue(getString(R.string.hint_spring) + ":");
                c.setCellStyle(csBorderBottomLeft);
                c = row.createCell(1);
                c.setCellValue(cEng.getString(DBAdapter.COL_SPRING));
                c.setCellStyle(csBorderBottomRight);
                c = row.createCell(2);
                c.setCellValue(getString(R.string.hint_clearance) + ":");
                c.setCellStyle(csBorderBottom);
                c = row.createCell(3);
                c.setCellValue(cEng.getString(DBAdapter.COL_CLEARANCE));
                c.setCellStyle(csBorderBottomRight);

                // 12. row, header for part rows
                row = s.createRow(11);
                c = row.createCell(0);
                c.setCellValue(getString(R.string.xls_part));
                c.setCellStyle(csHeaderSmallBlueBorderTopBottomLeft);
                c = row.createCell(1);
                c.setCellValue(getString(R.string.xls_change_date));
                c.setCellStyle(csHeaderSmallBlueBorderTopBottom);
                c = row.createCell(2);
                c.setCellValue(getString(R.string.xls_part_fuel));
                c.setCellStyle(csHeaderSmallBlueBorderTopBottom);
                c = row.createCell(3);
                c.setCellValue(getString(R.string.hint_notes));
                c.setCellStyle(csHeaderSmallBlueBorderTopBottomRight);

                // part rows, from row 13. on
                String[] parts = getResources().getStringArray(
                        R.array.engine_parts);
                int rowNbr = 12;

                long pscId = cEng.getLong(DBAdapter.COL_ENG_PSC_ID);
                ArrayList<Integer> pscPart = new ArrayList<>();
                if (pscId != Integer.MAX_VALUE) {
                    pscPart.add(CONROD);
                    pscPart.add(PISTON);
                    pscPart.add(SLEEVE);
                    pscPart.add(PISTONROD);
                    pscPart.add(PISTONRODCLIPS);
                    if (cPsc.getInt(DBAdapter.COL_PSC_HEADER) > 0) {
                        pscPart.add(UNDERHEAD);
                    }
                }

                for (int partID = 0; partID < parts.length; partID++) {
                    ArrayList<PartsData> DataList;
                    if (pscPart.contains(partID)) {
                        DataList = mDB.getPscPartsDataList(getActivity(), pscId, partID);
                    } else {
                        DataList = mDB.getPartsDataList(getActivity(), motorID, partID);
                    }
                    PartsData Data;

                    for (int y = 0; y < DataList.size(); y++) {
                        Data = DataList.get(y);
                        row = s.createRow(rowNbr);
                        c = row.createCell(0);
                        c.setCellStyle(csBorderLeft);
                        c.setCellValue(parts[partID] + " #"
                                + (DataList.size() - y));
                        c = row.createCell(1);
                        c.setCellValue(formattedDate.format(Data.get_date()));
                        c = row.createCell(2);
                        c.setCellValue(Data.get_fuel()
                                + getString(R.string.txt_ml));
                        c = row.createCell(3);
                        c.setCellStyle(csBorderRight);
                        c.setCellValue(Data.get_notes());
                        rowNbr++;
                    }
                    // set bottom border after last part
                    c = row.getCell(0);
                    c.setCellStyle(csBorderBottomLeft);
                    c = row.getCell(1);
                    c.setCellStyle(csBorderBottom);
                    c = row.getCell(2);
                    c.setCellStyle(csBorderBottom);
                    c = row.getCell(3);
                    c.setCellStyle(csBorderBottomRight);
                }

                rowNbr++;

                // header for refuel rows
                row = s.createRow(rowNbr);
                c = row.createCell(0);
                c.setCellValue(getString(R.string.hint_date));
                c.setCellStyle(csHeaderSmallBlueBorderTopBottomLeft);
                c = row.createCell(1);
                c.setCellValue(getString(R.string.hint_place));
                c.setCellStyle(csHeaderSmallBlueBorderTopBottom);
                c = row.createCell(2);
                c.setCellValue(getString(R.string.xls_refuel_amount));
                c.setCellStyle(csHeaderSmallBlueBorderTopBottom);
                c = row.createCell(3);
                c.setCellValue(getString(R.string.hint_notes));
                c.setCellStyle(csHeaderSmallBlueBorderTopBottomRight);
                rowNbr++;

                // refuel rows
                Cursor eventCursor = mDB.getEventEngine(motorID);
                if (eventCursor != null && eventCursor.moveToFirst()) {
                    do {
                        // only print refuel actions
                        if (eventCursor.getInt(DBAdapter.COL_ACTION_ID) == 0) {
                            row = s.createRow(rowNbr);
                            c = row.createCell(0);
                            date = new Date(
                                    eventCursor
                                            .getLong(DBAdapter.COL_EVENT_DATE));
                            c.setCellValue(formattedDate.format(date));
                            c.setCellStyle(csBorderBottomLeft);
                            c = row.createCell(1);
                            c.setCellValue(eventCursor
                                    .getString(DBAdapter.COL_EVENT_PLACE));
                            c.setCellStyle(csBorderBottom);
                            c = row.createCell(2);
                            c.setCellValue(eventCursor
                                    .getInt(DBAdapter.COL_EVENT_FUEL)
                                    + getString(R.string.txt_ml));
                            c.setCellStyle(csBorderBottom);
                            c = row.createCell(3);
                            c.setCellValue(eventCursor
                                    .getString(DBAdapter.COL_EVENT_NOTES));
                            c.setCellStyle(csBorderBottomRight);
                            rowNbr++;
                        }
                    } while (eventCursor.moveToNext());
                }
                eventCursor.close();
                cEng.moveToNext();
            }
            cEng.close();

            // create sheets for psc sets
            Cursor cPsc = mDB.getAllPsc(DBAdapter.KEY_PSC_NAME);
            ArrayList<String> makes = mDB.getAllMakes();

            for (String string : makes) {
                // create a sheet for every make
                // replace not allowed characters :/\*?[]
                for (String str : mForbiddenChars) {
                    string = string.replace(str, " ");
                }
                sheets.add(wb.createSheet(string));
                s = sheets.get(sheets.size() - 1);
                int pscRowNbr = 0;

                cPsc.moveToFirst();
                do {
                    if (cPsc.getString(DBAdapter.COL_PSC_MAKE).equals(string)) {
                        // Setup the Page margins - Left, Right, Top and Bottom
                        s.setMargin(Sheet.LeftMargin, 0.25);
                        s.setMargin(Sheet.RightMargin, 0.25);
                        s.setMargin(Sheet.TopMargin, 0.75);
                        s.setMargin(Sheet.BottomMargin, 0.75);
                        s.setHorizontallyCenter(true);

                        // Setup the Header and Footer Margins
                        HSSFPrintSetup ps = (HSSFPrintSetup) s.getPrintSetup();
                        ps.setPaperSize(PrintSetup.A4_PAPERSIZE);
                        ps.setFitWidth((short) 1);
                        ps.setHeaderMargin(0.25);
                        ps.setFooterMargin(0.25);

                        // Set Header Information
                        Header header = s.getHeader();
                        header.setLeft(getString(R.string.app_name));
                        // header.setCenter(HSSFHeader.font("Arial", "Bold") +
                        // HSSFHeader.fontSize((short) 14)
                        // + "SAMPLE ORDER");
                        header.setRight(formattedDate.format(currentDate));

                        // Set Footer Information with Page Numbers
                        Footer footer = s.getFooter();
                        footer.setRight(getString(R.string.xls_page)
                                + HeaderFooter.page()
                                + getString(R.string.xls_of)
                                + HeaderFooter.numPages());

                        // 1. title row
                        Row row = s.createRow(pscRowNbr);
                        c = row.createCell(0);
                        c.setCellValue(cPsc.getString(DBAdapter.COL_PSC_NAME));
                        c.setCellStyle(csHeaderBigLimeBorderTopBottomLeft);
                        c = row.createCell(1);
                        c.setCellStyle(csHeaderBigLimeBorderTopBottom);
                        c = row.createCell(2);
                        c.setCellStyle(csHeaderBigLimeBorderTopBottom);
                        c = row.createCell(3);
                        c.setCellStyle(csHeaderBigLimeBorderTopBottomRight);
                        c.setCellValue(cPsc.getString(DBAdapter.COL_PSC_MAKE));

                        s.setColumnWidth(0, (10 * 550));
                        s.setColumnWidth(1, (10 * 550));
                        s.setColumnWidth(2, (10 * 550));
                        s.setColumnWidth(3, (10 * 550));

                        pscRowNbr++;

                        // 2. title row
                        row = s.createRow(pscRowNbr);
                        c = row.createCell(0);
                        c.setCellValue(getString(R.string.txt_built_in));
                        c.setCellStyle(csBorderBottomLeft);
                        c = row.createCell(1);
                        long engId = mDB.getWherePscBuiltin(cPsc
                                .getLong(DBAdapter.COL_ROWID));
                        if (engId != Integer.MAX_VALUE) {
                            cEng = mDB.getEngine(engId);
                            c.setCellValue(cEng.getString(DBAdapter.COL_NAME));
                            cEng.close();
                        } else {
                            c.setCellValue(getString(R.string.txt_none));
                        }
                        c.setCellStyle(csBorderBottomRight);
                        c = row.createCell(2);
                        c.setCellValue(getString(R.string.hint_date) + ":");
                        c.setCellStyle(csBorderBottom);
                        c = row.createCell(3);
                        Date date = new Date(
                                cPsc.getLong(DBAdapter.COL_PSC_DATE));
                        c.setCellValue(formattedDate.format(date));
                        c.setCellStyle(csBorderBottomRight);

                        pscRowNbr++;

                        // 3. title row
                        row = s.createRow(pscRowNbr);
                        c = row.createCell(0);
                        c.setCellValue(getString(R.string.hint_status) + ":");
                        c.setCellStyle(csBorderBottomLeft);
                        c = row.createCell(1);
                        String[] statusArr = getResources().getStringArray(
                                R.array.engine_status);
                        c.setCellValue(statusArr[cPsc
                                .getInt(DBAdapter.COL_PSC_STATUS)]);
                        c.setCellStyle(csBorderBottomRight);
                        c = row.createCell(2);
                        c.setCellValue(getString(R.string.hint_notes) + ":");
                        c.setCellStyle(csBorderBottom);
                        c = row.createCell(3);
                        c.setCellValue(cPsc.getString(DBAdapter.COL_PSC_NOTES));
                        c.setCellStyle(csBorderBottomRight);

                        pscRowNbr++;
                        pscRowNbr++;

                        // header for part rows
                        row = s.createRow(pscRowNbr);
                        c = row.createCell(0);
                        c.setCellValue(getString(R.string.xls_part));
                        c.setCellStyle(csHeaderSmallBlueBorderTopBottomLeft);
                        c = row.createCell(1);
                        c.setCellValue(getString(R.string.xls_change_date));
                        c.setCellStyle(csHeaderSmallBlueBorderTopBottom);
                        c = row.createCell(2);
                        c.setCellValue(getString(R.string.xls_part_fuel));
                        c.setCellStyle(csHeaderSmallBlueBorderTopBottom);
                        c = row.createCell(3);
                        c.setCellValue(getString(R.string.hint_notes));
                        c.setCellStyle(csHeaderSmallBlueBorderTopBottomRight);

                        pscRowNbr++;

                        // psc part rows
                        String[] parts = getResources().getStringArray(
                                R.array.engine_parts);

                        ArrayList<Integer> pscPart = new ArrayList<>();
                        pscPart.add(CONROD);
                        pscPart.add(PISTON);
                        pscPart.add(SLEEVE);
                        pscPart.add(PISTONROD);
                        pscPart.add(PISTONRODCLIPS);
                        if (cPsc.getInt(DBAdapter.COL_PSC_HEADER) > 0) {
                            pscPart.add(UNDERHEAD);
                        }

                        for (int x : pscPart) {
                            ArrayList<PartsData> DataList;
                            DataList = mDB.getPscPartsDataList(getActivity(),
                                    cPsc.getLong(DBAdapter.COL_ROWID), x);
                            PartsData Data;

                            for (int y = 0; y < DataList.size(); y++) {
                                Data = DataList.get(y);
                                row = s.createRow(pscRowNbr);
                                c = row.createCell(0);
                                c.setCellStyle(csBorderLeft);
                                c.setCellValue(parts[x] + " #"
                                        + (DataList.size() - y));
                                c = row.createCell(1);
                                c.setCellValue(formattedDate.format(Data
                                        .get_date()));
                                c = row.createCell(2);
                                c.setCellValue(Data.get_fuel()
                                        + getString(R.string.txt_ml));
                                c = row.createCell(3);
                                c.setCellStyle(csBorderRight);
                                c.setCellValue(Data.get_notes());
                                pscRowNbr++;
                            }
                            // set bottom border after last part
                            c = row.getCell(0);
                            c.setCellStyle(csBorderBottomLeft);
                            c = row.getCell(1);
                            c.setCellStyle(csBorderBottom);
                            c = row.getCell(2);
                            c.setCellStyle(csBorderBottom);
                            c = row.getCell(3);
                            c.setCellStyle(csBorderBottomRight);
                        }
                        pscRowNbr++;
                        pscRowNbr++;
                    }
                } while (cPsc.moveToNext());
            }
            cPsc.close();
        }

        // Create a path where we will place our List of objects on external
        // storage
        File file = new File(mActualFilepath, getString(R.string.filename_xls));
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(file);
            wb.write(os);
            Log.w("FileUtils", "Writing file" + file);
            success = true;
            Toast.makeText(getActivity().getApplicationContext(),
                    getString(R.string.toast_excel) + file.toString(),
                    Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.w("FileUtils", "Error writing " + file, e);
        } catch (Exception e) {
            Log.w("FileUtils", "Failed to save file", e);
        } finally {
            try {
                if (null != os)
                    os.close();
            } catch (Exception ex) {
            }
        }

        return success;
    }

    private void sendMail() {
        Intent i = new Intent(Intent.ACTION_SEND_MULTIPLE);
        ArrayList<Uri> files = new ArrayList<>();
        SharedPreferences prefmail = getActivity().getSharedPreferences(
                "Default_Values", Activity.MODE_PRIVATE);

        File fileBak = new File(mActualFilepath,
                getString(R.string.filename_backup));
        if (fileBak.exists()) {
            Uri sb = Uri.fromFile(fileBak);
            files.add(sb);
        }
        File fileXls = new File(mActualFilepath,
                getString(R.string.filename_xls));
        if (fileXls.exists()) {
            Uri sx = Uri.fromFile(fileXls);
            files.add(sx);
        }

        // if file to send exists, send them via mail client
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL,
                new String[]{prefmail.getString("Email", "")});
        i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.mail_subject));
        i.putExtra(Intent.EXTRA_TEXT, getString(R.string.mail_text));
        i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
        try {
            startActivity(Intent.createChooser(i,
                    getString(R.string.mail_title)));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getActivity().getApplicationContext(),
                    getString(R.string.mail_noclients), Toast.LENGTH_SHORT)
                    .show();
        }
    }
}