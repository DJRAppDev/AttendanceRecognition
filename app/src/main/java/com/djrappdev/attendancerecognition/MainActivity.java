package com.djrappdev.attendancerecognition;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private final String app_id = "858c50a3";
    private final String app_key = "1f30faa8bbb6406231731963e9129191";
    private RequestQueue queue;
    private Button addStudent;
    private String studentPath;
    private TextView statusText;
    private EditText osisField;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addStudent = findViewById(R.id.addStudent);
        statusText = findViewById(R.id.attendanceStatus);
        osisField = findViewById(R.id.osisField);

        queue = Volley.newRequestQueue(this);

        //TODO: Prompt user to type in OSIS so student can be enrolled and finish image to base64 conversion
        addStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!osisField.getText().toString().equals("")) {
                    dispatchTakePictureIntent(osisField.getText().toString());
                }

            }
        });
    }

    //Functions to take photo and save it to internal storage
    private void dispatchTakePictureIntent(String OSIS) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile(OSIS);
            } catch (IOException ex) {
                // Error occurred while creating the File
                statusText.setText("Failed to create student's file!");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile(String OSIS) throws IOException {
        // Create an image file name
        String imageFileName = OSIS;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        studentPath = image.getAbsolutePath();
        return image;
    }

    //Functions to enroll and verify different faces
    public void addStudent(final String image, final String OSIS) {
        String url = "https://api.kairos.com/enroll";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.getMessage());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("image", image);
                params.put("subject_id", OSIS);
                params.put("gallery_name", "Students");

                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("app_id", app_id);
                headers.put("app_key", app_key);
                return headers;
            }
        };
        queue.add(postRequest);
    }
}