package com.aphex.mytourassistent.views.fragments.tours;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.aphex.mytourassistent.R;
import com.aphex.mytourassistent.databinding.FragmentCompletedTourDetailsBinding;
import com.aphex.mytourassistent.repository.db.entities.GeoPointActualWithPhotos;
import com.aphex.mytourassistent.repository.db.entities.Photo;
import com.aphex.mytourassistent.repository.db.entities.TourWithGeoPointsActual;
import com.aphex.mytourassistent.viewmodels.ToursViewModel;
import com.aphex.mytourassistent.views.activities.photos.PhotosActivity;

import org.jetbrains.annotations.NotNull;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.advancedpolyline.MonochromaticPaintList;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CompletedTourDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CompletedTourDetailsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FragmentCompletedTourDetailsBinding binding;
    private ToursViewModel toursViewModel;

    private CompletedTourDetailsFragmentArgs completedTourDetailsFragmentArgs;
    private long tourId;
    private boolean mIsFirstTime;
    private Polyline mPolyline;
    private CompassOverlay mCompassOverlay;
    private RotationGestureOverlay mRotationGestureOverlay;
    private ScaleBarOverlay mScaleBarOverlay;
    private TourWithGeoPointsActual tourWithAllGeoPoints;

    public CompletedTourDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CompletedTourDetailsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CompletedTourDetailsFragment newInstance(String param1, String param2) {
        CompletedTourDetailsFragment fragment = new CompletedTourDetailsFragment();
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
        // Inflate the layout for this
        tourId = CompletedTourDetailsFragmentArgs.fromBundle(getArguments()).getTOURID();
        binding = FragmentCompletedTourDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }



    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState == null) {
            mIsFirstTime = true;
        } else {
            mIsFirstTime = false;
        }

        toursViewModel = new ViewModelProvider(requireActivity()).get(ToursViewModel.class);

        initMap();

        //FETCHING DATA FROM DATABASE TOUR AND LOCATIONS
        toursViewModel.getTourWithGeoPointsActual(tourId, mIsFirstTime).observe(requireActivity(), tourWithGeoPointsActual -> {
            if (tourWithGeoPointsActual != null) {
                this.tourWithAllGeoPoints = tourWithGeoPointsActual;
                if (tourWithGeoPointsActual.tour.comment != null) {
                    binding.tvComment.setText(tourWithGeoPointsActual.tour.comment);
                    binding.btnAddComment.setText(R.string.btn_edit_comment);
                } else {
                    binding.btnAddComment.setText(R.string.btn_add_comment);
                }

                StringBuilder sb = new StringBuilder();
                String startDatePlanned = new SimpleDateFormat("yyyy-MM-dd HH")
                        .format(new Date(tourWithGeoPointsActual.tour.startTimePlanned));
                String finishDatePlanned = new SimpleDateFormat("yyyy-MM-dd HH")
                        .format(new Date(tourWithGeoPointsActual.tour.finishTimePlanned));
                String tourType = "";
                String tourStatus = "";

                String startDateActual = new SimpleDateFormat("yyyy_MM_dd HH")
                        .format(new Date(tourWithGeoPointsActual.tour.startTimeActual));
                String finishDateActual = new SimpleDateFormat("yyyy_MM_dd HH")
                        .format(new Date(tourWithGeoPointsActual.tour.finishTimeActual));

                long difference = tourWithGeoPointsActual.tour.finishTimeActual - tourWithGeoPointsActual.tour.startTimeActual;

                long secondsInMilli = 1000;
                long minutesInMilli = secondsInMilli * 60;
                long hoursInMilli = minutesInMilli * 60;
                long daysInMilli = hoursInMilli * 24;

                long elapsedDays = difference / daysInMilli;
                difference = difference % daysInMilli;

                long elapsedHours = difference / hoursInMilli;

                String duration = elapsedDays
                        + " " + getString(R.string.days) + " ";
                duration += elapsedHours + " " + getString(R.string.hours);






                switch (tourWithGeoPointsActual.tour.tourType) {
                    case 1:
                        tourType = getString(R.string.tour_type_walking);
                        break;
                    case 2:
                        tourType = getString(R.string.tour_type_bicycling);
                        break;
                    case 3:
                        tourType = getString(R.string.tour_type_skiing);
                        break;
                }

                binding.tvTourTitle.setText(new StringBuilder().append(getString(R.string.tours_list_title)).append(tourWithGeoPointsActual.tour.title).toString());
                binding.tvTourType.setText(new StringBuilder().append(getString(R.string.tours_list_type)).append(tourType).toString());
                binding.tvTourDatePlanStart.setText(new StringBuilder().append(getString(R.string.tours_list_date_start_plan)).append(startDatePlanned).toString());
                binding.tvTourDatePlanEnd.setText(new StringBuilder().append(getString(R.string.tours_list_date_end_plan)).append(finishDatePlanned).toString());
                binding.tvTourDateStart.setText(new StringBuilder().append(getString(R.string.tours_list_date_start)).append(startDateActual).toString());
                binding.tvTourDateEnd.setText(new StringBuilder().append(getString(R.string.tours_list_date_end)).append(finishDateActual).toString());
                binding.tvTourDuration.setText(new StringBuilder().append(getString(R.string.tours_list_duration)).append(duration).toString());


                //Drawing the route
                for (GeoPointActualWithPhotos gp : tourWithGeoPointsActual.geoPointsActual) {
                    GeoPoint geoPt = new GeoPoint(gp.geoPointActual.lat, gp.geoPointActual.lng);
                    toursViewModel.addToGeoPointsOnCompleted(geoPt);
                    mPolyline.addPoint(geoPt);

                    //Iterate and add all images
                    for (Photo ph: gp.photos) {
                        try {
                            Marker photoIcon = new Marker(binding.mapView);
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), Uri.parse(ph.imageUri));
                            Drawable dr = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, (int) (48.0f * getResources().getDisplayMetrics().density), (int) (48.0f * getResources().getDisplayMetrics().density), true));
                            photoIcon.setIcon(dr);
                            photoIcon.setPosition(geoPt);
                            photoIcon.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                                @Override
                                public boolean onMarkerClick(Marker marker, MapView mapView) {
                                    Intent intent = new Intent(requireContext(), PhotosActivity.class);
                                    intent.putExtra("GPA_ID", gp.geoPointActual.geoPointActualId);
                                    startActivity(intent);
                                    return true;
                                }
                            });
                            photoIcon.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                            binding.mapView.getOverlays().add(photoIcon);
                        } catch (IOException e) {


                        }
                    }

                }


                ArrayList<GeoPoint> geoPoints = toursViewModel.getGeoPointsOnCompleted().getValue();

                if (!tourWithGeoPointsActual.geoPointsActual.isEmpty()) {
                    GeoPoint geoPointStart = Objects.requireNonNull(geoPoints).get(0);

                    // Markers:
                    Marker startMarker = new Marker(binding.mapView);
                    startMarker.setPosition(geoPointStart);
                    startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                    binding.mapView.getOverlays().add(startMarker);
                    startMarker.setIcon(getResources().getDrawable(R.drawable.ic_baseline_circle_24, null));



                    GeoPoint geoPointFinish = Objects.requireNonNull(geoPoints).get(geoPoints.size()-1);
                    Marker endMarker = new Marker(binding.mapView);
                    endMarker.setPosition(geoPointFinish);
                    endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                    binding.mapView.getOverlays().add(endMarker);
                    endMarker.setIcon(getResources().getDrawable(R.drawable.ic_baseline_flag_24, null));

                    binding.mapView.getController().setCenter(geoPointFinish);
                    binding.mapView.getController().setZoom(15.5);
                }




                binding.btnAddComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                        builder.setTitle(R.string.add_comment_dialog_title);
                        EditText editText = new EditText(requireActivity());
                        editText.setInputType(InputType.TYPE_CLASS_TEXT);
                        builder.setView(editText);
                        builder.setPositiveButton(R.string.btn_save_comment, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                binding.tvComment.setText(editText.getText());
                                if (tourWithGeoPointsActual != null) {
                                    toursViewModel.addComment(editText.getText().toString(), tourWithGeoPointsActual.tour);
                                }
                            }
                        });
                        builder.setNegativeButton(R.string.btn_cancel_comment, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.show();
                    }
                });

                binding.btnShareTour.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ArrayList<Uri> photosUri = new ArrayList<>();
                        Intent intent = new Intent();
                        try {
                            Bitmap mapScreenShot = getMapScreenShot();
                            Uri uriMapScreenShot = saveImageToExternalStorage(requireActivity(), mapScreenShot);
                            photosUri.add(uriMapScreenShot);
                            if (binding.swIncludePhotos.isChecked()) {
                                for (GeoPointActualWithPhotos gpa: tourWithGeoPointsActual.geoPointsActual) {
                                    for (Photo photo: gpa.photos) {
                                        photosUri.add(Uri.parse(photo.imageUri));
                                    }
                                }
                                intent.setAction(Intent.ACTION_SEND_MULTIPLE);
                                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, photosUri);
                            } else {
                                intent.setAction(Intent.ACTION_SEND);
                                intent.putExtra(Intent.EXTRA_STREAM,uriMapScreenShot);
                            }


                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            intent.setType("image/*");
                            Intent shareIntent = Intent.createChooser(intent, getString(R.string.share_with));

                            List<ResolveInfo> resInfoList = requireActivity().getPackageManager().queryIntentActivities(shareIntent, PackageManager.MATCH_DEFAULT_ONLY);

                            for (ResolveInfo resolveInfo : resInfoList) {
                                String packageName = resolveInfo.activityInfo.packageName;
                                requireActivity().grantUriPermission(packageName, uriMapScreenShot, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            }
                            startActivity(shareIntent);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

                binding.btnDeleteTour.setOnClickListener(v -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                    builder.setTitle(R.string.btn_delete_tour);
                    builder.setMessage(R.string.are_you_sure_to_delete_tour);
                    builder.setPositiveButton(R.string.btn_yes, (dialog, which) -> {
                        toursViewModel.deleteTour(tourId);
                        requireActivity().onBackPressed();
                    });
                    builder.setNegativeButton(R.string.btn_cancel, (dialog, which) -> dialog.cancel());
                    builder.show();

                });

            }
        });


    }

    //https://stackoverflow.com/questions/10753969/how-to-take-a-screenshot-of-the-current-mapview
    private Bitmap getMapScreenShot() throws IOException {
        boolean enabled = binding.mapView.isDrawingCacheEnabled();
        binding.mapView.setDrawingCacheEnabled(true);
        Bitmap bm = binding.mapView.getDrawingCache();
        return bm;
//
//        /* now you've got the bitmap - go save it */
//
//        File path = requireContext().getFilesDir();
//        path = new File(path, "Pictures");
//        path.mkdirs();  // make sure the Pictures folder exists.
//        File file = new File(path, "filename.png");
//        BufferedOutputStream outStream = null;
//        try {
//            outStream = new BufferedOutputStream(new FileOutputStream(file));
//            boolean success = bm.compress(Bitmap.CompressFormat.PNG, 100, outStream);
//            outStream.flush();
//            outStream.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }



//        Uri uri = Uri.fromFile(file);
//
//        Glide.with(this)
//                .load(Uri.fromFile(file))
//                .into(binding.imageView);
////get bitmap from uri
//        Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), uri);
//




       // return Uri.fromFile(file);

    }

    public Uri saveImageToExternalStorage(
            Activity activity,
            Bitmap bitmap
    ) {
        FileOutputStream outputStream = null;
        Uri uri=null;
        try {
            File picturesDirectory = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File tempFile = File.createTempFile(
                    "map_" + System.currentTimeMillis(),
                    ".jpg",
                    picturesDirectory
            );
            outputStream = new FileOutputStream(tempFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            uri = FileProvider.getUriForFile(
                    activity, requireActivity().getPackageName()+".provider", tempFile
            );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("Error", "Exception while saving image to external storage: ${e.message}"
);
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return uri;
    }

    private void initMap() {

        this.mPolyline = new Polyline(binding.mapView);
        final Paint paintBorder = new Paint();
        paintBorder.setStrokeWidth(20);
        paintBorder.setStyle(Paint.Style.FILL_AND_STROKE);
        paintBorder.setColor(Color.BLACK);
        paintBorder.setStrokeCap(Paint.Cap.ROUND);
        paintBorder.setAntiAlias(true);

        final Paint paintInside = new Paint();
        paintInside.setStrokeWidth(10);
        paintInside.setStyle(Paint.Style.FILL);
        paintInside.setColor(Color.WHITE);
        paintInside.setStrokeCap(Paint.Cap.ROUND);
        paintInside.setAntiAlias(true);

        mPolyline.getOutlinePaintLists().add(new MonochromaticPaintList(paintBorder));
        mPolyline.getOutlinePaintLists().add(new MonochromaticPaintList(paintInside));

        binding.mapView.getOverlays().add(mPolyline);

        //map_view.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        binding.mapView.setTileSource(TileSourceFactory.MAPNIK);
        binding.mapView.setBuiltInZoomControls(true);
        binding.mapView.setMultiTouchControls(true);
        binding.mapView.getController().setZoom(10.0);


        // Compass overlay;
        this.mCompassOverlay = new CompassOverlay(requireActivity(), new InternalCompassOrientationProvider(requireActivity()), binding.mapView);
        this.mCompassOverlay.enableCompass();
        binding.mapView.getOverlays().add(this.mCompassOverlay);

        // Multi touch:
        mRotationGestureOverlay = new RotationGestureOverlay(requireActivity(), binding.mapView);
        mRotationGestureOverlay.setEnabled(true);
        binding.mapView.setMultiTouchControls(true);
        binding.mapView.getOverlays().add(this.mRotationGestureOverlay);

        // Zoom-knapper;
        final DisplayMetrics dm = getResources().getDisplayMetrics();
        mScaleBarOverlay = new ScaleBarOverlay(binding.mapView);
        mScaleBarOverlay.setCentred(true);
        //play around with these values to get the location on screen in the right place for your application
        mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10);
        binding.mapView.getOverlays().add(this.mScaleBarOverlay);

        // Fange opp posisjon i klikkpunkt på kartet:
        final MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };
        binding.mapView.getOverlays().add(new MapEventsOverlay(mReceive));


       /* if (!activeTourViewModel.getGeoPointsPlanned(tourId).isEmpty()) {
            //this means user already have selected geopoints
            //in this case, we will connect all waypoints when user come to this screen
            //wen need to iterate over all the waypoints and connect them together
            //waypint1
            //waypint 2
            //waypint 3
            //iteration 1


            databaseWriteExecutor.execute(() -> {

                for (GeoPoint gp: activeTourViewModel.getGeoPoints()) {
                    Marker marker = new Marker(binding.mapView);
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                    marker.setIcon(getResources().getDrawable(R.drawable.ic_baseline_my_location_24, null));
                    marker.setTitle("Klikkpunkt");
                    marker.setPosition(gp);
                    binding.mapView.getOverlays().add(marker);
                }

                RoadManager roadManager = new OSRMRoadManager(this, "Aaa");
                Road road = roadManager.getRoad(activeTourViewModel.getGeoPoints());
                Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
                binding.mapView.getOverlays().add(roadOverlay);
            });
        }*/
    }
}