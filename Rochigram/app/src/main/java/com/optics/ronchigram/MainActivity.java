/**
 * Created by Pratik Tambe on 4/2/2016.
 *
 * Author: Pratik M Tambe (c) 2016
 */
package com.optics.ronchigram;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.widget.ZoomButton;
import android.widget.ZoomButtonsController;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

class GlobalConstants {
    public static final int ronchigram_width = 125;
    public static final int ronchigram_height = 125;
    public static final int bitmap_width = 250;
    public static final int bitmap_height = 250;
    public static final int ronchigram_width_offset = 0;//(bitmap_width-ronchigram_width)/4;
    public static final int ronchigram_height_offset = 0;//(bitmap_height-ronchigram_height)/4;
    public static final float scaleFactorX = (float)((float)bitmap_width/(float)ronchigram_width);
    public static final float scaleFactorY = (float)((float)bitmap_height/(float)ronchigram_height);
    public static final int ronchigram_background_color = Color.YELLOW;
    public static final int ronchigram_foreground_color = Color.DKGRAY;
    public static final int bitmap_background_color = Color.BLACK;
    public static final int bitmap_skip_pixels_interval = 1;
    public static final int ronchigram_offset_offset = 700;
    public static final int ronchigram_diameter_offset = 3;
    public static final int ronchigram_focal_length_offset = 1;
    public static final int ronchigram_grating_offset = 20;
    //this is your shared preference file name, in which we will save data
    public static final String RONCHIGRAM_PREFS = "Ronchigram[k=-1]Pref";
}

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private SeekBar offsetBar;
    private TextView offsetView;
    private TextView diameterView;
    private SeekBar diameterBar;
    private TextView focalLengthView;
    private SeekBar focalLengthBar;
    private TextView gratingView;
    private SeekBar gratingBar;
    private Bitmap bitmap;
    private ImageView ronchiImageView;
    private Button zoomInButton;
    private Button zoomOutButton;
    private Button autoFocusButton;
    private Button saveImageButton;
    private PictureThread thread0;
    //private Button instructions;
    private TextView renderingStatusView;
    private TextView marqueeHorizontalView;

    private AdView mAdView;
    private InterstitialAd mInterstitialAd;

    //private Button closeAbout;
    //The "x" and "y" position of the "Show Button" on screen.
    private Point p;
    private Camera camera;
    private Context ctx;
    private SurfaceView glsv;
    private MainView mView;
    private PowerManager.WakeLock mWL;

    public double twoDigitRoundedDouble(double value){
        if (value != 0.0){
            value = value*100;
            value = (double)((int)value);
            value = value/100;
        }
        return value;
    }

    @Override
    protected void onStart(){
        super.onStart();

        final Toast toast = Toast.makeText(this,"Read the rolling text for important information", Toast.LENGTH_SHORT);
        //final Toast toast = Toast.makeText(this,"Instructions:\nFirst, set the attributes 'Diameter', 'Focal Length' and 'Grating'.\nThen adjust the attribute 'Offset' to view the desired Ronchigram.\nThe attributes 'Zoom+/-', 'Autofocus' and 'Save Pic' allows one to compare the actual Ronchigram with the one rendered from this App and save a snapshot at the folder location '/Ronchigram[k=-1]' on an external storage.\"", Toast.LENGTH_SHORT);
        LinearLayout toastLayout = (LinearLayout) toast.getView();
        TextView toastTV = (TextView) toastLayout.getChildAt(0);
        toastTV.setTextSize(15);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        CountDownTimer timer = null;
        timer = new CountDownTimer(3000, 1000)
        {
            public void onTick(long millisUntilFinished)
            {
                toast.show();
            }
            public void onFinish()
            {
                toast.cancel();
            }

        }.start();

    }

    @Override
    protected void onResume(){
        super.onResume();
        mView.onResume();
        if(!mWL.isHeld()) {
            mWL.acquire();
        }

        final Toast toast = Toast.makeText(this,"Read the rolling text for important information.", Toast.LENGTH_SHORT);
        //final Toast toast = Toast.makeText(this,"Instructions:\nFirst, set the attributes 'Diameter', 'Focal Length' and 'Grating'.\nThen adjust the attribute 'Offset' to view the desired Ronchigram.\nThe attributes 'Zoom+/-', 'Autofocus' and 'Save Pic' allows one to compare the actual Ronchigram with the one rendered from this App and save a snapshot at the folder location '/Ronchigram[k=-1]' on an external storage.\"", Toast.LENGTH_SHORT);
        LinearLayout toastLayout = (LinearLayout) toast.getView();
        TextView toastTV = (TextView) toastLayout.getChildAt(0);
        toastTV.setTextSize(15);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        CountDownTimer timer = null;
        timer = new CountDownTimer(3000, 1000)
        {
            public void onTick(long millisUntilFinished)
            {
                toast.show();
            }
            public void onFinish()
            {
                toast.cancel();
            }
            public void onCancel(){

            }

        }.start();

        if (mAdView != null) {
            mAdView.resume();
        }
    }

    /** Called when leaving the activity */
    @Override
    public void onPause() {
        if (mWL.isHeld()) {
            mWL.release();
        }
        mView.onPause();
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    /** Called before the activity is destroyed */
    @Override
    public void onDestroy() {
        if (mWL.isHeld()) {
            mWL.release();
        }
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    /*
    // The method that displays the popup.
    private void showAbout(final Activity context, Point p) {
        int popupWidth = 700;
        int popupHeight = 250;

        // Inflate the popup_layout.xml
        LinearLayout viewGroup = (LinearLayout) context.findViewById(R.id.popup);
        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.popup_layout, viewGroup);

        // Creating the PopupWindow
        final PopupWindow popup = new PopupWindow(context);
        popup.setContentView(layout);
        popup.setWidth(popupWidth);
        popup.setHeight(popupHeight);
        popup.setFocusable(true);

        // Some offset to align the popup a bit to the right, and a bit down, relative to button's position.
        int OFFSET_X = 30;
        int OFFSET_Y = 0;

        // Clear the default translucent background
        popup.setBackgroundDrawable(new BitmapDrawable());

        // Displaying the popup at the specified location, + offsets.
        popup.showAtLocation(layout, Gravity.NO_GRAVITY, p.x + OFFSET_X, p.y + OFFSET_Y);

        // Getting a reference to Close button, and close the popup when clicked.

        ImageButton close = (ImageButton) layout.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                popup.dismiss();
            }
        });

    }
    */

    public void displayInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    public void LoadFromPref()
    {
        SharedPreferences settings 	= ctx.getSharedPreferences(GlobalConstants.RONCHIGRAM_PREFS, 0);
        // Note here the 2nd parameter 0 is the default parameter for private access,
        //Operating mode. Use 0 or MODE_PRIVATE for the default operation,
        int offset = settings.getInt("offset",0); // 1st parameter Name is the key and 2nd parameter is the default if data not found
        int diameter = settings.getInt("diameter", 0);
        int focal_length = settings.getInt("focal_length",0);
        int grating = settings.getInt("grating",0);
        int zoom_setting = settings.getInt("zoom_setting",0);

        // Offset Bar
        //offsetBar.setMax(0);
        //offsetBar.setMax(offsetBar.getMax());
        offsetBar.setProgress(offset);
        offsetBar.refreshDrawableState();

        // diameter Bar
        //diameterBar.setMax(0);
        //diameterBar.setMax(diameterBar.getMax());
        diameterBar.setProgress(diameter);
        diameterBar.refreshDrawableState();

        // focal Length Bar
        //focalLengthBar.setMax(0);
        //focalLengthBar.setMax(focalLengthBar.getMax());
        focalLengthBar.setProgress(focal_length);
        focalLengthBar.refreshDrawableState();

        // grating Bar
        //gratingBar.setMax(0);
        //gratingBar.setMax(gratingBar.getMax());
        gratingBar.setProgress(grating);
        gratingBar.refreshDrawableState();

        //zoom setting
        mView.setZoomSetting(zoom_setting);
    }

    public void StoreToPref()
    {
        // get the existing preference file
        SharedPreferences settings = ctx.getSharedPreferences(GlobalConstants.RONCHIGRAM_PREFS, 0);
        //need an editor to edit and save values
        SharedPreferences.Editor editor = settings.edit();

        int offset = offsetBar.getProgress();
        int diameter = diameterBar.getProgress();
        int focal_length = focalLengthBar.getProgress();
        int grating = gratingBar.getProgress();
        int zoom_setting = mView.getZoomSetting();
        editor.putInt("offset", offset);
        editor.putInt("diameter",diameter);
        editor.putInt("focal_length", focal_length);
        editor.putInt("grating", grating);
        editor.putInt("zoom_setting", zoom_setting);

        //final step to commit (save)the changes in to the shared pref
        editor.commit();
    }

    public void DeleteSingleEntryFromPref(String keyName)
    {
        SharedPreferences settings = ctx.getSharedPreferences(GlobalConstants.RONCHIGRAM_PREFS, 0);
        //need an editor to edit and save values
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(keyName);
        editor.commit();
    }

    public void DeleteAllEntriesFromPref()
    {
        SharedPreferences settings = ctx.getSharedPreferences(GlobalConstants.RONCHIGRAM_PREFS, 0);
        //need an editor to edit and save values
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.commit();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mWL = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.FULL_WAKE_LOCK, "WakeLock");
        mWL.acquire();

        ctx = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bitmap = Bitmap.createBitmap(GlobalConstants.bitmap_width,GlobalConstants.bitmap_width,Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setDither(true);
        canvas.drawColor(GlobalConstants.bitmap_background_color);
        offsetBar = (SeekBar) findViewById(R.id.offsetBar);
        offsetView = (TextView) findViewById(R.id.offsetView);
        diameterBar = (SeekBar) findViewById(R.id.diameterBar);
        diameterView = (TextView) findViewById(R.id.diameterView);
        focalLengthBar = (SeekBar) findViewById(R.id.focalLengthBar);
        focalLengthView = (TextView) findViewById(R.id.focalLengthView);
        gratingBar = (SeekBar) findViewById(R.id.gratingBar);
        gratingView = (TextView) findViewById(R.id.gratingView);
        ronchiImageView = (ImageView) findViewById(R.id.ronchigramView);
        ronchiImageView.setImageBitmap(bitmap);
        renderingStatusView = (TextView) findViewById(R.id.textRenderingStatusView);
        marqueeHorizontalView = (TextView) findViewById(R.id.scrollingHorizonatalView);
        zoomInButton = (Button) findViewById(R.id.zoomInButton);
        zoomOutButton = (Button) findViewById(R.id.zoomOutButton);
        autoFocusButton = (Button) findViewById(R.id.autoFocusButton);
        saveImageButton = (Button) findViewById(R.id.saveImageButton);


        mView = (MainView) findViewById(R.id.main_view);
        //mView = new MainView(this);
        //mView.setX(100);
        //mView.setY(100);
        //addContentView(mView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        //addContentView(mView, new ViewGroup.LayoutParams(320, 240));


        // Create the InterstitialAd and set the adUnitId.
        mInterstitialAd = new InterstitialAd(this);
        // Defined in res/values/strings.xml
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));


        AdRequest adRequestInterstetial = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequestInterstetial);
        mInterstitialAd.setAdListener(new AdListener() {
            public void onAdLoaded() {
                //mView.stopPreview();
                displayInterstitial();
            }
            public void onAdClosed(){
                //mView.startPreview();
            }
        });




        // Gets the ad view defined in layout/ad_fragment.xml with ad unit ID set in
        // values/strings.xml.
        mAdView = (AdView) findViewById(R.id.ad_view);

        // Create an ad request. Check your logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        /*
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        */

        AdRequest adRequest = new AdRequest.Builder()
                .build();

        // Start loading the ad in the background.
        mAdView.loadAd(adRequest);

        //snapShot = (Button) findViewById(R.id.surfaceViewButton);
        //snapShot.setText("Take Picture");
        //instructions = (Button) findViewById(R.id.instructionsViewButton);
        //instructions.setText("Instructions");
        //glsv = (SurfaceView)findViewById(R.id.surfaceView);
        //closeAbout = (Button) findViewById(R.id.close);
        //closeAbout.setText("Close");
        p = new Point();
        p.x = 150;
        p.y = 100;
        Ronchigram(GlobalConstants.ronchigram_width, GlobalConstants.ronchigram_height, canvas, paint);
        thread0 = new PictureThread(ronchiImageView, bitmap, renderingStatusView);
        thread0.start();

        /*
        instructions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //Open popup window
                if (p != null)
                    showAbout(MainActivity.this, p);
            }
        });
        */

        zoomInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mView.zoomIn();
            }
        });

        zoomOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mView.zoomOut();
            }
        });


        autoFocusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mView.autoFocus();
            }
        });

        saveImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mView.saveImage();
            }
        });



        // Initialize the textview with '0'
        offsetView.setText("Offset: " + twoDigitRoundedDouble(0.01 * (offsetBar.getProgress() - GlobalConstants.ronchigram_offset_offset))+" inches");
        offsetBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                thread0.updateRonchigram(0.01 * (seekBar.getProgress() - GlobalConstants.ronchigram_offset_offset),
                        0.5 * (diameterBar.getProgress() + GlobalConstants.ronchigram_diameter_offset),
                        (focalLengthBar.getProgress() + GlobalConstants.ronchigram_focal_length_offset),
                        (gratingBar.getProgress() + GlobalConstants.ronchigram_grating_offset)
                );
                renderingStatusView.setText("Please wait...Rendering the image.");
                offsetView.setText("Offset: " + twoDigitRoundedDouble(0.01 * (seekBar.getProgress() - GlobalConstants.ronchigram_offset_offset)) + " inches");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                offsetView.setText("Offset: " + twoDigitRoundedDouble(0.01 * (seekBar.getProgress() - GlobalConstants.ronchigram_offset_offset)) + " inches");
            }
        });

        // Initialize the textview with '0'
        diameterView.setText("Diameter: "+twoDigitRoundedDouble(0.5 * (diameterBar.getProgress() + GlobalConstants.ronchigram_diameter_offset))+" inches");
        diameterBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //thread.updateRonchigram(0.5 * (seekBar.getProgress() - GlobalConstants.ronchigram_diameter_offset));
                thread0.updateRonchigram(0.01 * (offsetBar.getProgress() - GlobalConstants.ronchigram_offset_offset),
                        0.5 * (seekBar.getProgress() + GlobalConstants.ronchigram_diameter_offset),
                        (focalLengthBar.getProgress() + GlobalConstants.ronchigram_focal_length_offset),
                        (gratingBar.getProgress() + GlobalConstants.ronchigram_grating_offset)
                );
                renderingStatusView.setText("Please wait...Rendering the image.");
                diameterView.setText("Diameter: "+twoDigitRoundedDouble(0.5 * (seekBar.getProgress() + GlobalConstants.ronchigram_diameter_offset))+" inches");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                diameterView.setText("Diameter: "+twoDigitRoundedDouble(0.5 * (seekBar.getProgress() + GlobalConstants.ronchigram_diameter_offset))+" inches");
            }
        });

        // Initialize the textview with '0'
        focalLengthView.setText("Focal Length: "+twoDigitRoundedDouble((focalLengthBar.getProgress() + GlobalConstants.ronchigram_focal_length_offset))+" inches");
        focalLengthBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                thread0.updateRonchigram(0.01 * (offsetBar.getProgress() - GlobalConstants.ronchigram_offset_offset),
                        0.5 * (diameterBar.getProgress() + GlobalConstants.ronchigram_diameter_offset),
                        (seekBar.getProgress() + GlobalConstants.ronchigram_focal_length_offset),
                        (gratingBar.getProgress() + GlobalConstants.ronchigram_grating_offset)
                );
                renderingStatusView.setText("Please wait...Rendering the image.");
                focalLengthView.setText("Focal Length: " + twoDigitRoundedDouble((seekBar.getProgress() + GlobalConstants.ronchigram_focal_length_offset))+" inches");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                focalLengthView.setText("Focal Length: " + twoDigitRoundedDouble((seekBar.getProgress() + GlobalConstants.ronchigram_focal_length_offset))+" inches");
            }
        });

        // Initialize the textview with '0'
        gratingView.setText("Grating: "+twoDigitRoundedDouble((gratingBar.getProgress() + GlobalConstants.ronchigram_grating_offset))+" lines per inch");
        gratingBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                thread0.updateRonchigram(0.01 * (gratingBar.getProgress() - GlobalConstants.ronchigram_grating_offset),
                        0.5 * (diameterBar.getProgress() + GlobalConstants.ronchigram_diameter_offset),
                        (focalLengthBar.getProgress() + GlobalConstants.ronchigram_focal_length_offset),
                        (seekBar.getProgress() + GlobalConstants.ronchigram_grating_offset)
                );
                renderingStatusView.setText("Please wait...Rendering the image.");
                gratingView.setText("Grating: " + twoDigitRoundedDouble((seekBar.getProgress() + GlobalConstants.ronchigram_grating_offset))+" lines per inch");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                gratingView.setText("Grating: " + twoDigitRoundedDouble((seekBar.getProgress() + GlobalConstants.ronchigram_grating_offset))+" lines per inch");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.About:
                final Toast toast = Toast.makeText(this, "Author: Pratik M Tambe (C) 2016\nVersion 0.4\nCredits: \n(1) Code Inspiration: brainwagon (Mark VandeWettering) - GitHub\n(2) Amateur Telescope Making Class: Guy Brandenburg  \n(3) National Capital Astronomers, Washington D.C. Metro Area", Toast.LENGTH_LONG);
                LinearLayout toastLayout = (LinearLayout) toast.getView();
                TextView toastTV = (TextView) toastLayout.getChildAt(0);
                toastTV.setTextSize(15);
                toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                CountDownTimer timer = null;
                timer = new CountDownTimer(8000, 1000)
                {
                    public void onTick(long millisUntilFinished)
                    {
                        toast.show();
                    }
                    public void onFinish()
                    {
                        toast.cancel();
                    }

                }.start();
                return true;
            case R.id.Disclaimer:
                final Toast toast1 = Toast.makeText(this, "Disclaimer:\nThis App/software is supplied \"AS IS\" without any warranties and support.\n\nThe author assumes no responsibility or liability for the use of the App/software, conveys no license or title under any patent, or copyright.\n\nThe author reserves the right to make changes in the App/software without notification. The author also makes no representation or warranty that such application will be suitable for the specified use without further testing or modification.", Toast.LENGTH_LONG);
                LinearLayout toastLayout1 = (LinearLayout) toast1.getView();
                TextView toastTV1 = (TextView) toastLayout1.getChildAt(0);
                toastTV1.setTextSize(15);
                toast1.show();
                toast1.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                CountDownTimer timer1 = null;
                timer1 = new CountDownTimer(20000, 1000)
                {
                    public void onTick(long millisUntilFinished)
                    {
                        toast1.show();
                    }
                    public void onFinish()
                    {
                        toast1.cancel();
                    }

                }.start();
                return true;
            case R.id.LoadConfig:
                final Toast toast2 = Toast.makeText(this, "Loading saved preferences...", Toast.LENGTH_SHORT);
                LinearLayout toastLayout2 = (LinearLayout) toast2.getView();
                TextView toastTV2 = (TextView) toastLayout2.getChildAt(0);
                toastTV2.setTextSize(15);
                toast2.show();
                toast2.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                LoadFromPref();
                return true;
            case R.id.SaveConfig:
                final Toast toast3 = Toast.makeText(this, "Saving preferences...", Toast.LENGTH_SHORT);
                LinearLayout toastLayout3 = (LinearLayout) toast3.getView();
                TextView toastTV3 = (TextView) toastLayout3.getChildAt(0);
                toastTV3.setTextSize(15);
                toast3.show();
                toast3.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                StoreToPref();
                return true;
            case R.id.Instructions:
                final Toast toast4 = Toast.makeText(this, "Instructions:\n1. Set the attributes 'Diameter', 'Focal Length' and 'Grating'.\n2. Adjust the attribute 'Offset' to view the desired Ronchigram Simulation.\n3. Click on the attributes 'Zoom +' or 'Zoom -' to obtain the desired Ronchigram Field Of View (FOV).\n4. Click on the attribute 'Autofocus' once step # 3. is executed.\n5. Click on the attribute 'Save Pic' to save a snapshot at the folder location '/Ronchigram[k=-1]' on an external storage.\n\nAny android file explorer app may be used to view the snapshot.\nNote: Camera Preview (Live Video Stream) quality may vary depending on the handset hardware.\nThis process allows one to compare the actual Ronchigram with the one rendered from this App.\n", Toast.LENGTH_LONG);
                LinearLayout toastLayout4 = (LinearLayout) toast4.getView();
                TextView toastTV4 = (TextView) toastLayout4.getChildAt(0);
                toastTV4.setTextSize(15);
                toast4.show();
                toast4.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                CountDownTimer timer2 = null;
                timer2 = new CountDownTimer(25000, 1000)
                {
                    public void onTick(long millisUntilFinished)
                    {
                        toast4.show();
                    }
                    public void onFinish()
                    {
                        toast4.cancel();
                    }

                }.start();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public class Vec{
        double [] pt = new double [3];
    }
    public double VecDot(Vec a, Vec b){
        return (double)((a.pt[0])*(b.pt[0])+(a.pt[1])*(b.pt[1])+(a.pt[2])*(b.pt[2]));
    }

    public double VecLen(Vec a){
        return (double)Math.sqrt(VecDot(a, a));
    }
    //################################ Example Paramaters #########################################
    //Diameter of the mirror in inches e.g. 6 inches
    //double diameter = 6.0 ;
    //focal length of the mirror in inches e.g. 48 inches (or 96 inches ROC - Radius Of Curvature)
    //double     flen = 48.0 ;
    //Offset in inches
    //double   offset = 0.0;
    //Grating lines per unit - e.g. 100
    //double grating = 100.0 ;
    //For Parabola K = -1.0
    //################################ Example Paramaters #########################################
    //Diameter of the mirror in inches e.g. 6 inches
    double diameter = 0.5 * (0 + GlobalConstants.ronchigram_diameter_offset) ;
    //focal length of the mirror in inches e.g. 48 inches (or 96 inches ROC - Radius Of Curvature)
    double     flen = (0 + GlobalConstants.ronchigram_focal_length_offset) ;
    //Offset in inches
    double   offset = 0.01 * (50 - GlobalConstants.ronchigram_offset_offset) ;
    //Grating lines per unit - e.g. 100
    double grating = (0 + GlobalConstants.ronchigram_grating_offset) ;
    //For Parabola K = -1.0
    double     K = -1.0 ;
    int c, x, y ;
    double r, fx, fy, gx, t ;

    public void VecNormalize(Vec a)
    {
        double l = VecLen(a) ;

        a.pt[0] /= (double)l ; a.pt[1] /= (double)l ; a.pt[2] /= (double)l ;
    }

    public void VecSub(Vec r, Vec a, Vec b)
    {
        r.pt[0] = a.pt[0] - b.pt[0] ;
        r.pt[1] = a.pt[1] - b.pt[1] ;
        r.pt[2] = a.pt[2] - b.pt[2] ;
    }

    public void Reflect(Vec R, Vec I, Vec N)
    {
        double c = -2.0 * VecDot(I, N) ;
        R.pt[0] = c * N.pt[0] + I.pt[0] ;
        R.pt[1] = c * N.pt[1] + I.pt[1] ;
        R.pt[2] = c * N.pt[2] + I.pt[2] ;
        VecNormalize(R);
    }

    public void Ronchigram(int width, int height, Canvas canvas, Paint paint) {
        final int canvasSavedState = canvas.save();
        canvas.scale(GlobalConstants.scaleFactorX, GlobalConstants.scaleFactorY);//scales the images to make the screen look somewhat normal
        // coordinates for the center of the window
        r = diameter / (double)2;
        Vec I, N, R, P, O ;

        I = new Vec();
        N = new Vec();
        R = new Vec();
        P = new Vec();
        O = new Vec();

        O.pt[0] = O.pt[1] = 0.0;
        //distance of the mirror to grating = ROC + offset = 2*focal length + offset
        O.pt[2] = 2.0 * flen + offset;

        for (y = 0; y < height; y++) {
            //scale down the diameter to fit the resolution
            fy = (double) (y * diameter) / (double) height - (double) diameter / 2.0;
            for (x = 0; x < width; x++) {
                fx = (double) (x * diameter) / (double) width - (double) diameter / 2.0;
                if (fx * fx + fy * fy > r * r) {
                    if((x % GlobalConstants.bitmap_skip_pixels_interval) == 0) {
                        paint.setStyle(Paint.Style.FILL);
                        paint.setColor(Color.BLACK);
                        canvas.drawPoint(x + GlobalConstants.ronchigram_width_offset, y + GlobalConstants.ronchigram_height_offset, paint);
                    }
                    continue;
                }
                // construct P
                P.pt[0] = fx;
                P.pt[1] = fy;

                if (Math.abs(K + 1.0) < 1e-5)
                    P.pt[2] = (double) (fx * fx + fy * fy) / (double) (4.0 * flen);
                else
                    P.pt[2] = (double) (2 * flen - (double) Math.sqrt((K + 1) * (-fx * fx - fy * fy) + 4 * flen * flen)) / (double) (K + 1);

                //light rays
                // I starts at O and ends at P
                VecSub(I, P, O);
                VecNormalize(I);

                N.pt[0] = -2.0 * P.pt[0];
                N.pt[1] = -2.0 * P.pt[1];
                N.pt[2] = 4.0 * flen - 2.0 * (K + 1) * P.pt[2];
                VecNormalize(N);

                Reflect(R, I, N);

                // compute the intersection of R
                // with the plane z = R + offset
                t = (double) (2.0 * flen + offset - P.pt[2]) / (double) R.pt[2];

                assert (t > 0.0);

                gx = (double) (P.pt[0] + t * R.pt[0]) * 2.0 * grating - 0.5;

                if (((int) Integer.valueOf((int) Math.floor(gx)) & 1) != 0) {
                    if((x % GlobalConstants.bitmap_skip_pixels_interval) == 0) {
                        paint.setStyle(Paint.Style.FILL);
                        paint.setColor(GlobalConstants.ronchigram_foreground_color);
                        canvas.drawPoint(x + GlobalConstants.ronchigram_width_offset, y + GlobalConstants.ronchigram_height_offset, paint);
                    }
                } else {
                    if((x % GlobalConstants.bitmap_skip_pixels_interval) == 0) {
                        paint.setStyle(Paint.Style.FILL);
                        paint.setColor(GlobalConstants.ronchigram_background_color);
                        canvas.drawPoint(x + GlobalConstants.ronchigram_width_offset, y + GlobalConstants.ronchigram_height_offset, paint);
                    }
                }

            }
        }
        canvas.restoreToCount(canvasSavedState);
    }
}
