package com.a12599.myapplication;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.Gradient;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.HeatMap;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.WeightedLatLng;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.spec.ECField;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private final int maxCityNumber=20;
    private final int maxMonitorNumber=15;

    MapView mMapView;
    BaiduMap mBaiduMap;
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();

    private TextView textView2,getDataView,markerView,voronoiView;
    private Spinner Spinner1;
    private Marker marker;
    private HeatMap heatmap;
    private boolean firstLocate=true;
    private boolean firstDraw=true;
    private boolean marked=false;
    private boolean voronoied= false;
    private List<LatLng> List = new ArrayList<LatLng>();
    private List<WeightedLatLng> weigthedList = new ArrayList<WeightedLatLng>();
    private int position = 0;
    private GeoCoder[][] mSearch = new GeoCoder[maxCityNumber][maxMonitorNumber];


    private String[] cityName= new String[maxCityNumber];
    private String[][] monitorName = new String[maxCityNumber][maxMonitorNumber];

    private double pm25[][] = new double[maxCityNumber][maxMonitorNumber];
    private double pm10[][] = new double[maxCityNumber][maxMonitorNumber];
    private double so2[][] = new double[maxCityNumber][maxMonitorNumber];
    int cityCounter = 0, monitorNumber[] = new int[maxCityNumber],monitorCounter;
    int cannotFind=0;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        mMapView = (MapView) findViewById(R.id.mapView);

        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener( myListener );    //注册监听函数
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        option.setScanSpan(0);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(false);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(false);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
        mLocationClient.start();

        mBaiduMap = mMapView.getMap();

        InputStream inputStream = getResources().openRawResource(R.raw.data);
        InputStreamReader inputStreamReader=null;
        try{
            inputStreamReader = new InputStreamReader(inputStream,"utf-8");
        } catch (UnsupportedEncodingException e1)
        {
            e1.printStackTrace();
        }

        BufferedReader reader = new BufferedReader(inputStreamReader);
        StringBuffer sb= new  StringBuffer("");
        String line;
        int row=0;
        try {
            while ((line= reader.readLine()) != null)
            {
                int index;
                if ((index = line.indexOf("<title>"))>=0)
                {
                    cityName[cityCounter++]=line.substring(7,line.length());
                    row=0;
                    monitorNumber[cityCounter-1]=monitorCounter;
                    monitorCounter=0;
                    continue;
                }
                if (row==0)
                {
                    monitorName[cityCounter-1][monitorCounter++]=line;
                    row++;
                    continue;
                }
                if (row==1)
                {
                    pm25[cityCounter - 1][monitorCounter - 1] = Integer.parseInt(line);
                    row++;
                    int a=0;
                    continue;
                }
                if (row==2)
                {
                    pm10[cityCounter - 1][monitorCounter - 1] = Integer.parseInt(line);
                    row++;
                    continue;
                }
                if (row==3)
                {
                    so2[cityCounter - 1][monitorCounter - 1] = Integer.parseInt(line);
                    row=0;
                }
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        monitorNumber[cityCounter-1]=monitorCounter;

        //while (!loadData);
        for (int i=0;i<1;i++)
            for (int j=0;j<monitorNumber[i]-1;j++)
            {
                mSearch[i][j] = GeoCoder.newInstance();
                mSearch[i][j].setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener1());
            };

        textView2 = (TextView) findViewById(R.id.textView2);
        textView2.setOnClickListener(new TxtClickListener2());

        getDataView = (TextView) findViewById(R.id.getDataView);
        getDataView.setOnClickListener(new DataTxtClickListener());

        markerView = (TextView) findViewById(R.id.markerView);
        markerView.setOnClickListener(new MarkerTxtClickListener());

        markerView = (TextView) findViewById(R.id.voronoiView);
        markerView.setOnClickListener(new VoronoTxtClickListener());

        Spinner1 = (Spinner) findViewById(R.id.spinner1);
        Spinner1.setOnItemSelectedListener(new SpinSelectedListener1());
    }



    //地理编码检索监听类
    class OnGetGeoCoderResultListener1 implements OnGetGeoCoderResultListener {
        @Override
        public void onGetGeoCodeResult(GeoCodeResult result) {
            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                //没有检索到结果
                Toast.makeText(MainActivity.this, "抱歉，未能找到结果"+List.size()+ " "+cannotFind,
                        Toast.LENGTH_LONG).show();

                //return;
            }
            //获取地理编码结果
            List.add(result.getLocation());
            Toast.makeText(MainActivity.this, "OK",
                    Toast.LENGTH_LONG).show();
        }
        @Override
        public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                //没有找到检索结果
            }
            //获取反向地理编码结果
        }
    }


    //定义一个内部类,实现View.OnClickListener接口,并重写onClick()方法 textview2的监听
    class TxtClickListener2 implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            mLocationClient.requestLocation();
        }
    }

    //textView3的监听
    class DataTxtClickListener implements  View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            for (int i=0;i<1;i++)
                for (int j=1;j<monitorNumber[i]-1;j++)
                {
                    mSearch[i][j].geocode(new GeoCodeOption()
                            .city(cityName[i])
                            .address(monitorName[i][j]));
                }
        }
    }

    class MarkerTxtClickListener implements  View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            if (!marked)
            {
                marked=true;
                int count = 0;
                for (int i = 0; i < cityCounter; i++) {
                    for (int j = 1; j < monitorNumber[i] - 1; j++) {
                        LatLng temp= List.get(count+j+500-1);
                        //传入经纬度
                        LatLng point = new LatLng(temp.latitude, temp.longitude);
                        //自己的覆盖图片
                        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.mark);
                        MarkerOptions option = new MarkerOptions().position(point).title(monitorName[i][j]).icon(bitmap).zIndex(9).period(10);;
                        option.animateType(MarkerOptions.MarkerAnimateType.grow);
                        //给地图添加一层覆盖
                        marker = (Marker) (mBaiduMap.addOverlay(option));
                        //更新位置信息
                        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(point));

                        Bundle bundle=new Bundle();
                        bundle.putString("info",monitorName[i][j]+"\nPM2.5 "+pm25[i][j]+"\nPM10 "+pm10[i][j]+"\nSo2 "+so2[i][j]+"\n");
                        marker.setExtraInfo(bundle);

                        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker)
                            {
                                //从marker中获取info信息
                                Bundle bundle = marker.getExtraInfo();
                                String str = bundle.getString("info");

                                TextView tv= new TextView(MainActivity.this);
                                tv.setText(str);
                                BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromView(tv);
                                //infowindow位置
                                LatLng latLng = marker.getPosition();
                                //infowindow点击事件
                                InfoWindow.OnInfoWindowClickListener listener = new InfoWindow.OnInfoWindowClickListener() {
                                    @Override
                                    public void onInfoWindowClick() {
                                        //隐藏infowindow
                                        mBaiduMap.hideInfoWindow();
                                    }
                                };
                                //显示infowindow
                                InfoWindow infoWindow = new InfoWindow(bitmapDescriptor, latLng, -47, listener);
                                mBaiduMap.showInfoWindow(infoWindow);
                                return true;
                            }
                        });
                    }
                    count+=monitorNumber[i];
                }
                mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker)
                    {
                        //从marker中获取info信息
                        Bundle bundle = marker.getExtraInfo();
                        String str = bundle.getString("info");

                        TextView tv= new TextView(MainActivity.this);
                        tv.setText(str);
                        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromView(tv);
                        //infowindow位置
                        LatLng latLng = marker.getPosition();
                        //infowindow点击事件
                        InfoWindow.OnInfoWindowClickListener listener = new InfoWindow.OnInfoWindowClickListener() {
                            @Override
                            public void onInfoWindowClick() {
                                //隐藏infowindow
                                mBaiduMap.hideInfoWindow();
                            }
                        };
                        //显示infowindow
                        InfoWindow infoWindow = new InfoWindow(bitmapDescriptor, latLng, -47, listener);
                        mBaiduMap.showInfoWindow(infoWindow);
                        return true;
                    }
                });
            }
            else {
                marked=false;
                marker.remove();
            }
        }
    }

    class SpinSelectedListener1 implements Spinner.OnItemSelectedListener
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            //判断是否是第一次绘图
            if(!firstDraw)
            {
                weigthedList.clear();
                heatmap.removeHeatMap();
            }
            position=pos;

            //设置渐变颜色值
            int[] DEFAULT_GRADIENT_COLORS = {ContextCompat.getColor(Spinner1.getContext(),R.color.start), ContextCompat.getColor(Spinner1.getContext(),R.color.end)};
            //设置渐变颜色起始值
            float[] DEFAULT_GRADIENT_START_POINTS = { 0.2f, 1f };
            //构造颜色渐变对象
            Gradient gradient = new Gradient(DEFAULT_GRADIENT_COLORS, DEFAULT_GRADIENT_START_POINTS);
            Random r = new Random();
            int rlat,rlng,lat,lng;
            LatLng ll;
            if (firstDraw)
            {
                firstDraw=false;
                switch(position)
                {
                    case 0: //PM2.5
                        for (int i = 0; i < 500; i++) {
                            // 116.220000,39.780000 116.570000,40.150000
                            rlat = r.nextInt(370000);
                            rlng = r.nextInt(370000);
                            lat = 39780000 + rlat;
                            lng = 116220000 + rlng;
                            ll = new LatLng(lat / 1E6, lng / 1E6);
                            List.add(ll);
                        }
                        break;
                    case 1: //PM10
                        for (int i = 0; i < 500; i++) {
                            // 杭州
                            rlat = r.nextInt(370000);
                            rlng = r.nextInt(370000);
                            lat = 30260000 + rlat;
                            lng = 120190000 + rlng;
                            ll = new LatLng(lat / 1E6, lng / 1E6);
                            List.add(ll);
                        }
                        break;
                    case 2: //SO2
                        for (int i = 0; i < 500; i++) {
                            // 绍兴
                            rlat = r.nextInt(370000);
                            rlng = r.nextInt(370000);
                            lat = 30010000 + rlat;
                            lng = 120580000 + rlng;
                            ll = new LatLng(lat / 1E6, lng / 1E6);
                            List.add(ll);
                        }
                        break;
                }
                heatmap = new HeatMap.Builder()
                        .data(List)
                        .gradient(gradient)
                        .radius(50)
                        .build();
            }
            else {
                int count = 0;
                for (int i = 0; i < cityCounter; i++) {
                    for (int j = 1; j < monitorNumber[i] - 1; j++) {
                        switch (position) {
                            case 0:
                                WeightedLatLng temp1 = new WeightedLatLng(List.get(count + j + 500 -1), pm25[i][j] / 10);
                                weigthedList.add(temp1);
                                break;
                            case 1:
                                WeightedLatLng temp2 = new WeightedLatLng(List.get(count + j + 500 - 1), pm10[i][j] / 10);
                                weigthedList.add(temp2);
                                break;
                            case 2:
                                WeightedLatLng temp3 = new WeightedLatLng(List.get(count + j + 500 - 1), so2[i][j] / 10);
                                weigthedList.add(temp3);
                                break;
                        }
                    }
                    count += monitorNumber[i];
                }
                heatmap = new HeatMap.Builder()
                        .weightedData(weigthedList)
                        .gradient(gradient)
                        .radius(50)
                        .build();
            }
            //在地图上添加热力图
            mBaiduMap.addHeatMap(heatmap);
        }

        @Override
        public void  onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }
    }

    class VoronoTxtClickListener implements View.OnClickListener
    {
        Triangle initialTriangle;
        Triangulation dt;

        @Override
        public void onClick(View v) {
            if (voronoied==false)
            {
                voronoied=true;
                initialTriangle = new Triangle(
                        new Pnt(35,113),
                        new Pnt( 45, 113),
                        new Pnt(40, 119));
                dt=new Triangulation(initialTriangle);

                for (int i=500;i<List.size();i++)
                {
                    double coord[] = new double[2];
                    coord[0]=List.get(i).latitude;
                    coord[1]=List.get(i).longitude;

                    Pnt site = new Pnt(coord);
                    addSite(site);
                }

                HashSet<Pnt> done = new HashSet<Pnt>(initialTriangle);
                for (Triangle triangle: dt)
                    for (Pnt site:triangle)
                    {
                        if (done.contains(site)) continue;
                        done.add(site);
                        List<Triangle> list =dt.surroundingTriangles(site,triangle);
                        Pnt[] vertices = new Pnt[list.size()];
                        int i=0;
                        for (Triangle tri:list)
                            vertices[i++]=tri.getCircumcenter();
                        draw(vertices);
                    }
                DrawBoundary();
            }else {
                voronoied=false;
                mBaiduMap.clear();
            }

        }

        /**
         * Add a new site to the DT.
         * @param point the site to add
         */
        public void addSite(Pnt point) {
            dt.delaunayPlace(point);
        }

        /**
         * Draw a polygon.
         * @param polygon an array of polygon vertices
         */
        public void draw (Pnt[] polygon) {
            double latitude,longitude;
            List<LatLng> pts = new ArrayList<LatLng>();
            for (int i = 0; i < polygon.length; i++) {
                latitude =  polygon[i].coord(0);
                longitude =  polygon[i].coord(1);
                LatLng point = new LatLng(latitude,longitude);
                pts.add(point);
            }
            //构建用户绘制多边形的Option对象
            OverlayOptions polygonOption = new PolygonOptions()
                    .points(pts)
                    .stroke(new Stroke(3, 0xAA00FF00))
                    .fillColor(0x99FFFF00);
            //在地图上添加多边形Option，用于显示
            mBaiduMap.addOverlay(polygonOption);
        }

        public void DrawBoundary()
        {
            List<LatLng> pts = new ArrayList<LatLng>();
            InputStream inputStream = getResources().openRawResource(R.raw.beijingboundary);
            InputStreamReader inputStreamReader=null;
            try{
                inputStreamReader = new InputStreamReader(inputStream,"utf-8");
            } catch (UnsupportedEncodingException e1)
            {
                e1.printStackTrace();
            }

            BufferedReader reader = new BufferedReader(inputStreamReader);
            StringBuffer sb= new  StringBuffer("");
            String line;
            int row=0;
            double longitude=0,latitude=0;
            try {
                while ((line= reader.readLine()) != null)
                {
                    if (row==0)
                    {
                        longitude= Double.parseDouble(line);
                    }
                    else
                    {
                        latitude=Double.parseDouble(line);
                    }
                    row=(row+1)%2;
                    if (row==0) {
                        LatLng point = new LatLng(latitude, longitude);
                        pts.add(point);
                    }
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            //构建用户绘制多边形的Option对象
            OverlayOptions polygonOption = new PolygonOptions()
                    .points(pts)
                    .stroke(new Stroke(5, 0xAAFFFF00))
                    .fillColor(0x990000ff);
            //在地图上添加多边形Option，用于显示
            mBaiduMap.addOverlay(polygonOption);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if(firstLocate) //是否是第一次定位
                firstLocate=false;
            else
                marker.remove();
            //传入经纬度
            LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
            //自己的覆盖图片
            BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.mark);
            MarkerOptions option = new MarkerOptions().position(point).icon(bitmap).zIndex(9).period(10);;
            option.animateType(MarkerOptions.MarkerAnimateType.grow);
            //给地图添加一层覆盖
            marker = (Marker) (mBaiduMap.addOverlay(option));
            //更新位置信息
            mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(point));
        }
    }
}
