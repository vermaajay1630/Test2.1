package com.example.test21;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public static final int CAM_REQUEST_CODE = 100;
    ImageView img1;
    Button b1, b2;
    Uri imageUri;
    ProgressDialog progressDialog;
    StorageReference storageReference;
    FirebaseDatabase fdatabase;
    FirebaseStorage fstorage;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        img1 = findViewById(R.id.imgView);
        b1 =  findViewById(R.id.camBtn);
        b2 =  findViewById(R.id.uploadBtn);
        fdatabase = FirebaseDatabase.getInstance();
        fstorage = FirebaseStorage.getInstance();
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkCameraPermission();
            }
        });
        
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImg();
            }
        });
    }

    private void uploadImg() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading");
        progressDialog.show();
        SimpleDateFormat formatter = new SimpleDateFormat("YYYY_MM_DD_HH_MM_SS", Locale.getDefault());
        Date now = new Date();
        String filename = formatter.format(now);
        storageReference = FirebaseStorage.getInstance().getReference("images/"+filename);
        storageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
    @Override
    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
        img1.setImageURI(null);
        if (progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }
}).addOnFailureListener(new OnFailureListener() {
    @Override
    public void onFailure(@NonNull Exception e) {
        if(progressDialog.isShowing()){
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Upload Failed", Toast.LENGTH_SHORT).show();
        }
    }
});

    }

    private void checkCameraPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAM_REQUEST_CODE);
        }else{
            openCamera();
        }
    }

    private void openCamera() {
        Intent cam = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cam, CAM_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            if(requestCode == CAM_REQUEST_CODE && resultCode == RESULT_OK){
                Bitmap bm = (Bitmap)data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                String path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), bm, "val", null);
                imageUri = Uri.parse(path);
                img1.setImageURI(imageUri);

            }
    }
}