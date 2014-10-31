package ws1415.veranstalterapp;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;

/**
 *
 */
public class ManageRoutesFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_manage_routes, container, false);
        return view;
    }

    //private void populateListView(){
      //  Cursor cursor = newDB.rawQuery("SELECT " + db.KEY_ID + " as _id, " + db.KEY_USERNAME + ", "
        //        + db.KEY_FIRSTNAME + ", " + db.KEY_LASTNAME + " FROM " + db.TABLE_USERS, null);

        //String[] fromFields = new String[]{db.KEY_USERNAME, db.KEY_FIRSTNAME, db.KEY_LASTNAME};

        //int[] toViewIDs = new int[]{R.id.item_username, R.id.item_firstname, R.id.item_lastname};

        //SimpleCursorAdapter myCursorAdapter = new SimpleCursorAdapter(
          //      this, R.layout.item_layout, cursor, fromFields, toViewIDs);

        //setListAdapter(myCursorAdapter);
    //}
}
