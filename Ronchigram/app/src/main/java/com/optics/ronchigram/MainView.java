package com.optics.ronchigram;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

/**
 * Created by pratik on 4/16/2016.
 */
public class MainView extends GLSurfaceView {
    MainRenderer mRenderer;
    private Activity activity;

    /*
        MainView(Context context) {
            super ( context );
            mRenderer = new MainRenderer(this,context);
            setEGLContextClientVersion (2);
            setRenderer ( mRenderer );
            setRenderMode ( GLSurfaceView.RENDERMODE_WHEN_DIRTY );
        }
    */
    public MainView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRenderer = new MainRenderer(this, context);
        setEGLContextClientVersion(2);
        setRenderer(mRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        super.surfaceCreated(holder);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        mRenderer.close();
        super.surfaceDestroyed(holder);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        super.surfaceChanged(holder, format, w, h);
    }

    //Motion events function not used
    public void motionEvents(MotionEvent event) {
        mRenderer.onTouchEvent(event);
    }

    public void startPreview() {
        mRenderer.startPreview();
    }

    public void stopPreview() {
        mRenderer.stopPreview();
    }

    public void zoomIn() {
        mRenderer.zoomIn();
    }

    public void zoomOut() {
        mRenderer.zoomOut();
    }

    public void autoFocus() {
        mRenderer.autoFocus();
    }

    public void saveImage() {
        mRenderer.saveImage();
    }

    public int getZoomSetting(){
        return mRenderer.getZoomSetting();
    }

    public void setZoomSetting(int zoom){
        mRenderer.setZoomSetting(zoom);
    }
}