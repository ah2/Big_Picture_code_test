package com.example.bigpicture;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.card.MaterialCardView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import kotlin.jvm.internal.Intrinsics;

public final class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
   private GoogleMap mMap;
   private LinearLayout images_view;
   ProgressDialog pd;
   LatLng midLatLng;
   boolean bottomshown;

   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      this.setContentView(R.layout.activity_maps);
      images_view = findViewById(R.id.images);
      bottomshown = false;
      final HorizontalScrollView images_bar = findViewById(R.id.scroll_View);

      if (TextUtils.isEmpty(getResources().getString(R.string.google_maps_api_key))) {
         throw new IllegalStateException("You forgot to supply a Google Maps API key");
      }

      Fragment gMapFr = this.getSupportFragmentManager().findFragmentById(R.id.map);
      if (gMapFr == null) {
         throw new NullPointerException("null cannot be cast to non-null type com.google.android.gms.maps.SupportMapFragment");
      } else {
         SupportMapFragment mapFragment = (SupportMapFragment)gMapFr;
         mapFragment.getMapAsync(this);

         View gallaryButtonView = this.findViewById(R.id.gallaryButton);
         Intrinsics.checkNotNullExpressionValue(gallaryButtonView, "findViewById(R.id.gallaryButton)");
         Button gButton = (Button)gallaryButtonView;
         gButton.setOnClickListener((new OnClickListener() {
            public final void onClick(View it) {
               Intent intent = new Intent(MapsActivity.this, gallary.class);
               MapsActivity.this.startActivity(intent);
            }
         }));

          final Button hideButton = new Button(this);
          hideButton.setText("hide");
          hideButton.setOnClickListener((new OnClickListener() {
              public final void onClick(View it) {
                  images_bar.animate().translationY(images_bar.getHeight());
                  hideButton.setVisibility(View.GONE);
                  bottomshown = false;
              }
          }));
          hideButton.setVisibility(View.GONE);
          ((LinearLayout)this.findViewById(R.id.buttons_bar)).addView(hideButton);

          final Button tabButton = new Button(this);
          tabButton.setText("open tabs");
          tabButton.setOnClickListener((new OnClickListener() {
              public final void onClick(View it) {

              }
          }));
          ((LinearLayout)this.findViewById(R.id.buttons_bar)).addView(tabButton);


          View searchButtonView = this.findViewById(R.id.searchButton);
          Intrinsics.checkNotNullExpressionValue(searchButtonView, "findViewById(R.id.button)");
          Button searchButton = (Button)searchButtonView;
          searchButton.setOnClickListener((new OnClickListener() {
              @RequiresApi(api = Build.VERSION_CODES.O)
              public final void onClick(View it) {
                  pd = new ProgressDialog(MapsActivity.this);
                  pd.setMessage("Please wait");
                  pd.setCancelable(false);
                  pd.show();

                  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                  //new JsonTask().execute("https://bigpicture2.herokuapp.com/api/v1/search?date="+ LocalDate.now().format(formatter));
                  add_photo_cards_to_view_from_json(Utils.getstringfromfile(getApplicationContext(), "search.json"));
                  //findViewById(R.id.scroll_View).setVisibility(View.VISIBLE);
                  pd.dismiss();

                  if(!bottomshown)
                      images_bar.animate().translationY(-images_bar.getHeight());
                  bottomshown = false;
                  hideButton.setVisibility(View.VISIBLE);
            }
         }));


          findViewById(R.id.mapbutton).setOnClickListener((new View.OnClickListener() {
              @RequiresApi(api = Build.VERSION_CODES.O)
              public final void onClick(View it) {
                  hideButton.setVisibility(View.VISIBLE);
                  if(bottomshown)
                      images_bar.animate().translationY(images_bar.getHeight());
                  else
                      images_bar.animate().translationY(-images_bar.getHeight());
                  bottomshown =! bottomshown;
              }
          }));
      }
   }

   public void onMapReady(@NotNull GoogleMap googleMap) {
      Intrinsics.checkNotNullParameter(googleMap, "googleMap");

      if (googleMap == null) {
         Intrinsics.throwUninitializedPropertyAccessException("mMap");
      }

      mMap = googleMap;

      mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
         @Override
         public void onCameraIdle() {
            //get latlng at the center by calling
            midLatLng = mMap.getCameraPosition().target;
         }
      });

      Intent intent = getIntent();
      PictureCardData card = intent.getParcelableExtra("card");
      if (card != null) {
         mMap.addMarker(new MarkerOptions().position(card.location).title(card.name).icon(BitmapDescriptorFactory.fromBitmap(card.image)));

         GoogleMap.OnCameraIdleListener maplisten = new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
               //get latlng at the center by calling
               midLatLng = mMap.getCameraPosition().target;
            }
         };
         // Create a new CameraUpdateAnimator for a given mMap
         // with an OnCameraIdleListener to set when the animation ends
         map.CameraUpdateAnimator animator = new map.CameraUpdateAnimator(mMap, maplisten);
         animator.add(CameraUpdateFactory.newLatLngZoom(card.location, 17), true, 5000);
         animator.execute();
      }

      else{
         LatLng sharjah = new LatLng(25.28D, 55.47D);
         googleMap.addMarker((new MarkerOptions()).position(sharjah).title("Marker in Sharjah"));
         //googleMap.moveCamera(CameraUpdateFactory.newLatLng(sharjah));
         googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sharjah, 12.0f));
      }
   }

   protected void add_photo_cards_to_view_from_json(String results) {
      List<PictureCardData> PicDataList = null;
      try {
         PicDataList = Utils.getPictureDataFromjsonstring( results);
         assert PicDataList != null;
      } catch (JSONException e) {
         e.printStackTrace();
         return;
      }

      //Log.i("data", PicDataList);
      images_view.removeAllViewsInLayout();

      GoogleMap.OnCameraIdleListener maplisten = new GoogleMap.OnCameraIdleListener() {
         @Override
         public void onCameraIdle() {
            //get latlng at the center by calling
            LatLng midLatLng = mMap.getCameraPosition().target;
         }
      };

      // Create a new CameraUpdateAnimator for a given mMap
      // with an OnCameraIdleListener to set when the animation ends
      final map.CameraUpdateAnimator animator = new map.CameraUpdateAnimator(mMap, maplisten);

      for(int i = 0; i < PicDataList.size(); i++) {
          images_view.addView(getCardViewFromPicdataV2(PicDataList.get(i),this,animator));
      }
   }

    protected MaterialCardView getCardViewFromPicdataV2(final PictureCardData card, Context context, final map.CameraUpdateAnimator animator){

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        MaterialCardView mCard = (MaterialCardView)inflater.inflate(R.layout.matterial_card_test, null);
        mCard.setLayoutParams(new FrameLayout.LayoutParams(600, ViewGroup.LayoutParams.MATCH_PARENT));
        TextView name = (TextView) mCard.findViewById(R.id.mCardname);
        TextView title = (TextView) mCard.findViewById(R.id.mCardtitle);
        ImageView image =  mCard.findViewById(R.id.mCardImage);

        name.setText(card.getName());
        title.setText(card.getTitle());
        Glide.with(context).load(card.url.replace("_c.jpg","_t.jpg")).into(image);
        mMap.addMarker(new MarkerOptions().position(card.location).title(card.name).snippet(card.title)
        .icon(BitmapDescriptorFactory.fromResource(R.drawable.image_not_found)));

        mCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mMap.clear();
                animator.add(CameraUpdateFactory.newLatLngZoom(card.location, 17), true, 1000);
                animator.execute();
            }
        });

        return mCard;
   }

       private class JsonTask extends AsyncTask<String, String, String> {

       protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(MapsActivity.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
       }

        protected String doInBackground(String... params) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }
                return buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()){
                pd.dismiss();
            }

            //txtJson.setText(result);
            //Log.i("retrieved json:", result);
            add_photo_cards_to_view_from_json(result);
        }
    }
}
