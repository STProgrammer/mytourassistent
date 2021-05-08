package com.aphex.mytourassistent.tours;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.aphex.mytourassistent.R;
import com.aphex.mytourassistent.databinding.FragmentMyToursBinding;
import com.aphex.mytourassistent.dummy.DummyContent.DummyItem;
import com.aphex.mytourassistent.entities.TourWithGeoPointsPlanned;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyMyTourRecyclerViewAdapter extends RecyclerView.Adapter<MyMyTourRecyclerViewAdapter.ViewHolder> {

    private final List<TourWithGeoPointsPlanned> mTours;
    private FragmentMyToursBinding binding;

    public MyMyTourRecyclerViewAdapter(List<TourWithGeoPointsPlanned> items) {
        mTours = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(FragmentMyToursBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.bind(mTours.get(position));
    }

    @Override
    public int getItemCount() {
        return mTours.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private FragmentMyToursBinding binding;

        public ViewHolder(FragmentMyToursBinding viewBinding) {
            super(viewBinding.getRoot());
            binding = viewBinding;
        }

        void bind(TourWithGeoPointsPlanned tourPlannedItem) {
            binding.itemTitle.setText(tourPlannedItem.tour.title);
            binding.itemTourType.setText(tourPlannedItem.tour.tourType);
            binding.itemTourStatus.setText(tourPlannedItem.tour.tourStatus);
            }
        }

}