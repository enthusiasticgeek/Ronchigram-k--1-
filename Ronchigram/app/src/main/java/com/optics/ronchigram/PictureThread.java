/**
 * Created by Pratik Tambe on 3/30/2016.
 * Author: Pratik M Tambe (c) 2016
 */
package com.optics.ronchigram;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

public class PictureThread extends Thread{
    private ImageView ronchiImageView;
    private TextView renderingStatusView;
    private Bitmap bitmap;
    private Canvas canvas;
    private Paint paint;
    private Handler handler;
    private boolean running = false;
    private int bitmap_height;
    private int bitmap_width;

    private static final String TAG = "PictureThread";

    public PictureThread(ImageView imageView, Bitmap bitmap, TextView renderingStatusView) {
        this.ronchiImageView = imageView;
        this.bitmap = bitmap;
        this.renderingStatusView = renderingStatusView;
        this.bitmap_width = bitmap.getWidth();
        this.bitmap_height = bitmap.getHeight();
        this.canvas = new Canvas(this.bitmap);
        this.paint = new Paint();
        this.paint.setDither(true);
        handler = new Handler();
    }

    public void updateRonchigram (double offset, double diameter, double focal_length, double grating){
        canvas.drawColor(GlobalConstants.bitmap_background_color);
        Ronchigram(GlobalConstants.ronchigram_width, GlobalConstants.ronchigram_height, canvas, paint, offset, diameter, focal_length, grating);
        running = true;
    }

    @Override
    public void run() {

        //Looper.prepare();
        //handler = new Handler(Looper.getMainLooper());
        //Looper.loop();
        while(true){
            if(running){
                //canvas.drawBitmap(bitmap, 0, 0, paint);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        ronchiImageView.setImageBitmap(bitmap);
                        running = false;
                        renderingStatusView.setText("");
                        //ronchiImageView.invalidate();
                        //Log.v(TAG, "");
                    }
                });
                //Log.v(TAG, "Rendering Image...");
            }
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

    public void Ronchigram(int width, int height, Canvas canvas, Paint paint, double offset, double diameter, double flen, double grating) {

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
                    //paint.setStyle(Paint.Style.FILL);

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
                if (t <= 0) {
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
