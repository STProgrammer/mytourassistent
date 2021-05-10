package com.aphex.mytourassistent.tours;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aphex.mytourassistent.R;
import com.aphex.mytourassistent.activetour.ActiveTourActivity;
import com.aphex.mytourassistent.databinding.FragmentMyToursBinding;
import com.aphex.mytourassistent.entities.Tour;
import com.aphex.mytourassistent.enums.TourStatus;
import com.aphex.mytourassistent.enums.TourType;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link com.aphex.mytourassistent.entities.Tour}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyMyTourRecyclerViewAdapter extends RecyclerView.Adapter<MyMyTourRecyclerViewAdapter.ViewHolder> {

    private final List<Tour> mTours;
    private FragmentMyToursBinding binding;

    private Context context;

    public MyMyTourRecyclerViewAdapter(List<Tour> items) {
        mTours = items;
    }


    public onClickButton startPlannedTourDetailsFragment;

    public void setStartPlannedTourDetailsFragment(onClickButton startPlannedTourDetailsFragment) {
        this.startPlannedTourDetailsFragment = startPlannedTourDetailsFragment;
    }

    public interface onClickButton {
        public void onClickToDetailsFragment();
        public void onClickToDeleteTour(long tourId);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
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

        void bind(Tour tour) {
            String date = new SimpleDateFormat("yyyy-MM-dd")
                    .format(new Date(tour.startTimePlanned));
            binding.itemTitle.setText(tour.title);
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



            binding.itemTourType.setText(tourType);
            binding.itemTourStatus.setText(tourStatus);
            binding.itemTourDate.setText(date);
            if (tour.tourStatus == TourStatus.COMPLETED.getValue()) {
                binding.btnTourStart.setVisibility(View.INVISIBLE);
            } else {
                binding.btnTourStart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //check if
                        startActiveTourActivity(tour.tourId, tour.tourStatus);
                    }
                });
            }
            binding.btnTourDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startPlannedTourDetailsFragment.onClickToDetailsFragment();

                }
            });

            binding.ivDeleteTour.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startPlannedTourDetailsFragment.onClickToDeleteTour(tour.tourId);

                }
            });
        }
    }

    public void startActiveTourActivity(long tourId, int tourStatus) {
        Intent intent = new Intent(context, ActiveTourActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("TOUR_ID", tourId);
        intent.putExtra("TOUR_STATUS", tourStatus);
        context.startActivity(intent);
    }

}