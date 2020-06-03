/**
 * Author: Michael Canche
 * Dartmouth College, Spring 2020, Professor Campbell
 */
package edu.dartmouth.cs65.searchandrescue.code.fragments;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import edu.dartmouth.cs65.searchandrescue.R;
import edu.dartmouth.cs65.searchandrescue.code.activities.PartyActivity;
import edu.dartmouth.cs65.searchandrescue.code.services.TrackingService;
import edu.dartmouth.cs65.searchandrescue.code.structures.MissingProfile;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private View view;
    private Location curLocation;
    private ArrayList<LatLng> mLocationList;
    private int zoomLevel = 18;
    private boolean mLocationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private Intent serviceIntent;
    private boolean firstUpdate = true;
    private DatabaseReference mMapLinesRef;
    private String activeCode;
    private String instanceID = new Random().nextInt()+"";
    private DatabaseReference mDatabase;
    private ArrayList<Marker> curMarkers;
    private String[] options = {"Last Seen Here", "Search Area", "Area Clear", "Lives Here", "Revisit Here",
            "Contact Here"};

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Setup
        getActivity().setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        curMarkers = new ArrayList<>();
        mMap = null;
        if (view == null)
            view = inflater.inflate(R.layout.fragment_maps, container, false);

        Intent codePass = getActivity().getIntent();
        activeCode = codePass.getStringExtra("current code");
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mMapLinesRef = FirebaseDatabase.getInstance().getReference().child("codes").child(activeCode).child("Map Data");

        while(!mLocationPermissionGranted)
            getLocationPermission();
        mLocationList = new ArrayList<>();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        //Create map if location permissions are granted
        if (mLocationPermissionGranted) {
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }

        return view;

    }



    //When GoogleMap is ready to be used
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Store GoogleMap
        mMap = googleMap;
        if (mMap != null) {
            if(isAdded()) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.dark_map));
                        mMap.setMyLocationEnabled(true);
                        mMap.getUiSettings().setMyLocationButtonEnabled(true);
                        firstUpdate = true;
                        setupMapListener();

                    }
                });
            }
                new SetUpTask().execute();
        }
    }


    //BroadcastReceiver for updating locations
    BroadcastReceiver mLocationBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(TrackingService.BROADCAST_LOCATION)) {
                Location location = intent.getParcelableExtra("location");
                //Setup first location
                if (firstUpdate) {
                    if (location != null) {
                        setStartLocation(location);
                        firstUpdate = false;
                    }
                }
                //Update path when more than one location is stored
                if (!firstUpdate) {
                    updatePath(location);
                    if(mLocationList != null) {
                        StringBuilder sb = new StringBuilder();
                        for (LatLng s : mLocationList)
                        {
                            sb.append(s.latitude + "," + s.longitude);
                            sb.append(" ");
                        }
                        mDatabase.child("codes").child(activeCode).child("Map Data").child(instanceID).setValue(sb.toString());
                    }

                }
                curLocation = location;
            }
        }
    };

    //Sets a marker and adjusts camera for the starting location
    public void setStartLocation(Location location) {
        if (mMap != null) {
            if (location != null)
                curLocation = location;
            LatLng startLatLng = new LatLng(curLocation.getLatitude(),
                    curLocation.getLongitude());
            if (startLatLng != null) {
                mLocationList.add(startLatLng);
                CameraPosition myPosition = new CameraPosition.Builder()
                        .target(startLatLng).zoom(zoomLevel).build();
                mMap.addMarker(new MarkerOptions().position(startLatLng)
                        .title("Starting Location"));
                //mMap.animateCamera(CameraUpdateFactory.newCameraPosition(myPosition));
            }
        }
    }

    //Request location permissions from user
    private void getLocationPermission() {
        //If we have permission, track this
        if (ContextCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        }
        //If we do not have permission, ask for it
        else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    //Ask for permission
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                //Continue if we get permissions
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
                //If no permissions, return to MainActivity
                else {
                    if(isAdded()) {
                    Intent noPermsIntent = new Intent(getActivity(),
                            PartyActivity.class);
                    noPermsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    noPermsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(noPermsIntent);
                    }
                }
            }
        }
    }

    //Update the polyline path with a new location
    private void updatePath(Location location) {
        LatLng newLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        mLocationList.add(newLatLng);
        Polyline polyline = mMap.addPolyline(new PolylineOptions()
                .color(Color.WHITE)
                .add(
                        new LatLng(curLocation.getLatitude(), curLocation.getLongitude()),
                        new LatLng(location.getLatitude(), location.getLongitude())));

    }

    //Overflowed method, update the polyline path with an ArrayList of latlngs
    //Also adds marker to start and end
    private void updatePath(ArrayList<LatLng> latlngList) {
        //Start and end markers
        Polyline polyline = mMap.addPolyline(new PolylineOptions()
                .color(Color.WHITE)
                .addAll(latlngList));
    }


    //Start foreground service to track location changes
    //Also TrackingService also handles setting up activity recognition if needed
    private void startTrackingService() {
        if(isAdded()) {
            serviceIntent = new Intent(getActivity(), TrackingService.class);
            Bundle bundle = getActivity().getIntent().getExtras();
            getActivity().startForegroundService(serviceIntent);
        }
    }

    private void checkDatabase() {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            String ll;
            MarkerOptions mO;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(curMarkers.size() > 0) {
                    for (Marker marker : curMarkers)
                        marker.remove();
                    curMarkers.clear();
                }

                for (DataSnapshot zoneSnapshot: dataSnapshot.child("codes").child(activeCode).child("Map Markers").getChildren()) {
                    String m = zoneSnapshot.getValue(String.class);
                    String[] latlng = m.split(" ");
                    double latitude = Double.parseDouble(latlng[0]);
                    double longitude = Double.parseDouble(latlng[1]);
                    LatLng location = new LatLng(latitude, longitude);
                    mO = new MarkerOptions()
                            .title(options[Integer.parseInt(latlng[2])])
                            .snippet(latlng[3] + " " + latlng[4])
                            .position(location);
                    switch(Integer.parseInt(latlng[2])) {
                        case 0:
                            mO.icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_RED));
                            break;
                        case 1:
                            mO.icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                            break;
                        case 2:
                            mO.icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                            break;
                        case 3:
                            mO.icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                            break;
                        case 4:
                            mO.icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                            break;
                        case 5:
                            mO.icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
                            break;
                    }
                    if(isAdded()) {
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                curMarkers.add(mMap.addMarker(mO));
                            }
                        });
                    }
                }
                for (DataSnapshot zoneSnapshot: dataSnapshot.child("codes").child(activeCode).child("Map Data").getChildren()) {
                    ll = zoneSnapshot.getValue(String.class);
                    if(isAdded()) {
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                updatePath(new MissingProfile().createLatLngFromString(ll));
                            }
                        });
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
        getActivity().stopService(new Intent(getActivity(), TrackingService.class));
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void setupMapListener() {
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng point) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Marker Meaning");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm");
                        mDatabase.child("codes").child(activeCode).child("Map Markers").push().setValue(point.latitude + " " + point.longitude + " " + which + " " + sdf.format(Calendar.getInstance().getTime()));
                        new checkDatabaseTask().execute();

                    }
                });
                builder.show();
            }
        });
    }

    //Inner class to setup map data
    private class SetUpTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() { }

        @Override
        protected Void doInBackground(Void... params) {
            startTrackingService();
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(mLocationBroadcastReceiver,
                    new IntentFilter(TrackingService.BROADCAST_LOCATION));
            checkDatabase();
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) { }

        @Override
        protected void onPostExecute(Void result) {

        }
    }

    //Inner class to setup map data
    private class checkDatabaseTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() { }

        @Override
        protected Void doInBackground(Void... params) {
            checkDatabase();
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) { }

        @Override
        protected void onPostExecute(Void result) {

        }
    }
}
