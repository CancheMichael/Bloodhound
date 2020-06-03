/**
 * Author: Michael Canche
 * Dartmouth College, Spring 2020, Professor Campbell
 */
package edu.dartmouth.cs65.searchandrescue.code.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

import edu.dartmouth.cs65.searchandrescue.R;


public class PartyActivity extends AppCompatActivity {
    private Button mJoin, mCreate;
    private DatabaseReference mDatabase;
    private String codeInput, code;
    private FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        createActionBar();
        super.onCreate(savedInstanceState);
        //Set up
        setContentView(R.layout.activity_party);
        mJoin = findViewById(R.id.joinButton);
        mCreate = findViewById(R.id.createButton);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();

        //Join Button
        mJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PartyActivity.this);
                builder.setTitle("Enter Party Code");


                final EditText input = new EditText(PartyActivity.this);
                input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                builder.setView(input);

               //Button to go to main activity
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Gets user input
                        codeInput  = input.getText().toString();
                        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                //Checks if code is valid
                                    if (dataSnapshot.child("codes").child(codeInput).exists()) {
                                        //Checks if the current user is associated with code if
                                        //If true, just goes to main activity
                                        if (dataSnapshot.child("users").child(user.getUid()).exists()) {
                                            mDatabase.child("users").child(user.getUid()).child(codeInput).setValue(codeInput);
                                        }
                                        //If false, associates user with code
                                        else {
                                            mDatabase.child("users").child(user.getUid()).setValue(user.getUid());
                                            mDatabase.child("users").child(user.getUid()).child(codeInput).setValue(codeInput);
                                        }
                                        //Start main activity
                                        Intent mainIntent = new Intent(PartyActivity.this, MainActivity.class);
                                        mainIntent.putExtra("current code", codeInput);
                                        startActivity(mainIntent);
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Invalid party code",
                                                Toast.LENGTH_LONG).show();
                                    }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        //Start MainActivity
        mCreate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                code = new Random().nextInt() + "";
                new AlertDialog.Builder(PartyActivity.this)
                        .setMessage("Party code is: " + code)
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                startMainActivity();
                            }
                        })
                        .setNegativeButton("Send Email", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sendEmail();
                            }
                        })
                        .show();
            }

        });
    }


    private void createActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setTitle("Party Search");
        }
    }

    //Action bar buttons
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            //Logout current firebase user and return to sign in
            case R.id.action_bar_logout:
                FirebaseAuth.getInstance().signOut();
                Intent logoutIntent = new Intent(PartyActivity.this,
                        SignInActivity.class);
                logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                logoutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(logoutIntent);
                finish();
                return true;
            //Open history activity
            case R.id.action_bar_history:
                Intent codeHistoryIntent = new Intent(PartyActivity.this,
                        CodeHistoryActivity.class);
                startActivity(codeHistoryIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //History and logout button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_party, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    //Set up email intent, then starts main activity
    public void sendEmail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Invite to Search Party");
        intent.putExtra(Intent.EXTRA_TEXT, "Your Bloodhound Search Party code is: " + code);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 200);
        }

    }

    //Begins MainActivity after email intent
    private void startMainActivity() {
        mDatabase.child("codes").child(code).setValue(code);

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.child("users").child(user.getUid()).exists()) {
                    mDatabase.child("users").child(user.getUid()).child(code).setValue(code);
                }
                else {
                    mDatabase.child("users").child(user.getUid()).setValue(user.getUid());
                    mDatabase.child("users").child(user.getUid()).child(code).setValue(code);
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Intent mainIntent = new Intent(PartyActivity.this, MainActivity.class);
        mainIntent.putExtra("current code", code);
        startActivity(mainIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //If from email intent
        if(requestCode == 200) {
            startMainActivity();
        }
    }
}
