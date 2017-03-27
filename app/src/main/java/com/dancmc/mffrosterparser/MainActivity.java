package com.dancmc.mffrosterparser;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.text.TextUtilsCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dancmc.directorypicker.ImageDirectoryPickerDialog;
import com.dancmc.directorypicker.ImagePickerDialog;
import com.dancmc.mffrosterparser.database.GearDataSource;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements ImageDirectoryPickerDialog.Contract, ImagePickerDialog.Contract {

    private static final String TAG = "MainActivity";
    @BindView(R.id.textview_json)
    TextView mTextView;
    @BindView(R.id.progress_text)
    TextView mProgressText;
    @BindView(R.id.progress_bar)
    LinearLayout mProgressLayout;

    GearDataSource mDataSource;

    private int mAspectRatio;
    private final int RATIO_16_9 = 169;
    private final int RATIO_16_10 = 1610;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("MFF JSON", mTextView.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(MainActivity.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        mDataSource = new GearDataSource(MainActivity.this);

    }

    //================================================================================
    //
    // Process chosen folder of screenshots
    //
    //================================================================================

    @OnClick(R.id.screenshot_chooser)
    void chooseScreenshotFolder() {

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 12);
        } else if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 11);

        } else {
            FragmentManager fm = getSupportFragmentManager();
            ImagePickerDialog.newDialog(12, ImagePickerDialog.MODE_IMAGE_PICKER_MULTI, null).show(fm, "this");
        }

    }

    @Override
    public void setDefaultStartFolder(File file) {

    }

    @Override
    public void setFileListResult(int i, ArrayList<File> arrayList) {
        Log.d(TAG, "setFileListResult: ");
        mProgressLayout.setVisibility(View.VISIBLE);
        mProgressText.setText("Processing files....");
        processScreenshots(arrayList);
    }

    private ArrayList<Character> characterList;
    public static final int CHARACTER_GEARS = 1;
    public static final int CHARACTER_DETAILS = 2;

    void processScreenshots(ArrayList<File> files) {

        Executor tpt = Executors.newFixedThreadPool(6);

        mTotalProcessed = 0;
        mTotalToProcess = files.size();
        characterList = new ArrayList<>();

        // split into 6 groups
        ArrayList<ArrayList<File>> fileGroups = new ArrayList<>();
        for (int i = 0; i < mTotalToProcess; i++) {
            try {
                fileGroups.get(i % 6);
            } catch (IndexOutOfBoundsException e) {
                fileGroups.add(new ArrayList<File>());
            }
            fileGroups.get(i % 6).add(files.remove(0));
        }


        for (ArrayList<File> fileList : fileGroups) {
            new AsyncScreenshotOperation().executeOnExecutor(tpt, fileList);
        }

    }

    class AsyncScreenshotOperation extends AsyncTask<ArrayList<File>, Void, ArrayList<Character>> {

        ArrayList<Integer> typeList = new ArrayList<>();
        ArrayList<Integer> gearNumbers = new ArrayList<>();


        @Override
        protected ArrayList<Character> doInBackground(ArrayList<File>... params) {
            ArrayList<File> fileList = params[0];
            OCR ocr = new OCR(MainActivity.this);

            ArrayList<Character> list = new ArrayList<>();


            for (File file : fileList) {
                Character character;

                // calculate scale for the image file
                double scaleFactor = getScaleForFile(file);

                String details = ocr.useOCR(getScaledRect(scaleFactor, 586, 184, 733, 223), file, Color.parseColor("#F38843"), 40);
                Log.d(TAG, "doInBackground: 2" + details);
                // Check if in character details page
                if (details != null && details.equalsIgnoreCase("attack")) {

                    int[] colorArray = new int[20];
                    colorArray[0] = Color.parseColor("#FFC300");
                    colorArray[1] = Color.parseColor("#FFFFFF");
                    colorArray[2] = Color.parseColor("#D9C5AF");
                    for (int i = 3; i < colorArray.length; i++) {
                        colorArray[i] = Color.parseColor("#FFFFFF");
                    }

                    Rect[] rectArray = new Rect[18];
                    //tier
                    rectArray[0] = getScaledRect(scaleFactor, 460, 393, 555, 427);
                    //charalias
                    rectArray[1] = getScaledRect(scaleFactor, 104, 479, 546, 520);
                    //unialias
                    rectArray[2] = getScaledRect(scaleFactor, 108, 523, 554, 560);
                    //phys att
                    rectArray[3] = getScaledRect(scaleFactor, 911, 240, 1186, 280);
                    //energy att
                    rectArray[4] = getScaledRect(scaleFactor, 911, 280, 1186, 327);
                    //atk spd
                    rectArray[5] = getScaledRect(scaleFactor, 911, 327, 1186, 374);
                    //crit rate
                    rectArray[6] = getScaledRect(scaleFactor, 911, 374, 1186, 423);
                    //crit dam
                    rectArray[7] = getScaledRect(scaleFactor, 911, 423, 1186, 468);

                    // def pen
                    rectArray[8] = getScaledRect(scaleFactor, 911, 468, 1186, 517);
                    //ignore dodge
                    rectArray[9] = getScaledRect(scaleFactor, 911, 517, 1186, 568);

                    //phys def
                    rectArray[10] = getScaledRect(scaleFactor, 1530, 238, 1816, 280);
                    //energy def
                    rectArray[11] = getScaledRect(scaleFactor, 1530, 280, 1816, 329);
                    //hp
                    rectArray[12] = getScaledRect(scaleFactor, 1530, 329, 1816, 376);
                    //recorate
                    rectArray[13] = getScaledRect(scaleFactor, 1530, 376, 1816, 421);
                    // dodge
                    rectArray[14] = getScaledRect(scaleFactor, 1530, 421, 1816, 472);
                    // mv spd
                    rectArray[15] = getScaledRect(scaleFactor, 1541, 810, 1814, 847);
                    // debuff
                    rectArray[16] = getScaledRect(scaleFactor, 1653, 856, 1816, 895);
                    //scd
                    rectArray[17] = getScaledRect(scaleFactor, 1666, 903, 1816, 937);

                    character = ocr.useOCRScreenshot(rectArray, file, colorArray, 80, CHARACTER_DETAILS, -1);
                    list.add(character);
                    typeList.add(CHARACTER_DETAILS);
                    gearNumbers.add(-1);

                    // Check if in gears pages
                } else {

                    String gear = ocr.useOCR(getScaledRect(scaleFactor, 156, 23, 313, 74), file, Color.parseColor("#BBE8FF"), 70);
                    Log.d(TAG, "doInBackground: 2" + gear);
                    if (gear != null && gear.equalsIgnoreCase("gear")) {

                        // get gearname and match to character
                        String gearName = ocr.useOCR(getScaledRect(scaleFactor, 387, 143, 1018, 187), file, Color.parseColor("#FFFFFF"), 30);

                        if (gearName != null) {
                            AbstractMap.SimpleEntry<String, Integer> map = mDataSource.searchGears(gearName);
                            if (map != null) {

                                int[] colorArray = new int[20];

                                Arrays.fill(colorArray, Color.parseColor("#0A1223"));

                                // Rects to get each row of gear data
                                Rect[] rectArray = new Rect[8];
                                rectArray[0] = getScaledRect(scaleFactor, 400, 304, 1024, 349);
                                rectArray[1] = getScaledRect(scaleFactor, 400, 348, 1024, 384);
                                rectArray[2] = getScaledRect(scaleFactor, 400, 390, 1024, 432);
                                rectArray[3] = getScaledRect(scaleFactor, 400, 434, 1024, 475);
                                rectArray[4] = getScaledRect(scaleFactor, 400, 477, 1024, 517);
                                rectArray[5] = getScaledRect(scaleFactor, 400, 521, 1024, 559);
                                rectArray[6] = getScaledRect(scaleFactor, 400, 564, 1024, 603);
                                rectArray[7] = getScaledRect(scaleFactor, 400, 606, 1024, 646);

                                // some values are white and some green
                                character = ocr.useOCRScreenshot(rectArray, file, colorArray, 60, CHARACTER_GEARS, map.getValue());
                                character.id = map.getKey();

                                list.add(character);
                                typeList.add(CHARACTER_GEARS);
                                gearNumbers.add(map.getValue());
                            }
                        }
                    }
                }
            }

            ocr.close();
            return list;
        }

        @Override
        protected void onPostExecute(ArrayList<Character> characters) {

            for (int i = 0; i < characters.size(); i++) {
                mTotalProcessed++;
                finaliseResult(characters.get(i), typeList.get(i), gearNumbers.get(i));
            }
        }
    }

    private void finaliseResult(Character character, int type, int gearNumber) {
        Log.d(TAG, "finaliseResult: " + type + " " + gearNumber);

        boolean foundDuplicate = false;
        for (Character character2 : characterList) {
            if (character2.id.equals(character.id)) {
                Log.d(TAG, "finaliseResult: found dupe");
                switch (type) {
                    case CHARACTER_GEARS:
                        character2.gear[gearNumber] = character.gear[gearNumber];
                        break;
                    case CHARACTER_DETAILS:
                        character2.uniform = character.uniform;
                        character2.level = character.level;
                        character2.tier = character.tier;
                        character2.attack = character.attack;
                        character2.defense = character.defense;
                        character2.hp = character.hp;
                        character2.dodge = character.dodge;
                        character2.ignore_dodge = character.ignore_dodge;
                        character2.defpen = character.defpen;
                        character2.scd = character.scd;
                        character2.critrate = character.critrate;
                        character2.critdamage = character.critdamage;
                        character2.atkspeed = character.atkspeed;
                        character2.recorate = character.recorate;
                        character2.movspeed = character.movspeed;
                        character2.debuff = character.debuff;
                        break;
                }
                foundDuplicate = true;
            }
        }

        if (!foundDuplicate) {
            characterList.add(character);
        }

        mProgressText.setText("Processed " + mTotalProcessed + " out of " + mTotalToProcess);

        if (mTotalProcessed == mTotalToProcess) {

            Gson gson = new Gson();
            ArrayList<String> stringArrayList = new ArrayList<>();

            for (Character c : characterList) {
                if (c.uniform.equals("")) {
                    c.uniform = mDataSource.getRandomUni(c.id);
                }
                String resultString = ("\"" + c.id + "\":" + gson.toJson(c));
                stringArrayList.add(resultString);
            }
            String result = "{" + TextUtils.join(",", stringArrayList) + "}";
            mProgressLayout.setVisibility(View.GONE);
            mTextView.setText(result);
            writeTextFile(result, new File(Environment.getExternalStorageDirectory(), "MFF_json.txt"));
        }
    }

    //================================================================================
    //
    // Process gear screenshots from marvelfuturefight.info website
    //
    //================================================================================

    File screenshotFolder = new File(Environment.getExternalStorageDirectory(), "/TestShots");
    private int mTotalToProcess;
    private int mTotalProcessed;
    private StringBuffer mCSVString;
    private long mStartTime;


    //@OnClick(R.id.website_chooser)
    void processGears() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 12);
        } else if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 11);

        } else {
            mTotalProcessed = 0;
            mStartTime = System.currentTimeMillis();
            mCSVString = new StringBuffer();
            mCSVString.append("character|uniform|gear1|gear2|gear3|gear4\n");

            ArrayList<File> list = new ArrayList<File>(Arrays.asList(screenshotFolder.listFiles()));

            Executor tpt = Executors.newFixedThreadPool(6);
            mTotalToProcess = list.size();

            ArrayList<ArrayList<File>> fileGroups = new ArrayList<>();
            while (list.size() >= 10) {
                ArrayList<File> temp = new ArrayList<File>();
                for (int i = 0; i < 10; i++) {
                    temp.add(list.remove(0));
                }
                fileGroups.add(temp);
            }
            fileGroups.add(list);

            for (ArrayList<File> files : fileGroups) {
                new AsyncListOperation().executeOnExecutor(tpt, files);
            }
        }
    }


    class AsyncListOperation extends AsyncTask<ArrayList<File>, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(ArrayList<File>... params) {
            ArrayList<File> screenshots = params[0];
            OCR ocr = new OCR(MainActivity.this);

            int colorCharText = Color.parseColor("#FFFFFF");
            Rect charRect = new Rect(1782, 321, 2157, 354);

            int colorUniformText = Color.parseColor("#B3A999");
            Rect uniformRect = new Rect(1699, 364, 2119, 396);

            int colorGearText = Color.parseColor("#A8DFFF");
            Rect gear1 = new Rect(1824, 424, 2124, 534);
            Rect gear2 = new Rect(1824, 545, 2124, 655);
            Rect gear3 = new Rect(1824, 665, 2124, 775);
            Rect gear4 = new Rect(1824, 785, 2124, 895);

            Rect[] rectArray = {charRect, uniformRect, gear1, gear2, gear3, gear4};
            int[] colorArray = {colorCharText, colorUniformText, colorGearText, colorGearText, colorGearText, colorGearText};
            ArrayList<String> results = new ArrayList<>();
            for (File screenshot : screenshots) {
                String result = ocr.useOCRWeb(rectArray, screenshot, colorArray, 70);
                results.add(result);
                Log.d(TAG, "AsyncOCR: " + result);
            }
            ocr.close();
            return results;
        }

        @Override
        protected void onPostExecute(ArrayList<String> strings) {
            for (String s : strings) {
                mTotalProcessed++;
                executeCSVOutput(s);
            }
        }
    }

    void executeCSVOutput(String s) {
        mCSVString.append(s + "\n");
        Log.d(TAG, "executeCSVOutput: " + mTotalProcessed);
        if (mTotalProcessed == mTotalToProcess) {

            writeTextFile(mCSVString.toString(), new File(Environment.getExternalStorageDirectory(), "/gears.csv"));

            Log.d(TAG, "total time taken : " + (double) (System.currentTimeMillis() - mStartTime) / 1000.0 + " seconds");
            ((TextView) findViewById(R.id.textview_json)).setText("total time taken : " + (double) (System.currentTimeMillis() - mStartTime) / 1000.0 + " seconds for " + mTotalToProcess + " items");
        }
    }

    //================================================================================
    //
    // General Utilities
    //
    //================================================================================

    boolean writeTextFile(String string, File file) {

        try {
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            osw.write(string);
            osw.flush();
            fos.getFD().sync();
            osw.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    double getScaleForFile(File file) {
// Calculate aspect ratio of image file and also scale


        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        double longer = Math.max(options.outWidth, options.outHeight);
        double shorter = Math.min(options.outWidth, options.outHeight);
        double ratio = longer / shorter;
        double mScaleFactor = 1.0;
        if (ratio == 16.0 / 9.0) {
            mAspectRatio = RATIO_16_9;
            mScaleFactor = shorter / 1080.0;
        } else if (ratio == 16.0 / 10.0) {
            mAspectRatio = RATIO_16_10;
            mScaleFactor = shorter / 1200.0;
            Log.d(TAG, "run: " + shorter);
        }
        return mScaleFactor;
    }

    Rect getScaledRect(double mScaleFactor, int left, int top, int right, int bottom) {
        return new Rect((int) (left * mScaleFactor), (int) (top * mScaleFactor), (int) (right * mScaleFactor), (int) (bottom * mScaleFactor));
    }

}
