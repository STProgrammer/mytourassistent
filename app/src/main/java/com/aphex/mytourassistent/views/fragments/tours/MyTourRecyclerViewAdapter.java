package com.aphex.mytourassistent.views.fragments.tours;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aphex.mytourassistent.R;
import com.aphex.mytourassistent.databinding.ItemMyTourBinding;
import com.aphex.mytourassistent.repository.db.entities.Tour;
import com.aphex.mytourassistent.enums.TourStatus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link com.aphex.mytourassistent.repository.db.entities.Tour}.
 */
public class MyTourRecyclerViewAdapter extends RecyclerView.Adapter<MyTourRecyclerViewAdapter.ViewHolder> {

    private final List<Tour> mTours;

    public MyTourRecyclerViewAdapter(List<Tour> items) {
        mTours = items;
    }


    public OnClickButton onClickButton;

    public void setOnClickButton(OnClickButton onClickButton) {
        this.onClickButton = onClickButton;
    }

    public interface OnClickButton {
        public void onClickToDeleteTour(long tourId);

        public void onClickStartActiveTourActivity(long tourId, int tourStatus);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(ItemMyTourBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
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
        private ItemMyTourBinding binding;

        public ViewHolder(ItemMyTourBinding viewBinding) {
            super(viewBinding.getRoot());
            binding = viewBinding;
        }

        void bind(Tour tour) {

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(binding.tvTourTitle.getContext().getString(R.string.date_format_simple));
            String startDatePlanned = simpleDateFormat.format(new Date(tour.startTimePlanned));
            String finishDatePlanned = simpleDateFormat.format(new Date(tour.finishTimePlanned));
            binding.tvTourTitle.setText(tour.title);
            String tourType = "";
            String tourStatus = "";

            switch (tour.tourType) {
                case 1:
                    tourType = itemView.getContext().getString(R.string.tour_type_walking);
                    break;
                case 2:
                    tourType = itemView.getContext().getString(R.string.tour_type_bicycling);
                    break;
                case 3:
                    tourType = itemView.getContext().getString(R.string.tour_type_skiing);
                    break;
            }

            switch (tour.tourStatus) {
                case 1:
                    tourStatus = itemView.getContext().getString(R.string.status_not_started);
                    break;
                case 2:
                    tourStatus = itemView.getContext().getString(R.string.status_active);
                    break;
                case 3:
                    tourStatus = itemView.getContext().getString(R.string.status_paused);
                    break;
                case 4:
                    tourStatus = itemView.getContext().getString(R.string.status_completed);
                    break;
            }
            binding.tvTourType.setText(tourType);
            binding.tvTourStatus.setText(tourStatus);
            binding.tvTourDateStart.setText(startDatePlanned);
            binding.tvTourDateEnd.setText(finishDatePlanned);
            if (tour.tourStatus == TourStatus.COMPLETED.getValue()) {
                binding.btnTourStart.setVisibility(View.INVISIBLE);
            } else {
                binding.btnTourStart.setOnClickListener(v -> {
                    onClickButton.onClickStartActiveTourActivity(tour.tourId, tour.tourStatus);
                });
            }

            binding.ivDeleteTour.setOnClickListener(v -> onClickButton.onClickToDeleteTour(tour.tourId));
        }
    }
}