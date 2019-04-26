package com.neuroid.gmap.Map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;


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

    private static final int LOCATION_REQUEST=500;
    private GoogleMap mMap;

    private Button btnFindPath;
    private AutoCompleteTextView editTextOrigin;
    private AutoCompleteTextView editTextDestination;
    private TextView tvDistance,tvDuration,tvFrom,tvDestination,tvDifficultyScore;
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

    String GOOGLE_BROWSER_API_KEY = "AIzaSyAoCYbp3vnbsszqEZMAIB9yRf1ZNgjOA8c";
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




        fetchNearbyPlaces();

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
//        fetchNearbyPlaces(endAdd.latitude,endAdd.longitude);
        recyclerView = findViewById(R.id.recyclerReview);


        setReviewList();


    }

    private void setReviewList() {
        List<Review> newreviewList ;
        newreviewList = dbManager.fetchData(originSt,destOr,Common.RID);
        String difScore = calculateDifficulty();
        tvDifficultyScore.setText(difScore);

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

    private String getDifficultyScore(String review) {
        String newScore = "";

        if(review.toLowerCase().contains("very bad")){
            newScore = "0.8";
        } else if(review.toLowerCase().contains("bad")){
            newScore = "0.6";
        } else  if(review.toLowerCase().contains("traffic")){
            newScore = "0.7";
        } else  if( review.toLowerCase().contains("nice")){
            newScore = "0.3";
        } else  if( review.toLowerCase().contains("good")){
            newScore = "0.1";
        } else  if (review.toLowerCase().contains("heavy")){
            newScore = "0.8";
        } else  if(review.toLowerCase().contains("bad road")){
            newScore = "0.7";
        } else if (review.toLowerCase().contains("conjunction")) {
            newScore = "0.4";
        } else if (review.toLowerCase().contains("rush")){
            newScore = "0.6";
        }
        return  newScore;
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
    private void fetchNearbyPlaces(){
        double latitude = -33.8670522;
        double longitude = 151.1957362;
        String type = "food";


        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+ latitude +","+ longitude+"&rankby=distance&type="+"food"+"&key=" +GOOGLE_BROWSER_API_KEY;

        RequestQueue requestQueue = Volley.newRequestQueue(MapsActivity.this);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Do something with response
                        //mTextView.setText(response.toString());
                        List<Marker> markersList = new ArrayList<Marker>();

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
//                                JSONObject placeName = object.getJSONObject("name");

                                if (!object.isNull("name")) {
                                    placeName = object.getString("name");
                                }
                                if (!object.isNull("vicinity")) {
                                    vicinity = object.getString("vicinity");
                                }

                                Log.d("Tagasad", String.valueOf(latLng));

//                                Object vicinity = object.get("vicinity");
                                markerOptions.position(latLng);
                                markerOptions.title(placeName + " : " + vicinity);
                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                                Marker models=  mMap.addMarker(markerOptions);

                                markersList.add(models);

                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        /**create for loop for get the latLngbuilder from the marker list*/
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for (Marker m : markersList) {
                            builder.include(m.getPosition());
                        }
                        /**initialize the padding for map boundary*/
                        int padding = 50;
                        /**create the bounds from latlngBuilder to set into map camera*/
                        LatLngBounds bounds = builder.build();
                        /**create the camera with bounds and padding to set into map*/
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));

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

    private void parseLocationResult(JSONObject result) {

    }


}
