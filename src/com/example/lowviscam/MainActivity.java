package com.example.lowviscam;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.R.id;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.PictureCallback;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends Activity {

    protected static final String TAG = "DEBUG";
    public static final int MEDIA_TYPE_IMAGE = 1;
	private Camera mCamera;
    private CameraPreview mPreview;
    public static int cam = 1;
    
    //Initialized SoundPool Variables
    float leftVolume;
	float rightVolume;
	float rate = 1.0f;
	int loop;
	SoundPool soundPool;
	int soundID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create an instance of Camera
        mCamera = getCameraInstance();
        mCamera.setDisplayOrientation(90);
        
        // Create Face Detection Listener
        mCamera.setFaceDetectionListener(new MyFaceDetectionListener(){
        		@Override
        		public void onFaceDetection(Face[] faces, Camera camera) {
        			
        			// Initialize beep sound
        			soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        			soundID = soundPool.load(getApplicationContext(), R.raw.beep, 1);

        	        if (faces.length > 0){
        	            /*Log.d("FaceDetection", "face detected: "+ faces.length +
        	                    " Face 1 Location X: " + faces[0].rect.centerX() +
        	                    " Y: " + faces[0].rect.centerY() );*/
        	            // Toast says face detected
        	            showToast(faces.length);
        	            
        	            // Distance from Center Calculations
        	            /* Camera Preview cut into 5 x 5 grid, farther left or right produces more beeps,
        	             * higher produces a higher tone beep, lower produces a lower tone beep. Being 
        	             * perfectly produces a ding meaning correct.
        	             */
        	            int yCenter = faces[0].rect.centerY();
        	            int xCenter = faces[0].rect.centerX();
        	            int totalY = 0;
        	            int totalX = 0;
        	            // More than one face
        	            /* xCenter and yCenter will equal the average of all facial 
        	             * coordinates.
        	             */
        	            if (faces.length > 1){
        	            	for (int i = 0; i < faces.length; i++){
        	            		totalY = totalY + faces[0].rect.centerY();
        	            		totalX = totalX + faces[0].rect.centerX();
        	            	}
        	            	yCenter = totalY/faces.length;
        	            	xCenter = totalX/faces.length;
        	            }
        	            
        	            // Horizontal Position
        	            if ( yCenter >= -100 && yCenter <= 100 && xCenter >= -100 && xCenter <= 100 ){ // Perfectly centered
        	            	leftVolume = 1.0f;
            				rightVolume = 1.0f;
            				loop = 0;
            				soundID = soundPool.load(getApplicationContext(), R.raw.ding, 1);
        	            } else if ( yCenter <= 100 && yCenter >= -100 ){
        	            	leftVolume = 1.0f;
            				rightVolume = 1.0f;
            				loop = 0;
        	            } else if ( yCenter > 100 && yCenter <= 300 ) {
            				leftVolume = 1.0f;
            				rightVolume = 0.0f;
            				loop = 0;
        	            } else if ( yCenter > 300 && yCenter <= 500 ){
        	            	leftVolume = 1.0f;
            				rightVolume = 0.0f;
            				loop = 2;
        	            } else if ( yCenter > 500 ){
        					leftVolume = 1.0f;
            				rightVolume = 0.0f;
            				loop = 3;
        				} else if (  yCenter <= 100 && yCenter >= -300 ){
        					leftVolume = 0.0f;
            				rightVolume = 1.0f;
            				loop = 0;
        				} else if ( yCenter < -300 && yCenter >= -500 ){
        					leftVolume = 0.0f;
            				rightVolume = 1.0f;
            				loop = 2;
        				} else if ( yCenter < -500 ){
        					leftVolume = 0.0f;
            				rightVolume = 1.0f;
            				loop = 3;
        				}
        	            
        	            // Vertical Position
        	            if ( xCenter < -500 ) {
        	            	rate = 0.5f;
        	            } else if ( xCenter < -300 && xCenter >= -500 ){
        	            	rate = 0.75f;
        	            } else if ( xCenter < -100 && xCenter >= -300 ){
        	            	rate = 1.0f;
        				} else if ( xCenter >= -100 && xCenter <= 100 ){
        					rate = 1.25f;
        				} else if (  xCenter > 100 && xCenter <= 300 ){
        					rate = 1.5f;
        				} else if (  xCenter > 300 && xCenter <= 500 ){
        					rate = 1.75f;
        				} else if ( xCenter > 500 ){
        	            	rate = 2.0f;
        				} 
        					
        	            // Wait for sound to load
        	            soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
        	                public void onLoadComplete(SoundPool soundPool, int sampleId,
        	                    int status) {
        	                	soundPool.play(soundID, leftVolume, rightVolume, 1, loop, rate);
        	                } 
        	            });
        	        }
        		}
        });
    
        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        
        // Add a listener to the Capture button
        ImageButton captureButton = (ImageButton) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // get an image from the camera
                    mCamera.takePicture(null, null, mPicture);
                    
                    // wait so picture saves
                    // need to revise to wait for the callbacks to provide the actual image data
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            try {
                                synchronized (this) {
                                    wait(3000);
                                }
                            } catch (InterruptedException ex) {
                        }

                        // TODO
                        //TO RUN AFTER 3 SECONDS
                            mCamera.startPreview();
                        }
                        };

                        thread.start();
                }
            }
        );
        
        
        // Add a listener to the Reverse Camera button
        ImageButton reverseButton = (ImageButton) findViewById(R.id.button_reverse_cam);
        reverseButton.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // restart activity with front facing camera
                	if ( cam == 1){
                		cam = 2;
                	} else {
                		cam = 1;
                	}
                	recreate();
                }
            }
        );
        // Disable Reverse Camera Button if only 1 camera available
        if (Camera.getNumberOfCameras() == 1){
        	reverseButton.setVisibility(View.INVISIBLE);
        }
    }
    
    private PictureCallback mPicture = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Log.d(TAG, "Error creating media file, check storage permissions.");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };
    
	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance(){
	    Camera c = null;
	    try {
			if (cam == 1){
	    		c = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK); // attempt to get a Camera instance
	       	} else {
	       		c = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
	       	}
	    }
	    catch (Exception e){
	        // Camera is not available (in use or does not exist)
	    }
	    return c; // returns null if camera is unavailable
	}
	
	/** Create a file Uri for saving an image or video */
	private static Uri getOutputMediaFileUri(int type){
	      return Uri.fromFile(getOutputMediaFile(type));
	}

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type){
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.

	    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), "LowVisCam");
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.d("LowVisCam", "failed to create directory");
	            return null;
	        }
	    }

	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File mediaFile;
	    if (type == MEDIA_TYPE_IMAGE){
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "IMG_"+ timeStamp + ".jpg");
	    } else {
	        return null;
	    }
	    
	    return mediaFile;
	}
	
	@Override
    protected void onPause() {
        super.onPause();
        releaseCamera();              // release the camera immediately on pause event
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

        // Get the Camera instance as the activity achieves full user focus
        if (mCamera == null) {
        	onCreate(null); // Not correct way to resume activity. Find new way.
        }
    }
    
    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    return super.onCreateOptionsMenu(menu);
	}
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_gallery:
            	/*Intent myIntent = new Intent(CurrentActivity.this, NextActivity.class);
            	myIntent.putExtra("key", value); //Optional parameters
            	CurrentActivity.this.startActivity(myIntent);*/
                return true;
            case R.id.action_settings:
                
                return true;
            case R.id.action_about:
            	
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    public void showToast(int numFaces){
        Context context = getApplicationContext();
        CharSequence text = null;
        if (numFaces == 1){
        	text = "1 Face Detected";
        }
        if (numFaces > 1){
        	char charNumFaces = (char) ('0' + numFaces);
        	text = charNumFaces + " Faces Detected";
        }
        int duration = Toast.LENGTH_SHORT;
        final Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

}

/* LEGAL ATTRIBUTION FOR ICONS
 * App logo and Capture Icon made by Daniel Bruce from Flaticon.com
 * Reverse Camera Icon made by SimpleIcon from Flaticon.com
 */
