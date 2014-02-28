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
import android.content.Context;
import android.content.Intent;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ShareCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
	
	Boolean isLight;

    // Name of person giving out these coupons. When the user clicks on a coupon and shares
    // to another app, this name will be part of the pre-populated text.
    // TODO: Fill in your name here
    private static final String SENDER_NAME = "";
    static File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
              Environment.DIRECTORY_PICTURES), "LowVisCam");

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
        
        ActionBar actionBar = getActionBar();
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
        //gridView.setOnItemLongClickListener(this);
        //ImageButton shareButton = (ImageButton) findViewById(R.id.button_share);
        //shareButton.setOnClickListener(this);
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
    		

    		
			coupons.add(new Coupon(tag,
                    "Take a stroll in the flower garden", list[i].getName()));
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

        // Create share intent
        //Intent shareIntent = new Intent();
        Intent shareIntent = ShareCompat.IntentBuilder.from(this)
        		.setText(getShareText(coupon))
                .setType("image/jpeg")
                .setStream(coupon.mImageUri)
                .setChooserTitle(getString(R.string.share_using))
                .createChooserIntent();
        startActivity(shareIntent);
    }
    
    /*@Override
    public void onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO Auto-generated method stub
         Log.d("in onLongClick");
         String str=listView.getItemAtPosition(index).toString();

         Log.d("long click : " +str);
        return true;
    }*/

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
    private static class CouponAdapter extends BaseAdapter {

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
            viewCache.mImageView.setImageURI(coupon.mImageUri);

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
         * @param subtitleString is the description
         * @param imageAssetFilePath is the file path from the application's assets folder for
         *                           the image associated with this coupon
         * @throws IOException 
         */
        private Coupon(String titleString, String subtitleString, String imageAssetFilePath) throws IOException {

            mImageUri = Uri.parse(mediaStorageDir.getPath() + "/" +
                    imageAssetFilePath);
            ExifInterface exif = new ExifInterface(mImageUri.getPath());
            mTitle = exif.getAttribute("UserComment");
        }
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
	    return super.onCreateOptionsMenu(menu);
	}
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_search:
                //openSearch();
                return true;
            case R.id.action_about:
                
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}