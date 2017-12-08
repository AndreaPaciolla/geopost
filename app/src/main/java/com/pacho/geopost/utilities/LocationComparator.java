package com.pacho.geopost.utilities;

import com.pacho.geopost.models.UserModel;
import java.util.Comparator;

/**
 * Created by do_ma on 08/12/2017.
 */

public class LocationComparator implements Comparator<UserModel> {
    @Override
    public int compare(UserModel o1, UserModel o2) {
        return o1.getDistanceFromMe().compareTo(o2.getDistanceFromMe());
    }
}