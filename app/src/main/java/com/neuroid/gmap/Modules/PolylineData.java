package com.neuroid.gmap.Modules;

import com.google.android.gms.maps.model.Polyline;
import com.google.maps.model.DirectionsLeg;

public class PolylineData {

    private Polyline polyline;
    private Route route;
    private String Rid;

    public PolylineData(Polyline polyline, Route route, String rid) {
        this.polyline = polyline;
        this.route = route;
        Rid = rid;
    }

    public Polyline getPolyline() {
        return polyline;
    }

    public void setPolyline(Polyline polyline) {
        this.polyline = polyline;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public String getRid() {
        return Rid;
    }

    public void setRid(String rid) {
        Rid = rid;
    }

    @Override
    public String toString() {
        return "PolylineData{"+
                "polyline=" + polyline +
                ", route=" + route +
                "}";
    }
}
