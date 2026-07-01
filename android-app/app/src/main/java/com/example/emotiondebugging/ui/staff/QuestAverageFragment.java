package com.example.emotiondebugging.ui.staff;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.model.response.QuestMonthlyMetricResponse;
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
    private TextView topTitle;
    private TextView bottomTitle;
    private LineChart topChart;
    private LineChart bottomChart;

    public QuestAverageFragment() { super(R.layout.fragment_quest_average); }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        viewModel = new ViewModelProvider(requireActivity()).get(QuestReportViewModel.class);
        ImageView filter = view.findViewById(R.id.btnFilterMetric);
        topTitle = view.findViewById(R.id.tvChartTitle1);
        bottomTitle = view.findViewById(R.id.tvChartTitle2);
        topChart = view.findViewById(R.id.lineChartTop);
        bottomChart = view.findViewById(R.id.lineChartBottom);

        viewModel.getMonthlyMetrics().observe(getViewLifecycleOwner(), value -> updateCharts());
        viewModel.getSelectedMetric().observe(getViewLifecycleOwner(), value -> updateCharts());
        viewModel.getMessage().observe(getViewLifecycleOwner(), value -> {
            if (value != null && !value.isEmpty()) Toast.makeText(requireContext(), value, Toast.LENGTH_SHORT).show();
        });
        filter.setOnClickListener(v -> showMetricDialog());
    }

    private void updateCharts() {
        List<QuestMonthlyMetricResponse> rows = viewModel.getMonthlyMetrics().getValue();
        String metric = viewModel.getSelectedMetric().getValue();
        topTitle.setText(metric);
        bottomTitle.setText(QuestReportViewModel.METRIC_COMPLETION);
        drawChart(topChart, rows, metric);
        drawChart(bottomChart, rows, QuestReportViewModel.METRIC_COMPLETION);
    }

    private void drawChart(LineChart chart, List<QuestMonthlyMetricResponse> rows, String metric) {
        if (rows == null || rows.isEmpty()) {
            chart.clear();
            chart.setNoDataText("No student runs yet");
            chart.invalidate();
            return;
        }
        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (int index = 0; index < rows.size(); index++) {
            QuestMonthlyMetricResponse row = rows.get(index);
            entries.add(new Entry(index, metricValue(row, metric)));
            labels.add(row.getChart_month());
        }

        LineDataSet set = new LineDataSet(entries, metric);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setDrawValues(false);
        chart.setData(new LineData(set));
        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelRotationAngle(-25f);
        chart.getAxisRight().setEnabled(false);
        Description description = new Description();
        description.setText("");
        chart.setDescription(description);
        chart.invalidate();
    }

    private float metricValue(QuestMonthlyMetricResponse row, String metric) {
        if (QuestReportViewModel.METRIC_COMPLETION.equals(metric)) return row.getCompletionRate();
        if (QuestReportViewModel.METRIC_ABANDONMENT.equals(metric)) return row.getAbandonmentRate();
        if (QuestReportViewModel.METRIC_DURATION.equals(metric)) return row.getAverageDurationMinutes();
        return row.getTotalRuns();
    }

    private void showMetricDialog() {
        View content = LayoutInflater.from(requireContext()).inflate(R.layout.layout_metric_menu, null);
        CheckBox runs = content.findViewById(R.id.cbSeverity);
        CheckBox completion = content.findViewById(R.id.cbSeverityRate);
        CheckBox abandonment = content.findViewById(R.id.cbTotalErrors);
        CheckBox duration = content.findViewById(R.id.cbAcceptance);
        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setView(content).create();
        runs.setOnClickListener(v -> select(dialog, QuestReportViewModel.METRIC_RUNS));
        completion.setOnClickListener(v -> select(dialog, QuestReportViewModel.METRIC_COMPLETION));
        abandonment.setOnClickListener(v -> select(dialog, QuestReportViewModel.METRIC_ABANDONMENT));
        duration.setOnClickListener(v -> select(dialog, QuestReportViewModel.METRIC_DURATION));
        dialog.show();
    }

    private void select(AlertDialog dialog, String metric) {
        viewModel.setSelectedMetric(metric);
        dialog.dismiss();
    }
}
