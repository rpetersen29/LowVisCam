package com.example.lowviscam;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    protected static final String TAG = "DEBUG";
    public static final int MEDIA_TYPE_IMAGE = 1;
	private Camera mCamera;
    private CameraPreview mPreview;
    public static int cam = 1;
    public static boolean isLight = true;
    public static int orientation = 0;
    public static Typeface  mFace;    
    
    //Initialized SoundPool Variables
    float leftVolume;
	float rightVolume;
	float rate = 1.0f;
	int loop;
	SoundPool soundPool;
	int soundID;
	
	// Tag global variable
	String tagText;
	
	//Picture location global
	File pictureFile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);	

        if (isLight == false){
        	setTheme(R.style.AppBaseThemeDark);
        	setContentView(R.layout.activity_main_dark);
        } else {
        	setTheme(R.style.AppBaseTheme);
        	setContentView(R.layout.activity_main);
        }
        
        // Retrieve APHont font and apply it
        mFace = Typeface.createFromAsset(getBaseContext().getAssets(),"fonts/APHont-Bold_q15c.otf");
        //textView.setTypeface(mFace);
        SpannableString s = new SpannableString("LowVisCam");
        s.setSpan(new TypefaceSpan(this, "APHont-Bold_q15c.otf"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
     
        // Update the action bar title with the TypefaceSpan instance
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(s);
        
        // Create an instance of Camera
        mCamera = getCameraInstance();
        
        mCamera.setDisplayOrientation(90);
        showCamToast(cam);
        
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
        	        	showFaceToast(faces.length);
        	            
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
        	            		totalY = totalY + faces[i].rect.centerY();
        	            		totalX = totalX + faces[i].rect.centerX();
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
        
        //
        
        
        // Add a listener to the Capture button
        ImageButton captureButton = (ImageButton) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                	
                	// get orientation
                	if (cam == 1){
                		orientation = getCameraOrientation(MainActivity.this, Camera.CameraInfo.CAMERA_FACING_BACK, mCamera);
        	       	} else {
        	       		orientation = getCameraOrientation(MainActivity.this, Camera.CameraInfo.CAMERA_FACING_FRONT, mCamera);
        	       	}
                	
                    // get an image from the camera
                    mCamera.takePicture(shutterCallback, null, mPicture);
                    
                    final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    
                    // Set dialog properties
                    final EditText tag = new EditText(MainActivity.this);

                    
                    builder.setMessage("Enter the image tag below")		//R.string.dialog_message
                    	.setTitle("Set Image Tag")
                    	.setView(tag);
                    
                    
                    // Add the buttons
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                               public void onClick(DialogInterface dialog, int id) {
                                   	// User clicked OK button
                            	   	tagText = tag.getText().toString();
                            	   	//Log.d(TAG, tagText);
                            	   	// Add image tag;
									try {
										ExifInterface exif = new ExifInterface(pictureFile.getPath());
										if (tagText.matches("")){
											tagText = "No Tag Added";
										}
										exif.setAttribute("UserComment", tagText);
										exif.saveAttributes();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
                                    
                               		dialog.cancel();
                               		Toast.makeText(MainActivity.this, "Picture Saved.", Toast.LENGTH_SHORT).show();
                               		mCamera.startPreview();
                               }
                           });
                    /*builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                               public void onClick(DialogInterface dialog, int id) {
                                   	// User cancelled the dialog
                               		dialog.cancel();
                               		// TODO: enter something to delete photo
                               		
                               		mCamera.startPreview();
                               }
                           });*/
                    
                    AlertDialog dialog = builder.create();
                    
                    // wait so picture shows
                    // need to revise to wait for the callbacks to provide the actual image data
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            try {
                                synchronized (this) {
                                    wait(1500);
                                }
                            } catch (InterruptedException ex) {
                        }

                        // TODO
                        //TO RUN AFTER 1.5 SECONDS

                        }
                        };

                    //thread.start();
                    dialog.show();
                    //TODO: Bring up keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(tag, InputMethodManager.SHOW_FORCED);

                   
                }
            }
        );
        
        
        // Add a listener to the Reverse Camera button
        ImageButton reverseButton = (ImageButton) findViewById(R.id.button_reverse_cam);
        reverseButton.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // restart activity with opposite facing camera
                	if ( cam == 1){
                		cam = 2;
                	} else {
                		cam = 1;
                	}
                	recreate();
                	showCamToast(cam);
                }
            }
        );
        // Disable Reverse Camera Button if only 1 camera available
        if (Camera.getNumberOfCameras() == 1){
        	reverseButton.setVisibility(View.INVISIBLE);
        }
    }
    
    
    // Plays camera snap sound
    private final ShutterCallback shutterCallback = new ShutterCallback() {
        public void onShutter() {
            AudioManager mgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            mgr.playSoundEffect(AudioManager.FLAG_PLAY_SOUND);
        }
    };
    
    
    // Saves picture
    private PictureCallback mPicture = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            Uri pictureFileURI = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Log.d(TAG, "Error creating media file, check storage permissions.");
                return;
            }

            try {
            	// TODO: change image size
            	/*BitmapFactory.Options bounds = new BitmapFactory.Options();
                bounds.inJustDecodeBounds = true;

                Bitmap bm = BitmapFactory.decodeFile(pictureFile.getPath(), bounds);
            	Bitmap original = BitmapFactory.decodeByteArray(data , 0, data.length);
                Bitmap resized = Bitmap.createScaledBitmap(original, bm.getWidth(), bm.getHeight(), false);
                     
                ByteArrayOutputStream blob = new ByteArrayOutputStream();
                resized.compress(Bitmap.CompressFormat.JPEG, 100, blob);
             
                data =  blob.toByteArray();*/
            	
            	//Creates Portrait rotated image
            	Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
            			data.length);

            	        int width = bitmap.getWidth();
            	        int height = bitmap.getHeight();

            	        FileOutputStream fos = new FileOutputStream(pictureFile);
            	        
            	        Matrix matrix = new Matrix();
            	        matrix.postRotate(orientation);
            	        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width,
            	            height, matrix, false);

            	        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos); //Possibly change quality for sharing
                
            	// write data to disk
                fos.write(data);
                fos.close();
                
                //rotate image
                /*BitmapFactory.Options bounds = new BitmapFactory.Options();
                bounds.inJustDecodeBounds = true;

                Bitmap bm = BitmapFactory.decodeFile(pictureFileURI.getPath(), bounds);
                width = bounds.outWidth;
                height = bounds.outHeight;
                ExifInterface exif = new ExifInterface(pictureFileURI.getPath());
                String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
                int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
                int rotationAngle = 0;
                if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
                if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
                if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
				
                Matrix matrix2 = new Matrix();
                matrix2.setRotate(rotationAngle);
                Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix2, false);
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);*/
                
                //Output orientation info to log
                //Log.d("ImageRotation", orientString);
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };
    
    
    
    public static int getCameraOrientation(Activity activity,
            int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        // flips front facing: if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
        // TODO: still need to rotate images
        if (cam == 6) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }
    
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
	    if (isLight == true){
	    	inflater.inflate(R.menu.main, menu);
        } else {
        	inflater.inflate(R.menu.main_dark, menu);
        }
	    return super.onCreateOptionsMenu(menu);
	}
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_gallery:
            	Intent myIntent = new Intent(MainActivity.this, GalleryActivity.class);
            	//myIntent.putExtra("key", value); //Optional parameters
            	myIntent.putExtra("isLight", isLight);
            	MainActivity.this.startActivity(myIntent);
                return true;
            case R.id.action_switch_contrast:
                if (isLight == true){
                	isLight = false;
                	recreate();
                } else {
                	isLight = true;
                    recreate();
                }
                return true;
            case R.id.action_about:
            	// 1. Instantiate an AlertDialog.Builder with its constructor
            	AlertDialog.Builder builder = new AlertDialog.Builder(this);

            	// 2. Chain together various setter methods to set the dialog characteristics
            	String aboutString = getResources().getString(R.string.dialog_message);
            	SpannableString s = new SpannableString(aboutString);
                s.setSpan(new TypefaceSpan(this, "APHont-Regular_q15c.otf"), 0, s.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                
            	builder.setMessage(s)
            	       .setTitle(R.string.dialog_title)
            	       .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            	    	   public void onClick(DialogInterface dialog, int id) {
            	    		   // User clicked OK button
            	    	   }
            	       });;

            	// 3. Get the AlertDialog from create()
            	AlertDialog dialog = builder.create();
            	dialog.show();
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    public void showFaceToast(int numFaces){
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
    
    public void showCamToast(int cam){
        Context context = getApplicationContext();
        CharSequence text = null;
        if (cam == 1){
        	text = "Back Facing Camera Selected";
        } else {
        	text = "Front Facing Camera Selected";
        }
        //text.setTypeface(mFace);
        int duration = Toast.LENGTH_SHORT;
        final Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

}

/* LEGAL ATTRIBUTION FOR ICONS
 * App logo and Capture Icon made by Daniel Bruce from Flaticon.com
 * Reverse Camera Icon made by SimpleIcon from Flaticon.com
 */
