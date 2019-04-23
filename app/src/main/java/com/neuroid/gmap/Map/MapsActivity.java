package com.neuroid.gmap.Map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.neuroid.gmap.Common.Common;
import com.neuroid.gmap.Modules.DirectionFinder;
import com.neuroid.gmap.Modules.DirectionFinderListener;
import com.neuroid.gmap.Modules.PolylineData;
import com.neuroid.gmap.Modules.Route;
import com.neuroid.gmap.R;
import com.neuroid.gmap.adapter.ReviewAdapter;
import com.neuroid.gmap.db.DBManager;
import com.neuroid.gmap.model.Review;


import java.util.ArrayList;
import java.util.List;




public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, DirectionFinderListener, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnPolylineClickListener {



    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private static final int LOCATION_REQUEST=500;
    private GoogleMap mMap;

    private Button btnFindPath;
    private AutoCompleteTextView editTextOrigin;
    private AutoCompleteTextView editTextDestination;
    private TextView tvDistance,tvDuration,tvFrom,tvDestination;
    private View bottomSheet;

    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarker = new ArrayList<>();
    private List<Polyline> polyLinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;

    private static final LatLngBounds BOUNDS_INDIA = new LatLngBounds(new LatLng(23.63936, 68.14712), new LatLng(28.20453, 97.34466));


    private static final String TAG = "MapActivity";



    public ArrayList<PolylineData> polylineData = new ArrayList<>();
    private List<Review> reviewList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ReviewAdapter mAdapter;

    private SQLiteDatabase sqLiteDatabase;

    BottomSheetBehavior sheetBehavior;

    private Button addReview,CancelButton,dialogbtn;
    private EditText edAddReview;
    private View popupInputDialogView = null;

    private DBManager dbManager;

    String originSt,destOr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnFindPath = findViewById(R.id.buttonFindPath);
        editTextOrigin = findViewById(R.id.editTextOrigin);
        editTextDestination = findViewById(R.id.editTextDestination);


        dbManager = new DBManager(MapsActivity.this);
        dbManager.open();

        SharedPreferences sharedpreferences = getSharedPreferences("Logined", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();

        boolean abc= sharedpreferences.getBoolean("Logined",false);
        if(!abc){
            initializeMapDatabase();

        }
        editor.putBoolean("Logined",true);
        editor.commit();


        tvDestination =findViewById(R.id.tvDestination);
        tvDistance = findViewById(R.id.tvDistance);
        tvDuration = findViewById(R.id.tvDuration);
        tvFrom = findViewById(R.id.tvFrom);
        dialogbtn = findViewById(R.id.dialogReviewBtn);

        dialogbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog();
            }
        });

        btnFindPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                sendRequest();
            }
        });

        bottomSheet =findViewById(R.id.bottom_sheet);
        sheetBehavior = BottomSheetBehavior.from(bottomSheet);

        /**
         * bottom sheet state change listener
         * we are changing button text when sheet changed state
         * */
        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED: {
//                        btnBottomSheet.setText("Close Sheet");
                    }
                    break;
                    case BottomSheetBehavior.STATE_COLLAPSED: {
//                        btnBottomSheet.setText("Expand Sheet");
                    }
                    break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

//        mAdapter = new PlaceAutocompleteAdapter(this, mGeoDataClient, BOUNDS_INDIA, null);
//        editTextOrigin.setAdapter(mAdapter);

//        getFeedBacks();

    }

    private void initializeMapDatabase() {

        dbManager.insert("thrissur","akkikkavu","Route1","Nice Route","45");
        dbManager.insert("thrissur","akkikkavu","Route1","Bad Route","45");
        dbManager.insert("thrissur","akkikkavu","Route1","Good Road","45");
        dbManager.insert("thrissur","akkikkavu","Route2","Traffic Route","45");
        dbManager.insert("thrissur","akkikkavu","Route2","Very Bad Route","45");
        dbManager.insert("thrissur","akkikkavu","Route2","Satisfactory Route","45");

        dbManager.insert("thrissur","thiruvananthapuram ","Route2","Satisfactory Route","45");
        dbManager.insert("thrissur","thiruvananthapuram ","Route2","Bad Route","45");
        dbManager.insert("thrissur","thiruvananthapuram ","Route2","High Traffic Route","45");
        dbManager.insert("thrissur","thiruvananthapuram ","Route2","Nice Route","45");


        dbManager.insert("thrissur","thiruvananthapuram ","Route1","Satisfactory Route","45");
        dbManager.insert("thrissur","thiruvananthapuram ","Route1","Bad Route","45");
        dbManager.insert("thrissur","thiruvananthapuram ","Route1","High Traffic Route","45");
        dbManager.insert("thrissur","thiruvananthapuram ","Route1","Nice Route","45");


        Cursor cursor = dbManager.fetch();

        Log.d("db", String.valueOf(cursor.getCount()));

    }


    private void sendRequest() {
        String origin = editTextOrigin.getText().toString();
        String destination = editTextDestination.getText().toString();

        if (origin.isEmpty()) {
            Toast.makeText(this, "Please enter origin!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination.isEmpty()){
            Toast.makeText(this, "Please enter destination!", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            new DirectionFinder(this, origin, destination).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this,new String[]{ Manifest.permission.ACCESS_FINE_LOCATION },LOCATION_REQUEST );
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setOnPolylineClickListener(this);

    }


    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case LOCATION_REQUEST:
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    mMap.setMyLocationEnabled(true);
                }
                break;
        }
    }

    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait", "Finding direction", true);
        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }
        if (destinationMarker != null) {
            for (Marker marker : destinationMarker) {
                marker.remove();
            }
        }
        if (polyLinePaths != null) {
            for (Polyline polylinePath : polyLinePaths) {
                polylinePath.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polyLinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarker = new ArrayList<>();

        Log.d(TAG, String.valueOf(routes.size()));

        if(polylineData.size() > 0) {
            for(PolylineData polylineData1: polylineData){
                polylineData1.getPolyline().remove();
            }

            polylineData.clear();
            polylineData = new ArrayList<>();
        }

        int x =0;
        for (Route route : routes) {

            Log.d(TAG,route.distance.text);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 13));

            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                    .title(route.startAddress)
                    .position(route.startLocation)));

            destinationMarker.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .title(route.endAddress)
                    .snippet(route.distance.text)
                    .position(route.endLocation)));

            String fromAdd  ="From  :  " + route.startAddress;
            String destAdd  ="Destination  :  " + route.endAddress;

            tvFrom.setText(fromAdd);
            tvDestination.setText(destAdd);

            PolylineOptions polylineOptions = new PolylineOptions()
                    .geodesic(true)
                    .color(R.color.darkgrey)
                    .width(10);

            for (int i = 0; i < route.points.size(); i++) {
                polylineOptions.add(route.points.get(i));
            }
            polyLinePaths.add(mMap.addPolyline(polylineOptions));
            polyLinePaths.get(x).setClickable(true);
            polyLinePaths.get(x).setTag(route.distance.text);

//            polyLinePaths.get(x).
            String v= String.valueOf(x+1);
            String rid= "Route"+v;
            originSt = editTextOrigin.getText().toString().toLowerCase();
            destOr = editTextDestination.getText().toString().toLowerCase();
            if(x == 0){
                polyLinePaths.get(x).setColor(ContextCompat.getColor(getApplicationContext(),R.color.wallet_holo_blue_light));
                polyLinePaths.get(x).setZIndex(1);
                tvDuration.setText(route.duration.text);
                tvDistance.setText(route.distance.text);
                bottomSheet.setVisibility(View.VISIBLE);
                Common.RID = rid;

            }else {
                polyLinePaths.get(x).setColor(ContextCompat.getColor(getApplicationContext(),R.color.darkgrey));
                polyLinePaths.get(x).setZIndex(0);
            }

            polylineData.add(new PolylineData(polyLinePaths.get(x),route, rid ));
            x++;
        }
        recyclerView = findViewById(R.id.recyclerReview);


        setReviewList();


    }

    private void setReviewList() {
        List<Review> newreviewList = new ArrayList<>();
        newreviewList = dbManager.fetchData(originSt,destOr,Common.RID);
        mAdapter = new ReviewAdapter(newreviewList,MapsActivity.this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        // set the adapter
        recyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }


    @Override
    public void onPolylineClick(Polyline polyline) {

        for(PolylineData polylineDataClick: polylineData){
            Log.d(TAG,"onploine click to string" + polylineDataClick.toString());

            if(polyline.getId().equals(polylineDataClick.getPolyline().getId())){
                polylineDataClick.getPolyline().setColor(ContextCompat.getColor(getApplicationContext(),R.color.wallet_holo_blue_light));
                polylineDataClick.getPolyline().setZIndex(1);

                tvDuration.setText(polylineDataClick.getRoute().duration.text);
                tvDistance.setText(polylineDataClick.getRoute().distance.text);
                Common.RID = polylineDataClick.getRid();
                setReviewList();

            }
            else {
                polylineDataClick.getPolyline().setColor(ContextCompat.getColor(getApplicationContext(),R.color.darkgrey));
                polylineDataClick.getPolyline().setZIndex(0);
            }
        }
    }

    private void alertDialog(){
        // Create a AlertDialog Builder.
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MapsActivity.this);
        // Set title, icon, can not cancel properties.
        alertDialogBuilder.setTitle("Add Review ");
        alertDialogBuilder.setCancelable(false);

        // Init popup dialog view and it's ui controls.
        initPopupViewControls();

        // Set the inflated layout view object to the AlertDialog builder.
        alertDialogBuilder.setView(popupInputDialogView);

        // Create AlertDialog and show.
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        addReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String review = edAddReview.getText().toString();
                if(!review.isEmpty()){
                    dbManager.insert(originSt,destOr,Common.RID,review,"20");
                    setReviewList();
                    Toast.makeText(MapsActivity.this,"Review Added",Toast.LENGTH_SHORT).show();
                }
                alertDialog.cancel();
            }
        });

        CancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.cancel();
            }
        });

    }
    /* Initialize popup dialog view and ui controls in the popup dialog. */
    private void initPopupViewControls()
    {
        // Get layout inflater object.
        LayoutInflater layoutInflater = LayoutInflater.from(MapsActivity.this);

        // Inflate the popup dialog from a layout xml file.
        popupInputDialogView = layoutInflater.inflate(R.layout.add_review, null);

        // Get user input edittext and button ui controls in the popup dialog.
        edAddReview = popupInputDialogView.findViewById(R.id.edAddReview);
        addReview = popupInputDialogView.findViewById(R.id.btnSubmitReview);
        CancelButton = popupInputDialogView.findViewById(R.id.btnCancel);

        // Display values from the main activity list view in user input edittext.
//        initEditTextUserDataInPopupDialog();
    }



}
