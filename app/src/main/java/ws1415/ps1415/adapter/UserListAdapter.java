package ws1415.ps1415.adapter;

import android.app.Activity;
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
import com.skatenight.skatenightAPI.model.UserListData;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.activity.ProfileActivity;
import ws1415.ps1415.controller.UserController;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.util.ImageUtil;
import ws1415.ps1415.util.UniversalUtil;
import ws1415.ps1415.util.UserImageLoader;

/**
 * Dieser Adapter wird genutzt, um eine Liste mit Benutzerinformationen zu füllen.
 *
 * @author Martin Wrodarczyk
 */
public class UserListAdapter extends BaseAdapter {
    private static final int DATA_PER_REQUEST = 15;
    private List<String> mailData;
    private List<UserListData> mData;
    private LayoutInflater mInflater;
    private Context mContext;
    private Activity mActivity;
    private Bitmap defaultBitmap;
    private boolean loadingData;

    /**
     * Erwartet die komplette Liste der E-Mail Adressen der Benutzer die angezeigt werden sollen.
     * Dabei werden zu Beginn nur die ersten {@code DATA_PER_REQUEST} Benutzer angezeigt und beim Scrollen
     * werden die nächsten {@code DATA_PER_REQUEST} Benutzer geladen.
     *
     * @param userMails Liste der E-Mail Adressen
     * @param context Context
     */
    public UserListAdapter(List<String> userMails, Context context) {
        this.mailData = userMails;
        mContext = context;
        mData = new ArrayList<>();
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        defaultBitmap = ImageUtil.getRoundedBitmap(BitmapFactory.
                decodeResource(context.getResources(), R.drawable.default_picture));
        if(userMails != null && !userMails.isEmpty()) addNextUserInfo(userMails);
    }

    /**
     * Übergibt man statt dem context eine Activity, so wird der {@code FEATURE_INDETERMINATE_PROGRESS]
     * beim Downloaden von neuen Benutzerinformationen angezeigt. Dafür muss bei der übergebenen
     * Activity zu Beginn von onCreate {@code requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS))
     * ausgeführt werden.
     *
     * @param userMails
     * @param activity
     */
    public UserListAdapter(List<String> userMails, Activity activity) {
        this.mailData = userMails;
        mContext = activity;
        mActivity = activity;
        mData = new ArrayList<>();
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        defaultBitmap = ImageUtil.getRoundedBitmap(BitmapFactory.
                decodeResource(mContext.getResources(), R.drawable.default_picture));
        if(userMails != null && !userMails.isEmpty()) addNextUserInfo(userMails);
    }

    /**
     * Ruft die Informationen der Benutzer der Ergebnisteilliste der Suche ab.
     *
     * @param userMails Ergebnis der Suche
     */
    private void addNextUserInfo(final List<String> userMails) {
        if (!loadingData) {
            if(mActivity != null) mActivity.setProgressBarIndeterminateVisibility(Boolean.TRUE);
            int dataSize = (userMails.size() < DATA_PER_REQUEST) ? userMails.size() : DATA_PER_REQUEST;
            final List<String> subList = new ArrayList<>(userMails.subList(0, dataSize));
            loadingData = true;
            UserController.listUserInfo(new ExtendedTaskDelegateAdapter<Void, List<UserListData>>() {
                @Override
                public void taskDidFinish(ExtendedTask task, List<UserListData> userListDatas) {
                    mData.addAll(userListDatas);
                    mailData.removeAll(subList);
                    notifyDataSetChanged();
                    loadingData = false;
                    if(mActivity != null) mActivity.setProgressBarIndeterminateVisibility(Boolean.FALSE);
                }

                @Override
                public void taskFailed(ExtendedTask task, String message) {
                    Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                    loadingData = false;
                    if(mActivity != null) mActivity.setProgressBarIndeterminateVisibility(Boolean.FALSE);
                }
            }, subList);
        }
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

        if(i == getCount()-1 && !mailData.isEmpty()) addNextUserInfo(mailData);

        if (convertView == null) {
            holder = new Holder();
            convertView = mInflater.inflate(R.layout.list_view_item_user, viewGroup, false);
            holder.picture = (ImageView) convertView.findViewById(R.id.list_item_user_picture);
            holder.primaryText = (TextView) convertView.findViewById(R.id.list_item_user_primary);
            holder.secondaryText = (TextView) convertView.findViewById(R.id.list_item_user_secondary);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        UserListData item = getItem(i);
        BlobKey userPicture = item.getUserPicture();

        String primaryText = setUpPrimaryText(item);
        String secondaryText = setUpSecondaryText(item);

        UserImageLoader.getInstance(mContext).displayImage(userPicture, holder.picture);
        holder.primaryText.setText(primaryText);
        holder.secondaryText.setText(secondaryText);

        return convertView;
    }

    private void setImage(final ImageView userPictureView, final BlobKey userPictureKey) {
        if (userPictureKey != null) {
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

    private String setUpPrimaryText(UserListData userListData) {
        String firstName = userListData.getFirstName();
        String lastName = userListData.getLastName();
        return (lastName == null) ? firstName : firstName + " " + lastName;
    }

    private String setUpSecondaryText(UserListData userListData) {
        String city = userListData.getCity();
        String dateOfBirth = userListData.getDateOfBirth();
        Integer age = null;
        if (dateOfBirth != null) {
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
        if (age != null && city != null) {
            secondaryText = mContext.getString(R.string.from_city) + " " + city + ", "
                    + mContext.getResources().getQuantityString(R.plurals.years_old, age, age);
        } else if (age != null) {
            secondaryText = mContext.getResources().getQuantityString(R.plurals.years_old, age, age);
        } else if (city != null) {
            secondaryText = mContext.getString(R.string.from_city) + " " + city;
        }
        return secondaryText;
    }

    /**
     * Entfernt den Benutzer mit der übergebenen Postion aus der Liste.
     *
     * @param position Position in der Liste
     */
    public void removeUser(int position){
        mData.remove(position);
        notifyDataSetChanged();
    }
}
