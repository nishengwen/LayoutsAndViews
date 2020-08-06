package com.example.myapplication.ui.notifications;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.widget.OneSingleThreadPool;
import com.example.myapplication.widget.PraiseView;

import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

public class NotificationsFragment extends Fragment {

    private NotificationsViewModel notificationsViewModel;
    Handler uiHandler=new Handler(Looper.myLooper());
    Timer timer;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             final ViewGroup container, Bundle savedInstanceState) {

        notificationsViewModel =
                ViewModelProviders.of(this).get(NotificationsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);
        final TextView textView = root.findViewById(R.id.text_notifications);
        notificationsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        final PraiseView praiseView=root.findViewById(R.id.praise_view);
        praiseView.init(R.array.drawables);

        final OneSingleThreadPool oneSingleThreadPool=new OneSingleThreadPool();
        root.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(timer!=null){
//                    timer.cancel();
//                }
//                timer= new Timer(true);
//                timer.schedule(new MyTimeTask(),2000,1000);
//                Toast.makeText(getContext(),"关闭线程",Toast.LENGTH_SHORT).show();
//                oneSingleThreadPool.stopAll();
                praiseView.addPraise(1);
            }
        });
        timer= new Timer(true);
        timer.schedule(new MyTimeTask(),2000,1000);



        final TextView inputEdit=root.findViewById(R.id.Edittext);
        inputEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oneSingleThreadPool.startLogin(new OneSingleThreadPool.Callback() {
                    @Override
                    public Object getParams(Object o) {
                        return null;
                    }

                    @Override
                    public void onSuccess(Object o) {
                        Toast.makeText(getContext(),"成功结束"+o,Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed(Object o) {
                        Toast.makeText(getContext(),o.toString(),Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onStart() {
                        Toast.makeText(getContext(),"开始------",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onInterrupt(String s) {
                        Toast.makeText(getContext(),s,Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });



//        inputEdit.addTextChangedListener(new TextWatcher() {
//            private CharSequence temp;
//            private int editStart ;
//            private int editEnd ;
//            @Override
//            public void beforeTextChanged(CharSequence s, int arg1, int arg2,
//                                          int arg3) {
//                temp = s;
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int arg1, int arg2,
//                                      int arg3) {
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                editStart = inputEdit.getSelectionStart();
//                editEnd = inputEdit.getSelectionEnd();
//                if (temp.length() > 10) {
//                    Toast.makeText(getContext(),"asdfas",Toast.LENGTH_SHORT).show();
//                    s.delete(editStart-1, editEnd);
//                    int tempSelection = editStart;
//                    inputEdit.setText(s);
//                    inputEdit.setSelection(tempSelection);
//                }
//            }
//        });


        TextView tv=root.findViewById(R.id.textview);

        tv.setText("136710000012384794357");


        return root;
    }

    class MyTimeTask extends TimerTask{
        int i;
        @Override
        public void run() {
           boolean isMain= (Thread.currentThread()==Looper.getMainLooper().getThread());
                if(i>2){
                    cancel();
                }
//            Toast.makeText(getActivity(),isMain+""+i++,Toast.LENGTH_SHORT).show();
            Log.d("toast",isMain+""+i++);
        }
    }
}
