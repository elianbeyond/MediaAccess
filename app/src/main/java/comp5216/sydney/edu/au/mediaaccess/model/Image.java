package comp5216.sydney.edu.au.mediaaccess.model;

import com.google.firebase.firestore.IgnoreExtraProperties;

/**
 * Image POJO.
 */
@IgnoreExtraProperties
public class Image {

    public static final String FIELD_CITY = "city";
    public static final String FIELD_CATEGORY = "file_data";

    private String name;
    private String url;


    public Image() {}

    public Image(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getData() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
