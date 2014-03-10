/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.lowviscam;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ShareCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Activity to display a grid of coupons. The number of columns varies based on screen width,
 * and goes down to a one-column grid on a small device such as a phone.
 *
 * <p>A coupon consists of a photo, title, and subtitle.
 *
 * <p>Tapping on a coupon to redeem it brings up the Android "share"
 * dialog with a pre-populated message based on the coupon text.
 */
public class GalleryActivity extends Activity implements OnItemClickListener {
	
	public Boolean isLight;
	public static Typeface mFace;
	public int numSearchResults;
	public String firstResult;

    // Name of person giving out these coupons. When the user clicks on a coupon and shares
    // to another app, this name will be part of the pre-populated text.
    // TODO: Fill in your name here
    private static final String SENDER_NAME = "";
    static File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
              Environment.DIRECTORY_PICTURES), "LowVisCam");
    File listTemp[] = mediaStorageDir.listFiles();
    String[] tagList = new String[listTemp.length];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isLight = getIntent().getExtras().getBoolean("isLight");
        if (isLight == false){
        	this.setTheme(R.style.AppBaseThemeDark);
        } else {
        	this.setTheme(R.style.AppBaseTheme);
        }
        setContentView(R.layout.activity_gallery);
        
        // Retrieve APHont font and apply it
        mFace = Typeface.createFromAsset(getAssets(),"fonts/APHont-Regular_q15c.otf");
        SpannableString s = new SpannableString("Image Gallery");
        s.setSpan(new TypefaceSpan(this, "APHont-Bold_q15c.otf"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
     
        // Update the action bar title with the TypefaceSpan instance
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(s);

        actionBar.setDisplayHomeAsUpEnabled(true);

        // Fetch the {@link LayoutInflater} service so that new views can be created
        LayoutInflater inflater = (LayoutInflater) getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        // Find the {@link GridView} that was already defined in the XML layout
        GridView gridView = (GridView) findViewById(R.id.grid);

        // Initialize the adapter with all the coupons. Set the adapter on the {@link GridView}.
        try {
			gridView.setAdapter(new CouponAdapter(inflater, createAllCoupons()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        

        // Set a click listener for each picture in the grid
        gridView.setOnItemClickListener(this);
        gridView.setOnTouchListener(new OnSwipeTouchListener() {

            public void onSwipeTop() {
                //Toast.makeText(GalleryActivity.this, "top", Toast.LENGTH_SHORT).show();
            }
            public void onSwipeRight() {
                //Toast.makeText(GalleryActivity.this, "right", Toast.LENGTH_SHORT).show();
            }
            public void onSwipeLeft() {
                //Toast.makeText(GalleryActivity.this, "left", Toast.LENGTH_SHORT).show();
            }
            public void onSwipeBottom() {
                //Toast.makeText(GalleryActivity.this, "bottom", Toast.LENGTH_SHORT).show();
            }

        });
        gridView.setOnItemLongClickListener(new OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            	// Find coupon that was clicked based off of position in adapter
                Coupon coupon = (Coupon) parent.getItemAtPosition(position);
                //Get absolutepath of image for adding.
                File list[] = mediaStorageDir.listFiles();
                File tmpFile = list[list.length-position-1];
                String photoUri = coupon.mImageUri.toString();
				try {
					photoUri = MediaStore.Images.Media.insertImage(
					        getContentResolver(), tmpFile.getAbsolutePath(), null, null);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                // Create share intent
                Intent shareIntent = ShareCompat.IntentBuilder.from(GalleryActivity.this)
                		.setText(coupon.mTitle)
                        .setType("image/jpeg")
                        .setStream(Uri.parse(photoUri))
                        .setChooserTitle(getString(R.string.share_using))
                        .createChooserIntent();
                startActivity(shareIntent);

                return true;
            }
        }); 

    }

    /**
     * Generate the list of all coupons.
     * @return The list of coupons.
     * @throws IOException 
     */
    private List<Coupon> createAllCoupons() throws IOException {
        // TODO: Customize this list of coupons for your personal use.
    	List<Coupon> coupons = new ArrayList<Coupon>();
    	File list[] = mediaStorageDir.listFiles();
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
    		

    		
			coupons.add(new Coupon(tag, list[i].getName()));
        }
    	
    	
        // You can add a title, subtitle, and a photo (in the assets directory).
        
        return coupons;
    }

    private List<Coupon> createSearchedCoupons(String query) throws IOException {
        // TODO: Customize this list of coupons for your personal use.
    	List<Coupon> coupons = new ArrayList<Coupon>();
    	
    	numSearchResults = 0;
    	
    	File list[] = mediaStorageDir.listFiles();
    	
    	int j=0;
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
    		

    		if (tag.toLowerCase().contains(query.toLowerCase())){
    			coupons.add(new Coupon(tag, list[i].getName()));
    			tagList[j] = tag;
    			numSearchResults++;
    			j++;
    		}
        }
    	
    	
        // You can add a title, subtitle, and a photo (in the assets directory).
        
        return coupons;
    }
    /**
     * Callback method for a when a coupon is clicked. A new share intent is created with the
     * coupon title. Then the user can select which app to share the content of the coupon with.
     *
     * @param parent The AdapterView where the click happened.
     * @param view The view within the AdapterView that was clicked (this
     *            will be a view provided by the adapter).
     * @param position The position of the view in the adapter.
     * @param id The row id of the item that was clicked.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    	// Find coupon that was clicked based off of position in adapter
        Coupon coupon = (Coupon) parent.getItemAtPosition(position);
    	
    	Intent intent = new Intent(this, ViewImage.class);
    	/*myIntent.putExtra("key", value);*/
        intent.putExtra("mImageUri", coupon.mImageUri.toString());
    	GalleryActivity.this.startActivity(intent);
    }
    
    /**
     * Create the share intent text based on the coupon title, subtitle, and whether or not
     * there is a {@link #SENDER_NAME}.
     *
     * @param coupon to create the intent text for.
     * @return string to be used in the share intent.
     */
    private String getShareText(Coupon coupon) {
        // Return image tag
    	return coupon.mTitle;
    }

    /**
     * Adapter for grid of coupons.
     */
    private class CouponAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private List<Coupon> mAllCoupons;

        /**
         * Constructs a new {@link CouponAdapter}.
         *
         * @param inflater to create new views
         * @param allCoupons for list of all coupons to be displayed
         */
        public CouponAdapter(LayoutInflater inflater, List<Coupon> allCoupons) {
            if (allCoupons == null) {
                throw new IllegalStateException("Can't have null list of coupons");
            }
            mAllCoupons = allCoupons;
            mInflater = inflater;
        }

        @Override
        public int getCount() {
            return mAllCoupons.size();
        }

        @Override
        public Coupon getItem(int position) {
            return mAllCoupons.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View result = convertView;
            if (result == null) {
                result = mInflater.inflate(R.layout.grid_item, parent, false);
            }

            // Try to get view cache or create a new one if needed
            ViewCache viewCache = (ViewCache) result.getTag();
            if (viewCache == null) {
                viewCache = new ViewCache(result);
                result.setTag(viewCache);
            }

            // Fetch item
            Coupon coupon = getItem(position);

            // Bind the data
            viewCache.mTitleView.setText(coupon.mTitle);
            //viewCache.mImageView.setImageURI(coupon.mImageUri);
            Bitmap bm = decodeSampledBitmapFromUri(coupon.mImageUri.toString(), 200, 200);
            viewCache.mImageView.setImageBitmap(bm);

            return result;
        }
    }

    /**
     * Cache of views in the grid item view to make recycling of views quicker. This avoids
     * additional {@link View#findViewById(int)} calls after the {@link ViewCache} is first
     * created for a view. See
     * {@link CouponAdapter#getView(int position, View convertView, ViewGroup parent)}.
     */
    private static class ViewCache {

        /** View that displays the title of the coupon */
        private final TextView mTitleView;

        /** View that displays the image associated with the coupon */
        private final ImageView mImageView;

        /**
         * Constructs a new {@link ViewCache}.
         *
         * @param view which contains children views that should be cached.
         */
        private ViewCache(View view) {
            mTitleView = (TextView) view.findViewById(R.id.title);
            mImageView = (ImageView) view.findViewById(R.id.image);
            
            mTitleView.setTypeface(mFace);
        }
    }

    /**
     * Model object for coupon.
     */
    private static class Coupon {

        /** Title of the coupon. */
        private final String mTitle;

        /** Content URI of the image for the coupon. */
        private final Uri mImageUri;

        /**
         * Constructs a new {@link Coupon}.
         *
         * @param titleString is the title
         * @param imageAssetFilePath is the file path from the application's assets folder for
         *                           the image associated with this coupon
         * @throws IOException 
         */
        private Coupon(String titleString, String imageAssetFilePath) throws IOException {

            mImageUri = Uri.parse(mediaStorageDir.getPath() + "/" +
                    imageAssetFilePath);
            ExifInterface exif = new ExifInterface(mImageUri.getPath());
            mTitle = exif.getAttribute("UserComment");
        }
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
    
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    if (isLight == true){
	    	inflater.inflate(R.menu.gallery, menu);
        } else {
        	inflater.inflate(R.menu.gallery_dark, menu);
        }

	    // Get the SearchView and set the searchable configuration
	    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	    SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
	    // Assumes current activity is the searchable activity
	    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
	    searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
	    searchView.setSubmitButtonEnabled(true);
	    searchView.setOnQueryTextListener( new OnQueryTextListener() {
	        @Override
	        public boolean onQueryTextSubmit(String query) {
	        	search(query);
	        	// First image toast
	        	String firstResult;
	        	if (numSearchResults == 0){
	        		firstResult = "No results found for  " +query+".";
	        	} else{
	        		firstResult = "The first image result is titled " +tagList[0];
	        	}
	        	Toast.makeText(GalleryActivity.this, firstResult, Toast.LENGTH_SHORT).show();
	            return true;
	        }

	        @Override
	        public boolean onQueryTextChange(String newText) {
	            if ( TextUtils.isEmpty(newText)) {
	                search("");
	            }

	            return true;
	        }

	        public void search(String query) {
	        	// hide keyboard
	            InputMethodManager inputManager = (InputMethodManager) GalleryActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
	            inputManager.hideSoftInputFromWindow(GalleryActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	        	
	        	// Fetch the {@link LayoutInflater} service so that new views can be created
	            LayoutInflater inflater = (LayoutInflater) getSystemService(
	                    Context.LAYOUT_INFLATER_SERVICE);

	            // Find the {@link GridView} that was already defined in the XML layout
	            GridView gridView = (GridView) findViewById(R.id.grid);
	        	try {
	    			gridView.setAdapter(new CouponAdapter(inflater, createSearchedCoupons(query)));
	    		} catch (IOException e) {
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		}
	        	
	        	
	        }

	    });

	    return true;
	    //return super.onCreateOptionsMenu(menu);
	}
    
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_search:

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

}