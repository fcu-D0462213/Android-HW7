package com.example.hotel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private HotelArrayAdapter adapter=null;

    private static final int List_Hotels=1;

    private Handler handler=new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case List_Hotels:{
                    List<Hotel> hotels=(List<Hotel>)msg.obj;
                    refreshHotelList(hotels);
                    break;
                }
            }
        }
    };

    private void refreshHotelList(List<Hotel> hotels){
        adapter.clear();
        adapter.addAll(hotels);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView lvHotels=(ListView)findViewById(R.id.listview_hotel);

        adapter=new HotelArrayAdapter(this,new ArrayList<Hotel>());
        lvHotels.setAdapter(adapter);

        getHotelFromFirebase();
    }
    class FirebaseThread extends Thread{
        private DataSnapshot dataSnapshot;

        public FirebaseThread(DataSnapshot dataSnapshot) {
            this.dataSnapshot = dataSnapshot;
        }
        @Override
        public void run(){
            List<Hotel>lsHotels=new ArrayList<>();
            for(DataSnapshot ds:dataSnapshot.getChildren()) {
                DataSnapshot dsName = ds.child("Name");
                DataSnapshot dsAdd = ds.child("Add");

                String HotelName = (String) dsName.getValue();
                String HotelAdd = (String) dsAdd.getValue();

                DataSnapshot dsImg = ds.child("Picture1");
                String imgUrl = (String) dsImg.getValue();
                Bitmap hotelImg = getImgBitmap(imgUrl);

                Hotel ahotel = new Hotel();
                ahotel.setAdd(HotelAdd);
                ahotel.setName(HotelName);
                ahotel.setImgUrl(hotelImg);
                lsHotels.add(ahotel);
                Log.v("hotel", HotelName + ";" + HotelAdd);
            }
                Message msg=new Message();
                msg.what=List_Hotels;
                msg.obj=lsHotels;
                handler.sendMessage(msg);
            }
        }
    private void getHotelFromFirebase(){
        FirebaseDatabase database=FirebaseDatabase.getInstance();
        DatabaseReference myRef=database.getReference("");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                new FirebaseThread(dataSnapshot).start();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.v("hotel",databaseError.getMessage());
            }
        });
    }
    private Bitmap getImgBitmap(String imgUrl){
        try{
            URL url=new URL(imgUrl);
            Bitmap bm= BitmapFactory.decodeStream(
                    url.openConnection()
                            .getInputStream());
            return bm;
        }catch (MalformedURLException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    class HotelArrayAdapter extends ArrayAdapter<Hotel>{
        Context context;

        public HotelArrayAdapter(Context context,List<Hotel> items){
            super(context,0,items);
            this.context=context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            LayoutInflater inflater=LayoutInflater.from(context);
            LinearLayout itemlayout=null;
            if(convertView==null){
                itemlayout=(LinearLayout)inflater.inflate(R.layout.hotel_item,null);
            }else{
                itemlayout=(LinearLayout)convertView;
            }
            Hotel item=(Hotel)getItem(position);
            TextView tvName=(TextView)itemlayout.findViewById(R.id.tv_name);
            tvName.setText(item.getName());
            TextView tvAdd=(TextView)itemlayout.findViewById(R.id.tv_add);
            tvAdd.setText(item.getAdd());
            ImageView ivHotel=(ImageView)itemlayout.findViewById(R.id.iv_hotel);
            ivHotel.setImageBitmap(item.getImgUrl());
            return itemlayout;
        }
    }
}
