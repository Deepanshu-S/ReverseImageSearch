package com.deepanshusharma.reverseimagesearch;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static int RESULT_LOAD_IMAGE = 1;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
    private final OkHttpClient client = new OkHttpClient().newBuilder().followRedirects(false).followSslRedirects(false).build();
    SearchView searchBox;
    Button searchButton;

    public static final int PICK_IMAGE = 1;
    ImageView image;
    TextView responseView;

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.searchButton = findViewById(R.id.searchButton);
        this.searchBox = findViewById(R.id.searchBox);
        FloatingActionButton fab = findViewById(R.id.fab);
        Toolbar toolbar = findViewById(R.id.toolbar);
        image = (ImageView) findViewById(R.id.imageView);

        // Get intent, action and MIME type

        if ( type != null && intent!=null) {
            Log.e("dexter","intent received");

            if ("text/plain".equals(type)) {
                Log.e("dexter","text");
                Log.e("dexter",type);
                // Handle text being sent
                CharSequence sharedText = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);

                if (sharedText != null) {
                    searchBox.setQuery(sharedText, true);
                    searchGo();
                }

            } else if (type.startsWith("image/")) {

                Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
                Log.d("postFlow_share_bit_open", "" + imageUri);


                // Handle single image being sent
//                resolveIntentBitmap(intent);

                Bitmap bitmap = null;
                try {
                    Log.d("postFlow_share_bit",imageUri.toString());
                    bitmap = getBitmapFromUri(imageUri);
                    image.setImageBitmap(bitmap);
                    new ImageSearchTask().execute(bitmap);
                } catch (Exception e) {
                    Log.d("postFlow_share_bit_fail", e.toString());
                }
            }
            else {Log.e("dexter","else");
                Log.e("dexter",type);
            }
        }



        setSupportActionBar(toolbar);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
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
//        CharSequence sharedText = getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
//        if (sharedText != null) {
//            searchBox.setQuery(sharedText, true);
//            searchGo();
//        }





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

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK) {
            resolveIntentBitmap(data);

        }
    }


    private void resolveIntentBitmap(Intent data) {
        Bitmap bitmap = null;
        try {
            bitmap = getBitmapFromUri(data.getData());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d("postFlow", e.toString());
        } catch (IOException e) {
            Log.d("postFlow", e.toString());
        }
        image.setImageBitmap(bitmap);
        new ImageSearchTask().execute(bitmap);

    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    public static byte[] tobyteArray(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return byteArray;
    }


    public class ImageSearchTask extends AsyncTask<Bitmap, Void, String> {


        @Override
        protected void onPreExecute() {
            searchBox.setQueryHint("Searching....");
        }

        // TODO (26) Override onPreExecute to set the loading indicator to visible
        @Override
        protected String doInBackground(Bitmap... params) {
            Bitmap bitmap = params[0];
            RequestBody req = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("encoded_image", "", RequestBody.create(MEDIA_TYPE_PNG, tobyteArray(bitmap))).build();

            Log.d("postFlow_request_body", req.toString());

            Request request = new Request.Builder()
                    .url("https://www.google.com/searchbyimage/upload")
                    .post(req)
                    .build();
            Log.d("postFlow_request", request.toString());
            try (Response response = client.newCall(request).execute()) {
                //if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                Log.d("postFlow_client", request.toString());

                Headers responseHeaders = response.headers();

                for (int i = 0; i < responseHeaders.size(); i++) {
                    Log.d("postFlow_loop", i + responseHeaders.name(i) + ": " + responseHeaders.value(i));
                    if (responseHeaders.name(i).toString().equals("location"))
                        return responseHeaders.value(i);
                }

                Log.d("postFlow_response", response.toString());

                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("postFlow_catch", e.toString());
            }
            Log.d("postFlow_task_over", request.toString());

            return "";
        }

        @Override
        protected void onPostExecute(String resp) {
            // TODO (27) As soon as the loading is complete, hide the loading indicator
            if (resp != null && !resp.equals("")) {
                searchBox.setQueryHint("Complete");
                Log.d("postFlow_resp", resp);
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(resp));
                    startActivity(browserIntent);

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("postFlow_intent_fail", e.toString());
                }
                image.setImageDrawable(null);
                searchBox.setQueryHint("Search...");


            } else {
                searchBox.setQueryHint("Network error, please try again...");
            }

        }

    }


}

