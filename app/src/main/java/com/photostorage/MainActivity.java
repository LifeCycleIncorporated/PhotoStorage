package com.photostorage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.google.protobuf.Empty;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {
    private Button chooseButton, saveButton, showButton;
    private ImageView imageView;
    private EditText imageNameEditText;
    private ProgressBar progressBar;
    private Uri imageUri;
    private static final int IMAGE_REQUEST = 1;
    DatabaseReference databaseReference;
    StorageReference storageReference;

    StorageTask storageTask;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseReference = FirebaseDatabase.getInstance().getReference("Upload");
        storageReference = FirebaseStorage.getInstance().getReference("Upload");

        chooseButton = findViewById(R.id.imageChooseButtonId);
        saveButton = findViewById(R.id.saveImageButtonId);
        showButton = findViewById(R.id.showImageButtonId);

        imageView = findViewById(R.id.imageViewId);
        imageNameEditText = findViewById(R.id.imageNameEditTextId);

        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (storageTask!=null && storageTask.isInProgress()){
                    Toast.makeText(getApplicationContext(),"Saving process is running",Toast.LENGTH_SHORT).show();
                }else {
                    saveImage();
                }

            }
        });
        showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,PhotoViewActivity.class);
                startActivity(intent);
            }
        });
    }

    public void openFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,IMAGE_REQUEST);
    }

    private void saveImage(){
        String imageNames = imageNameEditText.getText().toString().trim();
        if (imageNames.isEmpty()){
            imageNameEditText.setError("Enter the image name");
            imageNameEditText.requestFocus();
            return;
        }

        StorageReference ref = storageReference.child(System.currentTimeMillis()+"-"+getFileExtension(imageUri));

        ref.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(MainActivity.this, "Image is stored successfully", Toast.LENGTH_SHORT).show();


                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        Uri downloadUrl = uriTask.getResult();

                        Upload upload = new Upload(imageNames, downloadUrl.toString());

                        String uploadId = databaseReference.push().getKey();

                        databaseReference.child(uploadId).setValue(upload);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Image is not stored successfully", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==IMAGE_REQUEST && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            imageUri = data.getData();
            Picasso.with(this).load(imageUri).into(imageView);
        }
    }


    public String getFileExtension(Uri imageUri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(imageUri));
    }

}