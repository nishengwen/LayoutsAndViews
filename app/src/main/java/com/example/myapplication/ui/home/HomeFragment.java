package com.example.myapplication.ui.home;

import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.widget.CommonphrasesAdapter;
import com.example.myapplication.widget.HeartView;
import com.example.myapplication.widget.IMConversation;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

public class HomeFragment extends Fragment {
    private HomeViewModel homeViewModel;
    private WeakHashMap<String, SVGADrawable> drawablesMap=new WeakHashMap<>();
    private LinkedList<SoftReference<SVGAImageView>> views=new LinkedList<>();
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        return root;
    }
    String[] animations;
    List<String> animationsList;
     SVGAParser parser;
    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView=view.findViewById(R.id.buy_list);
        LinearLayoutManager llm = new LinearLayoutManager(this.getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        CommonphrasesAdapter adapter=new CommonphrasesAdapter(getActivity());
        recyclerView.setAdapter(adapter);

        //TODO 不知道消息顺序怎样 先写个比较器
        Comparator conversationComparator=new Comparator<IMConversation>() {
            @Override
            public int compare(IMConversation o1, IMConversation o2) {
                if(o1.name.contains("系统消息")){
                    return -1;
                }else if(o2.name.contains("系统消息")){
                    return 1;
                }else{
                    return o1.time>o2.time?-1:1;
                }
            }
        };

        ArrayList<IMConversation> data=new ArrayList<>();
        for(int i=0;i<30;i++){
            IMConversation imConversation=new IMConversation();
            imConversation.time=i;
            imConversation.name=new Random().nextInt()+"   "+i;
            if(i==14||i==10){
                imConversation.name="系统消息1";


            }
            data.add(imConversation);
        }
        Collections.sort(data,conversationComparator);
        adapter.setData(data);
    final HeartView heartView=view.findViewById(R.id.im_live_bottom_anim);
         animations= getResources().getStringArray(R.array.praise_array);
         animationsList=  Arrays.asList(animations);
         parser = SVGAParser.Companion.shareParser();
         parser.init(getActivity());
        final ViewGroup container=   (ViewGroup)view;
        view.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                final SVGAImageView svgaImageView= getImageView();
//                ((ViewGroup)view).addView(svgaImageView);
                heartView.addHeart();
            }
        });
    }

    public SVGAImageView getImageView(){
        SoftReference<SVGAImageView> softReference= views.peekFirst();
        if(softReference==null||softReference.get()==null){
            if(softReference!=null){
                views.remove(softReference);
            }
            final SVGAImageView animationView= (SVGAImageView) View.inflate(getActivity(),R.layout.animation_view, null);
            animationView.setLayoutParams(new ViewGroup.LayoutParams(200,200));
            softReference =  new SoftReference<>(animationView);
            views.add(softReference);
        }
        return softReference.get();
    }

    public SVGAImageView getImageView2(){
        final SVGAImageView animationView= new SVGAImageView(getActivity());
        animationView.setLayoutParams(new ViewGroup.LayoutParams(200,200));
        animationView.setLoops(1);
        return animationView;
    }

    public void getDrawable(final String assetPath, final DrawableCallback drawableCallback){
        SVGADrawable drawable=  drawablesMap.get(assetPath);
        if(drawable==null){
            parser.decodeFromAssets(animationsList.get(1), new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(SVGAVideoEntity svgaVideoEntity) {
                    SVGADrawable svgaDrawable=     new SVGADrawable((svgaVideoEntity));
                    drawableCallback.path2Drawable(svgaDrawable);
                    drawablesMap.put(assetPath,svgaDrawable);
                }

                @Override
                public void onError() {
                    drawableCallback.onError();
                }
            });
        }else{
            drawableCallback.path2Drawable(drawable);
        }
    }

    interface  DrawableCallback{
        void path2Drawable(SVGADrawable drawable);
        void onError();
    }

    public void setLayout(LayerDrawable ld){
        for(int i=0;i<ld.getNumberOfLayers();i++){
            ld.setLayerInset(i,0,0,100,100);
        }
    }

}
