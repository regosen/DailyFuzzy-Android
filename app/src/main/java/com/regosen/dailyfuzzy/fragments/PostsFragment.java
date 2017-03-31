package com.regosen.dailyfuzzy.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.regosen.dailyfuzzy.R;
import com.regosen.dailyfuzzy.activities.PictureActivity;
import com.regosen.dailyfuzzy.models.Post;
import com.regosen.dailyfuzzy.models.PostFetcher;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static com.regosen.dailyfuzzy.models.PostFetcher.PostType.POST_FAVORITES;
import static com.regosen.dailyfuzzy.models.PostFetcher.PostType.POST_PAST;

/**
 * based on template code provided by Hathy
 */
public class PostsFragment extends Fragment{
         
    ListView postsList;
    ArrayAdapter<Post> adapter;
    Handler handler;

    PostFetcher.PostType postType = POST_PAST;
    List<Post> posts;
     
    public PostsFragment(){
        handler=new Handler();
        posts=new ArrayList<>();
    }    
     
    public static Fragment newInstance(PostFetcher.PostType postType){
        PostsFragment pf=new PostsFragment();
        pf.postType=postType;
        return pf;
    }

    private int TranslatePosition(int position)
    {
        int numPosts = (postType == POST_FAVORITES) ? PostFetcher.numFavorites() : PostFetcher.numPastPosts();
        return numPosts - position - 1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.posts
                                , container
                                , false);
        postsList=(ListView)v.findViewById(R.id.posts_list);
        postsList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
            {
                Intent it = new Intent(getContext(), PictureActivity.class);
                it.putExtra(PictureActivity.KEY_POST_POS, TranslatePosition(position));
                it.putExtra(PictureActivity.KEY_POST_TYPE, postType);
                startActivity(it);
            }
        });
        postsList.setEmptyView(v.findViewById(R.id.posts_empty));
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }
     
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {    
        super.onActivityCreated(savedInstanceState);
        initialize();
    }
     
    private void initialize(){

        if(posts.size()==0){
             
            new Thread(){
                public void run(){
                    posts.addAll(PostFetcher.getInstance().getPosts(postType));
                     
                    handler.post(new Runnable(){
                        public void run(){
                            createAdapter();
                        }
                    });
                }
            }.start();
        }else{
            createAdapter();
        }
    }

    private void createAdapter(){

        if(getActivity()==null) return;
         
        adapter=new ArrayAdapter<Post>(getActivity()
                                             ,R.layout.post_item
                                             , posts){
            @Override
            public View getView(int position,
                                View convertView,
                                ViewGroup parent) {
 
                if(convertView==null){
                    convertView=getActivity()
                                .getLayoutInflater()
                                .inflate(R.layout.post_item, null);
                }
 
                TextView postTitle;
                postTitle=(TextView)convertView
                          .findViewById(R.id.post_title);

                ImageView postThumb;
                postThumb=(ImageView)convertView
                            .findViewById(R.id.img_thumb);

                Post post = PostFetcher.getInstance().getPostAt(postType, TranslatePosition(position));
                if (post != null) {
                    postTitle.setText(post.title);
                    String thumbUrl = post.thumb;
                    Picasso.with(getActivity().getApplicationContext()).load(thumbUrl).placeholder(R.drawable.thumb_placeholder).into(postThumb);
                }
                return convertView;
            }
        };
        postsList.setAdapter(adapter);
    }
         
}