/**
 * Author: Michael Canche
 * Dartmouth College, Spring 2020, Professor Campbell
 */
package edu.dartmouth.cs65.searchandrescue.code.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import edu.dartmouth.cs65.searchandrescue.R;

public class CodeHistoryActivity  extends AppCompatActivity {

    private ArrayAdapter<String> mAdapter;
    private DatabaseReference mDatabase;
    private FirebaseUser user;
    private ArrayList<String> codeList;
    private boolean delete = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createActionBar();
        //Set up
        setContentView(R.layout.activity_code_history);
        user = FirebaseAuth.getInstance().getCurrentUser();

        ListView mListView = findViewById(R.id.list);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1);

        mListView.setAdapter(mAdapter);
        codeList = new ArrayList<>();

        //Check database for codes associated with current user
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot data : dataSnapshot.child("users").child(user.getUid()).getChildren()) {
                        codeList.add(data.getValue(String.class));
                }
                mAdapter.addAll(codeList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //When code is selected
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                String code = mAdapter.getItem(position);

                if(!delete) {
                    //Start MainActivity with current code
                    Intent i = new Intent(CodeHistoryActivity.this, MainActivity.class);
                    i.putExtra("current code", code);
                    startActivity(i);
                }
                else {
                    //Removes current code from beings associated with the current user
                    codeList.remove(code);
                    mDatabase.child("users").child(user.getUid()).child(code).removeValue();
                    mAdapter.clear();
                    mAdapter.addAll(codeList);
                }
            }
        });

    }

    private void createActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setTitle("Code History");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            //Enable code deletion
            case R.id.action_bar_delete:
                delete = !delete;
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_history, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
