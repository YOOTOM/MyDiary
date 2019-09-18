package com.yootom.mydiary.impl;

import android.view.View;

import com.yootom.mydiary.adapter.NoteAdapter;

//각 아이템을 선택했을 때 이벤트를 처리하기 위한 리스너인터페이스
public interface OnNoteItemClickListener {
    void onItemClick(NoteAdapter.ViewHolder holder, View view, int position);
}