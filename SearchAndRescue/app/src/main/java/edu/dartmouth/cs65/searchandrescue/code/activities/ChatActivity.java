/**
 * Author: Michael Canche
 * Dartmouth College, Spring 2020, Professor Campbell
 */
package edu.dartmouth.cs65.searchandrescue.code.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.MenuItem;
import android.view.View;
import android.widget.MultiAutoCompleteTextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import edu.dartmouth.cs65.searchandrescue.R;
import edu.dartmouth.cs65.searchandrescue.code.structures.ChatMessage;
import edu.dartmouth.cs65.searchandrescue.code.structures.ChatMessageFRAdapter;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener{

    private String activeCode;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore database;
    private MultiAutoCompleteTextView input;
    private ChatMessageFRAdapter adapter;
    private FloatingActionButton btnSend, micButton;
    private int SPEECH_REQUEST = 22;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        createActionBar();
        super.onCreate(savedInstanceState);
        //Set up
        setContentView(R.layout.activity_chat);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Intent codePass = getIntent();
        activeCode = codePass.getStringExtra("current code");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        btnSend = findViewById(R.id.sendButton);
        micButton = findViewById(R.id.micButton);

        btnSend.setOnClickListener(this);
        micButton.setOnClickListener(this);

        input = findViewById(R.id.messageInput);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        database = FirebaseFirestore.getInstance();

        //Query the messages stored for the current code, sorted by time
        Query query = database.collection("messages_"+activeCode).orderBy("time");
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {}
        });
        //Attach the query and user id to ChatMessageFRAdapter
        adapter = new ChatMessageFRAdapter(ChatActivity.this, query, firebaseUser.getUid());
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        //Send message to chat
        if(v.getId() == R.id.sendButton) {
            String message = input.getText().toString();
            //If user has a name - used in email and Google login
            if(firebaseUser.getDisplayName() != null)
                database.collection("messages_"+activeCode).add(new ChatMessage(firebaseUser.getDisplayName(), message, firebaseUser.getUid()));
            //If user has no name, used in phone number login
            else
                database.collection("messages_"+activeCode).add(new ChatMessage(firebaseUser.getPhoneNumber(), message, firebaseUser.getUid()));
            input.setText("");
        }
        //Speech recognition
        else if(v.getId() == R.id.micButton) {
            startSpeech();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(adapter!=null)
            adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(adapter!=null)
            adapter.stopListening();
    }
    private void createActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setTitle("Chat");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //Return to MainActivity
            case android.R.id.home:
                Intent mainIntent = new Intent(ChatActivity.this, MainActivity.class);
                mainIntent.putExtra("current code", activeCode);
                startActivity(mainIntent);
                finish();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void startSpeech() {
        //Start speech intent
        Intent speechIntent = new Intent
                (RecognizerIntent.ACTION_RECOGNIZE_SPEECH).putExtra
                (RecognizerIntent.EXTRA_PROMPT, "Recording your message.").
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(speechIntent, SPEECH_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Stores the recognized speech into the text field
        if (requestCode == SPEECH_REQUEST && resultCode == RESULT_OK) {
                input.setText(data.getStringArrayListExtra
                        (RecognizerIntent.EXTRA_RESULTS).get(0));
        }
    }
}
