package com.example.emotiondebugging.ui.staff;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emotiondebugging.R;

public class QuestEventReportFragment extends Fragment {
    public QuestEventReportFragment() { super(R.layout.fragment_quest_events); }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        QuestNodeMetricAdapter adapter = new QuestNodeMetricAdapter();
        RecyclerView list = view.findViewById(R.id.rvNodeMetrics);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(adapter);
        QuestReportViewModel viewModel = new ViewModelProvider(requireActivity())
                .get(QuestReportViewModel.class);
        viewModel.getNodeMetrics().observe(getViewLifecycleOwner(), adapter::submitList);
    }
}
