package com.yootom.mydiary.constant;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.text.SimpleDateFormat;

//소스코드에서 사용하는 주요 상수들을 클래스변수로 선언
public class AppConstants {

    public static final int REQ_LOCATION_BY_ADDRESS = 101;
    public static final int REQ_WEATHER_BY_GRID = 102;

    public static final int REQ_PHOTO_CAPTURE = 103;
    public static final int REQ_PHOTO_SELECTION = 104;

    public static final int CONTENT_PHOTO = 105;
    public static final int CONTENT_PHOTO_EX = 106;

    public static String FOLDER_PHOTO;

    /**
     * 데이터베이스 이름
     */
    public static String DATABASE_NAME = "note_yootom.db";

    public static final int MODE_INSERT = 1;
    public static final int MODE_MODIFY = 2;


    @SuppressLint("SimpleDateFormat")
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
    @SuppressLint("SimpleDateFormat")
    public static SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH시");
    @SuppressLint("SimpleDateFormat")
    public static SimpleDateFormat dateFormat3 = new SimpleDateFormat("MM월 dd일");
    @SuppressLint("SimpleDateFormat")
    public static SimpleDateFormat dateFormat4 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @SuppressLint("SimpleDateFormat")
    public static SimpleDateFormat dateFormat5 = new SimpleDateFormat("yyyy-MM-dd");


    private static Handler handler = new Handler();
    public static void println(final String TAG, final String data) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                AppConstants.println(TAG, data);
            }
        });
    }
}
