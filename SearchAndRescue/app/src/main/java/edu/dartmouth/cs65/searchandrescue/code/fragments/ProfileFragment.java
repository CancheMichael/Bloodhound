/**
 * Author: Michael Canche
 * Dartmouth College, Spring 2020, Professor Campbell
 */
package edu.dartmouth.cs65.searchandrescue.code.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;

import edu.dartmouth.cs65.searchandrescue.R;
import edu.dartmouth.cs65.searchandrescue.code.structures.MissingProfile;

public class ProfileFragment extends Fragment {

    private View view;
    private DatabaseReference mDatabase;
    private StorageReference mStorageRef;
    private static final int PERMISSIONS_CAMERA_STORAGE = 0;
    private static final int FROM_GALLERY = 2;
    private EditText mNameField, mEyeColor, mAge, mHeight, mHairColor, mComments;
    private Button mChangeButton;
    private RadioGroup mGenderGroup;
    private RadioButton mMaleButton, mFemaleButton;
    private ImageView mProfilePicture;
    private boolean cameraStoragePermissionsGranted = false;
    private String[] permissionsNeeded = {Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int FROM_CAMERA = 3;
    private File imagePath = null;
    private Uri masterUri;
    private String path = "null";
    private String activeCode;
    private ImageLoaderConfiguration config;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Setup
        if(view == null)
            view = inflater.inflate(R.layout.fragment_displayperson, container, false);
        getActivity().setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        Intent codePass = getActivity().getIntent();
        activeCode = codePass.getStringExtra("current code");
        mNameField = view.findViewById(R.id.nameField);
        mEyeColor = view.findViewById(R.id.eyeColor);
        mAge = view.findViewById(R.id.age);
        mHeight = view.findViewById(R.id.Height);
        mHairColor = view.findViewById(R.id.hairColor);
        mComments = view.findViewById(R.id.comment);
        mChangeButton = view.findViewById(R.id.changeButton);

        mGenderGroup = view.findViewById(R.id.genderGroup);
        mMaleButton = view.findViewById(R.id.maleButton);
        mFemaleButton = view.findViewById(R.id.femaleButton);

        mProfilePicture = view.findViewById(R.id.profilePicture);

        config = new ImageLoaderConfiguration.Builder(view.getContext()).build();

        if (!ImageLoader.getInstance().isInited()) {
            ImageLoader.getInstance().init(config);
        }
        new SetUpTask().execute();
        return view;

    }

    private void setupInfo() {
        // Get a reference to our current case
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference().child("codes").child(activeCode);
        if (!ImageLoader.getInstance().isInited()) {
            ImageLoader.getInstance().init(config);
        }

        //Read data at our current case
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MissingProfile prof = snapshot.getValue(MissingProfile.class);
                    if(isAdded()) {
                        if(prof != null) {
                            if(prof.getName() != null)
                                mNameField.setText("Name: " + prof.getName());
                            if(prof.getAge() != null)
                                mAge.setText("Age: " + prof.getAge());
                            if(prof.getEyeColor() != null)
                                mEyeColor.setText("Eye Color: " + prof.getEyeColor());
                            if(prof.getHeight() != null)
                                mHeight.setText("Height: " + prof.getHeight());
                            if(prof.getHairColor() != null)
                                mHairColor.setText("Hair Color: " + prof.getHairColor());
                            if(prof.getComment() != null)
                                mComments.setText(prof.getComment());
                            if(prof.getGender() != null) {
                                if (prof.getGender().equals("male")) {
                                    mMaleButton.setChecked(true);
                                    mFemaleButton.setChecked(false);
                                }
                                else {
                                    mFemaleButton.setChecked(true);
                                    mMaleButton.setChecked(false);
                                }
                            }
                            if(prof.getPhoto() != null && !prof.getPhoto().equals("null")) {
                                ImageLoader.getInstance().displayImage(prof.getPhoto(), mProfilePicture);

                            }
                        }
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    //Inner class to setup info
    private class SetUpTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() { }

        @Override
        protected Void doInBackground(Void... params) {
            setupInfo();

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) { }

        @Override
        protected void onPostExecute(Void result) {

        }
    }
}
