package com.lux.ex091firebasechatting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.VH> {

    Context context;
    ArrayList<MessageItem> messageItems;

    public MyAdapter(Context context, ArrayList<MessageItem> messageItems) {
        this.context = context;
        this.messageItems = messageItems;
    }

    final int TYPE_MY=0;
    final int TYPE_OTHER=1;

    //리사이클러뷰가 보여줄 뷰의 종류(모양)가 다른 경우 해당 아이템마다 뷰타입을 정하여
    //리턴해주는 메소드 - 리턴해준 int 값(view Type)이 onCreateViewHolder()에 전달됨.
    @Override
    public int getItemViewType(int position) {
        if (messageItems.get(position).name.equals(G.nickName)) return TYPE_MY;
        else return TYPE_OTHER;
    }


    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(context);
        View itemView=null;
        if (viewType==TYPE_MY) itemView=inflater.inflate(R.layout.my_messagebox,parent,false);
        else itemView=inflater.inflate(R.layout.other_messagebox,parent,false);
        return new VH(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        MessageItem item=messageItems.get(position);

        holder.tvName.setText(item.name);
        holder.tvMsg.setText(item.message);
        holder.tvTime.setText(item.time);
        Glide.with(context).load(item.profileUrl).into(holder.civ);
    }

    @Override
    public int getItemCount() {
        return messageItems.size();
    }



    class VH extends RecyclerView.ViewHolder{
        //바인딩 클래스를 사용하기에 효율적이지 않은 경우도 있음.
        //지금처럼 레이아웃이 두 종류면 바인딩 클래스별로 VH를 만들어 사용해야 함.
        //그래서 기존처럼 findViewById()를 이용
        // [그럼 바인딩 클래스를 사용하지 않게됨 - 만약 필요없다면 바인딩클래스가 자동 만들어지지 않도록 xml 문서에 설정할 수 있음.]
        CircleImageView civ;
        TextView tvName, tvMsg, tvTime;
        public VH(@NonNull View itemView) {
            super(itemView);
            civ=itemView.findViewById(R.id.civ);
            tvName=itemView.findViewById(R.id.tv_name);
            tvMsg=itemView.findViewById(R.id.tv_msg);
            tvTime=itemView.findViewById(R.id.tv_time);

        }
    }
}
