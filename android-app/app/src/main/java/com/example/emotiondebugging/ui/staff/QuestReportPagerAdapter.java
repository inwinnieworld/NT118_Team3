package com.example.emotiondebugging.ui.staff;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class QuestReportPagerAdapter extends FragmentStateAdapter {

    public QuestReportPagerAdapter(@NonNull AppCompatActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new QuestAverageFragment();
        }
        return new QuestRankingFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}