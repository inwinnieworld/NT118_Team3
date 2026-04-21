package com.example.emotiondebugging.ui.staff;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emotiondebugging.R;

public class QuestRankingFragment extends Fragment {

    private QuestReportViewModel viewModel;
    private RecyclerView rvRanking;
    private QuestRankingAdapter adapter;

    public QuestRankingFragment() {
        super(R.layout.fragment_quest_ranking);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(QuestReportViewModel.class);

        rvRanking = view.findViewById(R.id.rvRanking);
        adapter = new QuestRankingAdapter();

        rvRanking.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRanking.setAdapter(adapter);

        viewModel.getRankingBoard().observe(getViewLifecycleOwner(), list -> adapter.submitList(list));

        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}