/*
 * Copyright (c)  2018 Team 24 CMPUT301 University of Alberta - All Rights Reserved.
 * You may use distribute or modify this code under terms and conditions of COde of Student Behavious at University of Alberta.
 * You can find a copy of the license ini this project. Otherwise, please contact harrold@ualberta.ca
 *
 */

package com.example.taskmagic;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import misc.BottomNavigationViewHelper;

/**
 * Created by hyusuf on 2018-03-08.
 */

public class HomeFeed extends AppCompatActivity {
    private UserTask task;
    private FireBaseManager fmanager;
    private DatabaseReference db;
    private FirebaseAuth auth;
    private RecyclerView.Adapter adapter;
    private RecyclerView recyclerView;
    private TextView textview;
    private BottomNavigationView mHomeNav;
    public static Boolean isActivity;
    private TaskList listUserTask;
    private String userName;
    private String personName;
    private String personId;
    private String email;
    private DatabaseReference reference;
    private GoogleApiClient mGoogleApiClient;
    final UserSingleton singleton = UserSingleton.getInstance();
    /**
     * this function sets up the home feed
     * @param savedInstanceState
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homefeed);

        auth = singleton.getmAuth();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mHomeNav=(BottomNavigationView)findViewById(R.id.home_bottom_navigation);
        BottomNavigationViewHelper.removeShiftMode(mHomeNav);
        fmanager = new FireBaseManager(singleton.getmAuth(), getApplicationContext());
        reference= FirebaseDatabase.getInstance().getReference();
        if(singleton.getmGoogleApiClient()!=null) {
            googleUser();
        }
        fmanager.getUserInfo(singleton.getUserId(), new OnGetUserInfoListener() {
            @Override
            public void onSuccess(User user) {
                singleton.setFullName(user.getFullName());
                singleton.setUserName(user.getUserName());
                singleton.setUserId(user.getId());
            }

            @Override
            public void onFailure(String message) {

            }
        });
        listener(singleton.getUserId());
        mHomeNav.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.logout:
                                singleton.logout();
                                Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK );
                                startActivity(intent);
                                return true;
                            case R.id.map:
                                startActivity(new Intent(HomeFeed.this, MapsActivity.class).putExtra("Mode", "Search"));
                                return true;
                            case R.id.addTask:
                                startActivity(new Intent(HomeFeed.this, CreateTaskActivity.class));
                                return true;
                            case R.id.search:
                                startActivity(new Intent(HomeFeed.this, SearchActivity.class));
                                return true;
                            case R.id.profile:
                                startActivity(new Intent(HomeFeed.this, ViewProfileActivity.class));
                                return true;
                            default: return false;
                        }
                    }

                });

        //https://medium.com/@harivigneshjayapalan/android-recyclerview-implementing-single-item-click-and-long-press-part-ii-b43ef8cb6ad8
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Log.d("click: ", "clicked" + position);
                UserTask chosenTask= listUserTask.getTask(position);
                Intent myIntent = new Intent(HomeFeed.this, ViewTaskActivity.class);
                myIntent.putExtra("UserTask",chosenTask);
                startActivity(myIntent);
            }
        }));

    }

    /**
     * this function listens to any changes to the datatbase and retrieves the info
     * @param requestor
     */
    private void listener(final String requestor) {
        fmanager.getRequestedTasks(requestor, new OnGetAllTaskReqListener() {
            @Override
            public void onSuccess(TaskList taskList) {
                Log.d("Succes", "onSuccess: "+taskList.getCount());
                TaskList list=new TaskList();//recheck this function
                list=taskList;
                listUserTask = taskList;
                updateView(taskList);
            }

            @Override
            public void onFailure(String message) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActivity=false;
    }

    /**
     * this function updates the recyclerview
     * @param taskList
     */
    public void updateView(TaskList taskList){
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        adapter=new TaskRecyclerAdapter(taskList,getApplicationContext());
        Log.d("xUserReqName", "message " + taskList.getTask(0).getRequesterName());
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onStart() {
        super.onStart();
        isActivity=true;

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    /**
     * retrieves google user thats signed in
     */
    private void googleUser(){
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (acct != null) {
            User user=new User();
            user.setEmailAddress(acct.getEmail());
            user.setFullName(acct.getGivenName());
            user.setUserName(acct.getDisplayName());
            user.setId(singleton.getUserId());
            reference.child("users").child(user.getId()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                }
            });

        }
    }

}