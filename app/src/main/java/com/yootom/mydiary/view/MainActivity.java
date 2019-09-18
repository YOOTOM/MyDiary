package com.yootom.mydiary.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Request;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;
import com.stanfy.gsonxml.GsonXml;
import com.stanfy.gsonxml.GsonXmlBuilder;
import com.stanfy.gsonxml.XmlParserCreator;
import com.yootom.mydiary.application.MyApplication;
import com.yootom.mydiary.R;
import com.yootom.mydiary.constant.AppConstants;
import com.yootom.mydiary.data.WeatherItem;
import com.yootom.mydiary.data.WeatherResult;
import com.yootom.mydiary.db.NoteDatabase;
import com.yootom.mydiary.fragment.Fragment1;
import com.yootom.mydiary.fragment.Fragment2;
import com.yootom.mydiary.fragment.Fragment3;
import com.yootom.mydiary.impl.OnRequestListener;
import com.yootom.mydiary.impl.OnTableItemSelectedListener;
import com.yootom.mydiary.model.Note;
import com.yootom.mydiary.util.GridUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements OnTableItemSelectedListener, OnRequestListener, AutoPermissionsListener, MyApplication.OnResponseListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    Fragment1 fragment1;
    Fragment2 fragment2;
    Fragment3 fragment3;

    BottomNavigationView bottomNavigationView;

    Location currentLocation;
    GPSListener gpsListener;

    int locationCount = 0;
    String currentWeather;
    String currentAddress;
    String currentDateString;
    Date currentDate;

    /**
     * 데이터베이스 인스턴스
     */
    public static NoteDatabase mDatabase = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragment1 = new Fragment1();
        fragment2 = new Fragment2();
        fragment3 = new Fragment3();

        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment1).commit();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        //이 리스너는 하단 탬에 들어 있는 각각의 버튼을 눌렀을때 OnNavigationItemSelected()메서드가 자동으로 호출 된다.
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.tab1 :
                        //Toast.makeText(getApplicationContext(), "첫 번째 탭 선택됨", Toast.LENGTH_LONG).show();
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment1).commit();

                        return true;
                    case R.id.tab2 :
                        //Toast.makeText(getApplicationContext(), "두 번째 탭 선택됨", Toast.LENGTH_LONG).show();
                        fragment2 = new Fragment2();
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment2).commit();

                        return true;
                    case R.id.tab3 :
                        //Toast.makeText(getApplicationContext(), "세 번째 탭 선택됨", Toast.LENGTH_LONG).show();
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment3).commit();

                        return true;
                }
                return false;
            }
        });
        AutoPermissions.Companion.loadAllPermissions(this, 101);

        setPicturePath();

        // 데이터베이스 열기
        openDatabase();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mDatabase != null) {
            mDatabase.close();
            mDatabase = null;
        }
    }

    /**
     * 데이터베이스 열기 (데이터베이스가 없을 때는 만들기)
     */
    public void openDatabase() {
        // open database
        if (mDatabase != null) {
            mDatabase.close();
            mDatabase = null;
        }

        mDatabase = NoteDatabase.getInstance(this);
        boolean isOpen = mDatabase.open();
        if (isOpen) {
            Log.d(TAG, "Note database is open.");
        } else {
            Log.d(TAG, "Note database is not open.");
        }
    }

    public void setPicturePath() {
        String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        AppConstants.FOLDER_PHOTO = sdcardPath + File.separator + "photo";
    }

    public void onTabSelected(int position){
        switch (position){
            case 0:
                bottomNavigationView.setSelectedItemId(R.id.tab1);
                break;
            case 1:
                fragment2 = new Fragment2();

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, fragment2).commit();
                break;
            case 2:
                bottomNavigationView.setSelectedItemId(R.id.tab3);
                break;
        }
    }

    @Override
    public void showFragment2(Note item) {

        fragment2 = new Fragment2();
        fragment2.setItem(item);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment2).commit();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        AutoPermissions.Companion.parsePermissions(this, requestCode, permissions, this);
    }

    @Override
    public void onDenied(int requestCode, @NonNull String[] permissions) {
        Toast.makeText(this, "permissions denied : " + permissions.length, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGranted(int requestCode, @NonNull String[] permissions) {
        Toast.makeText(this, "permissions granted : " + permissions.length, Toast.LENGTH_LONG).show();
    }

    public void onRequest(String command) {
        if (command != null) {
            if (command.equals("getCurrentLocation")) {
                getCurrentLocation();
            }
        }
    }

    //이 메서드가 호출되면 위치 확인이 시작된다.
    public void getCurrentLocation() {
        // set current time
        // 현재 일자를 확인
        currentDate = new Date();
        currentDateString = AppConstants.dateFormat3.format(currentDate);
        if (fragment2 != null) {
            fragment2.setDateString(currentDateString);
        }

        //현재 위치 요청
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try {
            currentLocation = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (currentLocation != null) {
                double latitude = currentLocation.getLatitude();
                double longitude = currentLocation.getLongitude();
                String message = "Last Location -> Latitude : " + latitude + "\nLongitude:" + longitude;
                AppConstants.println(TAG,message);

                getCurrentWeather();
                getCurrentAddress();
            }

            gpsListener = new GPSListener();
            long minTime = 10000;
            float minDistance = 0;

            manager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    minTime, minDistance, gpsListener);

            AppConstants.println(TAG,"Current location requested.");

        } catch(SecurityException e) {
            e.printStackTrace();
        }
    }


    public void stopLocationService() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try {
            manager.removeUpdates(gpsListener);

            AppConstants.println(TAG,"Current location requested.");

        } catch(SecurityException e) {
            e.printStackTrace();
        }
    }

    class GPSListener implements LocationListener {
        public void onLocationChanged(Location location) {
            currentLocation = location;

            locationCount++;

            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();

            String message = "Current Location -> Latitude : "+ latitude + "\nLongitude:"+ longitude;
            AppConstants.println(TAG,message);

            getCurrentWeather();
            getCurrentAddress();
        }

        public void onProviderDisabled(String provider) { }

        public void onProviderEnabled(String provider) { }

        public void onStatusChanged(String provider, int status, Bundle extras) { }
    }

    //현재 위치의 주소를 확인
    public void getCurrentAddress() {
        //Geocoder 클래스를 이용해 현재 위치를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(
                    currentLocation.getLatitude(),
                    currentLocation.getLongitude(),
                    1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            currentAddress = address.getLocality() + " " + address.getSubLocality();
            String adminArea = address.getAdminArea();
            String country = address.getCountryName();
            AppConstants.println(TAG,"Address : " + country + " " + adminArea + " " + currentAddress);

            if (fragment2 != null) {
                fragment2.setAddress(currentAddress);
            }
        }
    }
    //현재 위치의 날씨 요청
    public void getCurrentWeather() {

        //GridUtil 객체의 getGrid()메서드를 이용해 격자 번호를 확인한다.
        try {
            Map<String, Double> gridMap = GridUtil.getGrid(currentLocation.getLatitude(), currentLocation.getLongitude());
            double gridX = gridMap.get("x");
            double gridY = gridMap.get("y");
            AppConstants.println(TAG,"x -> " + gridX + ", y -> " + gridY);

            sendLocalWeatherReq(gridX, gridY);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    //기상청 날씨 서버로 요청을 전송
    public void sendLocalWeatherReq(double gridX, double gridY) {
        String url = "http://www.kma.go.kr/wid/queryDFS.jsp";
        url += "?gridx=" + Math.round(gridX);
        url += "&gridy=" + Math.round(gridY);

        Map<String,String> params = new HashMap<String,String>();

        MyApplication.send(AppConstants.REQ_WEATHER_BY_GRID, Request.Method.GET, url, params, this);
    }

    // 기상청 날씨 서버로 부터 응답을 받으면 호출된다.
    public void processResponse(int requestCode, int responseCode, String response) {
        if (responseCode == 200) {
            if (requestCode == AppConstants.REQ_WEATHER_BY_GRID) {
                // Grid 좌표를 이용한 날씨 정보 처리 응답
                //println("response -> " + response);

                //xml로 받은 응답 데이터를 자바 객체로 만들기
                XmlParserCreator parserCreator = new XmlParserCreator() {
                    @Override
                    public XmlPullParser createParser() {
                        try {
                            return XmlPullParserFactory.newInstance().newPullParser();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                };

                GsonXml gsonXml = new GsonXmlBuilder()
                        .setXmlParserCreator(parserCreator)
                        .setSameNameLists(true)
                        .create();

                //자바 객체 생성
                WeatherResult weather = gsonXml.fromXml(response, WeatherResult.class);

                // 응답받은 날씨 데이터를 파싱하기 위한 클래스이용한 처리
                // 날씨 데이터는 로그로 출력해서 확인할 수 있도록 처리
                // 입력 화면에 현재 날씨를 설정
                // 현재 기준 시간
                try {
                    Date tmDate = AppConstants.dateFormat.parse(weather.header.tm);
                    String tmDateText = AppConstants.dateFormat2.format(tmDate);
                    AppConstants.println(TAG,"기준 시간 : " + tmDateText);

                    for (int i = 0; i < weather.body.datas.size(); i++) {
                        WeatherItem item = weather.body.datas.get(i);
                        AppConstants.println(TAG,"#" + i + " 시간 : " + item.hour + "시, " + item.day + "일째");
                        AppConstants.println(TAG,"  날씨 : " + item.wfKor);
                        AppConstants.println(TAG,"  기온 : " + item.temp + " C");
                        AppConstants.println(TAG,"  강수확률 : " + item.pop + "%");

                        AppConstants.println(TAG,"debug 1 : " + (int)Math.round(item.ws * 10));
                        float ws = Float.valueOf(String.valueOf((int)Math.round(item.ws * 10))) / 10.0f;
                        AppConstants.println(TAG,"  풍속 : " + ws + " m/s");
                    }

                    // set current weather
                    WeatherItem item = weather.body.datas.get(0);
                    currentWeather = item.wfKor;
                    if (fragment2 != null) {
                        fragment2.setWeather(item.wfKor);
                    }

                    // 위치 요청을 종료합니다.
                    // stop request location service after 2 times
                    if (locationCount > 1) {
                        stopLocationService();
                    }

                } catch(Exception e) {
                    e.printStackTrace();
                }


            } else {
                // Unknown request code
                AppConstants.println(TAG,"Unknown request code : " + requestCode);

            }

        } else {
            AppConstants.println(TAG, "Failure response code : " + responseCode);

        }
    }
}
