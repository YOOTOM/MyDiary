package com.yootom.mydiary.impl;

import com.yootom.mydiary.model.Note;

//TODO 하나의 프래그먼트에서 다른 프래그먼트로 전환하는 용도로 사용하기 위해 정의한 인터페이스
public interface OnTableItemSelectedListener {
    void onTabSelected(int position);
    void showFragment2(Note item);
}
