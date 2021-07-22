package com.example.the_commoners_guinness.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.the_commoners_guinness.Category;
import com.example.the_commoners_guinness.LoginActivity;
import com.example.the_commoners_guinness.Post;
import com.example.the_commoners_guinness.R;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    Button btnLogout;
    RecyclerView rvPosts;
    SwipeRefreshLayout swipeContainer;

    protected PostsAdapter adapter;
    protected List<Post> allPosts;
    public static final String TAG = "HomeFragment";

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvPosts = view.findViewById(R.id.rvPosts);
        allPosts = new ArrayList<>();
        adapter = new PostsAdapter(getContext(), allPosts);

        rvPosts.setAdapter(adapter);
        rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));

        configureSwipeContainer(view);
        queryPosts();
        //setCategoryVoteStatus();

        btnLogout = view.findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser.logOut();
                startActivity(new Intent(getContext(), LoginActivity.class));
                getActivity().finish();
            }
        });

        //setCategoryTime(allPosts);
    }

    private void configureSwipeContainer(View view) {
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchTimelineAsync(0);
            }
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    private void fetchTimelineAsync (int i) {
        adapter.clear();
        queryPosts();
      //  setCategoryVoteStatus();
        swipeContainer.setRefreshing(false);
    }

    private void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.setLimit(20);
        query.addDescendingOrder("createdAt");

        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with retrieving posts", e);
                }
                for (Post post: posts) {
                    Log.i(TAG, "Post: " + post.getCaption() + ", username: " + post.getUser().getUsername());
                }
                allPosts.addAll(posts);
                adapter.notifyDataSetChanged();
            }
        });

    }

    private void queryFirstChallengePost() {
        ParseQuery<Category> query = ParseQuery.getQuery(Category.class);

        query.findInBackground(new FindCallback<Category>() {
            @Override
            public void done(List<Category> categories, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with retrieving posts", e);
                }
                for (Category category : categories) {
                    if (category.getObjectId().equals("pYTi1iMX4u")) {
                        category.remove("firstChallengePost");
                        category.saveInBackground();
                    }
                }
            }
        });
    }


//    private void setCategoryVoteStatus() {
//        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
//        query.include(Post.KEY_USER);
//        query.addDescendingOrder("createdAt");
//
//        ArrayList<String> seen = new ArrayList<String>();
//
//        query.findInBackground(new FindCallback<Post>() {
//            @Override
//            public void done(List<Post> posts, ParseException e) {
//                if (e != null) {
//                    Log.e(TAG, "Issue with setting category status", e);
//                }
//                for (Post post: posts) {
//                    Category category = post.getCategory();
//                    try {
//                        category.fetchIfNeeded();
//                        if (seen.contains(category.getName())){
//                            continue;
//                        }
//                    } catch (ParseException parseException) {
//                        parseException.printStackTrace();
//                    }
//                    if (System.currentTimeMillis() - post.getCreatedAt().getTime() < 600000) {
//                        Log.i("Here", "here");
//                        category.setVotingPeriod(true);
//                        category.setVotingPeriodTime(600000 - (System.currentTimeMillis() - post.getCreatedAt().getTime()));
//
//                        try {
//                            category.fetchIfNeeded();
//                            seen.add(category.getName());
//                        } catch (ParseException parseException) {
//                            parseException.printStackTrace();
//                        }
//                    } else {
//                        category.setVotingPeriod(false);
//                    }
//                    category.saveInBackground();
//                }
//            }
//        });
//
//    }
//    private void setCategoryVoteStatus() {
//        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
//        query.include(Post.KEY_USER);
//        query.addDescendingOrder("createdAt");
//
//        ArrayList<Category> seen = new ArrayList<Category>();
//
//        query.findInBackground(new FindCallback<Post>() {
//            @Override
//            public void done(List<Post> posts, ParseException e) {
//                if (e != null) {
//                    Log.e(TAG, "Issue with setting category status", e);
//                }
//                for (Post post: posts) {
//                    Category category = post.getCategory();
//                    if (seen.contains(category)){
//                        Log.i(TAG, seen.toString());
//                        continue;
//                    }
//                    if (System.currentTimeMillis() - post.getCreatedAt().getTime() < 600000) {
//                        category.setVotingPeriod(true);
//                        category.setVotingPeriodTime(600000 - (System.currentTimeMillis() - post.getCreatedAt().getTime()));
//                        seen.add(category);
//                    } else {
//                        category.setVotingPeriod(false);
//                    }
//                    category.saveInBackground();
//                }
//            }
//        });
//
//    }

//    private void setCategoryVoteStatus() {
//        ParseQuery<Category> query = ParseQuery.getQuery(Category.class);
//        query.addDescendingOrder("createdAt");
//
//        ArrayList<Category> seen = new ArrayList<Category>();
//
//        query.findInBackground(new FindCallback<Category>() {
//            @Override
//            public void done(List<Category> categories, ParseException e) {
//                for (Category category: categories) {
//                    if (System.currentTimeMillis() - category.getFirstChallengePost().getCreatedAt().getTime() > 84000000) {
//
//                    }
//                }
//            }
//        });
//
//    }




    private void setCategoryTime(List<Post> posts) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                for (Post post: posts) {
                    Category category = post.getCategory();
                    try {
                        Boolean votingPeriod = ((Category) category.fetchIfNeeded()).getVotingPeriod();
                        if (votingPeriod) {
                            continue;
                        } else {
                            category.getVotingPeriodTime();
                            category.setVotingPeriodTime(category.getVotingPeriodTime() - 1000);
                            category.saveInBackground();
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 1000);
    }


}