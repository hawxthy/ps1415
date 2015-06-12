package ws1415.ps1415.activity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.skatenight.skatenightAPI.model.BlobKey;
import com.skatenight.skatenightAPI.model.UserGroupMetaData;
import com.skatenight.skatenightAPI.model.UserProfile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import ws1415.ps1415.R;
import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.adapter.ProfilePagerAdapter;
import ws1415.ps1415.controller.MessageDbController;
import ws1415.ps1415.controller.UserController;
import ws1415.ps1415.model.Conversation;
import ws1415.ps1415.model.Gender;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.util.ImageUtil;
import ws1415.ps1415.util.UniversalUtil;
import ws1415.ps1415.util.UserImageLoader;
import ws1415.ps1415.widget.SlidingTabLayout;

/**
 * Diese Activity dient der Anzeige eines Benutzerprofils.
 *
 * @author Martin Wrodarczyk
 */
public class ProfileActivity extends FragmentActivity {
    public static final String EXTRA_MAIL = "email";
    public static final SimpleDateFormat DATE_OF_BIRTH_FORMAT = new SimpleDateFormat("dd-MM-yyyy");

    // Für das Wiederherstellen einer Activity
    private static final String STATE_EMAIL = "email";

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

    // Felder für die runtergeladenen Daten
    private UserProfile mUserProfile;
    private Bitmap mUserPicture;
    private String email;

    // Feld um zu prüfen, ob Serveraufruf grade läuft stattfindet
    private boolean addingFriendRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Prüft ob der Benutzer eingeloggt ist
        if (!UniversalUtil.checkLogin(this)) {
            finish();
            return;
        }

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_profile);
        setProgressBarIndeterminateVisibility(Boolean.FALSE);

        if (savedInstanceState != null) {
            email = savedInstanceState.getString(STATE_EMAIL);
        }

        // Intent
        Intent intent = getIntent();
        if (intent.getStringExtra(EXTRA_MAIL) != null) email = intent.getStringExtra(EXTRA_MAIL);

        // Header initialisieren
        mPicture = (ImageView) findViewById(R.id.profile_picture);
        mName = (TextView) findViewById(R.id.profile_name);
        mDescription = (TextView) findViewById(R.id.profile_description);

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.default_picture);
        mPicture.setImageBitmap(ImageUtil.getRoundedBitmapFramed(bm));

        if (email != null) {
            setProgressBarIndeterminateVisibility(Boolean.TRUE);
            getUserProfile();
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

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(STATE_EMAIL, email);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        email = intent.getStringExtra("email");
        if (email != null) {
            setProgressBarIndeterminateVisibility(Boolean.TRUE);
            getUserProfile();
        }
    }

    // Lädt das Profil eines Benutzer vom Server runter
    private void getUserProfile() {
        UserController.getUserProfile(new ExtendedTaskDelegateAdapter<Void, UserProfile>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserProfile userProfile) {
                if (mAdapter != null) {
                    UserImageLoader.getInstance(ProfileActivity.this).displayImageFramed(userProfile.getUserPicture(), mPicture);
                    setUpProfile(userProfile);
                }
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
            }

            @Override
            public void taskFailed(ExtendedTask task, String message) {
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
                Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_LONG).show();
            }
        }, email);
    }

    /**
     * Füllt das Profil mit den abgerufenen Daten.
     *
     * @param userProfile Profil
     */
    private void setUpProfile(UserProfile userProfile) {
        mUserProfile = userProfile;
        List<String> userGroups = userProfile.getMyUserGroups();
        userGroups = (userGroups == null) ? new ArrayList<String>() : userGroups;

        // Namen setzen
        String firstName = userProfile.getFirstName();
        String lastName = userProfile.getLastName();
        if (firstName == null) firstName = "";
        String fullName = (lastName == null) ? firstName : firstName + " " + lastName;
        mName.setText(fullName);
        setTitle(fullName);

        // Beschreibung setzen
        String description = userProfile.getDescription();
        if (description != null) mDescription.setText(description);

        // Tab setzen
        tabs[0] = getString(R.string.group_tab) + " (" + userGroups.size() + ")";
        tabs[2] = getString(R.string.events_tab) + " (" + userProfile.getEventCount() + ")";
        mTabs.setViewPager(mViewPager);

        // Daten für Allgemeines sammeln und Fragment übergeben
        List<Entry<String, String>> generalData = new ArrayList<>();
        setUpGeneralData(generalData, userProfile);
        mAdapter.getInfoFragment().setUpData(generalData);

        // Daten für Veranstaltungen dem Fragment übergeben
        mAdapter.getEventFragment().setUpData(email);

        // Daten für Gruppen dem Fragment übergeben
        mAdapter.getGroupFragment().setUpData(userGroups);
    }

    /**
     * Sammelt Informationen um diese in der Liste anzuzeigen
     *
     * @param generalData Informationen, die angezeigt werden sollen
     * @param userProfile Profilinformationen vom Benutzer
     */
    private void setUpGeneralData(List<Entry<String, String>> generalData, UserProfile userProfile) {
        String postalCode = userProfile.getPostalCode();
        String city = userProfile.getCity();
        String residence;
        if (postalCode != null && city != null) residence = postalCode + ", " + city;
        else if (postalCode == null && city != null) residence = city;
        else if (postalCode != null) residence = postalCode;
        else residence = getString(R.string.na);
        generalData.add(new SimpleEntry<>(getString(R.string.residence), residence));

        String dateOfBirth = userProfile.getDateOfBirth();
        if (dateOfBirth != null) {
            try {
                Date date = DATE_OF_BIRTH_FORMAT.parse(dateOfBirth);
                Calendar dob = Calendar.getInstance();
                dob.setTime(date);
                int age = UniversalUtil.calculateAge(dob);
                dateOfBirth = getResources().getQuantityString(R.plurals.result_date_of_birth,
                        age,
                        dob.get(Calendar.DAY_OF_MONTH),
                        dob.get(Calendar.MONTH) + 1,
                        dob.get(Calendar.YEAR),
                        age);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            dateOfBirth = getString(R.string.na);
        }
        generalData.add(new SimpleEntry<>(getString(R.string.date_of_birth), dateOfBirth));

        Gender gender = Gender.valueOf(userProfile.getGender());
        String genderString = null;
        if (gender.equals(Gender.NA)) genderString = getString(R.string.na);
        else if (gender.equals(Gender.MALE)) genderString = getString(R.string.gender_male);
        else if (gender.equals(Gender.FEMALE)) genderString = getString(R.string.gender_female);
        generalData.add(new SimpleEntry<>(getString(R.string.gender), genderString));

        String description = userProfile.getDescription();
        if (description == null) description = getString(R.string.na);
        generalData.add(new SimpleEntry<>(getString(R.string.description), description));
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem editItem = menu.findItem(R.id.action_edit_profile);
        MenuItem messageItem = menu.findItem(R.id.action_message_profile);
        MenuItem friendItem = menu.findItem(R.id.action_add_friend);

        if (email != null && email.equals(ServiceProvider.getEmail())) {
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
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_edit_profile:
                if (email != null) {
                    Intent editIntent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                    editIntent.putExtra(EditProfileActivity.EXTRA_MAIL, email);
                    startActivity(editIntent);
                } else {
                    UniversalUtil.showToast(this, getString(R.string.loading_data));
                }
                return true;
            case R.id.action_add_friend:
                if (email != null && !addingFriendRunning) {
                    addingFriendRunning = true;
                    addFriend();
                } else {
                    UniversalUtil.showToast(this, getString(R.string.loading_data));
                }
                return true;
            case R.id.action_message_profile:
                if (mUserProfile != null && email != null) {
                    if (MessageDbController.getInstance(ProfileActivity.this).existsConversation(email)) {
                        Intent conversation_intent = new Intent(ProfileActivity.this, ConversationActivity.class);
                        conversation_intent.putExtra(ConversationActivity.EXTRA_MAIL, mUserProfile.getEmail());
                        startActivity(conversation_intent);
                    } else {
                        createConversationDialog(mUserProfile);
                    }
                } else {
                    UniversalUtil.showToast(this, getString(R.string.loading_data));
                }
        }
        return super.onOptionsItemSelected(item);
    }

    private void createConversationDialog(final UserProfile userProfile) {
        new AlertDialog.Builder(ProfileActivity.this)
                .setMessage(getString(R.string.create_new_conversation_dialog))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String firstName = userProfile.getFirstName();
                        firstName = (firstName == null) ? "" : firstName;
                        String lastName = userProfile.getLastName();
                        lastName = (lastName == null) ? "" : lastName;
                        BlobKey blobKey = userProfile.getUserPicture();
                        String blobKeyString = (blobKey == null) ? "" : blobKey.getKeyString();
                        Conversation conversation = new Conversation(userProfile.getEmail(),
                                blobKeyString, firstName, lastName);
                        MessageDbController.getInstance(ProfileActivity.this).insertConversation(conversation);

                        Intent conversation_intent = new Intent(ProfileActivity.this, ConversationActivity.class);
                        conversation_intent.putExtra(ConversationActivity.EXTRA_MAIL, mUserProfile.getEmail());
                        startActivity(conversation_intent);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    private void addFriend() {
        setProgressBarIndeterminateVisibility(Boolean.TRUE);
        UserController.addFriend(new ExtendedTaskDelegateAdapter<Void, Boolean>() {
            @Override
            public void taskDidFinish(ExtendedTask task, Boolean aBoolean) {
                if (aBoolean) {
                    Toast.makeText(ProfileActivity.this, "Freund hinzugefügt", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "Ist bereits dein Freund", Toast.LENGTH_SHORT).show();
                }
                addingFriendRunning = false;
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
            }

            @Override
            public void taskFailed(ExtendedTask task, String message) {
                Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_LONG).show();
                addingFriendRunning = false;
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
            }
        }, ServiceProvider.getEmail(), email);
    }

    public void onPicturesClick(View view) {
        if (mUserProfile != null) {
            Intent intent = new Intent(this, ListPicturesActivity.class);
            intent.putExtra(ListPicturesActivity.EXTRA_TITLE, "Bilder von " + mUserProfile.getFirstName());
            intent.putExtra(ListPicturesActivity.EXTRA_MAIL, email);
            startActivity(intent);
        }
    }
}
