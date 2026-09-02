package com.example.mr_manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class AdminActivity extends AppCompatActivity {

    private ImageButton buttonBack;
    private Button buttonOpenCreateTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        buttonBack = findViewById(R.id.buttonAdminBack);
        buttonOpenCreateTask = findViewById(
                R.id.buttonOpenCreateTask
        );

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        buttonOpenCreateTask.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent createTaskIntent = new Intent(
                                AdminActivity.this,
                                CreateTaskActivity.class
                        );

                        startActivity(createTaskIntent);
                    }
                }
        );
    }
}