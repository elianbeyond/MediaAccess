package comp5216.sydney.edu.au.mediaaccess;

import static android.content.ContentValues.TAG;

import static java.lang.Thread.sleep;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    //request codes
    private static final int MY_PERMISSIONS_REQUEST_OPEN_CAMERA = 101;
    private static final int MY_PERMISSIONS_REQUEST_READ_PHOTOS = 102;
    private static final int MY_PERMISSIONS_REQUEST_RECORD_VIDEO = 103;
    private static final int MY_PERMISSIONS_REQUEST_READ_VIDEOS = 104;
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 105;
    public final String APP_TAG = "MobileComputingTutorial";
    public String photoFileName = "photo.jpg";
    public String videoFileName = "video.mp4";
    public String audioFileName = "audio.3gp";
    MarshmallowPermission marshmallowPermission = new MarshmallowPermission(this);

    private File file;

    private FirebaseAuth mAuth;

    private FirebaseFirestore mFirestore;

    private FirebaseStorage storage = FirebaseStorage.getInstance();


    private final MediaRecorder recorder = null;
    private final MediaPlayer player = null;
    private GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_week05);

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword("814846181@qq.com", "123456")
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });
        if (mMap==null){
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }


    public void onLoadPhotoClick(View view) {

        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // Bring up gallery to select a photo
        startActivityForResult(intent, MY_PERMISSIONS_REQUEST_READ_PHOTOS);

    }

    public void onLoadVideoClick(View view) {

        // Create intent for picking a video from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);

        // Bring up gallery to select a video
        startActivityForResult(intent, MY_PERMISSIONS_REQUEST_READ_VIDEOS);

    }

    public void onTakePhotoClick(View v) {
        // Check permissions
        if (!marshmallowPermission.checkPermissionForCamera()) {
            marshmallowPermission.requestPermissionForCamera();
        } else {
            // create Intent to take a picture and return control to the calling application
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            // set file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            photoFileName = "IMG_" + timeStamp + ".jpg";

            // Create a photo file reference
            Uri file_uri = getFileUri(photoFileName, 0);

            // Add extended data to the intent
            intent.putExtra(MediaStore.EXTRA_OUTPUT, file_uri);

            // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
            // So as long as the result is not null, it's safe to use the intent.
            if (intent.resolveActivity(getPackageManager()) != null) {
                // Start the image capture intent to take photo
                startActivityForResult(intent, MY_PERMISSIONS_REQUEST_OPEN_CAMERA);
            }
        }
    }

    public void onRecordVideoClick(View v) {
        // Check permissions
        if (!marshmallowPermission.checkPermissionForCamera()) {
            marshmallowPermission.requestPermissionForCamera();
        } else {
            // create Intent to capture a video and return control to the calling application
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

            // set file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            videoFileName = "VIDEO_" + timeStamp + ".mp4";

            // Create a video file reference
            Uri file_uri = getFileUri(videoFileName, 1);

            // add extended data to the intent
            intent.putExtra(MediaStore.EXTRA_OUTPUT, file_uri);

            // Start the video record intent to capture video
            startActivityForResult(intent, MY_PERMISSIONS_REQUEST_RECORD_VIDEO);


        }
    }

    // Returns the Uri for a photo/media stored on disk given the fileName
    public Uri getFileUri(String fileName) {
        // Get safe storage directory for photos
        File mediaStorageDir = new File(getExternalFilesDir(Environment.getExternalStorageDirectory().toString()), APP_TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(APP_TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        return Uri.fromFile(new File(mediaStorageDir.getPath() + File.separator + fileName));
    }

    private void scanFile(String path) {

        MediaScannerConnection.scanFile(MainActivity.this,
                new String[]{path}, null,
                new MediaScannerConnection.OnScanCompletedListener() {

                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("TAG", "Finished scanning " + path);
                    }
                });
    }

    // Returns the Uri for a photo/media stored on disk given the fileName and type
    public Uri getFileUri(String fileName, int type) {
        Uri fileUri = null;
        try {
            String typestr = "images"; //default to images type
            if (type == 1) {
                typestr = "videos";
            } else if (type != 0) {
                typestr = "audios";
            }

            File mediaStorageDir = new File(getExternalMediaDirs()[0], APP_TAG);

            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
                Log.d(APP_TAG, "failed to create directory");
            }

            // Create the file target for the media based on filename
            file = new File(mediaStorageDir, fileName);

            // Wrap File object into a content provider, required for API >= 24
            // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
            if (Build.VERSION.SDK_INT >= 24) {
                fileUri = FileProvider.getUriForFile(this.getApplicationContext(), "comp5216.sydney.edu.au.mediaaccess.fileProvider", file);
            } else {
                fileUri = Uri.fromFile(mediaStorageDir);
            }
        } catch (Exception ex) {
            Log.e("getFileUri", ex.getStackTrace().toString());
        }
        return fileUri;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        final VideoView mVideoView = findViewById(R.id.videoview);
        ImageView ivPreview = findViewById(R.id.photopreview);

        mVideoView.setVisibility(View.GONE);
        ivPreview.setVisibility(View.GONE);

        if (requestCode == MY_PERMISSIONS_REQUEST_OPEN_CAMERA) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(file.getAbsolutePath());
                scanFile(file.getAbsolutePath());
                ivPreview.setImageBitmap(takenImage);
                ivPreview.setVisibility(View.VISIBLE);
                //文件传输的方式 上传图片到Storage

                StorageReference storageRef = storage.getReference();
                StorageReference imagesRef = storageRef.child(file.getName());
                Log.d(TAG, "file.getPath" + file.getPath());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                takenImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imagesData = baos.toByteArray();

                UploadTask uploadTask = imagesRef.putBytes(imagesData);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                        // ...
                    }
                });


            } else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == MY_PERMISSIONS_REQUEST_READ_PHOTOS) {
            if (resultCode == RESULT_OK) {
                Uri photoUri = data.getData();
                // Do something with the photo based on Uri
                Bitmap selectedImage;
                try {
                    selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);

                    // Load the selected image into a preview
                    ivPreview.setImageBitmap(selectedImage);
                    ivPreview.setVisibility(View.VISIBLE);
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } else if (requestCode == MY_PERMISSIONS_REQUEST_READ_VIDEOS) {
            if (resultCode == RESULT_OK) {
                Uri videoUri = data.getData();
                mVideoView.setVisibility(View.VISIBLE);
                mVideoView.setVideoURI(videoUri);
                mVideoView.requestFocus();
                mVideoView.setOnPreparedListener(new OnPreparedListener() {
                    // Close the progress bar and play the video
                    public void onPrepared(MediaPlayer mp) {
                        mVideoView.start();
                    }
                });
            }
        } else if (requestCode == MY_PERMISSIONS_REQUEST_RECORD_VIDEO) {
            scanFile(file.getAbsolutePath());

            byte[] byteArray = null;

            try {
                Path path = Paths.get(file.getAbsolutePath());
                byteArray = Files.readAllBytes(path);
            } catch (IOException e) {
                e.printStackTrace();
            }




            Log.d(TAG, "file.getAbsolutePath：" + file.getAbsolutePath());


            //if you are running on emulator remove the if statement
            if (resultCode != RESULT_OK) {
                Uri takenVideoUri = getFileUri(videoFileName, 1);
                mVideoView.setVisibility(View.VISIBLE);
                mVideoView.setVideoURI(takenVideoUri);
                mVideoView.requestFocus();
                mVideoView.setOnPreparedListener(new OnPreparedListener() {
                    // Close the progress bar and play the video
                    public void onPrepared(MediaPlayer mp) {
                        mVideoView.start();
                    }
                });



            }

            //文件传输的方式 上传视频到Storage
            if(byteArray!=null){
                StorageReference storageRef = storage.getReference();
                StorageReference videosRef = storageRef.child(file.getName());
                Log.d(TAG, "file.getPath" + file.getPath());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                UploadTask uploadTask = videosRef.putBytes(byteArray);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                        // ...
                    }
                });
            }


        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (mMap != null) {
            // Map is ready
            Toast.makeText(this, "Map is ready to be used!", Toast.LENGTH_SHORT).show();

            // Add a marker in Sydney and move the camera
            LatLng sydney = new LatLng(-33.8692, 151.2089);
            mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

            // Enable zoom controls
            mMap.getUiSettings().setZoomControlsEnabled(true);
            // Animate the camera to zoom into the map
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 11));
        } else {
            Toast.makeText(this, "Error - Map was null!", Toast.LENGTH_SHORT).show();
        }
    }


}
