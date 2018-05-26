package com.example.asafkorem.imgur;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.bumptech.glide.Glide;

import java.util.concurrent.ExecutionException;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, Callback<JsonElement> {

    GridView gridView;
    Button getImagesButton;
    ImageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gridView = findViewById(R.id.grid_view);
        adapter = new ImageAdapter(this);
        gridView.setAdapter(adapter);

        getImagesButton = findViewById(R.id.get_images_button);
        getImagesButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(ImgurConfig.BASE_ADDRESS)
                .build();
        ImgurService service = restAdapter.create(ImgurService.class);
        service.getAlbumImages(ImgurConfig.MY_ALBUM_ID, this);
    }


    @Override
    public void success(JsonElement jsonElement, Response response) {
        JsonObject jo = jsonElement.getAsJsonObject();
        JsonArray jsonArray = jo.get("data").getAsJsonObject().get("images").getAsJsonArray();

        String urls[] = new String[12];
        int position = 0;
        for (JsonElement element : jsonArray)
        {
            urls[position] = element.getAsJsonObject().get("link").getAsString();
            Log.e("URL", ""+urls[position]);
            position++;
        }
        new GliderTask(this).execute(urls);

    }

    @Override
    public void failure(RetrofitError error) {
        // On error toast failure message
        Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show();
    }

    class GliderTask extends AsyncTask<String, Void, Integer> {
        Context context;
        public GliderTask(Context context) {
            this.context = context;
        }
        @Override
        protected Integer doInBackground(String... urls) {
            int position = 0;
            for (String url : urls) {
                try {
                    Bitmap bitmap = Glide.with(MainActivity.this)
                            .load(url)
                            .asBitmap().into(-1, -1).get();
                    adapter.setImage(position, bitmap);
                    position++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            return 5;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            adapter.notifyDataSetChanged();
            Toast.makeText(MainActivity.this, "Loaded!", Toast.LENGTH_LONG).show();
        }
    }
}