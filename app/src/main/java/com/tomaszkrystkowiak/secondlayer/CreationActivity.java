package com.tomaszkrystkowiak.secondlayer;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.ArrayList;
import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

public class CreationActivity extends AppCompatActivity {

    private static final String TAG = CreationActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;
    private final static int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private ArFragment arFragment;
    private ViewRenderable boardRenderable;
    private Button addButton;
    private Button saveButton;
    private TextView textView;
    private String boardTitle = "";
    private AppDatabase db;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationCallback mLocationCallback;
    private Location mLastKnownLocation;
    private boolean mLocationPermissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }
        setContentView(R.layout.activity_creation);
        while (!mLocationPermissionGranted) {
            getLocationPermission();
        }
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Log.i(TAG, "noResult");
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    Log.i(TAG, "Location result");
                    mLastKnownLocation = location;
                    Log.i(TAG, mLastKnownLocation.toString());
                }
            }
        };
        startLocationUpdates();
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.board_fragment);
        ViewRenderable.builder()
                .setView(this, R.layout.board)
                .build()
                .thenAccept(renderable -> boardRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load board renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 50);
                            toast.show();
                            return null;
                        });

        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    if (boardRenderable == null) {
                        return;
                    }
                    Anchor anchor = hitResult.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());

                    TransformableNode board = new TransformableNode(arFragment.getTransformationSystem());
                    board.setParent(anchorNode);
                    board.setRenderable(boardRenderable);
                    board.select();

                    addButton = boardRenderable.getView().findViewById(R.id.button_add);
                    addButton.setOnClickListener(new AddButtonClick());
                    saveButton = boardRenderable.getView().findViewById(R.id.button_save);
                    saveButton.setOnClickListener(new SaveButtonClick());
                    textView = boardRenderable.getView().findViewById(R.id.title_textView);
                    textView.setText(boardTitle);

                    arFragment.setOnTapArPlaneListener(null);

                });
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "boards").build();
    }

    protected void startLocationUpdates() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "You need to enable permissions to display location !", Toast.LENGTH_LONG).show();
        }

        Log.i(TAG, "Starting updates");
        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null);
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permissions granted");
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            Log.i(TAG, "Requesting permissions");
        }
    }

    private void stopLocationUpdates() {
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdates();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later");
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
            activity.finish();
            return false;
        }
        String openGlVersionString =
                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }
        return true;
    }

    public void startNewMessageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter the title");


        final EditText input = new EditText(this);

        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            boardTitle = input.getText().toString();
            textView.setText(boardTitle);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    public void startSaveDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Save the board?");
        builder.setMessage("The board will be saved.");
        builder.setPositiveButton("OK", (dialog, which) -> saveBoard());
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    public void saveBoard() {
        DbBoardSavingAsyncTask dbBoardSavingAsyncTask = new DbBoardSavingAsyncTask();
        dbBoardSavingAsyncTask.execute();
    }

    public Board prepareBoardToSave(){
        Board toSave = new Board();
        toSave.creator = getResources().getString(R.string.user_name);
        toSave.title = boardTitle;
        toSave.location = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
        toSave.date = Calendar.getInstance().getTime();
        toSave.messages = new ArrayList<>();
        return toSave;
    }

    private class AddButtonClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            startNewMessageDialog();
        }
    }

    private class SaveButtonClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            startSaveDialog();
        }
    }

    private class DbBoardSavingAsyncTask extends AsyncTask<Void, Void, Board> {


        @Override
        protected Board doInBackground(Void...voids) {

            Board toSave = prepareBoardToSave();
            db.boardDao().insert(toSave);
            return toSave;

        }

    }

}
