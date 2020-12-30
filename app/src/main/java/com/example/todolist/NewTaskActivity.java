package com.example.todolist;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NewTaskActivity extends AppCompatActivity {

    private static final String TAG = "NewTaskActivity_tag";

    private FirebaseDatabase dat_abase;

    private ImageView image_btn_Back;
    private TextView txt_btn_AddTask;

    private String list_Title;
    private String list_Id;
    private int list_Size;

    private EditText et_Task_Title, et_Task_Description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);

        image_btn_Back = findViewById(R.id.btn_Back);
        txt_btn_AddTask = findViewById(R.id.btn_Add_Task);
        et_Task_Description = findViewById(R.id.et_Task_Description);
        et_Task_Title = findViewById(R.id.et_Task_Title);

        dat_abase = FirebaseDatabase.getInstance();

        list_Title = getIntent().getStringExtra("title");
        list_Id = getIntent().getStringExtra("list_id");
        list_Size = getIntent().getIntExtra("list_size", 0);


        txt_btn_AddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String title = et_Task_Title.getText().toString().trim();
                String description = et_Task_Description.getText().toString().trim();
                if (title.equals("")) {
                    Toast.makeText(NewTaskActivity.this, "Add Title !", Toast.LENGTH_SHORT).show();
                } else if (description.equals("")) {
                    Toast.makeText(NewTaskActivity.this, "Add description !", Toast.LENGTH_SHORT).show();
                } else {
                    addNewTask(title, description);
                }
            }
        });

        image_btn_Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void addNewTask(String title, String description) {

        final ProgressDialog progressDialog = new ProgressDialog(NewTaskActivity.this);
        progressDialog.setMessage("Loading ...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();


        Log.d(TAG, "addNewTask: list id " + list_Id);

        DatabaseReference ref = dat_abase.getReference()
                .child("users")
                .child(FirebaseAuth.getInstance().getUid())
                .child("lists")
                .child(list_Id)
                .child("tasks");

        String todoId = ref.push().getKey();

        ToDo toDo = new ToDo(todoId, title, description, false, "" + System.currentTimeMillis());

        ref.child(todoId)
                .setValue(toDo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: add new Todo ");
                        updateListSize(list_Size);
                        progressDialog.dismiss();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(NewTaskActivity.this, "Failed to add new ToDo ", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateListSize(int size) {

        final int finalSize = size;
        dat_abase.getReference()
                .child("users")
                .child(FirebaseAuth.getInstance().getUid())
                .child("lists")
                .child(list_Id)
                .child("size")
                .setValue(++size)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: update " + finalSize);
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