package com.coomix.app.all.model.bean;

import java.io.Serializable;
import java.util.ArrayList;

public class Province implements Serializable {

    private static final long serialVersionUID = -1270319084497960333L;

    public String id;
    public String name;
    public ArrayList<City> cities;
}
