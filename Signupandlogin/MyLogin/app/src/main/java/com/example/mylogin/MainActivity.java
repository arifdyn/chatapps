package com.example.mylogin;

import static com.example.mylogin.cords.FirebaseCords.MAIN_CHAT_DATABASE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
//import android.widget.Toolbar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.mylogin.adapter.ChatAdapter;
import com.example.mylogin.fcm.SendPushNotification;
import com.example.mylogin.model.ChatModel;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;


    @Override
    public void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginPage.class));
            finish();
        }
        chatAdapter.startListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                mAuth.signOut();
                startActivity(new Intent(this, LoginPage.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    Toolbar toolbar;
    EditText chat_box;
    RecyclerView chat_list;

    ChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseMessaging.getInstance().subscribeToTopic("global_chat");

//        toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        chat_box = findViewById(R.id.chat_box);
        chat_list = findViewById(R.id.chat_list);

        initChatList();
    }

    private void initChatList() {
        chat_list.setHasFixedSize(true);
        chat_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true));


        Query query = MAIN_CHAT_DATABASE.orderBy("timestamp", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<ChatModel> option = new FirestoreRecyclerOptions.Builder<ChatModel>()
                .setQuery(query, ChatModel.class)
                .build();
        chatAdapter = new ChatAdapter(option);
        chat_list.setAdapter(chatAdapter);
        chatAdapter.startListening();
    }

    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), LoginPage.class));
        finish();
    }

    public void addMessage(View view) {
        String message = chat_box.getText().toString();
        FirebaseUser user = mAuth.getCurrentUser();
        if (!TextUtils.isEmpty(message)) {
            Date today = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String messageID = format.format(today);


            HashMap<String, Object> messageObj = new HashMap<>();
            messageObj.put("message", message);
            messageObj.put("username", user.getDisplayName());
            messageObj.put("timestamp", FieldValue.serverTimestamp());
            messageObj.put("messageID", messageID);

            MAIN_CHAT_DATABASE.document(messageID).set(messageObj).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        FirebaseMessaging.getInstance().unsubscribeFromTopic("global_chat");
                        SendPushNotification sendPushNotification = new SendPushNotification(MainActivity.this);
                        sendPushNotification.startPush(user.getDisplayName(), message, "global_chat");
                        chat_box.setText("");
                    } else {
                        Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

//    public void OpenExplorer(View view) {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
//                PackageManager.PERMISSION_GRANTED) {
//            ChoseImage();
//        } else {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 20);
//            } else {
//
//                Toast.makeText(this, "Storage permission needed", Toast.LENGTH_SHORT).show();
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 20);
//            }
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if(requestCode == 20){
//            if(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
//                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
//                ChoseImage();
//            }else{
//                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    private void ChoseImage() {
//
//
//    }

}
