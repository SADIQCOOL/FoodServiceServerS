package com.example.foodserviceservers;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    Button btnsignin;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnsignin=(Button)findViewById(R.id.btnsignin);

        btnsignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent singin = new Intent(MainActivity.this,SignIn.class);
                startActivity(singin);
            }
        });
    }
}
