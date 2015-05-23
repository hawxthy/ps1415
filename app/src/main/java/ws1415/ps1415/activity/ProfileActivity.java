package ws1415.ps1415.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.util.DateTime;
import com.skatenight.skatenightAPI.model.EventMetaData;
import com.skatenight.skatenightAPI.model.GroupMetaData;
import com.skatenight.skatenightAPI.model.UserInfo;
import com.skatenight.skatenightAPI.model.UserPicture;
import com.skatenight.skatenightAPI.model.UserProfile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import ws1415.common.controller.UserController;
import ws1415.common.model.Gender;
import ws1415.common.net.ServiceProvider;
import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegateAdapter;
import ws1415.common.util.ImageUtil;
import ws1415.ps1415.R;
import ws1415.ps1415.adapter.ProfilePagerAdapter;
import ws1415.ps1415.widget.SlidingTabLayout;

public class ProfileActivity extends FragmentActivity{
    // Für das TabLayout
    private ViewPager mViewPager;
    private SlidingTabLayout mTabs;
    private ProfilePagerAdapter mAdapter;
    private ActionBar mActionBar;
    private String[] tabs;

    // UI Komponenten
    private ImageView mPicture;
    private TextView mName;
    private TextView mDescription;

    private UserProfile mUserProfile;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_profile);

        // Intent
        Intent intent = getIntent();
        email = intent.getStringExtra("email");

        // Header initialisieren
        mPicture = (ImageView) findViewById(R.id.profile_picture);
        mName = (TextView) findViewById(R.id.profile_name);
        mDescription = (TextView) findViewById(R.id.profile_description);

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.default_picture);
        mPicture.setImageBitmap(ImageUtil.getRoundedBitmap(bm));

        if(email != null) {
            setProgressBarIndeterminateVisibility(Boolean.TRUE);
            UserController.getUserProfile(new ExtendedTaskDelegateAdapter<Void, UserProfile>() {
                @Override
                public void taskDidFinish(ExtendedTask task, UserProfile userProfile) {
                    setProgressBarIndeterminateVisibility(Boolean.FALSE);
                    setUpProfile(userProfile);
                }

                @Override
                public void taskFailed(ExtendedTask task, String message) {
                    setProgressBarIndeterminateVisibility(Boolean.FALSE);
                    Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_LONG).show();
                }
            }, email);
        }

        // Tab Layout initialisieren
        tabs = new String[]{getString(R.string.group_tab), getString(R.string.info_tab),
                getString(R.string.events_tab)};

        mViewPager = (ViewPager) findViewById(R.id.profile_pager);
        mTabs = (SlidingTabLayout) findViewById(R.id.profile_tabs);
        mActionBar = getActionBar();

        mActionBar.setHomeButtonEnabled(false);
        mActionBar.setDisplayHomeAsUpEnabled(true);

        mTabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.colorProfileScroll);
            }
        });
        mTabs.setDistributeEvenly(true);

        mAdapter = new ProfilePagerAdapter(getSupportFragmentManager(), tabs);
        mViewPager.setAdapter(mAdapter);
        mTabs.setViewPager(mViewPager);
        mViewPager.setCurrentItem(1);
    }

    /**
     * Füllt das Profil mit den abgerufenen Daten.
     *
     * @param userProfile Profil
     */
    private void setUpProfile(UserProfile userProfile) {
        mUserProfile = userProfile;
        UserInfo userInfo = userProfile.getUserInfo();
        UserPicture userPicture = userProfile.getUserPicture();
        List<GroupMetaData> userGroups = userProfile.getMyUserGroups();
        userGroups = (userGroups == null) ? new ArrayList<GroupMetaData>() : userGroups;
        List<EventMetaData> events = userProfile.getMyEvents();
        events = (events == null) ? new ArrayList<EventMetaData>() : events;

        EventMetaData test = new EventMetaData();
        test.setDate(new DateTime(new Date()));
        test.setTitle("Primary Title");
        events.add(test);
        events.add(test);

        GroupMetaData testGroup = new GroupMetaData();
        testGroup.setMembers(Arrays.asList("test", "test2"));
        testGroup.setName("Martin");
        userGroups.add(testGroup);
        userGroups.add(testGroup);

        // Namen setzen
        String firstName = userInfo.getFirstName();
        String lastName = userInfo.getLastName().getValue();
        String fullName = (lastName == null) ? firstName : firstName + " " + lastName;
        mName.setText(fullName);
        setTitle(fullName);

        // Profilbild setzen
        if(userProfile.getUserPicture().getPicture() != null) {
            Bitmap profilePicture = ImageUtil.DecodeTextToBitmap(userPicture.getPicture());
            mPicture.setImageBitmap(ImageUtil.getRoundedBitmap(profilePicture));
        }

        // Beschreibung setzen
        String description = userProfile.getUserInfo().getDescription().getValue();
        if(description != null) mDescription.setText(description);

        // Tab setzen
        tabs[0] = tabs[0] + " (" + userGroups.size() + ")";
        tabs[2] = tabs[2] + " (" + events.size() + ")";
        mTabs.setViewPager(mViewPager);

        // Daten für Allgemeines sammeln und Fragment übergeben
        List<Entry<String, String>> generalData= new ArrayList<>();
        setUpGeneralData(generalData, userInfo);
        mAdapter.getInfoFragment().setUpData(generalData);

        // Daten für Veranstaltungen dem Fragment übergeben
        mAdapter.getEventFragment().setUpData(events);

        // Daten für Gruppen dem Fragment übergeben
        mAdapter.getGroupFragment().setUpData(userGroups);
    }

    private void setUpGeneralData(List<Entry<String, String>> generalData, UserInfo userInfo){
        String postalCode = userInfo.getPostalCode().getValue();
        String city = userInfo.getCity().getValue();
        String residence;
        if(postalCode != null && city != null) residence = postalCode + ", " + city;
        else if (postalCode == null && city != null) residence = city;
        else if (postalCode != null) residence = postalCode;
        else residence = getString(R.string.na);
        generalData.add(new SimpleEntry<>(getString(R.string.residence), residence));

        String dateOfBirth = userInfo.getDateOfBirth().getValue();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        if(dateOfBirth != null) {
            try {
                Date date = format.parse(dateOfBirth);
                Calendar dob = Calendar.getInstance();
                dob.setTime(date);
                int age = calculateAge(dob);
                dateOfBirth = getString(R.string.result_date_of_birth,
                        dob.get(Calendar.DAY_OF_YEAR),
                        dob.get(Calendar.MONTH),
                        dob.get(Calendar.YEAR),
                        age);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            dateOfBirth = getString(R.string.na);
        }
        generalData.add(new SimpleEntry<>(getString(R.string.date_of_birth), dateOfBirth));

        Gender gender = Gender.getValue(userInfo.getGender());
        String genderString = null;
        if(gender.equals(Gender.NA)) genderString = getString(R.string.na);
        else if (gender.equals(Gender.MALE)) genderString = getString(R.string.gender_male);
        else if (gender.equals(Gender.FEMALE)) genderString = getString(R.string.gender_female);
        generalData.add(new SimpleEntry<>(getString(R.string.gender),genderString));

        String description = userInfo.getDescription().getValue();
        if(description == null) description = getString(R.string.na);
        generalData.add(new SimpleEntry<>(getString(R.string.description), description));
    }

    private int calculateAge(Calendar dob){
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) <= dob.get(Calendar.DAY_OF_YEAR))
            age--;
        return age;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        MenuItem editItem = menu.findItem(R.id.action_edit_profile);
        MenuItem messageItem = menu.findItem(R.id.action_message_profile);
        MenuItem friendItem = menu.findItem(R.id.action_add_friend);

        if(email != null && email.equals(ServiceProvider.getEmail())){
            editItem.setEnabled(true).setVisible(true);
            messageItem.setEnabled(false).setVisible(false);
            friendItem.setEnabled(false).setVisible(false);
        } else {
            editItem.setEnabled(false).setVisible(false);
            messageItem.setEnabled(true).setVisible(true);
            friendItem.setEnabled(true).setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_edit_profile:
                if(mUserProfile != null) {
                    Intent editIntent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                    UserInfo userInfo = mUserProfile.getUserInfo();
                    // Da UserInfo nicht serialisierbar auf Client Seite und auch es auch nicht möglich es es mit Json zu senden
                    editIntent.putExtra("email", userInfo.getEmail());
                    editIntent.putExtra("firstName", userInfo.getFirstName());
                    editIntent.putExtra("gender", userInfo.getGender());
                    editIntent.putExtra("lastName", userInfo.getLastName().getValue());
                    editIntent.putExtra("lastNameVisibility", userInfo.getLastName().getVisibility());
                    editIntent.putExtra("dateOfBirth", userInfo.getDateOfBirth().getValue());
                    editIntent.putExtra("dateOfBirthVisibility", userInfo.getDateOfBirth().getVisibility());
                    editIntent.putExtra("city", userInfo.getCity().getValue());
                    editIntent.putExtra("cityVisibility", userInfo.getCity().getVisibility());
                    editIntent.putExtra("plz", userInfo.getPostalCode().getValue());
                    editIntent.putExtra("plzVisibility", userInfo.getPostalCode().getVisibility());
                    editIntent.putExtra("description", userInfo.getDescription().getValue());
                    editIntent.putExtra("descriptionVisibility", userInfo.getDescription().getVisibility());
                    editIntent.putExtra("optOutSearch", mUserProfile.getOptOutSearch());
                    editIntent.putExtra("showPrivateGroups", mUserProfile.getShowPrivateGroups());
                    startActivity(editIntent);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
