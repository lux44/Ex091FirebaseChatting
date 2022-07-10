package com.lux.ex091firebasechatting;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.lux.ex091firebasechatting.databinding.ActivityMainBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    //Firebase 연동 - firebase console 에 프로젝트 생성

    ActivityMainBinding binding;

    boolean isFirst=true;   //처음 앱을 실행하여 프로필 정보가 없는가?
    boolean isChanged=false;    //기존 프로필 이미지를 변경한 적이 있는가?
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.civ.setOnClickListener(view -> clickImage());
        binding.btn.setOnClickListener(view -> clickButton());

        //sharedPreference 에 저장되어 있는 닉네임과 URL 이 있는지 확인
        SharedPreferences pref=getSharedPreferences("account",MODE_PRIVATE);
        G.nickName=pref.getString("nickName",null);
        G.profileUrl=pref.getString("profileUrl",null);

        if (G.nickName!=null){
            binding.et.setText(G.nickName);
            Glide.with(this).load(G.profileUrl).into(binding.civ);

            //처음이 아니다.
            isFirst=false;
        }
    }
    Uri imageUri;   //선택된 이미지의 uri

    void clickImage(){
        //사진 앱을 실행하여 이미지를 선택하고 결과를 받기
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        resultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> resultLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode()!=RESULT_OK) return;

            imageUri=result.getData().getData();
            Glide.with(MainActivity.this).load(imageUri).into(binding.civ);

            //새로운 사진을 선택했으니
            isChanged=true;
        }
    });

    void clickButton(){
        //로그인 한 적이 없거나 이미지를 변경했다면 저장하고 화면 전환 but, 있다면 그냥 화면 전환만
        if (isFirst || isChanged){
            //프로필 이미지와 닉네임을 firebase firestore database 에 저장
            //단, image 파일은 storage에 먼저 업로드 해야 함. 시간이 오래 걸리기 때문에 먼저 완료 해야만 함.
            saveData();
        }else {
            //ChattingActivity로 이동
            Intent intent=new Intent(this,ChattingActivity.class);
            startActivity(intent);
            finish();
        }
    }

    void  saveData(){
        //firebase storage 에 선택한 이미지 파일 업로드
        if (imageUri==null) {
            Toast.makeText(this, "프로필 이미지를 먼저 선택하세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        //firebase storage 관리 객체 소환
        FirebaseStorage firebaseStorage=FirebaseStorage.getInstance();

        //저장된 파일명이 중복되지 않도록 날짜를 이용하여 파일명 결정
        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddhhmmss");
        String fileName="IMG"+sdf.format(new Date())+".png";

        //firebase storage 에 이미지 업로드 하기 위한 참조 객체 얻어오기
        StorageReference imgRef=firebaseStorage.getReference("profileImage/"+fileName);
        imgRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //업로드 성공
                //업로드된 파일의 [다운로드 주소(서버에 있는 이미지의 인터넷 경로 url)]를 얻어와야 함
                imgRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Toast.makeText(MainActivity.this, "프로필 이미지 저장 완료", Toast.LENGTH_SHORT).show();
                        //firebase 저장소에 저장되어 있는 이미지에 대한 다운로드 주소를 문자열로 얻어오기
                        G.profileUrl=uri.toString();
                        G.nickName=binding.et.getText().toString(); //닉네임

                        //1. firebase firestore database 에 닉네임, 이미지 주소 url 을 저장
                        FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();
                        //"profile" 라는 이름의 Collection 참조(없으면 생성, 있으면 참조)
                        CollectionReference profileRef=firebaseFirestore.collection("profile");

                        //닉네임을 Document 명으로 하고 필드 값으로 이미지 경로 url 을 저장
                        //필드 값은 Map Collection 형식으로 저장함.
                        HashMap<String,Object> profile=new HashMap<>();
                        profile.put("profileUrl",G.profileUrl);

                        profileRef.document(G.nickName).set(profile);

                        //2. 이 디바이스의 sharedPreference 에도 저장 - 다시 로그인하기 싫어서
                        SharedPreferences pref=getSharedPreferences("account",MODE_PRIVATE);
                        SharedPreferences.Editor editor =pref.edit();

                        editor.putString("nickName",G.nickName);
                        editor.putString("profileUrl",G.profileUrl);

                        editor.commit();

                        //저장이 완료되었으니 채팅 화면으로 전환
                        Intent intent=new Intent(MainActivity.this,ChattingActivity.class);
                        startActivity(intent);

                        finish();
                    }
                });

            }
        });


    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        finish();
    }
}