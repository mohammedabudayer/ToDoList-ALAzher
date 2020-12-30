package com.example.todolist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;

public class TaskActivity extends AppCompatActivity {
    private static final String TAG = "TaskActivity_tag";
    String task_title;
    String task_description;
    String task_date;
    String task_id;
    String list_id;
    int list_size;

    private ImageView image_btn_Back;
    private EditText et_Task_Title, et_Task_Description;
    private TextView btnEdit;
    private TextView txt_Delete_Task;
    private TextView txt_Date;
    private FirebaseDatabase data_base;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        image_btn_Back = findViewById(R.id.btn_Back);
        et_Task_Title = findViewById(R.id.et_Task_Title);
        et_Task_Description = findViewById(R.id.et_Task_Description);
        btnEdit = findViewById(R.id.btn_Edit);
        txt_Delete_Task = findViewById(R.id.tv_Delete_Task);
        txt_Date = findViewById(R.id.tv_Date);

        data_base = FirebaseDatabase.getInstance();


        Intent i = getIntent();

        task_title = i.getStringExtra("task_title");
        task_description = i.getStringExtra("task_description");
        task_date = i.getStringExtra("task_date");
        task_id = i.getStringExtra("task_id");
        list_id = i.getStringExtra("list_id");
        list_size = i.getIntExtra("list_size", 0);

        et_Task_Title.setText(task_title);
        et_Task_Description.setText(task_description);

        // date
        SimpleDateFormat DateFor = new SimpleDateFormat("dd MMMM yyyy, HH:mm");
        String stringDate = DateFor.format(Long.parseLong(task_date));
        txt_Date.setText(stringDate);

        image_btn_Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: task id  : " + task_id);
                Log.d(TAG, "onClick: task_date  : " + task_date);

                String title = et_Task_Title.getText().toString().trim();
                String description = et_Task_Description.getText().toString().trim();

                if (!title.equals(task_title)) {
                    data_base.getReference()
                            .child("users")
                            .child(FirebaseAuth.getInstance().getUid())
                            .child("lists")
                            .child(list_id)
                            .child("tasks")
                            .child(task_id)
                            .child("title")
                            .setValue(title)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "onSuccess: success to create task");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: error " + e.getMessage());

                                }
                            });
                }

                if (!description.equals(task_description)) {
                    data_base.getReference()
                            .child("users")
                            .child(FirebaseAuth.getInstance().getUid())
                            .child("lists")
                            .child(list_id)
                            .child("tasks")
                            .child(task_id)
                            .child("description")
                            .setValue(description)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "onSuccess: success to create task");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: error " + e.getMessage());
                                }
                            });

                }
                finish();
            }
        });


        txt_Delete_Task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteTask(task_id);
            }
        });
    }

    private void deleteTask(final String task_id) {
        data_base.getReference()
                .child("users")
                .child(FirebaseAuth.getInstance().getUid())
                .child("lists")
                .child(list_id)
                .child("tasks")
                .child(task_id)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: success to create task");
                        updateListSize();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: error " + e.getMessage());
                    }
                });
    }

    private void updateListSize() {
        data_base.getReference()
                .child("users")
                .child(FirebaseAuth.getInstance().getUid())
                .child("lists")
                .child(list_id)
                .child("size")
                .setValue(--list_size)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: update");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e.getMessage());
                    }
                });
    }
}