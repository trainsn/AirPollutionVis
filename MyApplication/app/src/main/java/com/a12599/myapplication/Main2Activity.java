package com.a12599.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.view.LineChartView;

public class Main2Activity extends AppCompatActivity {
    private SQLiteDatabase db;
    private Cursor c;
    private LineChartView chart1;
    private Spinner spinner1, spinner2;

    //private LineChartView chart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        MyDBOpenHelper myDBOpenHelper;
        myDBOpenHelper = new MyDBOpenHelper(Main2Activity.this, "data.db", 1);
        db = myDBOpenHelper.getWritableDatabase();

        spinner2 = (Spinner) findViewById(R.id.spinner2);
        spinner1 = (Spinner) findViewById(R.id.spinner1);
        Intent it = getIntent();
        int pos = 0;
        switch (it.getStringExtra("污染物")) {
            case "aqi":
                pos = 0;
                break;
            case "pm25":
                pos = 1;
                break;
            case "pm10":
                pos = 2;
                break;
            case "co":
                pos = 3;
                break;
            case "so2":
                pos = 4;
                break;
            case "no2":
                pos = 5;
                break;
            case "o3":
                pos = 6;
                break;
        }
        spinner2.setSelection(pos);

        chart1 = (LineChartView) findViewById(R.id.chart1);
        setChart();

        TextView textView3, textView4;
        textView3 = (TextView) findViewById(R.id.textView3);
        textView3.setOnClickListener(new TxtClickListener3());
        textView4 = (TextView) findViewById(R.id.textView4);
        textView4.setOnClickListener(new TxtClickListener4());
    }

    class TxtClickListener3 implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            setChart();
        }
    }

    private void setChart(){
        Intent it = getIntent();
        String time;
        if (spinner1.getSelectedItem().toString().equals("最近一天")) {
            time = "时";
        } else {
            time = "月";
        }
        String pollution = spinner2.getSelectedItem().toString();
        String position = it.getStringExtra("检测点");

        c = db.rawQuery(String.format("select time,value from value where point = \"%s\" and pollution=\"%s\"", position, pollution), null);

        List<PointValue> mPointValues = new ArrayList<>();
        List<AxisValue> mAxisXValues = new ArrayList<>();


        if (c.moveToFirst()) {
            for (int i = 0; c.moveToNext(); i++) {
                String temp = c.getString(c.getColumnIndex("time"));
                if (temp.contains(time)) {
                    mAxisXValues.add(new AxisValue(i).setLabel(temp));
                    mPointValues.add(new PointValue(i, c.getFloat(c.getColumnIndex("value"))));
                }
            }
        }

        Line line = new Line(mPointValues).setColor(R.color.blue);  //折线的颜色
        List<Line> lines = new ArrayList<>();
        line.setShape(ValueShape.CIRCLE);//折线图上每个数据点的形状  这里是圆形 （有三种 ：ValueShape.SQUARE  ValueShape.CIRCLE  ValueShape.DIAMOND）
        line.setCubic(true);//曲线是否平滑，即是曲线还是折线
        line.setFilled(false);//是否填充曲线的面积
        line.setHasLabels(true);//曲线的数据坐标是否加上备注
        //line.setHasLabelsOnlyForSelected(true);//点击数据坐标提示数据（设置了这个line.setHasLabels(true);就无效）
        line.setHasLines(true);//是否用线显示。如果为false 则没有曲线只有点显示
        line.setHasPoints(true);//是否显示圆点 如果为false 则没有原点只有点显示（每个数据点都是个大的圆点）
        lines.add(line);
        LineChartData data = new LineChartData();
        data.setLines(lines);

        //坐标轴
        Axis axisX = new Axis(); //X轴
        axisX.setHasTiltedLabels(true);  //X坐标轴字体是斜的显示还是直的，true是斜的显示
        axisX.setTextColor(Color.GRAY);  //设置字体颜色
        axisX.setName(position);  //x轴名称
        axisX.setTextSize(10);//设置字体大小
        axisX.setMaxLabelChars(8); //最多几个X轴坐标，意思就是你的缩放让X轴上数据的个数7<=x<=mAxisXValues.length
        axisX.setValues(mAxisXValues);  //填充X轴的坐标名称
        axisX.setHasLines(true); //x 轴分割线
        data.setAxisXBottom(axisX); //x 轴在底部
        //data.setAxisXTop(axisX);  //x 轴在顶部

        // Y轴是根据数据的大小自动设置Y轴上限
        Axis axisY = new Axis();  //Y轴
        axisY.setName(pollution);//y轴名称
        axisY.setTextColor(Color.GRAY);
        axisY.setTextSize(10);//设置字体大小
        data.setAxisYLeft(axisY);  //Y轴设置在左边
        //data.setAxisYRight(axisY);  //y轴设置在右边

        //设置行为属性，支持缩放、滑动以及平移
        chart1.setInteractive(true);
        chart1.setZoomType(ZoomType.HORIZONTAL);
        chart1.setMaxZoom((float) 6);//最大方法比例
        chart1.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        chart1.setLineChartData(data);
        chart1.setVisibility(View.VISIBLE);
    }

    class TxtClickListener4 implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            finish();
        }
    }
}
