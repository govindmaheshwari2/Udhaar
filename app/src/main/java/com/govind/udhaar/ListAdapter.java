package com.govind.udhaar;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.modelViewHolder>{
    private static final String TAG ="ListAdapter";

    private ArrayList<Model> modelList=new ArrayList<>();
    private Context mContext;
    OnClickListener OnClickListener;
    OnLongClick OnLongClick;


    @NonNull
    @Override
    public modelViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_adapter,viewGroup,false);
        modelViewHolder holder=new modelViewHolder(view);
        return holder;
    }


    public ListAdapter(ArrayList<Model> user, Context mContext) {
        this.modelList = user;
        this.mContext = mContext;
    }

    @Override
    public void onBindViewHolder(@NonNull modelViewHolder viewHolder, int i) {
         viewHolder.user.setText(modelList.get(i).getUsername());
         viewHolder.amount.setText(modelList.get(i).getAmount());
        }

    @Override
    public int getItemCount() {
        return modelList.size();
    }

    public class modelViewHolder extends RecyclerView.ViewHolder{
        TextView user;
        TextView amount;
        RelativeLayout List_Adapter;
        public modelViewHolder(View itemView){
            super(itemView);
            user=itemView.findViewById(R.id.user);
            amount=itemView.findViewById(R.id.amount);
            List_Adapter=itemView.findViewById(R.id.List_Adapter);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mContext=view.getContext();
                    int position=getLayoutPosition();
                    if(position!=RecyclerView.NO_POSITION){
                        OnClickListener.onClick(position);
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position=getLayoutPosition();
                    if(position!=RecyclerView.NO_POSITION){
                        OnLongClick.onClick(position);
                    }
                    return false;
                }
            });
        }
    }

    public Context View(){
        return mContext;
    }

    public void setOnClickListen(OnClickListener OnClickListener) {
        this.OnClickListener = OnClickListener;
    }

    public void setOnLongClick(OnLongClick OnLongClick) {
        this.OnLongClick = OnLongClick;
    }

    public ArrayList<Model> getModelList(){
        return modelList;
    }

    public  void filter(ArrayList<Model> list){
        modelList=new ArrayList<>();
        modelList.addAll(list);
        notifyDataSetChanged();
    }



}
