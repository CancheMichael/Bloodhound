/**
 * Author: Michael Canche
 * Dartmouth College, Spring 2020, Professor Campbell
 */
package edu.dartmouth.cs65.searchandrescue.code.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.dartmouth.cs65.searchandrescue.R;
import edu.dartmouth.cs65.searchandrescue.code.structures.MissingProfile;


public class UpdateMissingProfile extends AppCompatActivity {

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
    private StorageReference profilePicRef;
    private MissingProfile prof;
    private ImageLoaderConfiguration config;
    private String originalPerson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        createActionBar();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personaldetails);
        //Setup
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        Intent codePass = getIntent();
        activeCode = codePass.getStringExtra("current code");
        mNameField = findViewById(R.id.nameField);
        mEyeColor = findViewById(R.id.eyeColor);
        mAge = findViewById(R.id.age);
        mHeight = findViewById(R.id.Height);
        mHairColor = findViewById(R.id.hairColor);
        mComments = findViewById(R.id.comment);
        mChangeButton = findViewById(R.id.changeButton);

        mGenderGroup = findViewById(R.id.genderGroup);
        mMaleButton = findViewById(R.id.maleButton);
        mFemaleButton = findViewById(R.id.femaleButton);

        mProfilePicture = findViewById(R.id.profilePicture);
        //loadPreferences();
        //Get Camera and Storage Permissions
        askPermission();

        //Change profile picture
        mChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraOrGallery();
            }
        });

        //Save profile picture after rotation
        if(savedInstanceState != null) {
            Uri pic = savedInstanceState.getParcelable("profilePicture");
            if(pic != null) {
                masterUri = pic;
                mProfilePicture.setImageURI(masterUri);
            }
            //Save image path to stop crashing when rotating in cropping tool
            String imgString = savedInstanceState.getString("imagePath", "nan");
            if(imgString != null && !imgString.equalsIgnoreCase("nan")) {
                File imgFile = new File(Environment.getExternalStorageDirectory(), imgString);
                if (imgFile != null) {
                    imagePath = imgFile;

                }
            }
        }
        config = new ImageLoaderConfiguration.Builder(UpdateMissingProfile.this).build();
        if (!ImageLoader.getInstance().isInited()) {
            ImageLoader.getInstance().init(config);
        }
        setupInfo();
    }

    //Check if permissions were granted
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_CAMERA_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    cameraStoragePermissionsGranted = true;
                }
                return;
            }
        }
    }

    //Create action bar with back button
    private void createActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setTitle("Profile");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    //Add Save button to top right of action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //Action bar buttons
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //Return to home
            case android.R.id.home:
                Intent mainIntent = new Intent(UpdateMissingProfile.this, MainActivity.class);
                mainIntent.putExtra("current code", activeCode);
                startActivity(mainIntent);
                finish();
                return true;
            //Save profile
            case R.id.action_save:
                //Get info from fields
                String eyeColor;
                String age;
                String name;
                String height;
                String hairColor;
                String comments;
                if(mEyeColor.getText() != null)
                    eyeColor =  mEyeColor.getText().toString();
                else
                    eyeColor = null;
                if(mAge.getText() != null)
                    age = mAge.getText().toString();
                else
                    age = null;
                if(mNameField.getText() != null)
                    name = mNameField.getText().toString();
                else
                    name = null;
                if(mHeight.getText() != null)
                    height = mHeight.getText().toString();
                else
                    height = null;
                if(mHairColor.getText() != null)
                    hairColor = mHairColor.getText().toString();
                else
                    hairColor = null;
                if(mComments.getText() != null)
                    comments = mComments.getText().toString();
                else
                    comments = null;
                String gender = "";
                int genderID = mGenderGroup.getCheckedRadioButtonId();
                boolean validProfile = true;
                //Check if profile is valid
                if(name.equals("")) {
                    mNameField.setError("This field is required.");
                    validProfile = false;
                }
                if(genderID == -1) {
                    Toast.makeText(getApplicationContext(), "Gender is required",
                            Toast.LENGTH_LONG).show();
                    validProfile = false;
                }
                else if(genderID == R.id.femaleButton) {
                    gender = "female";
                }
                else if(genderID == R.id.maleButton) {
                    gender = "male";
                }
                if(validProfile) {
                    prof = new MissingProfile();
                    prof.setEyeColor(eyeColor);
                    prof.setAge(age);
                    prof.setName(name);
                    prof.setHeight(height);
                    prof.setHairColor(hairColor);
                    prof.setComment(comments);
                    prof.setGender(gender);
                    prof.setPhoto("null");
                    if(imagePath != null) {
                        //Recreate photo path
                        //This is to solve image disappearing on save when rotation occurs
                        try {
                            createPhotoPath();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //Repeat saving and setting processes
                        Bitmap bp = BitmapFactory.decodeFile(imagePath.getAbsolutePath());
                        mProfilePicture.setImageBitmap(bp);
                        //prof.setPhoto(masterUri.toString());

                        profilePicRef = mStorageRef.child("images/" + activeCode + ".jpg");
                        if (masterUri != null) {
                            Task uploadTask = profilePicRef.putFile(masterUri);
                            // Register observers to listen for when the download is done or if it fails
                            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    profilePicRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            path = uri.toString();
                                            prof.setPhoto(path);
                                            //Removes the existing person, as only one person is saved
                                            mDatabase.child("codes").child(activeCode).child(originalPerson).removeValue();
                                            mDatabase.child("codes").child(activeCode).child(prof.getName()).setValue(prof);
                                            Intent mainIntent = new Intent(UpdateMissingProfile.this,
                                                    MainActivity.class);
                                            mainIntent.putExtra("current code", activeCode);
                                            startActivity(mainIntent);
                                            finish();
                                        }
                                    });
                                }
                            });
                    }
                    }
                    else {
                        //Removes the existing person, as only one person is saved
                        mDatabase.child("codes").child(activeCode).child(originalPerson).removeValue();
                        mDatabase.child("codes").child(activeCode).child(prof.getName()).setValue(prof);
                        //Return to Main page
                        mainIntent = new Intent(UpdateMissingProfile.this,
                                MainActivity.class);
                        mainIntent.putExtra("current code", activeCode);
                        startActivity(mainIntent);
                        finish();
                    }



                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupInfo() {
        // Get a reference to our posts
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dataRef = database.getReference().child("codes").child(activeCode);

        if (!ImageLoader.getInstance().isInited()) {
            ImageLoader.getInstance().init(config);
        }

        //Get current profile data
        dataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MissingProfile prof = snapshot.getValue(MissingProfile.class);
                    if(prof != null) {
                        if(prof.getName() != null) {
                            originalPerson = prof.getName();
                            mNameField.setText(prof.getName());
                        }
                        if(prof.getAge() != null)
                            mAge.setText(prof.getAge());
                        if(prof.getEyeColor() != null)
                            mEyeColor.setText(prof.getEyeColor());
                        if(prof.getHeight() != null)
                            mHeight.setText(prof.getHeight());
                        if(prof.getHairColor() != null)
                            mHairColor.setText(prof.getHairColor());
                        if(prof.getComment() != null)
                            mComments.setText(prof.getComment());
                        if(prof.getGender() != null) {
                            if (prof.getGender().equals("male"))
                                mMaleButton.setChecked(true);
                            else
                                mFemaleButton.setChecked(true);
                        }
                        if(prof.getPhoto() != null && !prof.getPhoto().equals("null")) {
                            ImageLoader.getInstance().displayImage(prof.getPhoto(), mProfilePicture);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

    });
    }

    //Ask for camera and storage permissions
    private void askPermission() {
        //Check for camera and storage permission
        if (ContextCompat.checkSelfPermission(UpdateMissingProfile.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(UpdateMissingProfile.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            //Request permissions
            ActivityCompat.requestPermissions(this,
                    permissionsNeeded, PERMISSIONS_CAMERA_STORAGE);
        }
        else {
            //If permissions were granted
            cameraStoragePermissionsGranted = true;
        }
    }

    //Where to get picture from
    private void cameraOrGallery() {
        //Dialog for change button
        AlertDialog.Builder adBuilder = new AlertDialog.Builder(UpdateMissingProfile.this);
        adBuilder.setTitle("Profile Picture Picker");
        try {
            //Creates a new path for the new profile picture
            createPhotoPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Camera
        adBuilder.setPositiveButton(
                "Take from camera",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        if(cameraStoragePermissionsGranted) {
                            Intent camIntent = new Intent(
                                    MediaStore.ACTION_IMAGE_CAPTURE);
                            if(imagePath != null) {
                                //Uri to retrieve photo
                                Uri photoURI = FileProvider.getUriForFile(
                                        UpdateMissingProfile.this,
                                        "edu.dartmouth.cs65.searchandrescue.fileprovider",
                                        imagePath);
                                camIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                startActivityForResult(camIntent, FROM_CAMERA);
                            }
                        }
                        //No permissions
                        else {
                            askPermission();
                            if(cameraStoragePermissionsGranted) {
                                Intent camIntent = new Intent(
                                        MediaStore.ACTION_IMAGE_CAPTURE);
                                if(imagePath != null) {
                                    //Uri to retrieve photo
                                    Uri photoURI = FileProvider.getUriForFile(
                                            UpdateMissingProfile.this,
                                            "edu.dartmouth.cs65.searchandrescue.fileprovider",
                                            imagePath);
                                    camIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                    startActivityForResult(camIntent, FROM_CAMERA);
                                }
                            }
                        }
                    }
                }
        );

        //Storage
        adBuilder.setNegativeButton(
                "Select from gallery",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int arg1) {
                        if(cameraStoragePermissionsGranted) {
                            Intent stoIntent = new Intent(Intent.ACTION_PICK);
                            stoIntent.setType("image/*");
                            startActivityForResult(stoIntent, FROM_GALLERY);
                        }
                        //No permissions
                        else {
                            askPermission();
                            if(cameraStoragePermissionsGranted) {
                                Intent stoIntent = new Intent(Intent.ACTION_PICK);
                                stoIntent.setType("image/*");
                                startActivityForResult(stoIntent, FROM_GALLERY);
                            }
                        }
                    }
                }
        );
        adBuilder.show();
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if(reqCode == FROM_GALLERY) {
                Uri imageUri = data.getData();
                if(imageUri != null) {
                    cropImageUri(imageUri);
                }
            }
            else if(reqCode == FROM_CAMERA) {
                //Retrieve image from camera
                Uri imageUri = FileProvider.getUriForFile(this,
                        "edu.dartmouth.cs65.searchandrescue.fileprovider",
                        imagePath);
                if(imageUri != null) {
                    cropImageUri(imageUri);
                }
            }
            //After cropping, save profile picture
            else if(reqCode == Crop.REQUEST_CROP) {
                setProfilePic(data);
            }
        }
    }

    private void cropImageUri(Uri imageUri) {
        if(imagePath != null) {
            Uri croppedUri = FileProvider.getUriForFile(this,
                    "edu.dartmouth.cs65.searchandrescue.fileprovider",
                    imagePath);
            //Uri.fromFile(new File(getCacheDir(), "cropped"));
            Crop.of(imageUri, croppedUri).asSquare().start(this);
            //Master uri for rotation edge case
            masterUri = croppedUri;
        }
    }

    private void setProfilePic(Intent data) {
        mProfilePicture.setImageURI(Crop.getOutput(data));
    }

    private void createPhotoPath() throws IOException {
        String time = new SimpleDateFormat("S_smH_DMY").format(new Date());
        time += "_profilePicture_";
        File imageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(time, ".png", imageDir);
        imagePath = image;
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        //Save profile picture and imagePath for rotation edge case
        if(masterUri != null)
            outState.putParcelable("profilePicture", masterUri);
        if(imagePath != null)
            outState.putString("imagePath", imagePath.getAbsolutePath());
        super.onSaveInstanceState(outState);
    }
}
