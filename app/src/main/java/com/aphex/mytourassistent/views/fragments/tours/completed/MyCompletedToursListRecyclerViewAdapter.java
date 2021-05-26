package com.aphex.mytourassistent.views.fragments.tours.completed;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.aphex.mytourassistent.R;
import com.aphex.mytourassistent.databinding.ItemMyCompletedTourBinding;
import com.aphex.mytourassistent.repository.db.entities.Tour;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Tour}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyCompletedToursListRecyclerViewAdapter extends RecyclerView.Adapter<MyCompletedToursListRecyclerViewAdapter.ViewHolder> {

    private final List<Tour> mTours;
    public OnClickButton onClickButton;

    public MyCompletedToursListRecyclerViewAdapter(List<Tour> items) {
        mTours = items;
    }


    public void setOnClickButton(OnClickButton onClickButton) {
        this.onClickButton = onClickButton;
    }

    public interface OnClickButton {
        public void onClickToDetailsFragment(long tourId);

        public void onClickToDeleteTour(long tourId);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(binding.tvTourTitle.getContext().getString(R.string.date_format_simple));
            String startTimeActual = simpleDateFormat.format(new Date(tour.startTimeActual));
            String finishTimeActual = simpleDateFormat.format(new Date(tour.finishTimeActual));

            long difference = tour.finishTimeActual - tour.startTimeActual;

            long secondsInMilli = 1000;
            long minutesInMilli = secondsInMilli * 60;
            long hoursInMilli = minutesInMilli * 60;
            long daysInMilli = hoursInMilli * 24;

            long elapsedDays = difference / daysInMilli;
            difference = difference % daysInMilli;

            long elapsedHours = difference / hoursInMilli;

            String duration = elapsedDays
                    + " " + binding.tvLabelDuration.getContext().getString(R.string.days) + " ";
            duration += elapsedHours + " " + binding.tvLabelDuration.getContext().getString(R.string.hours);


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


            binding.tvTourType.setText(tourType);
            binding.tvTourDateStart.setText(startTimeActual);
            binding.tvTourDateEnd.setText(finishTimeActual);
            binding.tvTourDuration.setText(duration);

            binding.btnTourDetails.setOnClickListener(v -> onClickButton.onClickToDetailsFragment(tour.tourId));

            binding.ivDeleteTour.setOnClickListener(v -> onClickButton.onClickToDeleteTour(tour.tourId));
        }
    }
}