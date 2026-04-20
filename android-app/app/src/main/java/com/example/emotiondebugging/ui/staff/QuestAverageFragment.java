package com.example.emotiondebugging.ui.staff;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.model.response.QuestTrendReportResponse;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class QuestAverageFragment extends Fragment {

    private QuestReportViewModel viewModel;
    private ImageView btnFilterMetric;
    private LineChart lineChartAssigned;
    private LineChart lineChartCompleted;

    public QuestAverageFragment() {
        super(R.layout.fragment_quest_average);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(QuestReportViewModel.class);

        btnFilterMetric = view.findViewById(R.id.btnFilterMetric);
        lineChartAssigned = view.findViewById(R.id.lineChartAssigned);
        lineChartCompleted = view.findViewById(R.id.lineChartCompleted);

        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getTrendReport().observe(getViewLifecycleOwner(), report -> {
            if (report != null) {
                drawAssignedChart(report.getAssigned());
                drawCompletedChart(report.getCompleted());
            }
        });

        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void drawAssignedChart(List<QuestTrendReportResponse.QuestTrendAssignedItem> items) {
        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            entries.add(new Entry(i, items.get(i).getTotal_assigned()));
            labels.add(items.get(i).getChart_date());
        }

        LineDataSet dataSet = new LineDataSet(entries, "Assigned");
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);

        LineData data = new LineData(dataSet);
        lineChartAssigned.setData(data);

        XAxis xAxis = lineChartAssigned.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelRotationAngle(-30f);

        lineChartAssigned.getAxisRight().setEnabled(false);
        Description description = new Description();
        description.setText("");
        lineChartAssigned.setDescription(description);
        lineChartAssigned.invalidate();
    }

    private void drawCompletedChart(List<QuestTrendReportResponse.QuestTrendCompletedItem> items) {
        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            entries.add(new Entry(i, items.get(i).getTotal_completed()));
            labels.add(items.get(i).getChart_date());
        }

        LineDataSet dataSet = new LineDataSet(entries, "Completed");
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);

        LineData data = new LineData(dataSet);
        lineChartCompleted.setData(data);

        XAxis xAxis = lineChartCompleted.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelRotationAngle(-30f);

        lineChartCompleted.getAxisRight().setEnabled(false);
        Description description = new Description();
        description.setText("");
        lineChartCompleted.setDescription(description);
        lineChartCompleted.invalidate();
    }
}