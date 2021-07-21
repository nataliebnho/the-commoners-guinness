package com.example.the_commoners_guinness.ui.dashboard;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.database.Cursor;
import android.icu.util.Output;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;

import com.example.the_commoners_guinness.Category;
import com.example.the_commoners_guinness.SetLocationMapsActivity;
import com.example.the_commoners_guinness.Post;
import com.example.the_commoners_guinness.R;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Calendar;

public class CreateFragment extends Fragment {

    public final String APP_TAG = "CreateFragment";
    ImageButton btnTakeVideo;
    ImageView btnUpload;
    Button btnShare;
    VideoView vvVideoToPost;
    File mediaFile;
    EditText etCaption;
    EditText etCategory;
    Button btnAddLocation;
    ParseGeoPoint location;
    private static final int VIDEO_CAPTURE = 101;
    public static final int VIDEO_UPLOAD = 102;
    private static final int MAPS_REQUEST_CODE = 40;
    Context context;
    ParseFile parseFile;

    public CreateFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable
            Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnTakeVideo = view.findViewById(R.id.btnTakeVideoChallenge);
        btnUpload = view.findViewById(R.id.ivUpload);
        vvVideoToPost = view.findViewById(R.id.vvVideoToPostChallenge);
        etCaption = view.findViewById(R.id.etCaptionChallenge);
        etCategory = view.findViewById(R.id.etCategory);
        btnShare = view.findViewById(R.id.btnPostChallenge);
        btnAddLocation = view.findViewById(R.id.btnAddLocation);

        btnTakeVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecordingVideo();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("video/*");
                startActivityForResult(i, VIDEO_UPLOAD);
            }
        });

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String caption = etCaption.getText().toString();
                String category = etCategory.getText().toString();

                savePost(ParseUser.getCurrentUser(), caption, category);
                etCaption.setText("");
                etCategory.setText("");
                vvVideoToPost.setBackgroundResource(0);
            }
        });

        btnAddLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), SetLocationMapsActivity.class);
                startActivityForResult(i, MAPS_REQUEST_CODE);
            }
        });
    }

    public void startRecordingVideo() {
        if (getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            //mediaFile = getPhotoFileUri(photoFileName);

            mediaFile = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES), "share_image_" + System.currentTimeMillis() + ".mp4");

            // wrap File object into a content provider. NOTE: authority here should match authority in manifest declaration
            Uri fileProvider = FileProvider.getUriForFile(getActivity(), "com.codepath.fileprovider.the-commoners-guinness", mediaFile);
            Log.i("CAMERA: ", fileProvider.toString());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
            startActivityForResult(intent, VIDEO_CAPTURE);

        } else {
            Toast.makeText(getContext(), "No camera on device", Toast.LENGTH_LONG).show();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        VideoView videoView = getView().findViewById(R.id.vvVideoToPostChallenge);
        if (requestCode == VIDEO_CAPTURE) {
            if (resultCode == getActivity().RESULT_OK) {
                videoView.setVideoURI(data.getData());
                videoView.setMediaController(new MediaController(getContext()));
                videoView.requestFocus();
                videoView.start();
            } else if (resultCode == getActivity().RESULT_CANCELED) {
                Toast.makeText(getContext(), "Video recording cancelled.",  Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), "Failed to record video",  Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == MAPS_REQUEST_CODE) {
            if (resultCode == getActivity().RESULT_OK) {
                location = data.getParcelableExtra("Location");
                Log.i("Location", location.toString());
            }
        }
        if (requestCode == VIDEO_UPLOAD) {
            if (resultCode == getActivity().RESULT_OK) {
               if (data != null) {
                   Uri uri = data.getData();
                   Toast.makeText(getContext(), "Video content URI: " + data.getData(), Toast.LENGTH_SHORT).show();
                   createDocumentFileFromFile(uri);
               }
            }
        }
    }

    private void createDocumentFileFromFile(Uri uri) {
        mediaFile = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES), "share_image_" + System.currentTimeMillis() + ".mp4");

        Uri outputUri =  FileProvider.getUriForFile(getActivity(), "com.codepath.fileprovider.the-commoners-guinness", mediaFile);

        try {
            InputStream in = getContext().getContentResolver().openInputStream(uri);
            OutputStream out = getContext().getContentResolver().openOutputStream(outputUri);
            try {
                int nbOfBytes = 0;
                final int BLOCKSIZE = 4096;
                byte[] bytesRead = new byte[BLOCKSIZE];
                while (true) {
                    nbOfBytes = in.read(bytesRead);
                    if (nbOfBytes == -1) {
                        break;
                    }
                    out.write(bytesRead, 0, nbOfBytes);
                }
            } finally {
                in.close();
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveVideoToInternalStorage (String filePath) {

        File newfile;

        try {
//            Uri fileProvider = FileProvider.getUriForFile(getActivity(), "com.codepath.fileprovider.the-commoners-guinness", mediaFile);


            File currentFile = new File(filePath);
            File wallpaperDirectory = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES), "share_image_" + System.currentTimeMillis() + ".mp4");
            newfile = new File(wallpaperDirectory, Calendar.getInstance().getTimeInMillis() + ".mp4");

            if (!wallpaperDirectory.exists()) {
                wallpaperDirectory.mkdirs();
            }

            if(currentFile.exists()){

                InputStream in = new FileInputStream(currentFile);
                OutputStream out = new FileOutputStream(newfile);

                // Copy the bits from instream to outstream
                byte[] buf = new byte[1024];
                int len;

                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
                Log.v("vii", "Video file saved successfully.");
            } else{
                Log.v("vii", "Video saving failed. Source file missing.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String getRealPathFromURI(Uri contentURI) {
//        String result;
//        String[] filePathCol = {MediaStore.Video.VideoColumns.DATA};
//        Cursor cursor = getContext().getContentResolver().query(contentURI, filePathCol, null, null, null);
//        if (cursor == null) { // Source is Dropbox or other similar local file path
//            result = contentURI.getPath();
//        } else {
//            cursor.moveToFirst();
//            int idx = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA);
//            result = cursor.getString(idx);
//            cursor.close();
//        }
//        return result;

        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = getContext().getContentResolver().query(contentURI, projection, null, null, null);
        if (cursor != null) {
            Log.i("CURSOR", "cursor is not null");
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else {
            Log.i("CURSOR", "cursor is null");
            return null;
        }

    }

    private void savePost(ParseUser currentUser, String caption, String categoryName) {
        Post post = new Post();

        post.setCaption(caption);

        Category category = new Category();
        category.setName(categoryName);
        post.setCategory(category);
        if (location != null) {
            post.setLocation(location);
        }
        post.setVideo(new ParseFile(mediaFile));
        //post.setVideo(parseFile);
        post.setUser(currentUser);

        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(APP_TAG, "Error while saving", e);
                    Toast.makeText(getContext(), "Error while saving!", Toast.LENGTH_SHORT).show();
                }
                Log.i(APP_TAG, "Post save was successful!");
            }
        });
    }

}