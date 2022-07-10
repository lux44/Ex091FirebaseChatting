package com.lux.ex091firebasechatting;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.lux.ex091firebasechatting.databinding.ActivityChattingBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ChattingActivity extends AppCompatActivity {

    ActivityChattingBinding binding;

    String chattingRoomName="chat";

    ArrayList<MessageItem> messageItems=new ArrayList<>();
    MyAdapter adapter;

    FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_chatting);
        binding=ActivityChattingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //제목줄에 채팅방 이름과 닉네임 보이기 [보통은 상대방 이름이 나옴]
        getSupportActionBar().setTitle(chattingRoomName);
        getSupportActionBar().setSubtitle(G.nickName);

        adapter=new MyAdapter(this,messageItems);
        binding.recycler.setAdapter(adapter);

        binding.btnSend.setOnClickListener(view -> clickSend());

        firebaseFirestore=FirebaseFirestore.getInstance();

        //"chat" 컬렉션의 데이터에 변화가 생기는 것을 감지
        CollectionReference chatRef=firebaseFirestore.collection(chattingRoomName);
        chatRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                //모든 Document()를 얻어오면 기존 데이터까지 누적해서 가져옴
                //데이터가 변경된 Document만 찾아달라고 요청

                for (DocumentChange documentChange:value.getDocumentChanges()){
                    //변경된 Document의 데이터를 취득한 Snapshot 객체를 얻어오기
                   DocumentSnapshot snapshot=documentChange.getDocument();

                    Map<String,Object> msg =snapshot.getData();
                    String name=msg.get("name").toString();
                    String message=msg.get("message").toString();
                    String time=msg.get("time").toString();
                    String profileUrl=msg.get("profileUrl").toString();

                    //읽어들인 메세지를 리사이클러뷰가 보여주는 messageItems에 추가하기 위해
                    messageItems.add(new MessageItem(name, message,time,profileUrl));
                    //Log.i("msg",msg.get("name")+", "+msg.get("message"));
                    adapter.notifyItemInserted(messageItems.size()-1);
                    //리사이클러뷰의 스크롤 위치를 마지막 position으로 이동
                    binding.recycler.scrollToPosition(adapter.getItemCount()-1);
                }
            }
        });
    }
    void  clickSend(){
        //닉네임, 프로필 이미지, 메세지, 작성시간을 저장
        String nickName=G.nickName;
        String message=binding.et.getText().toString();
        String profileUrl=G.profileUrl;

        Calendar calendar=Calendar.getInstance();
        String time=calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE);

        //firebase firestore DB 에 저장할 데이터들을 멤버로 가지는 MessageItem 객체를 통으로 저장하기
        MessageItem messageItem=new MessageItem(nickName,message,time,profileUrl);

        //'chat' 이라는 채팅방 이름으로 Collection을 만들어서 MessageItem 객체를 통으로 저장
        //단, Document 이름이 랜덤하면 저장 순서가 바뀔 수 있기 떄문에 Document 이름을 시간으로 설정
        firebaseFirestore.collection(chattingRoomName).document("MSG"+System.currentTimeMillis()).set(messageItem);

        //전송했으니, 다음 입력을 위해 EditText에 써있는 글씨 삭제
        binding.et.setText("");

        //소프트 키패드를 안보이도록 가리기
        InputMethodManager imm= (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
    }
}