package com.aphex.mytourassistent.tours;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.aphex.mytourassistent.R;
import com.aphex.mytourassistent.databinding.FragmentAddTourBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static org.osmdroid.tileprovider.util.StorageUtils.getStorage;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddTourFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddTourFragment extends Fragment {

    private FragmentAddTourBinding binding;
    DatePickerDialog picker;
    ToursViewModel toursViewModel;

    private SimpleDateFormat mSimpleDateFormat;
    private Calendar mCalendarStart;
    private Calendar mCalendarFinish;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AddTourFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddTourFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddTourFragment newInstance(String param1, String param2) {
        AddTourFragment fragment = new AddTourFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAddTourBinding.inflate(inflater, container, false);

        toursViewModel = new ViewModelProvider(requireActivity()).get(ToursViewModel.class);


        mSimpleDateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm", Locale.getDefault());


        binding.etTourStartTime.setInputType(InputType.TYPE_NULL);
        binding.etTourStartTime.setOnClickListener(textListenerStart);
        binding.etTourFinishTime.setInputType(InputType.TYPE_NULL);
        binding.etTourFinishTime.setOnClickListener(textListenerFinish);

        /*binding.etTourStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                picker = new DatePickerDialog(getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                binding.etTourStartTime.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                            }
                        }, year, month, day);
                picker.show();
            }
        });*/




        binding.btnChooseOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(getView()).navigate(R.id.chooseTourOnMapFragment);
            }
        });

        binding.btnChooseOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(getView()).navigate(R.id.chooseTourOnMapFragment);
            }
        });

        binding.btnAddNewTour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCalendarStart == null || mCalendarFinish == null) {
                    Toast.makeText(requireContext(),R.string.toast_empty_date, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (binding.etTourName.getText().toString().isEmpty()) {
                    Toast.makeText(requireContext(),R.string.toast_empty_name, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (binding.rgTourType.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(requireContext(),R.string.toast_empty_tourtype, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (toursViewModel.getGeoPoints().isEmpty()) {
                    Toast.makeText(requireContext(), R.string.toast_empty_geopoints, Toast.LENGTH_SHORT).show();
                    return;
                }

                long startTime = mCalendarStart.getTimeInMillis();
                long endTime = mCalendarFinish.getTimeInMillis();
                String tourType = "";
                if( binding.rbBicycling.isSelected()) {
                    tourType = getString(R.string.rb_bicycling);
                }
                else if ( binding.rbSkiing.isSelected()) {
                    tourType = getString(R.string.rb_skiing);
                }
                else {
                    tourType = getString(R.string.rb_walking);
                }
//show some progress bar
                toursViewModel.addNewTour(binding.etTourName.getText().toString(),
                        startTime, endTime, tourType);
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (toursViewModel.getGeoPoints().isEmpty()) {
//button will say choose tour on map
            binding.btnChooseOnMap.setText(R.string.btn_choose_on_map);
        } else{
            //button will say edit tour on map
            binding.btnChooseOnMap.setText(R.string.btn_edit_on_map);
        }

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }


    // Code is taken and partly modified from https://github.com/Kiarasht/Android-Templates/blob/master/Templates/DatePickerDialog/app/src/main/java/com/restart/datepickerdialog/MainActivity.java
    // Koden er tatt og delvis modifisert fra https://github.com/Kiarasht/Android-Templates/blob/master/Templates/DatePickerDialog/app/src/main/java/com/restart/datepickerdialog/MainActivity.java
    /* Define the onClickListener, and start the DatePickerDialog with users current time */
    private final View.OnClickListener textListenerStart = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mCalendarStart = Calendar.getInstance();
            new DatePickerDialog(requireContext(), mDateStartDataSet, mCalendarStart.get(Calendar.YEAR),
                    mCalendarStart.get(Calendar.MONTH), mCalendarStart.get(Calendar.DAY_OF_MONTH)).show();
        }
    };

    /* After user decided on a date, store those in our calendar variable and then start the TimePickerDialog immediately */
    private final DatePickerDialog.OnDateSetListener mDateStartDataSet = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mCalendarStart.set(Calendar.YEAR, year);
            mCalendarStart.set(Calendar.MONTH, monthOfYear);
            mCalendarStart.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            new TimePickerDialog(requireContext(), mTimeStartDataSet, mCalendarStart.get(Calendar.HOUR_OF_DAY), mCalendarStart.get(Calendar.MINUTE), true).show();
        }
    };

    /* After user decided on a time, save them into our calendar instance, and now parse what our calendar has into the TextView */
    private final TimePickerDialog.OnTimeSetListener mTimeStartDataSet = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mCalendarStart.set(Calendar.HOUR_OF_DAY, hourOfDay);
            mCalendarStart.set(Calendar.MINUTE, minute);
            binding.etTourStartTime.setText(mSimpleDateFormat.format(mCalendarStart.getTime()));
        }
    };

    /* Define the onClickListener, and start the DatePickerDialog with users current time */
    private final View.OnClickListener textListenerFinish = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mCalendarFinish = Calendar.getInstance();
            new DatePickerDialog(requireContext(), mDateFinishDataSet, mCalendarFinish.get(Calendar.YEAR),
                    mCalendarFinish.get(Calendar.MONTH), mCalendarFinish.get(Calendar.DAY_OF_MONTH)).show();
        }
    };

    /* After user decided on a date, store those in our calendar variable and then start the TimePickerDialog immediately */
    private final DatePickerDialog.OnDateSetListener mDateFinishDataSet = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mCalendarFinish.set(Calendar.YEAR, year);
            mCalendarFinish.set(Calendar.MONTH, monthOfYear);
            mCalendarFinish.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            new TimePickerDialog(requireContext(), mTimeFinishDataSet, mCalendarFinish.get(Calendar.HOUR_OF_DAY), mCalendarFinish.get(Calendar.MINUTE), true).show();
        }
    };

    /* After user decided on a time, save them into our calendar instance, and now parse what our calendar has into the TextView */
    private final TimePickerDialog.OnTimeSetListener mTimeFinishDataSet = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mCalendarFinish.set(Calendar.HOUR_OF_DAY, hourOfDay);
            mCalendarFinish.set(Calendar.MINUTE, minute);
            binding.etTourFinishTime.setText(mSimpleDateFormat.format(mCalendarFinish.getTime()));
        }
    };

}