package com.Helpme.mylocation.model;

public class Car {
	public static final String TABLE = "CAR";

    public class Column {
        public static final String CUSID = "cusid";
        public static final String CARID = "carid";
    }

    private String cusid,carid;


    // Constructor
    public Car(String cusid,String carid) {
        this.cusid = cusid;
    	this.carid = carid;
 
    }

    public Car() {

    }

    public String getcusId() {
        return cusid;
    }

    public void setcusId(String cusid) {
        this.cusid = cusid;
    }
    public String getCarid() {
        return carid;
    }

    public void setCarid(String carid) {
        this.carid = carid;
    }

}
