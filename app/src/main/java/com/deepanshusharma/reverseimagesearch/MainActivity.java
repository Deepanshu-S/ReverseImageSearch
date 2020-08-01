package com.deepanshusharma.reverseimagesearch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputLayout;


import java.io.BufferedReader;
import java.io.Console;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private final OkHttpClient client = new OkHttpClient().newBuilder().followRedirects(false).followSslRedirects(false).build();
    SearchView searchBox;
    Button searchButton;

    public static final int PICK_IMAGE = 1;
    ImageView image;
    TextView responseView;

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.searchButton = findViewById(R.id.searchButton);
        this.searchBox = findViewById(R.id.searchBox);
        FloatingActionButton fab = findViewById(R.id.fab);
        Toolbar toolbar = findViewById(R.id.toolbar);
        image = (ImageView) findViewById(R.id.imageView);
        image.setImageResource(R.drawable.ic_launcher_foreground);



        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
//        AdSize customAdSize = new AdSize(250, 250);
//        PublisherAdView adView = new PublisherAdView(this);
//        adView.setAdSizes(customAdSize);

        setSupportActionBar(toolbar);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);


                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);

//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchGo();
//            startActivity(WebViewActivity.);
//                Intent i = new Intent(String.valueOf(WebViewActivity.class));
//                i.putExtra(WebViewActivity., "http://www.google.com");
//                startActivity(i);
            }
        });


        searchBox.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchGo();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


        //Handle incoming text processing intent request
        CharSequence sharedText = getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
        if (sharedText != null) {
            searchBox.setQuery(sharedText, true);
            searchGo();
        }

    }




    public void searchGo() {
        String query = (String) searchBox.getQuery().toString().replace(' ', '+');
        if (query.isEmpty()) {
            Toast.makeText(this, "Please type something to search...", Toast.LENGTH_SHORT).show();
            return;
        }

        String preQuery = "https://www.google.com/search?tbm=isch&q=";
        Intent searchIntent = new Intent(Intent.ACTION_VIEW);
        searchIntent.setData(Uri.parse(preQuery + query));
        startActivity(searchIntent);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }



    private static int RESULT_LOAD_IMAGE = 1;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            image.setImageURI(imageUri);
            searchBox.setQueryHint("Almost started search....");

            new ImageSearchTask().execute(data);
            searchBox.setQueryHint("Should've ended search....");

        }
    }

    private void makePost(){
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("encoded_image", "filehere")
                .addFormDataPart("image_content", "")
                .build();

        Request request = new Request.Builder()
                .url("http://www.google.com/searchbyimage/upload")
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            Headers responseHeaders = response.headers();
            for (int i = 0; i < responseHeaders.size(); i++) {
                Log.d("Req_loop",responseHeaders.name(i) + ": " + responseHeaders.value(i));
            }

            Log.d("Request", response.toString() );
            System.out.println(response.body().string());
        }

        catch (IOException e) {
            e.printStackTrace();
        }
    }








    public class ImageSearchTask extends AsyncTask<Intent, Void, String> {

        // TODO (26) Override onPreExecute to set the loading indicator to visible

        @Override
        protected String doInBackground(Intent... params) {

            File file=new File(params[0].getData().toString());

            String fs ="";
            try {
                FileInputStream fis=new FileInputStream(file);
                fs = fis.toString();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Log.d("dexter_file", file.toString());
            Log.d("dexter_file_fs", fs);

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("encoded_image", "")
                    .addFormDataPart("image_content", "")
                    .build();

            Request request = new Request.Builder()
                    .url("https://www.google.com/searchbyimage/upload")
                    .post(requestBody)
                    .addHeader("allow_redirects", "false")
                    .build();


            //method 2
//            final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
//
//            RequestBody req = new MultipartBody.Builder().setType(MultipartBody.FORM)
////                    .addFormDataPart("file", file.getName(),RequestBody.create(MediaType.parse("text/csv"), file)).build();
//                    .addFormDataPart("encoded_image",file.getName(), RequestBody.create(MEDIA_TYPE_PNG, "fs")).build();
//
//            Log.d("dexter_request_body",req.toString());
//
//            Request request = new Request.Builder()
//                    .url("https://www.google.com/searchbyimage/upload")
//                    .post(req)
//                    .build();
            Log.d("dexter_request",request.toString());

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                Headers responseHeaders = response.headers();
                for (int i = 0; i < responseHeaders.size(); i++) {
                    Log.d("dexter", responseHeaders.name(i) + ": " + responseHeaders.value(i));
                }

                Log.d("dexter", response.toString());
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("dexter",e.toString());
            }
        return "";
        }

        @Override
        protected void onPostExecute(String resp) {
            // TODO (27) As soon as the loading is complete, hide the loading indicator
            if (resp != null && !resp.equals("")) {
                // TODO (17) Call showJsonDataView if we have valid, non-null results
                searchBox.setQueryHint("Complete");
                Log.d("dexter",resp);


            }
            else {    searchBox.setQueryHint("failed");
            }

        }
    }


    protected void onPreExecute(int resp){
        searchBox.setQueryHint("Started search....");

    }

}