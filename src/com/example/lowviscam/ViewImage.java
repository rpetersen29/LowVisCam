package com.example.lowviscam;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ViewImage extends Activity implements OnItemClickListener {
	
	ImageView mImageView;
	TextView mTitleView;
	int position;
	File list[] = mediaStorageDir.listFiles();
	
	static File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES), "LowVisCam");
	List<String> imageTags = new LinkedList<String>();
	List<String> imageURIs = new LinkedList<String>();
	
	 @Override
	    protected void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);

	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
	                                WindowManager.LayoutParams.FLAG_FULLSCREEN);

	        setContentView(R.layout.view_image);
	        
	        //create image lists for position information
	        try {
				createLists();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	        
	        //Get APHont
	        Typeface mFace = Typeface.createFromAsset(getAssets(),"fonts/APHont-Regular_q15c.otf");
	        
	        mImageView = (ImageView) findViewById(R.id.image);
	        
	        // Get selected image from Gallery
	        String ImageUri = getIntent().getExtras().getString("mImageUri");
	        
	        //find position in lists
	        for (int i = 0; i < list.length; i++){
	        	if (imageURIs.get(i).equals(ImageUri)){
	        		position = i;
	        		
	        		//String is = Integer.toString(i);
	        		//Log.d("Position", is );
	        	}
	        }
	        
	        Uri mImageUri = Uri.parse(ImageUri);
	        Bitmap bm = decodeSampledBitmapFromUri(mImageUri.toString(), 400, 800);
            mImageView.setImageBitmap(bm);
	        
	        ExifInterface exif = null;
			try {
				exif = new ExifInterface(mImageUri.getPath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
            String mTitle = exif.getAttribute("UserComment");
	        mTitleView = (TextView) findViewById(R.id.title);
	        mTitleView.setText(mTitle);
	        mTitleView.setTypeface(mFace);
	        //Set button label as tag
	        mImageView.setContentDescription(mTitle);
	        
	        mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                	//Toast.makeText(ViewImage.this, "touch", Toast.LENGTH_SHORT).show();
                }
	        });
	        //Swipe controls
	        mImageView.setOnTouchListener(new OnSwipeTouchListener() {

	            public void onSwipeTop() {
	                //Toast.makeText(ViewImage.this, "top", Toast.LENGTH_SHORT).show();
	            }
	            public void onSwipeRight() {
	            	if (position == 0){
	            		SpannableString s = new SpannableString("Beginning of list.");
	    	            s.setSpan(new TypefaceSpan(ViewImage.this, "APHont-Regular_q15c.otf"), 0, s.length(),
	    	                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	            		Toast.makeText(ViewImage.this, s, Toast.LENGTH_SHORT).show();
	            	} else {
	            		mImageView = (ImageView) findViewById(R.id.image);
	            		Bitmap bm = decodeSampledBitmapFromUri(imageURIs.get(position-1), 400, 800);
	            		mImageView.setImageBitmap(bm);
		            
	            		mTitleView = (TextView) findViewById(R.id.title);
	            		String mTitle = imageTags.get(position-1);
	            		mTitleView.setText(mTitle);
	            		mImageView.setContentDescription(mTitle);
	            		position--;
	            	}
	            }
	            public void onSwipeLeft() {
	            	if (position == list.length-1){
	            		SpannableString s = new SpannableString("End of list.");
	    	            s.setSpan(new TypefaceSpan(ViewImage.this, "APHont-Regular_q15c.otf"), 0, s.length(),
	    	                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	            		Toast.makeText(ViewImage.this, s, Toast.LENGTH_SHORT).show();
	            	} else {
	            		mImageView = (ImageView) findViewById(R.id.image);
	            		Bitmap bm = decodeSampledBitmapFromUri(imageURIs.get(position+1), 400, 800);
	            		mImageView.setImageBitmap(bm);
		            
	            		mTitleView = (TextView) findViewById(R.id.title);
	            		String mTitle = imageTags.get(position+1);
	            		mTitleView.setText(mTitle);
	            		mImageView.setContentDescription(mTitle);
	            		position++;
	            	}
	            }
	            public void onSwipeBottom() {
	                //Toast.makeText(ViewImage.this, "bottom", Toast.LENGTH_SHORT).show();
	            }

	        });

   }
	 
	 public Bitmap decodeSampledBitmapFromUri(String path, int reqWidth, int reqHeight) {

	        Bitmap bm = null;
	        // First decode with inJustDecodeBounds=true to check dimensions
	        final BitmapFactory.Options options = new BitmapFactory.Options();
	        options.inJustDecodeBounds = true;
	        BitmapFactory.decodeFile(path, options);

	        // Calculate inSampleSize
	        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	        // Decode bitmap with inSampleSize set
	        options.inJustDecodeBounds = false;
	        bm = BitmapFactory.decodeFile(path, options); 

	        return bm;      
	    }

	    public int calculateInSampleSize(

	        BitmapFactory.Options options, int reqWidth, int reqHeight) {
	        // Raw height and width of image
	        final int height = options.outHeight;
	        final int width = options.outWidth;
	        int inSampleSize = 1;

	        if (height > reqHeight || width > reqWidth) {
	            if (width > height) {
	                inSampleSize = Math.round((float)height / (float)reqHeight);    
	            } else {
	                inSampleSize = Math.round((float)width / (float)reqWidth);      
	            }   
	        }

	        return inSampleSize;    
	    }
	    
	    public void createLists() throws IOException {

	    	for( int i=list.length-1; i> -1; i--){
	    		
	    		//Get text tag
	    		String tag = "NA";
	    		try {
					ExifInterface exif = new ExifInterface(list[i].getPath());
					tag = exif.getAttribute("UserComment");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				imageTags.add(tag);
				imageURIs.add(mediaStorageDir.getPath() + "/" + list[i].getName());
	        }

	    }

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			
		}
 }