package com.a12599.myapplication;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.Dot;
import com.baidu.mapapi.map.DotOptions;
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
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.WeightedLatLng;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.SpatialRelationUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity {
    private final int citySize = 190;
    //private final int monitorSize = 13;
    MapView mMapView;
    BaiduMap mBaiduMap;
    private Intent it;
    private SQLiteDatabase db;
    private Cursor c;
    private LocationClient mLocationClient;
    private TextView textView13, textView12, textView22, textView23,textView21, textView01,textView02;
    private SimpleCursorAdapter adapter;
    private Spinner spinner1, spinner2, spinner3;
    private Marker marker;
    private HeatMap heatmap;
    private boolean firstLocate = true;
    private boolean firstDraw = true;
    private boolean firstMark = true;
    private boolean voronoied = false;
    private boolean loadBoundary = false;
    private String nameofpoint;
    private List<WeightedLatLng> weightedList = new ArrayList<>();
    private List<OverlayOptions> monitorList = new ArrayList<>();
    private List<Overlay> overlayList = new ArrayList<>();
    private BitmapDescriptor bitmap;
    private boolean dbable = true;

    private double sweepLoc;
    private final ArrayList<Point> sites = new ArrayList<>();
    private final ArrayList<VoronoiEdge> edgeList = new ArrayList<>();
    private HashSet<BreakPoint> breakPoints;
    private TreeMap<ArcKey, CircleEvent> arcs;
    private TreeSet<Event> events;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        mMapView = (MapView) findViewById(R.id.mapView);

        initMap();

        MyDBOpenHelper myDBOpenHelper;
        myDBOpenHelper = new MyDBOpenHelper(MainActivity.this, "data.db", 1);
        db = myDBOpenHelper.getWritableDatabase();

        spinner2 = (Spinner) findViewById(R.id.spinner2);
        c = db.rawQuery("select rowid _id,time from value group by time", null);
        if (c.moveToFirst()) {
            adapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, c, new String[]{"time"}, new int[]{android.R.id.text1}, 0);
            spinner2.setAdapter(adapter);
        }
        spinner1 = (Spinner) findViewById(R.id.spinner1);
        spinner3 = (Spinner) findViewById(R.id.spinner3);

        textView13 = (TextView) findViewById(R.id.textView13);
        textView13.setOnClickListener(new TxtClickListener13());

        textView12 = (TextView) findViewById(R.id.textView12);
        textView12.setOnClickListener(new TxtClickListener12());

        textView22 = (TextView) findViewById(R.id.textView22);
        textView22.setOnClickListener(new TxtClickListener22());

        textView23 = (TextView) findViewById(R.id.textView23);
        textView23.setOnClickListener(new TxtClickListener23());

        textView21 = (TextView) findViewById(R.id.textView21);
        textView21.setOnClickListener(new TxtClickListener21());

        textView01 = (TextView) findViewById(R.id.textView01);
        textView01.setOnClickListener(new TxtClickListener01());

        textView02=(TextView) findViewById(R.id.textView02);
        textView02.setOnClickListener(new TxtClickListener02());

        it =new Intent(this,Main2Activity.class);

        bitmap = BitmapDescriptorFactory.fromResource(R.drawable.mark);
        nameofpoint="安阳铁佛寺";
    }

    //refresh
        class TxtClickListener12 implements View.OnClickListener {
            @Override
            public void onClick(View v) {
            new Thread() {
                @Override
                public void run() {
                    dbable = false;
                    db.execSQL("delete from value where 1=1");
                    //crawal the data
                    try {
                        String s1 = "http://www.pm25china.net/";
                        URL url1 = new URL(s1); //首页
                        BufferedReader reader1;
                        reader1 = new BufferedReader(new InputStreamReader(url1.openStream()));
                        String line1, line2, line3;
                        String point, time, pollution = "", value;

                        int start1, end1;
                        while ((line1 = reader1.readLine()) != null) {
                            if (line1.contains("A&nbsp;&nbsp;")) //定位到首页A
                            {
                                do {
                                    start1 = 0;
                                    while ((start1 = line1.indexOf("k\" href", start1)) >= 0) {
                                        start1 += 10;
                                        end1 = line1.indexOf("/", start1) + 1;
                                        String s2 = s1 + line1.substring(start1, end1); //定位到每个城市的URL
                                        //String currentCity = line1.substring(line1.indexOf("g", end1) + 2, line1.indexOf("/", end1) - 1);

                                        //打开每个城市
                                        URL url2 = new URL(s2);
                                        BufferedReader reader2 = new BufferedReader(new InputStreamReader(url2.openStream()));
                                        int start2, end2;
                                        while ((line2 = reader2.readLine()) != null) {
                                            if ((start2 = line2.indexOf("d><a")) >= 0) {
                                                start2 += 12;
                                                end2 = line2.indexOf("\"", start2);
                                                String s3 = s1 + line2.substring(start2, end2);

                                                //写入监测站名
                                                point = line1.substring(line1.indexOf("g", end1) + 2, line1.indexOf("/", end1) - 1) + line2.substring(line2.indexOf(">", end2) + 1, line2.indexOf("<", end2));

                                                //打开每个监测站
                                                URL url3 = new URL(s3);
                                                BufferedReader reader3 = new BufferedReader(new InputStreamReader(url3.openStream()));
                                                int start3, end3;
                                                while ((line3 = reader3.readLine()) != null) {
                                                    if ((start3 = line3.indexOf("function")) >= 0) {
                                                        //写入污染物名
                                                        pollution = line3.substring(start3 + 9, line3.indexOf("("));
                                                        continue;
                                                    }
                                                    if ((start3 = line3.indexOf("name='")) >= 0) {
                                                        start3 += 6;
                                                        end3 = line3.indexOf("'", start3);
                                                        //写入时间
                                                        time = line3.substring(start3, end3);

                                                        start3 = line3.indexOf("'", end3 + 1) + 1;
                                                        end3 = line3.indexOf("'", start3);
                                                        //写入数值
                                                        value = line3.substring(start3, end3);
                                                        db.execSQL(String.format("insert into value values(\"%s\",\"%s\",\"%s\",%s)", point, pollution, time, value));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } while ((line1 = reader1.readLine()) != null);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    dbable = true;
                }

            }.start();
            Toast.makeText(MainActivity.this, "refreshing...",
                    Toast.LENGTH_LONG).show();
        }
    }

    //locate
    class TxtClickListener13 implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mLocationClient.requestLocation();
        }
    }

    //draw
    class TxtClickListener22 implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            /*if(!dbable)
            {
                Toast.makeText(MainActivity.this, "refreshing...",
                        Toast.LENGTH_LONG).show();
                return;
            }*/
            if (firstDraw)
                firstDraw = false;
            else {
                weightedList.clear();
                heatmap.removeHeatMap();
            }
            if (!monitorList.isEmpty()) {
                for (int i = overlayList.size() - 1; i >= 0; i--)
                    overlayList.get(i).remove();
                overlayList.clear();
                monitorList.clear();
            }

            //设置渐变颜色值
            int[] DEFAULT_GRADIENT_COLORS = {ContextCompat.getColor(spinner1.getContext(), R.color.start), ContextCompat.getColor(spinner1.getContext(), R.color.end)};
            //设置渐变颜色起始值
            float[] DEFAULT_GRADIENT_START_POINTS = {0.2f, 1f};
            //构造颜色渐变对象
            Gradient gradient = new Gradient(DEFAULT_GRADIENT_COLORS, DEFAULT_GRADIENT_START_POINTS);

            String time, pollution;
            Cursor temp = (Cursor) spinner2.getSelectedItem();
            time = temp.getString(1);
            pollution = spinner1.getSelectedItem().toString();

            Toast.makeText(MainActivity.this, time + " " + pollution,
                    Toast.LENGTH_LONG).show();

            c = db.rawQuery(String.format("select position.point,lat,lng,value from position inner join " +
                    "(select * from value where time=\"%s\" and pollution=\"%s\") as temp on position.point=temp.point", time, pollution), null);
            if (c.moveToFirst()) {
                do {
                    LatLng latLng = new LatLng(c.getDouble(c.getColumnIndex("lat")), c.getDouble(c.getColumnIndex("lng")));
                    String monitorName = c.getString(0);
                    double value = c.getDouble(c.getColumnIndex("value"));

                    //创建weigthedList
                    WeightedLatLng weightedLatLng = new WeightedLatLng(latLng, value);
                    weightedList.add(weightedLatLng);

                    //创建monitorMarker
                    Bundle bundle = new Bundle();
                    bundle.putString("info", monitorName + "\n" + time + "\n" + pollution + ": " + Double.toString(value));
                    MarkerOptions option = new MarkerOptions().position(latLng).title(monitorName).icon(bitmap).zIndex(9).period(10).extraInfo(bundle);
                    option.animateType(MarkerOptions.MarkerAnimateType.grow);
                    monitorList.add(option);
                } while (c.moveToNext());
            }
            heatmap = new HeatMap.Builder()
                    .weightedData(weightedList)
                    .gradient(gradient)
                    .radius(50)
                    .build();

            //在地图上添加热力图
            mBaiduMap.addHeatMap(heatmap);

            if (!firstMark) {
                //更新monitor
                overlayList = (mBaiduMap.addOverlays(monitorList));
            }
        }
    }

    //mark
    class TxtClickListener23 implements View.OnClickListener {
              @Override
            public void onClick(View v) {
                if (monitorList.isEmpty()) {
                    Toast.makeText(MainActivity.this, "please draw first",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                if (firstMark) {
                    //添加monitor
                    firstMark = false;
                    overlayList = mBaiduMap.addOverlays(monitorList);
                } else {
                    //删除monitor
                    firstMark = true;
                    for (int i = overlayList.size() - 1; i >= 0; i--)
                        overlayList.get(i).remove();
                }
        }
    }

    //time
    class TxtClickListener21 implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            c = db.rawQuery("select rowid _id,time from value group by time", null);
            if (c.moveToFirst()) {
                adapter = new SimpleCursorAdapter(MainActivity.this, android.R.layout.simple_spinner_item, c, new String[]{"time"}, new int[]{android.R.id.text1}, 0);
                spinner2.setAdapter(adapter);
            }
        }
    }

    class TxtClickListener01 implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (voronoied == true) {
                voronoied = false;
                mBaiduMap.clear();
            }
            else {
                voronoied = true;
                int i = spinner3.getSelectedItemPosition();
                    List<LatLng> bound = new ArrayList<>();
                    List<LatLng> box = new ArrayList<>();
                    c = db.rawQuery(String.format("select lat,lng from boundary where city = %d",i), null);
                    double maxlat = 0, minlat = 90, maxlng = 0, minlng = 180;
                    if (c.moveToFirst()){
                        do{
                            double lat = c.getDouble(c.getColumnIndex("lat"));
                            double lng = c.getDouble(c.getColumnIndex("lng"));
                            LatLng point = new LatLng(lat,lng);
                            bound.add(point);
                            if (lat>maxlat)
                                maxlat = lat;
                            if (lat<minlat)
                                minlat = lat;
                            if (lng>maxlng)
                                maxlng = lng;
                            if (lng<minlng)
                                minlng = lng;
                        }while (c.moveToNext());
                    }
                    //构建用户绘制多边形的Option对象
                    for (int j=0; j<bound.size();j++) {
                        Point boundPoint1 = new Point(bound.get(j).longitude, bound.get(j).latitude);
                        int next = (j == bound.size()-1) ? 0 : j + 1;
                        Point boundPoint2 = new Point(bound.get(next).longitude, bound.get(next).latitude);
                        List<LatLng> points = new ArrayList<>();
                        points.add(new LatLng(boundPoint1.y,boundPoint1.x));
                        points.add(new LatLng(boundPoint2.y,boundPoint2.x));
                        OverlayOptions ooPolyline =  new PolylineOptions().width(5).color(Color.YELLOW).points(points);
                        Polyline mPolyline = (Polyline) mBaiduMap.addOverlay(ooPolyline);
                    }

                    box.add(new LatLng(maxlat,maxlng));
                    box.add(new LatLng(minlat,maxlng));
                    box.add(new LatLng(minlat,minlng));
                    box.add(new LatLng(maxlat,minlng));
                    OverlayOptions polygonOption1 = new PolygonOptions()
                            .points(box)
                            .stroke(new Stroke(5, Color.GREEN))
                            .fillColor(ContextCompat.getColor(spinner1.getContext(),R.color.none))
                            .zIndex(8);
                    mBaiduMap.addOverlay(polygonOption1);

                    int monitorNum = 0;
                    ArrayList<Point> sites = new ArrayList<>();
                    c = db.rawQuery(String.format("select point,lat,lng from position where city = %d",i), null);
                    if (c.moveToFirst()) {
                        do {
                            double lat = c.getDouble(c.getColumnIndex("lat"));
                            double lng = c.getDouble(c.getColumnIndex("lng"));
                            String monitorName = c.getString(0);
                            System.out.println(monitorName);
                            monitorNum++;
                            Point monitor = new Point(lng,lat);
                            boolean contain = false;
                            for (Point p : sites)
                                if (p.x == monitor.x && p.y == monitor.y) {
                                    contain = true;
                                    break;
                                }
                            if (!contain)
                                sites.add(monitor);
                            DotOptions dotOptions = new DotOptions().center(new LatLng(lat,lng)).color(Color.BLACK).radius(10);
                            Dot mDot = (Dot) mBaiduMap.addOverlay(dotOptions);
                        } while (c.moveToNext());
                    }
                    Voronoi voronoi = new Voronoi(sites, maxlat, minlat, maxlng, minlng);
                    int index = 0;
                    for (VoronoiEdge e : voronoi.edgeList) {
                        if (e.p1 != null && e.p2 != null) {
                            //double topY = (e.p1.y == Double.POSITIVE_INFINITY) ? maxlat : e.p1.y; // HACK to draw from infinity
                            index++;
                            SpatialRelationUtil spatialRelationUtil =  new SpatialRelationUtil();
                            LatLng p1 = new LatLng(e.p1.y, e.p1.x);
                            LatLng p2 = new LatLng(e.p2.y, e.p2.x);
                            double minDist = 1000;
                            Point near = null;
                            //Both end is out of the boundary
                            if (!spatialRelationUtil.isPolygonContainsPoint(bound, p1) &&
                                    !spatialRelationUtil.isPolygonContainsPoint(bound, p2)) {
                                boolean cross = false;
                                Point firstInter = null, intersect = null;
                                int numInter = 0;
                                for (int j=0; j<bound.size();j++) {
                                    Point boundPoint1 = new Point(bound.get(j).longitude, bound.get(j).latitude);
                                    int next = (j == bound.size() - 1) ? 0 : j + 1;
                                    Point boundPoint2 = new Point(bound.get(next).longitude, bound.get(next).latitude);
                                    intersect = e.intersection(boundPoint1, boundPoint2);
                                    if (intersect!= null && intersect.inLine(boundPoint1,boundPoint2)){
                                        numInter++;
                                        if (firstInter == null){
                                            firstInter = intersect;
                                        }else {
                                            double x1 = e.p1.x - intersect.x, x2 = e.p2.x - intersect.x;
                                            double y1 = e.p1.y - intersect.y, y2 = e.p2.y - intersect.y;
                                            System.out.println(boundPoint1.toString()+ "\n" + boundPoint2.toString());
                                            System.out.println(intersect.toString());
                                            if (x1 * x2 + y1 * y2 < 0)
                                                cross = true;
                                            else
                                                cross = false;
                                            break;
                                        }
                                    }
                                }
                                if (cross) {
                                    e.p1 = firstInter;
                                    e.p2 = intersect;
                                } else
                                    continue;
                            } else
                            //either one end is out of the boundary
                            if (!spatialRelationUtil.isPolygonContainsPoint(bound, p1)){
                                for (int j=0; j<bound.size();j++){
                                    Point boundPoint1 = new Point(bound.get(j).longitude, bound.get(j).latitude);
                                    int next = (j == bound.size()-1)? 0: j+1;
                                    Point boundPoint2 = new Point(bound.get(next).longitude, bound.get(next).latitude);
                                    Point intersect = e.intersection(boundPoint1,boundPoint2);
                                    if (intersect!= null && intersect.inLine(boundPoint1,boundPoint2)){
                                        double x1 = intersect.x - e.p2.x, x2 = e.p1.x - e.p2.x;
                                        double y1 = intersect.y - e.p2.y, y2 = e.p1.y - e.p2.y;
                                        if (x1*x2 + y1*y2 > 0) {
                                            if (intersect.distanceTo(e.p2) < minDist) {
                                                minDist = intersect.distanceTo(e.p2);
                                                near = intersect;
                                            }
                                        }
                                    }
                                }
                                if (near != null)
                                     e.p1 = near;
                            } else if (!spatialRelationUtil.isPolygonContainsPoint(bound, p2)){
                                for (int j=0; j<bound.size();j++) {
                                    Point boundPoint1 = new Point(bound.get(j).longitude, bound.get(j).latitude);
                                    int next = (j == bound.size()-1) ? 0 : j + 1;
                                    Point boundPoint2 = new Point(bound.get(next).longitude, bound.get(next).latitude);
                                    Point intersect = e.intersection(boundPoint1, boundPoint2);
                                    if (intersect != null && intersect.inLine(boundPoint1, boundPoint2)) {
                                        double x1 = intersect.x - e.p1.x, x2 = e.p2.x - e.p1.x;
                                        double y1 = intersect.y - e.p1.y, y2 = e.p2.y - e.p1.y;
                                        if (x1*x2 + y1*y2 > 0){
                                            if (intersect.distanceTo(e.p1) < minDist) {
                                                minDist = intersect.distanceTo(e.p1);
                                                near = intersect;
                                            }
                                        }
                                    }
                                }
                                if (near != null)
                                    e.p2 = near;
                            }

                            List<LatLng> points = new ArrayList<>();
                            points.add(new LatLng(e.p1.y,e.p1.x));
                            points.add(new LatLng(e.p2.y,e.p2.x));
                            OverlayOptions ooPolyline =  new PolylineOptions().width(5).color(Color.RED).points(points);
                            Polyline mPolyline = (Polyline) mBaiduMap.addOverlay(ooPolyline);
                        }
                    }
            }
        }
    }

    class TxtClickListener02 implements View.OnClickListener{
        @Override
        public void onClick(View v){
            it.removeExtra("检测点");
            it.removeExtra("污染物");
            it.putExtra("检测点",nameofpoint);
            it.putExtra("污染物",spinner1.getSelectedItem().toString());
            startActivity(it);
        }
    }
    /*百度地图*/
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

    //初始化地图
    private void initMap() {
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

        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener(new MyLocationListener());    //注册监听函数
        mLocationClient.setLocOption(option);
        mLocationClient.start();

        mBaiduMap = mMapView.getMap();
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //从marker中获取info信息
                TextView tv = new TextView(MainActivity.this);
                tv.setText(marker.getExtraInfo().getString("info"));
                BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromView(tv);
                //infowindow位置
                LatLng latLng = marker.getPosition();
                //infowindow点击事件
                InfoWindow.OnInfoWindowClickListener listener = new InfoWindow.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick() {
                        //隐藏infowindow
                        mBaiduMap.hideInfoWindow();
                        nameofpoint="安阳铁佛寺";
                    }
                };
                //显示infowindow
                InfoWindow infoWindow = new InfoWindow(bitmapDescriptor, latLng, -100, listener);
                mBaiduMap.showInfoWindow(infoWindow);
                nameofpoint=marker.getExtraInfo().getString("info").substring(0,marker.getExtraInfo().getString("info").indexOf("\n"));
                return true;
            }
        });
    }

    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (firstLocate) //是否是第一次定位
                firstLocate = false;
            else
                marker.remove();
            //传入经纬度
            LatLng point = new LatLng(location.getLatitude(), location.getLongitude());

            Bundle bundle = new Bundle();
            bundle.putString("info", " ");

            //自己的覆盖图片
            MarkerOptions option = new MarkerOptions().position(point).title("current position").icon(bitmap).zIndex(9).period(10).extraInfo(bundle);
            option.animateType(MarkerOptions.MarkerAnimateType.grow);
            //给地图添加一层覆盖
            marker = (Marker) (mBaiduMap.addOverlay(option));
            //更新位置信息
            mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(point));
        }
    }
}

class MyDBOpenHelper extends SQLiteOpenHelper {
    private Context mContext;
    private final int citySize = 190;

    public MyDBOpenHelper(Context context, String name, int version) {
        super(context, name, null, version);
        mContext = context;
    }

    @Override
    //数据库第一次创建时被调用
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE position(point text,lat real,lng real,city int)");
        db.execSQL("CREATE TABLE value(point text,pollution text,time text,value real)");
        db.execSQL("CREATE TABLE boundary(city int, lat real, lng real)");
        //将pos导入数据库
        InputStream inputStream = mContext.getResources().openRawResource(R.raw.pos);
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(inputStream, "utf-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(inputStreamReader);
        String point, lat, lng;
        try {
            int city = 0, step = 0;
            String last = null;
            while ((point = reader.readLine()) != null) {
                if (step > 0){
                    if (point.substring(0,2).equals(last) == false)
                        city++;
                }
                step++;
                last = point.substring(0,2);
                lat = reader.readLine();
                lng = reader.readLine();
                db.execSQL(String.format("INSERT INTO position values(\"%s\",%s,%s,%d)", point, lat, lng, city));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            inputStream.close();
            inputStreamReader.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //将data导入数据库
        inputStream = mContext.getResources().openRawResource(R.raw.data);
        try {
            inputStreamReader = new InputStreamReader(inputStream, "utf-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        reader = new BufferedReader(inputStreamReader);
        int index;
        String line, pollution = "", time, value;
        point = "";
        try {
            while ((line = reader.readLine()) != null) {
                if (line.contains("#")) {
                    point = line.substring(1);
                    continue;
                }
                if (line.contains("$")) {
                    pollution = line.substring(1);
                    continue;
                }
                index = line.indexOf(' ');
                time = line.substring(0, index);
                value = line.substring(index + 1, line.length());
                db.execSQL(String.format("INSERT INTO value values(\"%s\",\"%s\",\"%s\",%s)", point, pollution, time, value));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            inputStream.close();
            inputStreamReader.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //input for the city boundary
        inputStream = mContext.getResources().openRawResource(R.raw.boundaries);
        inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(inputStream, "utf-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        reader = new BufferedReader(inputStreamReader);
        String points;
        try {
            for (int i=0;i<citySize;i++)
            {
                points = reader.readLine();
                int colon = 0, semicolon = -1;
                while (true)
                {
                    colon = points.indexOf(",");
                    lng = points.substring(0, colon);
                    semicolon = points.indexOf(";");
                    if (semicolon!= -1)
                        lat = points.substring(colon+2, semicolon);
                    else {
                        lat = points.substring(colon + 2);
                        break;
                    }
                    db.execSQL(String.format("INSERT INTO boundary values(%d,%s,%s)", i,lat,lng));
                    //LatLng monitor = new LatLng(Double.parseDouble(lat),Double.parseDouble(lng));
                    //cityBoundaries[i][step] = monitor;
                    points= points.substring(semicolon+1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            inputStream.close();
            inputStreamReader.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //软件版本号发生改变时调用
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
    }
}