package com.yootom.mydiary.fragment;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yootom.mydiary.constant.AppConstants;
import com.yootom.mydiary.db.NoteDatabase;
import com.yootom.mydiary.model.Note;
import com.yootom.mydiary.adapter.NoteAdapter;
import com.yootom.mydiary.impl.OnNoteItemClickListener;
import com.yootom.mydiary.R;
import com.yootom.mydiary.impl.OnTableItemSelectedListener;

import java.util.ArrayList;
import java.util.Date;

import lib.kingja.switchbutton.SwitchMultiButton;
/**
 * 날씨 아이콘
 *
 * ① 맑음
 * ② 구름 조금
 * ③ 구름 많음
 * ④ 흐림
 * ⑤ 비
 * ⑥ 눈/비
 * ⑦ 눈
 */
//리스트화면
public class Fragment1 extends Fragment {
    private static final String TAG  = Fragment1.class.getSimpleName();

    RecyclerView recyclerView;
    NoteAdapter noteAdapter;

    Context context;
    OnTableItemSelectedListener listener;

    @Override
    //이 프래그먼트가 액티비티 위에 올라갈때 호출
    //컨텍스트 객체나 리스너 객체를 참조하여 변수에 할당하는 역할을 한다.
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;

        if (context instanceof OnTableItemSelectedListener) {
            listener = (OnTableItemSelectedListener) context;
        }
    }

    @Override
    //이 프래그먼트가 액티비티에서 내려올때 호출
    public void onDetach() {
        super.onDetach();
        if (context != null) {
            context = null;
            listener = null;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment1,container,false);
        initUI(rootView);

        // 데이터 로딩
        loadNoteListData();

        return  rootView;

    }
    //'내용'과'사진'중에서 선택했을때 어뎁터의 swithLayout()메서드를 호출하여 아이템의 레이아웃을 바꿔준다.
    private void initUI(ViewGroup rootView){
        Button todayWriteButton = rootView.findViewById(R.id.todayWriteButton);
        todayWriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onTabSelected(1);
                }
            }
        });

        SwitchMultiButton switchButton = rootView.findViewById(R.id.switchButton);
        switchButton.setOnSwitchListener(new SwitchMultiButton.OnSwitchListener() {
            @Override
            public void onSwitch(int position, String tabText) {
                Toast.makeText(getContext(), tabText, Toast.LENGTH_SHORT).show();

                //레이아웃 바꾸기
                noteAdapter.switchLayout(position);
                noteAdapter.notifyDataSetChanged();
            }
        });

        recyclerView = rootView.findViewById(R.id.recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);


        noteAdapter = new NoteAdapter();

        noteAdapter.addItem(new Note(0, "0", "강남구 삼성동", "", "","오늘 너무 행복해!", "0", "capture1.jpg", "2월 10일"));
        noteAdapter.addItem(new Note(1, "1", "강남구 삼성동", "", "","친구와 재미있게 놀았어", "0", "capture1.jpg", "2월 11일"));
        noteAdapter.addItem(new Note(2, "0", "강남구 역삼동", "", "","집에 왔는데 너무 피곤해 ㅠㅠ", "0", null, "2월 13일"));

        recyclerView.setAdapter(noteAdapter);

        noteAdapter.setOnItemClickListener(new OnNoteItemClickListener() {
            @Override
            public void onItemClick(NoteAdapter.ViewHolder holder, View view, int position) {
                Note item = noteAdapter.getItem(position);

                Log.d(TAG, "아이템 선택됨 : " + item.get_id());
                if (listener != null) {
                    listener.showFragment2(item);
                }
            }
        });
    }

    /**
     * 리스트 데이터 로딩
     */
    public int loadNoteListData() {
        AppConstants.println(TAG,"loadNoteListData called.");

        /*String sql = "select"
                + " _id,"
                + " WEATHER,"
                + " ADDRESS,"
                + " LOCATION_X,"
                + " LOCATION_Y,"
                + " CONTENTS,"
                + " MOOD,"
                + " PICTURE,"
                + " CREATE_DATE,"
                + " MODIFY_DATE from"
                + NoteDatabase.TABLE_NOTE
                + " order by CREATE_DATE desc";*/

        String sql = "select " +
                "_id, " +
                "WEATHER, " +
                "ADDRESS, " +
                "LOCATION_X, " +
                "LOCATION_Y, " +
                "CONTENTS, " +
                "MOOD, " +
                "PICTURE, " +
                "CREATE_DATE, " +
                "MODIFY_DATE from "
                + NoteDatabase.TABLE_NOTE +
                " order by CREATE_DATE desc";

        int recordCount = -1;
        NoteDatabase database = NoteDatabase.getInstance(context);
        if (database != null) {
            Cursor outCursor = database.rawQuery(sql);

            recordCount = outCursor.getCount();
            AppConstants.println(TAG,"record count : " + recordCount + "\n");

            ArrayList<Note> items = new ArrayList<Note>();

            for (int i = 0; i < recordCount; i++) {
                outCursor.moveToNext();

                int _id = outCursor.getInt(0);
                String weather = outCursor.getString(1);
                String address = outCursor.getString(2);
                String locationX = outCursor.getString(3);
                String locationY = outCursor.getString(4);
                String contents = outCursor.getString(5);
                String mood = outCursor.getString(6);
                String picture = outCursor.getString(7);
                String dateStr = outCursor.getString(8);
                String createDateStr = null;
                if (dateStr != null && dateStr.length() > 10) {
                    try {
                        Date inDate = AppConstants.dateFormat4.parse(dateStr);
                        createDateStr = AppConstants.dateFormat3.format(inDate);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    createDateStr = "";
                }

                AppConstants.println(TAG,"#" + i + " -> " + _id + ", " + weather + ", " +
                        address + ", " + locationX + ", " + locationY + ", " + contents + ", " +
                        mood + ", " + picture + ", " + createDateStr);

                items.add(new Note(_id, weather, address, locationX, locationY, contents, mood, picture, createDateStr));
            }

            outCursor.close();

            noteAdapter.setItems(items);
            noteAdapter.notifyDataSetChanged();

        }

        return recordCount;
    }
}
