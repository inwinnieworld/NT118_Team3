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
    private ImageView btnFilterMetric;
    private TextView tvChartTitle1;
    private TextView tvChartTitle2;
    private LineChart lineChartTop;
    private LineChart lineChartBottom;

    public QuestAverageFragment() {
        super(R.layout.fragment_quest_average);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(QuestReportViewModel.class);

        btnFilterMetric = view.findViewById(R.id.btnFilterMetric);
        tvChartTitle1 = view.findViewById(R.id.tvChartTitle1);
        tvChartTitle2 = view.findViewById(R.id.tvChartTitle2);
        lineChartTop = view.findViewById(R.id.lineChartTop);
        lineChartBottom = view.findViewById(R.id.lineChartBottom);

        observeViewModel();

        btnFilterMetric.setOnClickListener(v -> showMetricDialog());
    }

    private void observeViewModel() {
        viewModel.getMonthlyMetrics().observe(getViewLifecycleOwner(), list -> {
            updateCharts(list, viewModel.getSelectedMetric().getValue());
        });

        viewModel.getSelectedMetric().observe(getViewLifecycleOwner(), metric -> {
            updateCharts(viewModel.getMonthlyMetrics().getValue(), metric);
        });

        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCharts(List<QuestMonthlyMetricResponse> list, String selectedMetric) {
        if (list == null || list.isEmpty()) return;

        String topTitle = selectedMetric;
        String bottomTitle = "TỶ LỆ CHẤP NHẬN TB";

        tvChartTitle1.setText(topTitle);
        tvChartTitle2.setText(bottomTitle);

        drawChart(lineChartTop, list, selectedMetric);
        drawChart(lineChartBottom, list, "TỶ LỆ CHẤP NHẬN TB");
    }

    private void drawChart(LineChart chart, List<QuestMonthlyMetricResponse> list, String metric) {
        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            QuestMonthlyMetricResponse item = list.get(i);
            float value;

            switch (metric) {
                case "TỶ LỆ NGHIÊM TRỌNG TB":
                    value = item.getSeverity_rate();
                    break;
                case "SỐ LỖI TB":
                    value = item.getTotal_errors();
                    break;
                case "TỶ LỆ CHẤP NHẬN TB":
                    value = item.getAcceptance_rate();
                    break;
                default:
                    value = item.getAvg_severity();
                    break;
            }

            entries.add(new Entry(i, value));
            labels.add(item.getChart_month());
        }

        LineDataSet dataSet = new LineDataSet(entries, "Month");
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);

        LineData data = new LineData(dataSet);
        chart.setData(data);

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

    private void showMetricDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.layout_metric_menu, null);

        CheckBox cbSeverity = dialogView.findViewById(R.id.cbSeverity);
        CheckBox cbSeverityRate = dialogView.findViewById(R.id.cbSeverityRate);
        CheckBox cbTotalErrors = dialogView.findViewById(R.id.cbTotalErrors);
        CheckBox cbAcceptance = dialogView.findViewById(R.id.cbAcceptance);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        View.OnClickListener listener = v -> {
            if (v == cbSeverity) {
                viewModel.setSelectedMetric("MỨC ĐỘ NGHIÊM TRỌNG TB");
            } else if (v == cbSeverityRate) {
                viewModel.setSelectedMetric("TỶ LỆ NGHIÊM TRỌNG TB");
            } else if (v == cbTotalErrors) {
                viewModel.setSelectedMetric("SỐ LỖI TB");
            } else if (v == cbAcceptance) {
                viewModel.setSelectedMetric("TỶ LỆ CHẤP NHẬN TB");
            }
            dialog.dismiss();
        };

        cbSeverity.setOnClickListener(listener);
        cbSeverityRate.setOnClickListener(listener);
        cbTotalErrors.setOnClickListener(listener);
        cbAcceptance.setOnClickListener(listener);

        dialog.show();
    }
}