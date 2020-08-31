package com.example.foodserviceservers.Common;


import com.example.foodserviceservers.Model.User;

public class Common {
    //create common to hold current user data

    public static User currentUser;
    // update/delete category

    public static final String UPDATE = "Update";
    public static final String DELETE = "Delete";
    public static final int PICK_IMAGE_REQUEST = 71;

    public static String convertCodeToStatus(String code){

        if(code.equals("0"))
            return "Placed";
        else if(code.equals("1"))
            return "On my way";
        else
            return "Shipped";


    }

}
