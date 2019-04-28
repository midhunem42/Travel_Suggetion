package com.neuroid.gmap.Map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
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
import com.neuroid.gmap.model.Difficulty;
import com.neuroid.gmap.model.Review;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, DirectionFinderListener, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnPolylineClickListener {


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private static final int LOCATION_REQUEST = 500;
    private GoogleMap mMap;
    private Button btnFindPath;
    private AutoCompleteTextView editTextOrigin;
    private AutoCompleteTextView editTextDestination;
    private TextView tvDistance, tvDuration, tvFrom, tvDestination, tvDifficultyScore,difficultyStatus;
    private View bottomSheet;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarker = new ArrayList<>();
    private List<Polyline> polyLinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    private static final String TAG = "MapActivity";
    public ArrayList<PolylineData> polylineData = new ArrayList<>();
    private RecyclerView recyclerView;
    private ReviewAdapter mAdapter;
    private BottomSheetBehavior sheetBehavior;
    private Button addReview, CancelButton, dialogbtn;
    private EditText edAddReview;
    private View popupInputDialogView = null;

    private DBManager dbManager;

    String originSt, destOr;

    String GOOGLE_BROWSER_API_KEY = "AIzaSyCocMkAw5pQRhVh2JWvrYm_8sjESDuiwWQ";
    String type = "";

    ImageButton movie, cafe, rooms, petrol, mall;
    double myLat=10.530345, myLong=76.214729;

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

        movie = findViewById(R.id.moviePlace);
        cafe = findViewById(R.id.cafePlace);
        rooms = findViewById(R.id.roomPlace);
        petrol = findViewById(R.id.petrolPlace);
        mall = findViewById(R.id.mallPlace);
        difficultyStatus = findViewById(R.id.difficultyStatus);

        cafe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchNearbyPlaces(0, myLat, myLong);
            }
        });

        rooms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchNearbyPlaces(1, myLat, myLong);

            }
        });

        petrol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchNearbyPlaces(2, myLat, myLong);

            }
        });

        mall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchNearbyPlaces(4, myLat, myLong);

            }
        });

        movie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchNearbyPlaces(3, myLat, myLong);

            }
        });

        dbManager = new DBManager(MapsActivity.this);
        dbManager.open();

        SharedPreferences sharedpreferences = getSharedPreferences("Logined", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();

        boolean abc = sharedpreferences.getBoolean("Logined", false);
        if (!abc) {
            initializeMapDatabase();

        }
        editor.putBoolean("Logined", true);
        editor.commit();


        tvDestination = findViewById(R.id.tvDestination);
        tvDistance = findViewById(R.id.tvDistance);
        tvDuration = findViewById(R.id.tvDuration);
        tvFrom = findViewById(R.id.tvFrom);
        dialogbtn = findViewById(R.id.dialogReviewBtn);
        tvDifficultyScore = findViewById(R.id.tvDifficultyScore);

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
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                sendRequest();
            }
        });

        bottomSheet = findViewById(R.id.bottom_sheet);
        sheetBehavior = BottomSheetBehavior.from(bottomSheet);

        /**
         * bottom sheet state change listener
         * we are changing button text when sheet changed state
         * */
        View view = findViewById(R.id.expandedView);
        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED: {
//                        btnBottomSheet.setText("Close Sheet");
                        view.setVisibility(View.VISIBLE);
                    }
                    break;
                    case BottomSheetBehavior.STATE_COLLAPSED: {
//                        btnBottomSheet.setText("Expand Sheet");
                        view.setVisibility(View.GONE);
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

    }



    private void initializeMapDatabase() {

        dbManager.insert("thrissur","akkikkavu","Route1","Nice Route","0.2");
        dbManager.insert("thrissur","akkikkavu","Route1","Bad Route","0.6");
        dbManager.insert("thrissur","akkikkavu","Route1","Good Road","0.3");
        dbManager.insert("thrissur","akkikkavu","Route2","Traffic Route","0.8");
        dbManager.insert("thrissur","akkikkavu","Route2","Very Bad Route","0.7");
        dbManager.insert("thrissur","akkikkavu","Route2","Satisfactory Route","0.5");

        dbManager.insert("thrissur","thiruvananthapuram ","Route2","Satisfactory Route","0.5");
        dbManager.insert("thrissur","thiruvananthapuram ","Route2","Bad Route","0.6");
        dbManager.insert("thrissur","thiruvananthapuram ","Route2","High Traffic Route","0.8");
        dbManager.insert("thrissur","thiruvananthapuram ","Route2","Nice Route","0.1");


        dbManager.insert("thrissur","thiruvananthapuram ","Route1","Satisfactory Route","0.5");
        dbManager.insert("thrissur","thiruvananthapuram ","Route1","Bad Route","0.8");
        dbManager.insert("thrissur","thiruvananthapuram ","Route1","High Traffic Route","0.9");
        dbManager.insert("thrissur","thiruvananthapuram ","Route1","Nice Route","0.1");

        reviewSet("alappuzha","eranamkulam","Route1","There was less restuarants on the way On the way from Alappuzha to Ernakulam,the road was smooth and then traffic was heavy");
        reviewSet("alappuzha","eranamkulam","Route2","There was lot of restuarants on the way On the way from Alappuzha to Ernakulam,the road was smooth and then traffic was heavy");
        reviewSet("alappuzha","eranamkulam","Route3","There was few restuarants on the way On the way from Alappuzha to Ernakulam,the road was smooth and then traffic was very smooth");

        reviewSet("alappuzha","eranamkulam","Route3","There was few restuarants on the way On the way from Alappuzha to Ernakulam,the road was smooth and then traffic was crawling");
        reviewSet("alappuzha","eranamkulam","Route2","the road was broken and then traffic was very smooth");
        reviewSet("idukki","eranamkulam","Route2","the road was broken and then traffic was very smooth");

        reviewSet("idukki","eranamkulam","Route1","There was lot of restuarants on the way On the way from Ernakulam to Idukki");
        reviewSet("idukki","eranamkulam","Route3","There was less restuarants on the way On the way from Ernakulam to Idukki");
        reviewSet("idukki","eranamkulam","Route3","There was less restuarants on the way On the way from Ernakulam to Idukki");
        reviewSet("idukki","eranamkulam","Route2","the road was broken and then traffic was less");

        reviewSet("palakkad","pathanamthitta","Route1","There was lot of restuarants on the way On the way from palakkad to pathanamthitta");
        reviewSet("palakkad","pathanamthitta","Route3","There was less restuarants on the way On the way from palakkad to pathanamthitta");
        reviewSet("palakkad","pathanamthitta","Route3","There was less restuarants on the way On the way from palakkad to pathanamthitta");
        reviewSet("palakkad","pathanamthitta","Route2","road was easy going and then traffic was crawling");

        reviewSet("thiruvananthapuram","pathanamthitta","Route2","There was lot of restuarants on the way");
        reviewSet("thiruvananthapuram","pathanamthitta","Route1","There was less restuarants on the way On the way from");
        reviewSet("thiruvananthapuram","pathanamthitta","Route2","road was easy going and then traffic was crawling");
        reviewSet("thiruvananthapuram","pathanamthitta","Route3","There was more restuarants on the way On the way from");


        reviewSet("kannur","kasaragod","Route1","There was less restuarants on the way On the way from");
        reviewSet("kannur","kasaragod","Route2","There was lot of restuarants on the way On the way from");
        reviewSet("kannur","kasaragod","Route3","There was more restuarants on the way On the way from");
        reviewSet("kannur","kasaragod","Route1","road was easy going and then traffic was crawling");

        reviewSet("kannur","kasaragod","Route2","the road was bumpy and then traffic was heavy ");

        reviewSet("kottayam","kozhikode","Route2","the road was bumpy and then traffic was crawling");
        reviewSet("kottayam","kozhikode","Route1","the road was bumpy and then traffic was crawling");
        reviewSet("kottayam","kozhikode","Route3","the road was bumpy and then traffic was crawling");

        reviewSet("malappuram","kozhikode","Route3","the road was bumpy and then traffic was crawling");
        reviewSet("malappuram","kozhikode","Route1","the road was smooth and then traffic was heavy . There was few restuarants on the way");
        reviewSet("malappuram","kozhikode","Route2"," On the way from Kozhikode to Malappuram,the road was smooth and then traffic was very smooth . There was lot of restuarants on the way");
        reviewSet("malappuram","kozhikode","Route3","the road was bumpy and then traffic was crawling");
        reviewSet("malappuram","kozhikode","Route1"," On the way from Kozhikode to Malappuram,the road was easy going and then traffic was heavy . There was less restuarants on the way");
        reviewSet("malappuram","kozhikode","Route1","On the way from Kozhikode to Malappuram,the road was easy going and then traffic was crawling . There was few restuarants on the way");

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


        LatLng endAdd = routes.get(0).endLocation;
        Log.d(TAG, String.valueOf(routes.get(0)));

        myLat = endAdd.latitude;
        myLong = endAdd.longitude;

//        fetchNearbyPlaces(endAdd.latitude,endAdd.longitude);


//        fetchNearbyPlaces(0,endAdd.latitude,endAdd.longitude);
        fetchNearbyPlaces(4,endAdd.latitude,endAdd.longitude);
//        fetchNearbyPlaces(2,endAdd.latitude,endAdd.longitude);

        recyclerView = findViewById(R.id.recyclerReview);


        setReviewList();


    }

    private void setReviewList() {
        List<Review> newreviewList ;
        newreviewList = dbManager.fetchData(originSt,destOr,Common.RID);
        String difScore = calculateDifficulty();
//        tvDifficultyScore.setText(difScore);

        double val= Double.parseDouble(difScore);
        if(val<0.01 && val< 0.02 ){
            difficultyStatus.setText("Difficult to travel");
        } else if(val> 0.115 &&val < 0.125){
            difficultyStatus.setText("Fair Route");
        } else if(val< 0.115){
            difficultyStatus.setText("bad Route");
        } else if(val> 0.125 &&val < 0.2){
            difficultyStatus.setText("Nice Route");
        }  else if(val> 0.3){
            difficultyStatus.setText("Nice Route");
        }else if(val > 0.7 && val< 0.9){
            difficultyStatus.setText("Fair Route");
        } else if(val > 0.9 && val< 1){
            difficultyStatus.setText("Very Good Route");
        }else if(val == 1){
            difficultyStatus.setText("Good Route");
        }

        mAdapter = new ReviewAdapter(newreviewList,MapsActivity.this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        // set the adapter
        recyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    private String calculateDifficulty() {
        List<Difficulty> difficultyList;
        difficultyList = dbManager.fetchScore(originSt,destOr,Common.RID);

        int scoreList = difficultyList.size();
        double value = 0;

        if(scoreList ==0){
            scoreList =1;
            value = 0.5;
        }

        for(Difficulty difficulty:difficultyList){
           double score = Double.parseDouble(difficulty.getScore());
           value = value + score;
        }

        value = value/scoreList;
        return String.valueOf(value);
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
                    String dScore = getDifficultyScore(review);
                    dbManager.insert(originSt,destOr,Common.RID,review,dScore);
                    dbManager.insert(destOr,originSt,Common.RID,review,dScore);
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

    private void reviewSet(String from,String to, String rid,String review){
        String dScore = getDifficultyScore(review);
        dbManager.insert(from,to,rid,review,dScore);
        dbManager.insert(from,to,rid,review,dScore);
    }
    private String getDifficultyScore(String review) {
        String newScore = "0";

        int x = 0;

        if(review.toLowerCase().contains("very bad")){
            newScore = "0.8";
            x = x + 1;
        } if(review.toLowerCase().contains("bumpy")){
            newScore = "0.5";
            x = x + 1;
        }
         if(review.toLowerCase().contains("bad")){
            newScore = "0.6";
             x = x + 1;

         }  if(review.toLowerCase().contains("bumpy")){
            newScore = "0.5";
            x = x + 1;

        }   if(review.toLowerCase().contains("traffic")){
            newScore = "0.4";
            x = x + 1;

        }   if( review.toLowerCase().contains("nice")){
            newScore = "0.3";
            x = x + 1;
        }   if( review.toLowerCase().contains("good")){
            newScore = "0.1";
            x = x + 1;
        }   if (review.toLowerCase().contains("heavy")){
            newScore = "0.8";
            x = x + 1;
        }   if(review.toLowerCase().contains("bad road")){
            newScore = "0.7";
            x = x + 1;
        }  if (review.toLowerCase().contains("conjunction")) {
            newScore = "0.4";
            x = x + 1;
        }  if (review.toLowerCase().contains("rush")){
            newScore = "0.6";
            x = x + 1;
        } if(review.toLowerCase().contains("smooth")){
            newScore = "0.2";
            x = x + 1;
        } if(review.toLowerCase().contains("very smooth")){
            newScore = "0.3";
            x = x + 1;
        }
        if(review.toLowerCase().contains("crawling")){
            newScore = "0.3";
            x = x + 1;
        }
        if(review.toLowerCase().contains("easy going")){
            newScore = "0.3";
            x = x + 1;
        }

        if(review.toLowerCase().contains("")){
            newScore = "0.3";
            x = x + 1;
        }

        double nv= Double.parseDouble(newScore);
         if(nv == 0){
             nv = 0.5;
         }
         if(x == 0){
             x =1;
         }

         double newDifficulty = nv/x;
        return String.valueOf(newDifficulty);
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

//    private void fetchNearbyPlaces(double latitude,double longitude){
    private void fetchNearbyPlaces(int types,double latitude, double longitude){

        String url = "";

        if(types == 0){
            type = "food";
            url ="https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+ latitude +","+ longitude+"&rankby=distance&type="+"restaurant"+"&key=" +GOOGLE_BROWSER_API_KEY;
        } else if(types == 1){
            type = "room";
            url ="https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+ latitude +","+ longitude+"&rankby=distance&type="+"room"+"&key=" +GOOGLE_BROWSER_API_KEY;
        } else if(types == 2){
            type = "petrol pump";
            url ="https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+ latitude +","+ longitude+"&rankby=distance&type="+"gas_station"+"&key=" +GOOGLE_BROWSER_API_KEY;
        } else  if(types == 3){
            type = "movie_theater";
            url ="https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+ latitude +","+ longitude+"&rankby=distance&type="+"movie_theater"+"&key=" +GOOGLE_BROWSER_API_KEY;
        }else  if(types == 4){
            type = "shopping";
            url ="https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+ latitude +","+ longitude+"&rankby=distance&type="+"shopping_mall"+"&key=" +GOOGLE_BROWSER_API_KEY;
        }

        RequestQueue requestQueue = Volley.newRequestQueue(MapsActivity.this);
        progressDialog = ProgressDialog.show(this, "Please wait", "Finding " + type, true);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Do something with response
                        //mTextView.setText(response.toString());

                        String placeName = "-NA-";
                        String vicinity = "-NA-";

                        Log.i(TAG, "onResponse: Result= " + response.toString());
                        try {
                            JSONArray resultsArray = response.getJSONArray("results");
                            for(int i =0 ;i < resultsArray.length() ;i++ ){

                                JSONObject object = resultsArray.getJSONObject(i);
                                JSONObject geometry = object.getJSONObject("geometry");
                                JSONObject location = geometry.getJSONObject("location");
                                double lati = location.getDouble("lat");
                                double lngi = location.getDouble("lng");

                                LatLng latLng = new LatLng(lati, lngi);

                                MarkerOptions markerOptions = new MarkerOptions();

                                if (!object.isNull("name")) {
                                    placeName = object.getString("name");
                                }
                                if (!object.isNull("vicinity")) {
                                    vicinity = object.getString("vicinity");
                                }


                                markerOptions.position(latLng);

                                markerOptions.title(placeName + " : " + vicinity);

                                if(type.equals("food")){
                                    markerOptions.icon(bitmapDescriptorFromVector(MapsActivity.this,R.drawable.ic_local_cafe_black_24dp));
                                    markerOptions.snippet(type);
                                }else  if(type.equals("shopping")){
                                    markerOptions.icon(bitmapDescriptorFromVector(MapsActivity.this,R.drawable.ic_local_mall_black_24dp));
                                    markerOptions.snippet(type);
                                }  else  if(type.equals("movie_theater")){
                                    markerOptions.icon(bitmapDescriptorFromVector(MapsActivity.this,R.drawable.ic_local_movies_black_24dp));
                                    markerOptions.snippet(type);
                                } else  if(type.equals("petrol pump")){
                                    markerOptions.icon(bitmapDescriptorFromVector(MapsActivity.this,R.drawable.ic_ev_station_black_24dp));
                                    markerOptions.snippet(type);
                                }else  if(type.equals("room")){
                                    markerOptions.icon(bitmapDescriptorFromVector(MapsActivity.this,R.drawable.ic_local_hotel_black_24dp));
                                    markerOptions.snippet(type);
                                } else {
                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                                }
//                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                                mMap.addMarker(markerOptions);

//                                markersList.add(models);

                            }
                            progressDialog.dismiss();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

//                        /**create for loop for get the latLngbuilder from the marker list*/
//                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
//                        for (Marker m : markersList) {
//                            builder.include(m.getPosition());
//                        }
//                        /**initialize the padding for map boundary*/
//                        int padding = 50;
//                        /**create the bounds from latlngBuilder to set into map camera*/
//                        LatLngBounds bounds = builder.build();
//                        /**create the camera with bounds and padding to set into map*/
//                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));

                        // Process the JSON

                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        // Do something when error occurred

                    }
                }
        );


        requestQueue.add(request);
    }
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
    private void parseLocationResult(JSONObject result) {

    }


}
