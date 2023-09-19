package comp5216.sydney.edu.au.mediaaccess.model;

import com.google.firebase.firestore.IgnoreExtraProperties;

/**
 * Image POJO.
 */
@IgnoreExtraProperties
public class Test {

    public static final String FIELD_CITY = "city";

    private String name;



    public Test() {}

    public Test(String name) {
        this.name = name;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
