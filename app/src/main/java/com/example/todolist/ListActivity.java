package com.example.todolist;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    private static final String TAG = "ListActivity_tag";

    private String list_Title;
    private String list_Id;
    private int list_Size;
    private ImageView image_btn_Back;
    private TextView txt_List_Title;
    private TextView txt_Delete_List;
    private Button btn_C_New_Task;
    private RecyclerView rv_Todos;

    private FirebaseDatabase data_base;

    private TodoAdapter Todo_adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        rv_Todos = findViewById(R.id.rv);
        image_btn_Back = findViewById(R.id.btn_Back);
        txt_List_Title = findViewById(R.id.txt_List_Title);
        txt_Delete_List = findViewById(R.id.txt_Delete_List);
        btn_C_New_Task = findViewById(R.id.btn_Cr_New_Task);

        setupRecycler();

        list_Title = getIntent().getStringExtra("list_title");
        list_Id = getIntent().getStringExtra("list_id");
        txt_List_Title.setText(list_Title);

        data_base = FirebaseDatabase.getInstance();

        loadList();

        txt_Delete_List.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteList(list_Id);
            }
        });

        btn_C_New_Task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListActivity.this, NewTaskActivity.class);
                intent.putExtra("title", list_Title);
                intent.putExtra("list_id", list_Id);
                intent.putExtra("list_size", list_Size);
                startActivity(intent);
            }
        });

        image_btn_Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void deleteList(String listId) {

        data_base.getReference()
                .child("users")
                .child(FirebaseAuth.getInstance().getUid())
                .child("lists")
                .child(listId)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: delete list " + list_Title);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: delete failed " + e.getMessage());

                    }
                });
    }

    private void setupRecycler() {

        Todo_adapter = new TodoAdapter();
        rv_Todos.setAdapter(Todo_adapter);

        Todo_adapter.setOnCheckedListener(new TodoAdapter.OnChecked() {

            @Override
            public void onChecked(final ToDo todo) {
                // update the status
                data_base.getReference()
                        .child("users")
                        .child(FirebaseAuth.getInstance().getUid())
                        .child("lists")
                        .child(list_Id)
                        .child("tasks")
                        .child(todo.getId())
                        .child("checked")
                        .setValue(!todo.isChecked())
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

            @Override
            public void onItemClicked(String title, String description, String date) {
                Intent intent = new Intent(ListActivity.this, TaskActivity.class);

                intent.putExtra("task_title", title);
                intent.putExtra("task_description", description);
                intent.putExtra("task_date", date);
                intent.putExtra("list_id", list_Id);
                intent.putExtra("list_size", list_Size);

                startActivity(intent);
            }
        });

    }

    private void loadList() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading ...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        data_base.getReference()
                .child("users")
                .child(FirebaseAuth.getInstance().getUid())
                .child("lists")
                .child(list_Id)
                .child("tasks")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d(TAG, "onSuccess: " + snapshot.toString());
                        progressDialog.dismiss();

                        List<ToDo> list = new ArrayList<>();

                        for (DataSnapshot data : snapshot.getChildren()) {
                            ToDo task = data.getValue(ToDo.class);
                            list.add(task);
                            if (list.size() != 0) {
                                list_Size = list.size();
                                Todo_adapter.setList(list);
                            } else {
                                Todo_adapter.setList(list);
                                Toast.makeText(ListActivity.this, "No List", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, "onCancelled: ");
                    }
                });
    }
}