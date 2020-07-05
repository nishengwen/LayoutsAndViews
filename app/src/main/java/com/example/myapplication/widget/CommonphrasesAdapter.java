
package com.example.myapplication.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 横向的RecycleView快捷输入
 */
public class CommonphrasesAdapter extends RecyclerView.Adapter<CommonphrasesAdapter.ViewHolder> {
    private static final String TAG = CommonphrasesAdapter.class.getSimpleName();
    private List<IMConversation> phrasesList;
    private Context mContext;
    private OnItemClick onItemClick;

    public CommonphrasesAdapter(Context context) {
        mContext = context;
        phrasesList = new ArrayList<>();
    }

    public void setData(List<IMConversation> data) {
        phrasesList.clear();
        phrasesList.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.im_input_quick_tv, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        ViewHolder mViewHolder = viewHolder;
        mViewHolder.textView.setText(phrasesList.get(i).name);
    }

    @Override
    public int getItemCount() {
        return phrasesList.size();
    }

    public OnItemClick getOnItemClick() {
        return onItemClick;
    }

    public void setOnItemClick(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    public interface OnItemClick {
        void onTextClick(String phrasesText, String result);
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.phrase_text);
        }
    }

}
