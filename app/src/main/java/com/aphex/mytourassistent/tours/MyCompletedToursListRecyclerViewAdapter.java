package com.aphex.mytourassistent.tours;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.aphex.mytourassistent.R;
import com.aphex.mytourassistent.activetour.ActiveTourActivity;
import com.aphex.mytourassistent.databinding.ItemMyCompletedTourBinding;
import com.aphex.mytourassistent.entities.Tour;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Tour}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyCompletedToursListRecyclerViewAdapter extends RecyclerView.Adapter<MyCompletedToursListRecyclerViewAdapter.ViewHolder> {

    private final List<Tour> mTours;
    private ItemMyCompletedTourBinding binding;

    private Context context;

    public MyCompletedToursListRecyclerViewAdapter(List<Tour> items) {
        mTours = items;
    }


    public OnClickButton onClickButton;

    public void setOnClickButton(OnClickButton onClickButton) {
        this.onClickButton = onClickButton;
    }

    public interface OnClickButton {
        public void onClickToDetailsFragment();
        public void onClickToDeleteTour(long tourId);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new ViewHolder(ItemMyCompletedTourBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
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
        private ItemMyCompletedTourBinding binding;

        public ViewHolder(ItemMyCompletedTourBinding viewBinding) {
            super(viewBinding.getRoot());
            binding = viewBinding;
        }

        void bind(Tour tour) {
            String startTimeActual = new SimpleDateFormat("yyyy-MM-dd HH:mm")
                    .format(new Date(tour.startTimeActual));
            String finishTimeActual = new SimpleDateFormat("yyyy-MM-dd HH:mm")
                    .format(new Date(tour.finishTimeActual));
            binding.tvTourTitle.setText(tour.title);
            String tourType = "";
            String tourStatus = "";


            switch(tour.tourType) {
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

            switch(tour.tourStatus) {
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
            binding.tvTourDatePlanStart.setText(startTimeActual);
            binding.tvTourDatePlanEnd.setText(finishTimeActual);

            binding.btnTourDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickButton.onClickToDetailsFragment();
                }
            });

            binding.ivDeleteTour.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickButton.onClickToDeleteTour(tour.tourId);
                }
            });
        }
    }
}