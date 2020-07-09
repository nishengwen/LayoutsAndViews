package com.example.myapplication.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.widget.CommonphrasesAdapter;
import com.example.myapplication.widget.IMConversation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView=view.findViewById(R.id.buy_list);
        LinearLayoutManager llm = new LinearLayoutManager(this.getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        CommonphrasesAdapter adapter=new CommonphrasesAdapter(getActivity());



        recyclerView.setAdapter(adapter    );


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
        final PopTextView ptt=  view.findViewById(R.id.text);


        view.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ptt.showAnnouncement("公告"+i++);
            }
        });
        view.findViewById(R.id.button).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                i=0;
                ptt.showFirstPopText("第一次");
                return true;
            }
        });
    }
    int i=0;
}
