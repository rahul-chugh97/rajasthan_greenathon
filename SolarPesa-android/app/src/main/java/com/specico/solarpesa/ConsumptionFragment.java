package com.specico.solarpesa;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ConsumptionFragment extends Fragment {

    LineChart lineChart;

    public ConsumptionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_consumption, container, false);

        lineChart = view.findViewById(R.id.lineChart);

        List<Entry> listEntry = new ArrayList<>();
        listEntry.add(new Entry(0.0f,1.4f));
        listEntry.add(new Entry(1.0f,1.2f));
        listEntry.add(new Entry(2.0f,1.3f));
        listEntry.add(new Entry(3.0f,1.5f));
        listEntry.add(new Entry(4.0f,1.5f));
        listEntry.add(new Entry(5.0f,1.6f));
        listEntry.add(new Entry(6.0f,1.4f));
        listEntry.add(new Entry(7.0f,1.2f));
        listEntry.add(new Entry(8.0f,1.4f));
        listEntry.add(new Entry(9.0f,1.6f));
        listEntry.add(new Entry(10.0f,1.6f));
        listEntry.add(new Entry(11.0f,1.6f));
        listEntry.add(new Entry(12.0f,1.3f));
        listEntry.add(new Entry(13.0f,0.0f));
        listEntry.add(new Entry(14.0f,0.3f));
        listEntry.add(new Entry(15.0f,0.7f));
        listEntry.add(new Entry(16.0f,1.2f));
        listEntry.add(new Entry(17.0f,1.4f));
        listEntry.add(new Entry(18.0f,1.6f));
        listEntry.add(new Entry(19.0f,1.5f));
        listEntry.add(new Entry(20.0f,1.5f));

        List<Entry> listEntry2 = new ArrayList<>();

        listEntry2.add(new Entry(20.0f,1.5f));
        listEntry2.add(new Entry(21.0f,1.6f));
        listEntry2.add(new Entry(22.0f,1.3f));
        listEntry2.add(new Entry(23.0f,0.0f));
        listEntry2.add(new Entry(24.0f,0.3f));
        listEntry2.add(new Entry(25.0f,0.7f));
        listEntry2.add(new Entry(26.0f,1.2f));
        listEntry2.add(new Entry(27.0f,1.4f));
        listEntry2.add(new Entry(28.0f,1.6f));
        listEntry2.add(new Entry(29.0f,1.5f));
        listEntry2.add(new Entry(30.0f,1.5f));

        LineDataSet lineDataSet = new LineDataSet(listEntry, "Company 1");
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);

        lineDataSet.setColor(R.color.green);
        lineDataSet.setCircleColor(R.color.green);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setDrawHighlightIndicators(false);
        lineDataSet.setDrawValues(false);

        LineDataSet lineDataSet2 = new LineDataSet(listEntry2, "Company 2");
        lineDataSet2.setAxisDependency(YAxis.AxisDependency.LEFT);

        lineDataSet2.setColor(R.color.blue);
        lineDataSet2.setCircleColor(R.color.blue);
        lineDataSet2.setDrawCircles(false);
        lineDataSet2.setDrawHighlightIndicators(false);
        lineDataSet2.setDrawValues(false);

        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(lineDataSet);
        dataSets.add(lineDataSet2);

        LineData data = new LineData(dataSets);

        XAxis xAxis = lineChart.getXAxis();

        xAxis.setDrawGridLines(false);
//      xAxis.setGranularity(30000f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        lineChart.getDescription().setText("");
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setHighlightPerTapEnabled(true);
        lineChart.setDoubleTapToZoomEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.getAxisLeft().setAxisMinimum(0f);
        lineChart.setData(data);
        lineChart.invalidate();

        return view;
    }

}
