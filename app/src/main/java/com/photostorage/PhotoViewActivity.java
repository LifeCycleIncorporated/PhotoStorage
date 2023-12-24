package com.photostorage;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
public class PhotoViewActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MyAdapter myAdapter;
    private List<Upload> uploadList;
    private ProgressBar imageProgressBar;
    DatabaseReference databaseReference;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);
        imageProgressBar = findViewById(R.id.imageProgressBarId);
        recyclerView = findViewById(R.id.recyclerViewId);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        uploadList = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("Upload");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Upload upload = dataSnapshot.getValue(Upload.class);
                    uploadList.add(upload);
                }
                myAdapter = new MyAdapter(PhotoViewActivity.this, uploadList );
                recyclerView.setAdapter(myAdapter);
                imageProgressBar.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(),"Error", Toast.LENGTH_SHORT).show();
                imageProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
}