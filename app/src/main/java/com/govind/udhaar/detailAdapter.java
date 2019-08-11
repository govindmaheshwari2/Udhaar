package com.govind.udhaar;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class detailAdapter extends RecyclerView.Adapter<detailAdapter.ViewHolder>{

    private static final String TAG ="DetailAdapter";

    private ArrayList<String> User=new ArrayList<>(),User2=new ArrayList<>();
    private ArrayList<String> timeUser=new ArrayList<>(),timeUser2=new ArrayList<>();
    private Context mContext;
    private ArrayList<String> Reason=new ArrayList<>();


    public detailAdapter(ArrayList<String> user, ArrayList<String> user2, ArrayList<String> timeUser, ArrayList<String> timeUser2,ArrayList<String> reason,Context mContext ) {
        User = user;
        User2 = user2;
        this.timeUser = timeUser;
        this.timeUser2 = timeUser2;
        this.mContext = mContext;
        this.Reason=reason;
        this.mContext=mContext;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.detail_layout,viewGroup,false);
        ViewHolder holder=new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull detailAdapter.ViewHolder viewHolder, int i) {
            viewHolder.user2.setText(User2.get(i));
            viewHolder.timeuser2.setText(timeUser2.get(i));
            viewHolder.user.setText(User.get(i));
            viewHolder.timeuser.setText(timeUser.get(i));
            viewHolder.reason.setText(Reason.get(i));
            if(User.get(i).trim().equalsIgnoreCase("")){
                viewHolder.userLayout.setVisibility(View.INVISIBLE);
            }else {
                viewHolder.user2layout.setVisibility(View.INVISIBLE);
            }
            viewHolder.setIsRecyclable(false);
    }

    @Override
    public int getItemCount() {
        return User.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView user,user2;
        TextView timeuser,timeuser2,reason;
        LinearLayout detailList,userLayout,user2layout;
        public ViewHolder(View itemView){
            super(itemView);
            user=itemView.findViewById(R.id.user);
            timeuser=itemView.findViewById(R.id.timeUser);
            user2=itemView.findViewById(R.id.user2);
            timeuser2=itemView.findViewById(R.id.timeUser2);
            reason=itemView.findViewById(R.id.Reason);
            detailList=itemView.findViewById(R.id.detailList);
            userLayout=itemView.findViewById(R.id.userLayout);
            user2layout=itemView.findViewById(R.id.user2layout);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mContext=view.getContext();
                    int position=getLayoutPosition();
                    if(position!=RecyclerView.NO_POSITION){
                        if(reason.getText().toString().equalsIgnoreCase("")){
                            return;
                        }
                        if(reason.getVisibility()==View.VISIBLE){
                            reason.setVisibility(View.GONE);
                        }else{
                            reason.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
        }
    }

}
