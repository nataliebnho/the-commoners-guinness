package com.example.the_commoners_guinness.ui.dashboard;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.icu.util.Output;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;

import com.example.the_commoners_guinness.Category;
import com.example.the_commoners_guinness.SetLocationMapsActivity;
import com.example.the_commoners_guinness.Post;
import com.example.the_commoners_guinness.R;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CreateFragment extends Fragment {

    public final String TAG = "CreateFragment";
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
    Category category;
    AutoCompleteTextView actv;
    List<String> categoryNames;
    List<Category> categoryObjects = new ArrayList<>();
    long votingPeriodMillis = 480000;

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
        //etCategory = view.findViewById(R.id.etCategory);
        btnShare = view.findViewById(R.id.btnPostChallenge);
        btnAddLocation = view.findViewById(R.id.btnAddLocation);
        actv = view.findViewById(R.id.autoCompleteTextView);

        categoryNames = queryCategories();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (getActivity(), android.R.layout.select_dialog_item, categoryNames);

        //Getting the instance of AutoCompleteTextView
        actv.setThreshold(1);//will start working from first character
        actv.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
        actv.setTextColor(Color.RED);


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
                String categoryName = actv.getText().toString();

                Log.i("Here: ", String.valueOf(categoryNames.isEmpty()));

                if (categoryNames.contains(categoryName)) {
                    int index = categoryNames.indexOf(categoryName);
                    savePostChallenge(ParseUser.getCurrentUser(), caption, categoryObjects.get(index));
                } else {
                    savePost(ParseUser.getCurrentUser(), caption, categoryName);
                }
                etCaption.setText("");
               // etCategory.setText("");
                actv.setText("");
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
        post.setUser(currentUser);

        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error while saving", e);
                    Toast.makeText(getContext(), "Error while saving!", Toast.LENGTH_SHORT).show();
                }
                Log.i(TAG, "Post save was successful!");
                if (category.getFirstChallengePost() == null) {
                    Log.i("Category", category.getName());
                    Log.i("Is post null: ", String.valueOf(post.getObjectId()));
                    category.setFirstChallengePost(post);
                    category.saveInBackground();
                }
            }
        });
    }

    private void savePostChallenge(ParseUser currentUser, String caption, Category categoryObj) {
        Post post = new Post();
        post.setCaption(caption);
        post.setCategory(categoryObj);

        if (location != null) {
            post.setLocation(location);
        }

        post.setVideo(new ParseFile(mediaFile));
        post.setUser(currentUser);

        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error while saving", e);
                    Toast.makeText(getContext(), "Error while saving!", Toast.LENGTH_SHORT).show();
                }
                Log.i(TAG, "Post save was successful!");
                if (categoryObj.getFirstChallengePost() == null || category.getFirstChallengePost().getCreatedAt().getTime() > votingPeriodMillis) {
                    Log.i("Category", categoryObj.getName());
                    Log.i("Is post null: ", String.valueOf(post.getObjectId()));
                    categoryObj.setFirstChallengePost(post);
                    categoryObj.saveInBackground();
                }
            }

        });
        // if the category's firstChallengePost field is null, then set the firstChallengePost to this post

    }

    private List<String> queryCategories() {
        List<String> categoryNames = new ArrayList<>();
        ParseQuery<Category> query = ParseQuery.getQuery(Category.class);
        query.findInBackground(new FindCallback<Category>() {
            @Override
            public void done(List<Category> categories, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with retrieving categories", e);
                }
                for (Category category: categories) {
                    categoryNames.add(category.getName());
                    categoryObjects.add(category);
                }
            }
        });

        return categoryNames;
    }


}

