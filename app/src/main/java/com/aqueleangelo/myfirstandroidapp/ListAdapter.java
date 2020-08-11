package com.aqueleangelo.myfirstandroidapp;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.view.MotionEventCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder> {
    private ArrayList<ListItemCard> mItemList;

    private OnItemClickListener mListener;

    public interface OnItemClickListener{
        void OnItemClick(int position);
        void OnCheckChange(int position, boolean isChecked);
        void OnStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    public void setOnItemClickListener(OnItemClickListener listener){ mListener = listener; }

    public static class ListViewHolder extends RecyclerView.ViewHolder{
        public TextView mTopText;
        public TextView mTime;
        public ImageView mHandleView;
        public CheckBox mCheckBox;

        public ListViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            mTopText = itemView.findViewById(R.id.topText);
            mTime = itemView.findViewById(R.id.bottomText);
            mHandleView = itemView.findViewById(R.id.handleView);
            mCheckBox = itemView.findViewById(R.id.checkBox);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.OnItemClick(position);
                        }
                    }
                }
            });

            mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if(listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.OnCheckChange(position, isChecked);
                        }
                    }
                }
            });
        }
    }

    public ListAdapter(ArrayList<ListItemCard> itemList){ mItemList = itemList; }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        ListViewHolder evh = new ListViewHolder(v, mListener);
        return evh;
    }

    @Override
    public void onBindViewHolder(@NonNull final ListViewHolder holder, int position) {
        ListItemCard currentItem = mItemList.get(position);

        holder.mTopText.setText(currentItem.getTopText());
        holder.mTime.setText(currentItem.getTime() + " segundos");
        holder.mCheckBox.setChecked(currentItem.isChecked());
        holder.mHandleView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getActionMasked() ==
                        MotionEvent.ACTION_DOWN) {
                    mListener.OnStartDrag(holder);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }
}
