package ws1415.SkatenightBackend.model;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Load;

import java.util.ArrayList;

/**
 * Created by Bernd Eissing on 04.06.2015.
 */
@Entity
public class UserGroupPreviewPictures {
    @Id
    private String id;
    @Load
    private ArrayList<String> blobKeysValues;

    public UserGroupPreviewPictures(){
        // Konstruktor für GAE
    }

    public UserGroupPreviewPictures(String id){
        this.id = id;
    }

    public String getId(){
        return id;
    }

    public void addBlobKeyValue(String value){
        if(blobKeysValues == null){
            blobKeysValues = new ArrayList<>();
        }
        blobKeysValues.add(value);
    }

    public void removeBlobKeyValue(String value){
        if(blobKeysValues != null){
            blobKeysValues.remove(value);
        }
    }

    public ArrayList<String> getBlobKeysValues() {
        return blobKeysValues;
    }

    public void setBlobKeysValues(ArrayList<String> blobKeysValues) {
        this.blobKeysValues = blobKeysValues;
    }
}
