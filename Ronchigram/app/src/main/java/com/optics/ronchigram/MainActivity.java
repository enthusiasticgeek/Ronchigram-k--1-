/**
 * Created by Pratik Tambe on 4/2/2016.
 *
 * Author: Pratik M Tambe (c) 2016
 */
package com.optics.ronchigram;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.widget.ZoomButton;
import android.widget.ZoomButtonsController;

import pub.devrel.easypermissions.EasyPermissions;


class GlobalConstants {
    public static final int ronchigram_width = 200;
    public static final int ronchigram_height = 200;
    public static final int bitmap_width = 200;
    public static final int bitmap_height = 200;
    public static final int ronchigram_width_offset = 0;//(bitmap_width-ronchigram_width)/4;
    public static final int ronchigram_height_offset = 0;//(bitmap_height-ronchigram_height)/4;
    public static final float scaleFactorX = (float)((float)bitmap_width/(float)ronchigram_width);
    public static final float scaleFactorY = (float)((float)bitmap_height/(float)ronchigram_height);
    public static final int ronchigram_background_color = Color.WHITE; //Color.parseColor("#FFFFFF");
    public static final int ronchigram_foreground_color = Color.DKGRAY;
    public static final int bitmap_background_color = Color.BLACK;
    public static final int bitmap_skip_pixels_interval = 1;
    public static final int ronchigram_offset_offset = 700;
    public static final int ronchigram_diameter_offset = 3;
    public static final int ronchigram_focal_length_offset = 1;
    public static final int ronchigram_grating_offset = 20;
    //this is your shared preference file name, in which we will save data
    public static final String RONCHIGRAM_PREFS = "Ronchigram[k=-1]Pref";
    //Show Image from SSD
    public static final int REQUEST_PERMISSION = 1;
    public static final int REQUEST_IMAGE_PICK = 2;
}

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private static final int CAMERA_REQUEST_CODE = 100;
    private SeekBar offsetBar;
    private TextView offsetView;

    private EditText diameterText;

    private EditText focalLengthText;

    private EditText gratingText;

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
    private TextView toastView;

    private ImageView imageView;
    private Button openImagePickerButton;
    //private Button closeAbout;
    //The "x" and "y" position of the "Show Button" on screen.
    private Point p;
    private Camera camera;
    private Context ctx;
    private SurfaceView glsv;
    private MainView mView;
    private PowerManager.WakeLock mWL;

    private ScaleGestureDetector scaleGestureDetector;
    private float scaleFactor = 1.0f;


    private void askAboutCamera(){

        EasyPermissions.requestPermissions(
                this,
                "From this point camera permission is required.",
                CAMERA_REQUEST_CODE,
                Manifest.permission.CAMERA );
    }


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
        askAboutCamera();
        toastMessage("Welcome to Ronchigram App! Click 3 vertical dots on the top right corner to get started!");
        marqueeHorizontalView.setSelected(true);
    }

    @Override
    protected void onResume(){
        super.onResume();
        mView.onResume();
        if(!mWL.isHeld()) {
            mWL.acquire();
        }
        toastMessage("Welcome to Ronchigram App! Click 3 vertical dots on the top right corner to get started!");
        marqueeHorizontalView.setSelected(true);
    }

    /** Called when leaving the activity */
    @Override
    public void onPause() {
        if (mWL.isHeld()) {
            mWL.release();
        }
        mView.onPause();

        super.onPause();
    }

    /** Called before the activity is destroyed */
    @Override
    public void onDestroy() {
        if (mWL.isHeld()) {
            mWL.release();
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

    }

    public void setOffsetFromButton(int offset){
        offsetBar.setProgress(offset);
    }

    public void loadFromPreferences() {
        SharedPreferences settings = ctx.getSharedPreferences(GlobalConstants.RONCHIGRAM_PREFS, Context.MODE_PRIVATE);

        int offset = settings.getInt("offset", 0);
        float diameter = settings.getFloat("diameter", 0.0f);
        float focal_length = settings.getFloat("focal_length", 0.0f);
        int grating = settings.getInt("grating", 0);
        int zoom_setting = settings.getInt("zoom_setting", 0);

        // Offset Bar
        offsetBar.setProgress(offset);

        // diameter Bar
        diameterText.setText(String.valueOf(diameter));

        // focal Length Bar
        focalLengthText.setText(String.valueOf(focal_length));

        // grating Bar
        gratingText.setText(String.valueOf(grating));

        //zoom setting
        mView.setZoomSetting(zoom_setting);
    }

    public void storeToPreferences() {
        SharedPreferences settings = ctx.getSharedPreferences(GlobalConstants.RONCHIGRAM_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        int offset = offsetBar.getProgress();
        float diameter = Float.parseFloat(diameterText.getText().toString());
        float focal_length = Float.parseFloat(focalLengthText.getText().toString());
        int grating = Integer.parseInt(gratingText.getText().toString());
        int zoom_setting = mView.getZoomSetting();

        editor.putInt("offset", offset);
        editor.putFloat("diameter", diameter);
        editor.putFloat("focal_length", focal_length);
        editor.putInt("grating", grating);
        editor.putInt("zoom_setting", zoom_setting);

        editor.apply();
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



    private void openImagePicker_orig() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, GlobalConstants.REQUEST_IMAGE_PICK);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");

        // Set the initial directory to the "Pictures" directory
        Uri initialUri = Uri.parse("content://com.android.externalstorage.documents/document/primary%3APictures");
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri);

        startActivityForResult(intent, GlobalConstants.REQUEST_IMAGE_PICK);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(0.8f, Math.min(scaleFactor, 2.0f));

            // Get the original dimensions of the image
            Drawable drawable = imageView.getDrawable();
            if (drawable != null) {
                int originalWidth = drawable.getIntrinsicWidth();
                int originalHeight = drawable.getIntrinsicHeight();

                // Get the dimensions of the ImageView
                int imageViewWidth = imageView.getWidth();
                int imageViewHeight = imageView.getHeight();

                // Calculate the maximum scale factor based on dimensions
                float maxScaleFactorX = (float) originalWidth / imageViewWidth;
                float maxScaleFactorY = (float) originalHeight / imageViewHeight;
                float maxScaleFactor = Math.min(maxScaleFactorX, maxScaleFactorY);

                // Apply the maximum scale factor
                scaleFactor = Math.min(scaleFactor, maxScaleFactor);
            }

            imageView.setScaleX(scaleFactor);
            imageView.setScaleY(scaleFactor);
            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GlobalConstants.REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            ImageView imageView = findViewById(R.id.imageView);
            imageView.setImageURI(selectedImageUri);

            // Rotate the image by 90 degrees clockwise
            Matrix matrix = new Matrix();
            matrix.postRotate(90);

            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            imageView.setImageBitmap(rotatedBitmap);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mWL = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.FULL_WAKE_LOCK, "rochigram:WakeLock");
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
        diameterText = (EditText) findViewById(R.id.diameterText);
        focalLengthText = (EditText) findViewById(R.id.focalLengthText);
        gratingText = (EditText) findViewById(R.id.gratingText);
        ronchiImageView = (ImageView) findViewById(R.id.ronchigramView);
        ronchiImageView.setImageBitmap(bitmap);
        renderingStatusView = (TextView) findViewById(R.id.textRenderingStatusView);
        marqueeHorizontalView = (TextView) findViewById(R.id.scrollingHorizontalView);
        zoomInButton = (Button) findViewById(R.id.zoomInButton);
        zoomOutButton = (Button) findViewById(R.id.zoomOutButton);
        autoFocusButton = (Button) findViewById(R.id.autoFocusButton);
        saveImageButton = (Button) findViewById(R.id.saveImageButton);
        toastView = (TextView) findViewById(R.id.text_toast);
        mView = (MainView) findViewById(R.id.main_view);

        imageView = findViewById(R.id.imageView);
        imageView.setOnClickListener(v -> openImagePicker());
        openImagePickerButton = findViewById(R.id.buttonOpenImagePicker);
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

        diameterText.setText("4.25"); // Set your initial value here
        focalLengthText.setText("20"); // Set your initial value here
        gratingText.setText("100"); // Set your initial value here
        //mView = new MainView(this);
        //mView.setX(100);
        //mView.setY(100);
        //addContentView(mView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        //addContentView(mView, new ViewGroup.LayoutParams(320, 240));

        // Create an ad request. Check your logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        /*
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        */
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

        openImagePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

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
                mView.saveImage(); toastMessage("Saving Image to the 'Pictures' folder with the format 'ronchi_<epoch_time>.jpg...");
            }
        });

        // Initialize the textview with '0'
        offsetView.setText("Offset: " + twoDigitRoundedDouble(0.01 * (offsetBar.getProgress() - GlobalConstants.ronchigram_offset_offset))+" inches");
        offsetBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            // Getting diameter as float
            float diameter1 = Float.parseFloat(diameterText.getText().toString());
            // Getting focal_length as float
            float focal_length1 = Float.parseFloat(focalLengthText.getText().toString());
            // Getting grating as int
            int grating1 = Integer.parseInt(gratingText.getText().toString());
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                thread0.updateRonchigram(0.01 * (seekBar.getProgress() - GlobalConstants.ronchigram_offset_offset),
                        (diameter1 + GlobalConstants.ronchigram_diameter_offset),
                        (focal_length1 + GlobalConstants.ronchigram_focal_length_offset),
                        (grating1 + GlobalConstants.ronchigram_grating_offset)
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

        diameterText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                String inputText = editable.toString();
                if (!inputText.isEmpty()) {
                    try {
                        float inputValue = Float.parseFloat(inputText);

                        // Apply your min and max constraints
                        float minValue = 3.0f;
                        float maxValue = 50.0f;

                        if (inputValue < minValue) {
                            diameterText.setError("Minimum value is 3");
                        } else if (inputValue > maxValue) {
                            diameterText.setError("Maximum value is 50");
                        } else {
                            diameterText.setError(null); // Clear error

                            // Getting diameter as float
                            float diameter1 = Float.parseFloat(diameterText.getText().toString());
                            // Getting focal_length as float
                            float focal_length1 = Float.parseFloat(focalLengthText.getText().toString());
                            // Getting grating as int
                            int grating1 = Integer.parseInt(gratingText.getText().toString());
                            thread0.updateRonchigram(0.01 * (offsetBar.getProgress() - GlobalConstants.ronchigram_offset_offset),
                                    (diameter1 + GlobalConstants.ronchigram_diameter_offset),
                                    (focal_length1 + GlobalConstants.ronchigram_focal_length_offset),
                                    (grating1 + GlobalConstants.ronchigram_grating_offset)
                            );
                        }
                    } catch (NumberFormatException e) {
                        diameterText.setError("Invalid input"); // Set error for non-numeric input
                    }
                } else {
                    diameterText.setError(null); // Clear error when gratingText is empty
                }
            }

        });

        focalLengthText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                String inputText = editable.toString();
                if (!inputText.isEmpty()) {
                    try {
                        float inputValue = Float.parseFloat(inputText);

                        // Apply your min and max constraints
                        float minValue = 1.0f;
                        float maxValue = 100.0f;

                        if (inputValue < minValue) {
                            focalLengthText.setError("Minimum value is 1");
                        } else if (inputValue > maxValue) {
                            focalLengthText.setError("Maximum value is 100");
                        } else {
                            focalLengthText.setError(null); // Clear error

                            // Getting diameter as float
                            float diameter1 = Float.parseFloat(diameterText.getText().toString());
                            // Getting focal_length as float
                            float focal_length1 = Float.parseFloat(focalLengthText.getText().toString());
                            // Getting grating as int
                            int grating1 = Integer.parseInt(gratingText.getText().toString());
                            thread0.updateRonchigram(0.01 * (offsetBar.getProgress() - GlobalConstants.ronchigram_offset_offset),
                                    (diameter1 + GlobalConstants.ronchigram_diameter_offset),
                                    (focal_length1 + GlobalConstants.ronchigram_focal_length_offset),
                                    (grating1 + GlobalConstants.ronchigram_grating_offset)
                            );
                        }
                    } catch (NumberFormatException e) {
                        focalLengthText.setError("Invalid input"); // Set error for non-numeric input
                    }
                } else {
                    focalLengthText.setError(null); // Clear error when gratingText is empty
                }
            }

        });
        gratingText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                String inputText = editable.toString();
                if (!inputText.isEmpty()) {
                    try {
                        float inputValue = Float.parseFloat(inputText);

                        // Apply your min and max constraints
                        int minValue = 20;
                        int maxValue = 1000;

                        if (inputValue < minValue) {
                            gratingText.setError("Minimum value is 20");
                        } else if (inputValue > maxValue) {
                            gratingText.setError("Maximum value is 1000");
                        } else {
                            gratingText.setError(null); // Clear error

                            // Getting diameter as float
                            float diameter1 = Float.parseFloat(diameterText.getText().toString());
                            // Getting focal_length as float
                            float focal_length1 = Float.parseFloat(focalLengthText.getText().toString());
                            // Getting grating as int
                            int grating1 = Integer.parseInt(gratingText.getText().toString());
                            thread0.updateRonchigram(0.01 * (offsetBar.getProgress() - GlobalConstants.ronchigram_offset_offset),
                                    (diameter1 + GlobalConstants.ronchigram_diameter_offset),
                                    (focal_length1 + GlobalConstants.ronchigram_focal_length_offset),
                                    (grating1 + GlobalConstants.ronchigram_grating_offset)
                            );
                        }
                    } catch (NumberFormatException e) {
                        gratingText.setError("Invalid input"); // Set error for non-numeric input
                    }
                } else {
                    gratingText.setError(null); // Clear error when gratingText is empty
                }
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);
        return true;
    }


    public void toastMessage(String message){
        // Retrieve the Layout Inflater and inflate the layout from xml
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast_layout,
                (ViewGroup) findViewById(R.id.toast_layout_root));
        // get the reference of TextView and ImageVIew from inflated layout
        TextView toastTextView = (TextView) layout.findViewById(R.id.toastTextView);
        ImageView toastImageView = (ImageView) layout.findViewById(R.id.toastImageView);
        // set the text in the TextView
        toastTextView.setText(message);
        // set the Image in the ImageView
        toastImageView.setImageResource(R.drawable.ic_launcher);
        // create a new Toast using context
        Toast toast = new Toast(getApplicationContext());
        //toast.setDuration(Toast.LENGTH_LONG); // set the duration for the Toast
        toast.setView(layout); // set the inflated layout
        toast.show(); // display the custom Toast
    }

    public void userMessage(String title, String message){
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection

            if (item.getItemId() == R.id.About){
                userMessage("About","Author: \nPratik M Tambe (C) 2016-2023\nRelease Version: 0.8\n\nPurpose: Amateur Telescope Making - Primary mirror surface evaluation (Parabolic mirror -> [k=-1])\n\nCredits: \n\n(1) Math Equations Reference: brainwagon (Mark VandeWettering) - GitHub: https://github.com/brainwagon/ronchi\n\n(2) Amateur Telescope Making (ATM) Class: Guy Brandenburg, Alan Tarica \n\n(3) National Capital Astronomers (NCA), Washington D.C. Metro Area");
                return true;
            }
            else if (item.getItemId() == R.id.Disclaimer) {
                userMessage("Disclaimer","This App/software is supplied \"AS IS\" without any warranties and support.\nThe author assumes no responsibility or liability for the use of the App/software, conveys no license or title under any patent, or copyright.\nThe author reserves the right to make changes in the App/software without notification. The author also makes no representation or warranty that such application will be suitable for the specified use without further testing or modification.");
                return true;
            }
            else if (item.getItemId() == R.id.LoadConfig) {
                toastMessage("Loading saved preferences...");
                loadFromPreferences();
                return true;
            }
            else if (item.getItemId() == R.id.SaveConfig) {
                toastMessage("Saving user preferences...");
                new AlertDialog.Builder(this)
                        .setTitle("Are you sure you want to save settings?")
                        .setMessage("This will over-write any prior saved changes. Proceed?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            storeToPreferences();
                            dialog.dismiss();
                        })
                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                        .show();
                //StoreToPref();
                return true;
            }
            else if (item.getItemId() == R.id.Instructions) {
                this.userMessage("Instructions:","\n\n1. Load prior settings from the menu (if any). One may also save the current settings in the menu.\nThis is the preferred method for convenience. \n\n2. Set the attributes 'Diameter', 'Focal Length' and 'Grating'.\n\n3. Adjust the attribute 'Offset' to view the desired Ronchigram simulation. This is simulating turning the knob CW/CCW on the apparatus to move it either closer or farther from the initial point of reference\nand compare this to the one seen in the camera.");
                return true;
            }
            else {
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
    //double flen = 48.0 ;
    //Offset in inches
    //double   offset = 0.0;
    //Grating lines per unit - e.g. 100
    //double grating = 100.0 ;
    //For Parabola K = -1.0
    //################################ Example Paramaters #########################################
    //Diameter of the mirror in inches e.g. 6 inches
    double diameter = 0.25 * (0 + GlobalConstants.ronchigram_diameter_offset) ;
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
                if ( t <= 0.0 ){
                    t = 1;
                }
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
