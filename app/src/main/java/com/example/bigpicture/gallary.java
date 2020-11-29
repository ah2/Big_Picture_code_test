package com.example.bigpicture;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;

import kotlin.jvm.internal.Intrinsics;

public class gallary extends AppCompatActivity {
    ProgressDialog pd;
    private TableLayout images_view;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.gallary);
        images_view = findViewById(R.id.gImages);

        View Button1view = this.findViewById(R.id.gallaryButton);
        Intrinsics.checkNotNullExpressionValue(Button1view, "findViewById(R.id.button1)");
        Button button1 = (Button)Button1view;
        button1.setOnClickListener((new View.OnClickListener() {
            public final void onClick(View it) {
                Intent intent = new Intent(gallary.this, MapsActivity.class);
                gallary.this.startActivity(intent);
            }
        }));

        View Button2view = this.findViewById(R.id.searchButton);
        Intrinsics.checkNotNullExpressionValue(Button2view, "findViewById(R.id.button)");
        Button button2 = (Button)Button2view;
        button2.setOnClickListener((new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            public final void onClick(View it) {
                pd = new ProgressDialog(gallary.this);
                pd.setMessage("Please wait");
                pd.setCancelable(false);
                pd.show();

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                //new JsonTask().execute("https://bigpicture2.herokuapp.com/api/v1/search?date="+LocalDate.now().format(formatter));
                add_photo_cards_to_view_from_json(Utils.getstringfromfile(getApplicationContext(), "search.json"));
                pd.dismiss();
            }
        }));
    }

    protected void add_card_view_from_json() {
        List<PictureCardData> PicDataList = null;
        PicDataList = Utils.getPictureDataFromjsonObj(getApplicationContext(), "search.json");
        //Log.i("data", PicDataList);
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 600);  // width, height
        int margin = Utils.dpToPixel(8, getResources().getDisplayMetrics().density);
        layoutParams.setMargins(margin, margin, margin, margin);

        assert PicDataList != null;
        for(int i = 0; i < PicDataList.size(); i++) {
            images_view.addView(getCardViewFromPicdatav2(PicDataList.get(i),this));
        }
    }

    protected View getCardViewFromPicdatav2(final PictureCardData card, Context context){

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View mCard =  inflater.inflate(R.layout.matterial_card_test, null);
        TextView name = (TextView) mCard.findViewById(R.id.mCardname);
        TextView title = (TextView) mCard.findViewById(R.id.mCardtitle);
        ImageView image =  mCard.findViewById(R.id.mCardImage);

        name.setText(card.getName());
        title.setText(card.getTitle());
        Glide.with(context).load(card.url.replace("_c.jpg","_t.jpg")).into(image);

        return mCard;
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

        for(int i = 0; i < PicDataList.size(); i++) {
            TableRow tr = new TableRow(this);
            for (int j =0;  j<4 && i+j < PicDataList.size();j++) {
                View tmp = getCardViewFromPicdatav2(PicDataList.get(i+j), this);
                tmp.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.WRAP_CONTENT,0.5f));
                tr.addView(tmp);
            }
            images_view.addView(tr);
        }
    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(gallary.this);
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
