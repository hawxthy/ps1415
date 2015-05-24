package ws1415.ps1415.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.skatenight.skatenightAPI.model.BlobKey;
import com.skatenight.skatenightAPI.model.UserInfo;
import com.skatenight.skatenightAPI.model.UserListData;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ws1415.common.controller.UserController;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegateAdapter;
import ws1415.common.util.ImageUtil;
import ws1415.ps1415.R;
import ws1415.ps1415.activity.ProfileActivity;
import ws1415.ps1415.util.UserImageLoader;
import ws1415.ps1415.util.UniversalUtil;

/**
 * Dieser Adapter wird genutzt, um eine Liste mit Benutzerinformationen zu füllen.
 *
 * @author Martin Wrodarczyk
 */
public class UserListAdapter extends BaseAdapter {
    private List<String> userMails;
    private List<UserListData> mData;
    private LayoutInflater mInflater;
    private Context mContext;
    private Bitmap defaultBitmap;

    public UserListAdapter(List<String> userMails, List<UserListData> data, Context context) {
        this.userMails = userMails;
        mData = data;
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        defaultBitmap = ImageUtil.getRoundedBitmap(BitmapFactory.
                decodeResource(context.getResources(), R.drawable.default_picture));
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public UserListData getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    private class Holder {
        private ImageView picture;
        private TextView primaryText;
        private TextView secondaryText;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        Holder holder;

        if (convertView == null) {
            holder = new Holder();
            convertView = mInflater.inflate(R.layout.list_view_user, viewGroup, false);
            holder.picture = (ImageView) convertView.findViewById(R.id.list_item_user_picture);
            holder.primaryText = (TextView) convertView.findViewById(R.id.list_item_user_primary);
            holder.secondaryText = (TextView) convertView.findViewById(R.id.list_item_user_secondary);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        UserListData item = getItem(i);
        UserInfo userInfo = item.getUserInfo();

        String primaryText = setUpPrimaryText(userInfo);
        String secondaryText = setUpSecondaryText(userInfo);

        holder.picture.setImageBitmap(defaultBitmap);
        UserImageLoader.getInstance().displayImage(item.getUserPicture(), holder.picture);
        holder.primaryText.setText(primaryText);
        holder.secondaryText.setText(secondaryText);

        return convertView;
    }

    private void setImage(final ImageView userPictureView, final BlobKey userPictureKey) {
        if(userPictureKey != null) {
            UserController.getUserPicture(new ExtendedTaskDelegateAdapter<Void, Bitmap>() {
                @Override
                public void taskDidFinish(ExtendedTask task, Bitmap bitmap) {
                    userPictureView.setImageBitmap(ImageUtil.getRoundedBitmap(bitmap));
                }

                @Override
                public void taskFailed(ExtendedTask task, String message) {
                    Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                }
            }, userPictureKey);
        }
    }

    private String setUpPrimaryText(UserInfo userInfo) {
        String firstName = userInfo.getFirstName();
        String lastName = userInfo.getLastName().getValue();
        return (lastName == null) ? firstName : firstName + " " + lastName;
    }

    private String setUpSecondaryText(UserInfo userInfo) {
        String city = userInfo.getCity().getValue();
        String dateOfBirth = userInfo.getDateOfBirth().getValue();
        Integer age = null;
        if(dateOfBirth != null) {
            try {
                Date dateOfBirthDate = ProfileActivity.DATE_OF_BIRTH_FORMAT.parse(dateOfBirth);
                Calendar dob = Calendar.getInstance();
                dob.setTime(dateOfBirthDate);
                age = UniversalUtil.calculateAge(dob);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        String secondaryText = "";
        if(age != null && city != null){
            secondaryText = mContext.getString(R.string.from_city) + " " + city + ", "
                    + mContext.getResources().getQuantityString(R.plurals.years_old, age, age);
        } else if(age != null){
            secondaryText = mContext.getResources().getQuantityString(R.plurals.years_old, age, age);
        } else if(city != null){
            secondaryText = mContext.getString(R.string.from_city) + " " + city;
        }
        return secondaryText;
    }
}
