package com.specico.solarpesa;


import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.specico.solarpesa.Utils.Constants;
import com.specico.solarpesa.Utils.SharedPrefUtil;

import java.util.ArrayList;
import java.util.List;

import zh.wang.android.yweathergetter4a.WeatherInfo;
import zh.wang.android.yweathergetter4a.YahooWeather;
import zh.wang.android.yweathergetter4a.YahooWeatherInfoListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class GenerationFragment extends Fragment implements YahooWeatherInfoListener {


    public GenerationFragment() {
        // Required empty public constructor
    }

    PieChart pieChart;
    TextView tvTemp;
    ImageView tempIcon;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragmentv
        View view = inflater.inflate(R.layout.fragment_generation, container, false);

        YahooWeather mYahooWeather = YahooWeather.getInstance();
        mYahooWeather.setNeedDownloadIcons(true);
        mYahooWeather.setUnit(YahooWeather.UNIT.CELSIUS);
        mYahooWeather.setSearchMode(YahooWeather.SEARCH_MODE.PLACE_NAME);
        String district = SharedPrefUtil.getStringPreference(getContext(), Constants.USER_DISTRICT);
        String state = SharedPrefUtil.getStringPreference(getContext(), Constants.USER_STATE);
        String place = district+" "+state;
        Log.d("place",place);
        mYahooWeather.queryYahooWeatherByPlaceName(getContext(),place , this);

        pieChart = view.findViewById(R.id.pieChart);

        List<PieEntry> entries = new ArrayList<>();

        entries.add(new PieEntry(45f, ""));
        entries.add(new PieEntry(30f, ""));
        entries.add(new PieEntry(25f, ""));
//        entries.add(new PieEntry(25f, "Blue"));
//        entries.add(new PieEntry(25f, "White"));

        PieDataSet set = new PieDataSet(entries, "Results");
        set.setColors(Color.parseColor("#a9cf38"),Color.parseColor("#7cb5ec"),
                Color.parseColor("#00ffffff"));
        set.setDrawValues(false);
        PieData data = new PieData(set);
        pieChart.getDescription().setText("");
        pieChart.getLegend().setEnabled(false);
        pieChart.setHoleRadius(60f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setRotationAngle(135f);
        pieChart.setRotationEnabled(false);
        pieChart.animateXY(2000,2000);
        pieChart.setDrawCenterText(true);
        pieChart.setCenterText("1.56 kWh");
        pieChart.setCenterTextSize(30f);
        pieChart.setData(data);
        pieChart.invalidate(); // refresh

        tvTemp = view.findViewById(R.id.tvTemp);
        tempIcon = view.findViewById(R.id.tempIcon);



        return view;
    }

    @Override
    public void gotWeatherInfo(WeatherInfo weatherInfo, YahooWeather.ErrorType errorType) {
        if(weatherInfo != null) {
            // Add your code here
            // weatherInfo object contains all information returned by Yahoo Weather API
            // if `weatherInfo` is null, you can get the error from `errorType`
            int temp = weatherInfo.getCurrentTemp();
            Log.d("Temp",temp+" "+(char) 0x00B0);
            tvTemp.setText(temp+" "+(char) 0x00B0+"C");

            String icon = weatherInfo.getCurrentConditionIconURL();
            Log.d("icon", icon);

            Uri photoUri = Uri.parse(icon);
            Glide.with(this)
                    .load(photoUri)
                    .centerCrop()
                    .into(tempIcon);
        }
    }
}
