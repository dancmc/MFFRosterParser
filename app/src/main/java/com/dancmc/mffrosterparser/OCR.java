package com.dancmc.mffrosterparser;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.dancmc.mffrosterparser.database.GearDataSource;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;



public class OCR {

    private static final String TAG = "OCR";

    private Context mContext;

    TessBaseAPI mTessAPI;

    GearDataSource mDataSource;


    public OCR(Context context) {
        mContext = context;
        File tessdataDir = new File(context.getExternalFilesDir(null).toString() + File.separator + "tessdata");
        if (!tessdataDir.exists()) {
            if (tessdataDir.mkdirs()) {
                InputStream is = null;
                OutputStream os = null;
                try {
                    is = context.getAssets().open("eng.traineddata");
                    os = new FileOutputStream(new File(tessdataDir, "eng.traineddata"));
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = is.read(buffer)) != -1) {
                        os.write(buffer, 0, read);
                    }
                    is.close();
                    is = null;
                    os.flush();
                    os.close();
                    os = null;
                } catch (Exception e) {
                    Log.e(TAG, "getInstance: ", e);
                }
            }
        }


        mTessAPI = new TessBaseAPI();
        // Eg. baseApi.init("/mnt/sdcard/tesseract/tessdata/eng.traineddata", "eng");
        mTessAPI.init(mContext.getExternalFilesDir(null).getPath(), "eng", TessBaseAPI.OEM_TESSERACT_ONLY);
        mTessAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "ABCDEF-GHIJKLMNOPQR'STUVWXYZabcde.fghijklmnopqrstuvw,xyz012345678()9&%+/!:");

        mDataSource = new GearDataSource(context);
    }


    private void setTessNoApostrophe() {
        mTessAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "ABCDEF-GHIJKLMNOPQRSTUVWXYZabcde.fghijklmnopqrstuvw,xyz012345678()9&%+/!:");
    }

    private void setTessStandard() {
        mTessAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "ABCDEF-GHIJKLMNOPQRSTUVWXYZabcde.'fghijklmnopqrstuvw,xyz012345678()9&%+/!:");
    }

    private void setTessUpperCase() {
        mTessAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "ABCDEF-GHIJKLMNOPQRSTUV'WXYZ.ier,012345678()9&%/!:");
    }

    private void setTessNumbers() {
        mTessAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, ".012345678()9+%");
    }

    public String useOCR(Rect rect, @Nullable File bitmap, int backgroundColor, int similarity) {
        // OCR on supplied screenshot file, otherwise use latest screenshot
        Bitmap bmpTemp;
        String path = bitmap.getPath();

        BitmapRegionDecoder bitmapRegion;
        try {
            bitmapRegion = BitmapRegionDecoder.newInstance(path, false);
        } catch (Exception e) {
            Log.d(TAG, "useOCRWeb: " + e.getMessage() + " " + path);
            return null;
        }

        bmpTemp = bitmapRegion.decodeRegion(rect, null);
        bmpTemp = bmpTemp.copy(Bitmap.Config.ARGB_8888, true);
        replaceWithWhite(bmpTemp, backgroundColor, similarity);
        setTessStandard();
        mTessAPI.setImage(bmpTemp);
        String result = mTessAPI.getUTF8Text();
        Log.d(TAG, "useOCR: " + result);
        bmpTemp.recycle();
        if (result != null) {
            result = result.replace("\n", "").toLowerCase();
            return result;
        } else {
            return "";
        }
    }



    public String useOCRWeb(Rect[] rectArray, @Nullable File bitmap, int[] backgroundColor, int similarity) {
        // OCR on supplied screenshot file, otherwise use latest screenshot
        Bitmap bmpTemp;
        String path = bitmap.getPath();

        BitmapRegionDecoder bitmapRegion;
        try {
            bitmapRegion = BitmapRegionDecoder.newInstance(path, false);
        } catch (Exception e) {
            Log.d(TAG, "useOCRWeb: " + e.getMessage() + " " + path);
            Log.d(TAG, "useOCRWeb: can't decode bitmap");
            return null;
        }
        String[] resultArray = new String[6];
        for (int i = 0; i < rectArray.length; i++) {
            bmpTemp = bitmapRegion.decodeRegion(rectArray[i], null);
            bmpTemp = bmpTemp.copy(Bitmap.Config.ARGB_8888, true);
            replaceWithWhite(bmpTemp, backgroundColor[i], similarity);
            setTessStandard();
            mTessAPI.setImage(bmpTemp);
            resultArray[i] = mTessAPI.getUTF8Text().replace("\n", " ").toLowerCase();
            bmpTemp.recycle();
        }

        String result = TextUtils.join("|", resultArray);
        return result;
    }

    public Character useOCRScreenshot(Rect[] rectArray, @Nullable File bitmap, int[] backgroundColor, int similarity, int type, int gearNumber) {
        // OCR on supplied screenshot file, otherwise use latest screenshot
        String path = bitmap.getPath();

        BitmapRegionDecoder bitmapRegion;
        try {
            bitmapRegion = BitmapRegionDecoder.newInstance(path, false);
        } catch (Exception e) {
            Log.d(TAG, "useOCRWeb: " + e.getMessage() + " " + path);
            return null;
        }

        Character character = new Character();


        for (int i = 0; i < rectArray.length; i++) {


            if (type == MainActivity.CHARACTER_DETAILS) {
                // the type check happens in Mainactivity asynctask first
                // shift for loop inside after

                Bitmap bmpTemp = bitmapRegion.decodeRegion(rectArray[i], null);
                bmpTemp = bmpTemp.copy(Bitmap.Config.ARGB_8888, true);
                replaceWithWhite(bmpTemp, backgroundColor[i], similarity);

                if (i == 0 || i == 1 || i == 2) {
                    setTessUpperCase();
                } else {
                    setTessNumbers();
                }
                mTessAPI.setImage(bmpTemp);
                String result = mTessAPI.getUTF8Text().replace("\n", "").replace("%", "").toLowerCase();
                Log.d(TAG, "useOCRScreenshot: "+result);

                switch (i) {
                    case 0:
                        if (result.contains("2")) {
                            character.tier = 2;
                        }
                        break;
                    case 1:
                        String charAlias = mDataSource.getCharAlias(result);
                        if (charAlias != null) {
                            character.id = charAlias;
                        }
                        break;
                    case 2:
                        String uniAlias = mDataSource.getUniAlias(result);
                        if (uniAlias != null) {
                            character.uniform = uniAlias;
                        }
                        break;
                    case 3:
                        character.attack.physical = Integer.parseInt(result);
                        break;
                    case 4:
                        character.attack.energy = Integer.parseInt(result);
                        break;
                    case 5:
                        character.atkspeed = Double.parseDouble(result);
                        break;
                    case 6:
                        character.critrate = Double.parseDouble(result);
                        break;
                    case 7:
                        character.critdamage = Double.parseDouble(result);
                        break;
                    case 8:
                        character.defpen = Double.parseDouble(result);
                        break;
                    case 9:
                        character.ignore_dodge = Double.parseDouble(result);
                        break;
                    case 10:
                        character.defense.physical = Integer.parseInt(result);
                        break;
                    case 11:
                        character.defense.energy = Integer.parseInt(result);
                        break;
                    case 12:
                        character.hp = Integer.parseInt(result);
                        break;
                    case 13:
                        character.recorate = Double.parseDouble(result);
                        break;
                    case 14:
                        character.dodge = Double.parseDouble(result);
                        break;
                    case 15:
                        character.movspeed = Double.parseDouble(result);
                        break;
                    case 16:
                        character.debuff = Double.parseDouble(result);
                        break;
                    case 17:
                        character.scd = Double.parseDouble(result);
                        break;
                }
                bmpTemp.recycle();
            } else if (type == MainActivity.CHARACTER_GEARS) {

                setTessNoApostrophe();
                Rect original  = rectArray[i];
                Rect rectLeft = new Rect(original.left, original.top, (int)((original.right-original.left)*0.8+original.left), original.bottom);
                Rect rectRight = new Rect((int)((original.right-original.left)*0.8+original.left), original.top, original.right, original.bottom);

                Bitmap bmpTemp = bitmapRegion.decodeRegion(rectLeft, null);
                bmpTemp = bmpTemp.copy(Bitmap.Config.ARGB_8888, true);
                replaceWithWhite(bmpTemp, backgroundColor[i], 115);
                mTessAPI.setImage(bmpTemp);
                String resultLeft = mTessAPI.getUTF8Text().toLowerCase().replace(" ", "");

                setTessNumbers();
                bmpTemp = bitmapRegion.decodeRegion(rectRight, null);
                bmpTemp = getResizedBitmap(bmpTemp, 5f);
                bmpTemp = bmpTemp.copy(Bitmap.Config.ARGB_8888, true);
                replaceWithWhite(bmpTemp, Color.parseColor("#FFFFFF"), 100);
                mTessAPI.setImage(bmpTemp);
                String resultRight = mTessAPI.getUTF8Text().replace("%", "").replace("+", "").replace(" ", "");


                if(resultRight==null||resultRight.equals("")){
                    Log.d(TAG, "useOCRScreenshot: null Right");
                    bmpTemp = bitmapRegion.decodeRegion(rectRight, null);
                    bmpTemp = bmpTemp.copy(Bitmap.Config.ARGB_8888, true);
                    replaceWithWhite(bmpTemp, Color.parseColor("#00FE01"), 40);
                    mTessAPI.setImage(bmpTemp);
                    resultRight = mTessAPI.getUTF8Text().replace("%", "").replace("+", "").trim();
                }

                Log.d(TAG, "useOCRScreenshot: " + resultLeft+" "+resultRight);
                if (resultLeft != null && resultRight!=null) {

                    if (resultLeft.contains("physicalattackper")) {
                        character.gear[gearNumber][i].type = "physical_attack_by_level";
                    } else if (resultLeft.contains("physicalattack")) {
                        character.gear[gearNumber][i].type = "physical_attack";
                    } else if (resultLeft.contains("energyattackper")) {
                        character.gear[gearNumber][i].type = "energy_attack_by_level";
                    } else if (resultLeft.contains("energyattack")) {
                        character.gear[gearNumber][i].type = "energy_attack";
                    } else if (resultLeft.contains("hpby")) {
                        character.gear[gearNumber][i].type = "hp_by_level";
                    } else if (resultLeft.contains("ignoredef")) {
                        character.gear[gearNumber][i].type = "defense_penetration";
                    } else if (resultLeft.contains("criticalrate")) {
                        character.gear[gearNumber][i].type = "critical_rate";
                    } else if (resultLeft.contains("criticaldam")) {
                        character.gear[gearNumber][i].type = "critical_damage";
                    } else if (resultLeft.contains("skillcool")) {
                        character.gear[gearNumber][i].type = "skill_cooldown";
                    } else if (resultLeft.contains("attackspeed")) {
                        character.gear[gearNumber][i].type = "attack_speed";
                    } else if (resultLeft.contains("allattack")) {
                        character.gear[gearNumber][i].type = "all_attack";
                    } else if (resultLeft.contains("hp")) {
                        character.gear[gearNumber][i].type = "hp";
                    } else if (resultLeft.contains("dodge")) {
                        character.gear[gearNumber][i].type = "dodge";
                    } else if (resultLeft.contains("movement")) {
                        character.gear[gearNumber][i].type = "movement_speed";
                    } else if (resultLeft.contains("recovery")) {
                        character.gear[gearNumber][i].type = "recovery_rate";
                    } else if (resultLeft.contains("physicaldefenseby")) {
                        character.gear[gearNumber][i].type = "physical_defense_by_level";
                    } else if (resultLeft.contains("energydefenseby")) {
                        character.gear[gearNumber][i].type = "energy_defense_by_level";
                    } else if (resultLeft.contains("physicaldefense")) {
                        character.gear[gearNumber][i].type = "physical_defense";
                    } else if (resultLeft.contains("energydefense")) {
                        character.gear[gearNumber][i].type = "energy_defense";
                    } else if (resultLeft.contains("alldefense")) {
                        character.gear[gearNumber][i].type = "all_defense";
                    }

                    character.gear[gearNumber][i].val = truncateDouble(Double.parseDouble(resultRight), 1);

                }
                bmpTemp.recycle();
            }

        }

        return character;
    }


    public static boolean colorsAreSimilar(int color, int color2) {
        int x = (int) (Math.pow((Color.red(color2) - Color.red(color)), 2) + Math.pow((Color.blue(color2) - Color.blue(color)), 2) + Math.pow((Color.green(color2) - Color.green(color)), 2));

        return x > 2700;
    }

    public static int getColorSimilarity(int color, int color2) {
        int x = (int) (Math.pow((Color.red(color2) - Color.red(color)), 2) + Math.pow((Color.blue(color2) - Color.blue(color)), 2) + Math.pow((Color.green(color2) - Color.green(color)), 2));

        return (int) Math.sqrt(x);
    }

    private void replaceWithWhite(Bitmap myBitmap, int colorToChange, int similarity) {

        int[] allpixels = new int[myBitmap.getHeight() * myBitmap.getWidth()];
        int width = myBitmap.getWidth();
        int height = myBitmap.getHeight();
        myBitmap.getPixels(allpixels, 0, width, 0, 0, width, height);

        // set similarity threshold
        similarity = (int) Math.pow(similarity, 2) * 3;

        for (int i = 0; i < allpixels.length; i++) {
            // calculate the difference in color between current pixel and color to be changed
            int r = Color.red(allpixels[i]);
            int g = Color.green(allpixels[i]);
            int b = Color.blue(allpixels[i]);
            double d = Math.pow(Color.red(colorToChange) - r, 2) + Math.pow(Color.green(colorToChange) - g, 2) + Math.pow(Color.blue(colorToChange) - b, 2);

            // if current pixel is similar to color to be changed, change to white, otherwise to black
            allpixels[i] = (d < similarity) ? Color.WHITE : Color.BLACK;
        }

        myBitmap.setPixels(allpixels, 0, width, 0, 0, width, height);
    }


    public Bitmap getResizedBitmap(Bitmap bm, float scale) {
        int width = bm.getWidth();
        int height = bm.getHeight();

        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scale*1.6f, scale);

        // "RECREATE" THE NEW BITMAP
        bm = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        return bm;
    }

    // Save screenshot copy
    public static void saveScreenshotCopy(Bitmap bmp, File file) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 65, out);
        } catch (Exception e) {
            Log.d(TAG, "useOCRWeb photocopy: " + e.getMessage());
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                Log.d(TAG, "useOCRWeb photocopy: " + e.getMessage());
            }
            //Log.d(TAG, "onImageAvailable: created screenshot " + (System.currentTimeMillis()));
        }
    }

    public static double truncateDouble(double d, int decimals){
        return Math.floor(d*Math.pow(10, decimals))/Math.pow(10, decimals);

    }

    public void close() {
        mTessAPI.end();
    }
}
