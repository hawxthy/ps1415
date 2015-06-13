package ws1415.ps1415.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewParent;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;

import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.adapter.UserListAdapter;
import ws1415.ps1415.controller.RightController;
import ws1415.ps1415.controller.UserController;
import ws1415.ps1415.model.NavDrawerGroupList;
import ws1415.ps1415.model.Right;
import ws1415.ps1415.model.UserGroupType;
import ws1415.ps1415.util.ImageUtil;
import ws1415.ps1415.util.UniversalUtil;
import ws1415.ps1415.widget.SlidingTabLayout;

import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonFlat;
import com.gc.materialdesign.views.Switch;
import com.melnykov.fab.FloatingActionButton;
import com.skatenight.skatenightAPI.model.BlobKey;
import com.skatenight.skatenightAPI.model.BooleanWrapper;
import com.skatenight.skatenightAPI.model.UserGroup;
import com.skatenight.skatenightAPI.model.UserGroupVisibleMembers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.adapter.GroupProfileTabsAdapter;
import ws1415.ps1415.controller.GroupController;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.util.GroupImageLoader;

public class GroupProfileActivity extends BaseFragmentActivity {
    private static final int SELECT_PHOTO = 1;
    private static final int PICTURE_CROP = 2;
    public static final String EXTRA_GROUP_NAME = "groupName";
    public static final String EXTRA_GROUP_MEMBERS = "groupMembers";
    public static final String EXTRA_INVITE_GROUP = "invite";
    // Attribute für das Laden von Bildern
    private File tempFile;
    private Uri pictureUri;
    private Bitmap mBitmap;

    // Die View Elemente
    private ImageView mGroupPicture;
    private TextView mGroupNameTextView;
    private TextView mGroupDescriptionTextView;
    private FloatingActionButton mJoinButton;
    private FloatingActionButton mLeaveButton;
    private FloatingActionButton mDeleteButton;
    private FloatingActionButton mChangeVisibility;
    private ViewPager mViewPager;
    private SlidingTabLayout mTabLayout;
    private GroupProfileTabsAdapter mAdapter;

    // Attribute zur Gruppe
    private String groupName;
    private UserGroup group;
    private boolean checkIsMember;
    private boolean checkGroupPassword;
    private boolean checkVisibility;

    // Attribute zum testen von Bedingungen
    private boolean checkGlobalMessageTextChecked = false;

    // Attribute für die Rechtevergabe
    private List<String> rightsToTake;
    private List<String> rightsToGive;

    String[] tabs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Prüft ob der Benutzer eingeloggt ist
        if (!UniversalUtil.checkLogin(this)) {
            finish();
            return;
        }

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.setContentView(NavDrawerGroupList.items, R.layout.activity_group_profile);
        setProgressBarIndeterminateVisibility(Boolean.FALSE);
        // Initialisiere die Views
        mGroupPicture = (ImageView) findViewById(R.id.group_profile_image_view);
        mGroupNameTextView = (TextView) findViewById(R.id.group_profile_group_name_text_view);
        mGroupDescriptionTextView = (TextView) findViewById(R.id.group_profile_group_description_text_view);
        mJoinButton = (FloatingActionButton) findViewById(R.id.group_profile_join_button);
        mLeaveButton = (FloatingActionButton) findViewById(R.id.group_profile_leave_button);
        mDeleteButton = (FloatingActionButton) findViewById(R.id.group_profile_delete_group_button);
        mViewPager = (ViewPager) findViewById(R.id.group_profile_view_pager);
        mTabLayout = (SlidingTabLayout) findViewById(R.id.group_profile_tab_layout);
        mChangeVisibility = (FloatingActionButton) findViewById(R.id.group_profile_change_visibility);

        // Gruppennamen aus dem Intent holen, falls nicht vorhanden Activity nicht starten.
        groupName = getIntent().getStringExtra(EXTRA_GROUP_NAME);
        if (groupName == null) {
            Toast.makeText(this, R.string.noGroupNameInExtra, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Beschrifte die Tabs
        tabs = new String[]{
                getResources().getString(R.string.title_fragment_group_members),
                getResources().getString(R.string.title_fragment_group_black_board),
                getResources().getString(R.string.title_fragment_group_news_board)};

        mAdapter = new GroupProfileTabsAdapter(getSupportFragmentManager(), tabs);
        mViewPager.setAdapter(mAdapter);

        // Give the TabLayout the ViewPager
        mTabLayout.setDistributeEvenly(true);
        mTabLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        mTabLayout.setViewPager(mViewPager);
        mViewPager.setCurrentItem(1);

        // Die Listen für die Rechte initialisieren;
        rightsToGive = new ArrayList<>();
        rightsToTake = new ArrayList<>();
        setClickListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_group_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setProgressBarIndeterminateVisibility(Boolean.TRUE);
        setUpProfile();
    }

    /**
     *
     *
     */
    private void setUpProfile() {
        GroupController.getInstance().getUserGroup(new ExtendedTaskDelegateAdapter<Void, UserGroup>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroup group) {
                if(group != null){
                    // group nuss nicht abgefragt werden, denn der Server gibt bereist einen Fehler wenn eine
                    // Gruppe mit dem Namen nicht existiert
                    GroupProfileActivity.this.group = group;
                    mAdapter.getGroupMembersFragment().setUp(group, getRights(), GroupProfileActivity.this);
                    if (group.getMemberRights().keySet().contains(ServiceProvider.getEmail())) {
                        checkIsMember = true;
                        mJoinButton.setVisibility(View.GONE);
                        mChangeVisibility.setVisibility(View.VISIBLE);
                        checkIfVisible();
                        if (getRights().contains(Right.FULLRIGHTS.name())) {
                            mLeaveButton.setVisibility(View.GONE);
                            mDeleteButton.setVisibility(View.VISIBLE);
                        } else {
                            if (mLeaveButton.getVisibility() == View.GONE) {
                                mLeaveButton.setVisibility(View.VISIBLE);
                            }
                        }
                    } else {
                        checkIsMember = false;
                        mLeaveButton.setVisibility(View.GONE);
                        if (mJoinButton.getVisibility() == View.GONE) {
                            mJoinButton.setVisibility(View.VISIBLE);
                        }
                    }
                    checkIfInvitationIntent();
                    GroupImageLoader.getInstance().setGroupImageToImageView(GroupProfileActivity.this, group.getBlobKey(), mGroupPicture);

                    mGroupNameTextView.setText(group.getName());
                    mGroupDescriptionTextView.setText(group.getDescription());
                    mAdapter.getGroupMembersFragment().setUp(group, getRights(), GroupProfileActivity.this);
                    mAdapter.getGroupBlackBoardFragment().setUp(group.getBlackBoard(), group, GroupProfileActivity.this);
                    mAdapter.getGroupNewsBoardFragment().setUp(group.getNewsBoard(), groupName, GroupProfileActivity.this);
                    setProgressBarIndeterminateVisibility(Boolean.FALSE);
                    setClickOnPicture();
                }else{
                    Toast.makeText(GroupProfileActivity.this, R.string.group_already_gone, Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
            }

            @Override
            public void taskFailed(ExtendedTask task, String message) {
                Toast.makeText(GroupProfileActivity.this, message, Toast.LENGTH_LONG).show();
            }
        }, groupName);
    }

    /**
     * überprüft, ob der Aufrufer ein Member ist und falls ja, dass wird überprüft ob
     * diese sicht gegenüber der Gruppe sichtbar geamcht hat oder nicht.
     */
    private void checkIfVisible() {
        if (checkIsMember) {
            if(!group.getGroupType().equals(UserGroupType.SECURITYGROUP.name())){
                GroupController.getInstance().getUserGroupVisibleMembers(new ExtendedTaskDelegateAdapter<Void, UserGroupVisibleMembers>() {
                    @Override
                    public void taskDidFinish(ExtendedTask task, UserGroupVisibleMembers visibleMembers) {
                        if (visibleMembers.getVisibleMembers() != null) {
                            if (visibleMembers.getVisibleMembers().contains(ServiceProvider.getEmail())) {
                                mChangeVisibility.setColorNormalResId(R.color.colorPrimaryLeave);
                                mChangeVisibility.setColorPressedResId(R.color.colorPressedBlackBoard);
                                mChangeVisibility.setImageResource(R.drawable.ic_eye_white_24dp);
                                checkVisibility = true;
                            } else {
                                mChangeVisibility.setColorNormalResId(R.color.colorPrimaryJoin);
                                mChangeVisibility.setColorPressedResId(R.color.colorPressedBlackBoard);
                                mChangeVisibility.setImageResource(R.drawable.ic_eye_off_white_24dp);
                                checkVisibility = false;
                            }
                        } else {
                            mChangeVisibility.setColorNormalResId(R.color.colorPrimaryJoin);
                            mChangeVisibility.setColorPressedResId(R.color.colorPressedBlackBoard);
                            mChangeVisibility.setImageResource(R.drawable.ic_eye_off_white_24dp);
                            checkVisibility = false;
                        }
                        mChangeVisibility.setVisibility(View.VISIBLE);
                    }
                }, groupName);
            }
        }
    }

    // Zum ändern des Gruppenbildes
    private void setClickOnPicture() {
        if (checkIsMember && (getRights().contains(Right.CHANGEGROUPPICTURE.name()) || getRights().contains(Right.FULLRIGHTS.name()))) {
            mGroupPicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent picturePickerIntent = new Intent(Intent.ACTION_PICK);
                    picturePickerIntent.setType("image/*");
                    startActivityForResult(picturePickerIntent, SELECT_PHOTO);
                }
            });
        }
    }

    /**
     * Diese Methode wird aufgerufen, wenn der Benutzer die ImageView drückt, ein Bild ausgewählt
     * und dieses zugeschnitten hat. Hier wird das Bild aus den Extras geladen, in eine Bitmap
     * umgewandelt und über den GroupController an den Server geschickt. Nachdem die geschehen ist,
     * wird das Bild mit dem GroupImageLoader vom Server heruntergeladen und in die ImageView gesetzt.
     *
     * @param requestCode
     * @param resultCode
     * @param imageReturnedIntent
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    Uri tempUriSelect = createTempFile();
                    pictureUri = imageReturnedIntent.getData();
                    ImageUtil.performCrop(pictureUri, this, PICTURE_CROP, tempUriSelect);
                }
                break;
            case PICTURE_CROP:
                Bundle extras = imageReturnedIntent.getExtras();
                mBitmap = extras.getParcelable("data");
                tempFile.deleteOnExit();
                if (mBitmap != null) {
                    // Bild hochladen
                    setProgressBarIndeterminateVisibility(Boolean.TRUE);
                    GroupController.getInstance().changePicture(new ExtendedTaskDelegateAdapter<Void, BlobKey>() {
                        @Override
                        public void taskDidFinish(ExtendedTask task, BlobKey blobKey) {
                            setProgressBarIndeterminateVisibility(Boolean.FALSE);
                            GroupImageLoader.getInstance().setGroupImageToImageView(GroupProfileActivity.this, blobKey, mGroupPicture);
                        }

                        @Override
                        public void taskFailed(ExtendedTask task, String message) {
                            setProgressBarIndeterminateVisibility(Boolean.FALSE);
                            Toast.makeText(GroupProfileActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    }, groupName, ImageUtil.BitmapToInputStream(mBitmap));
                }
                break;
        }
    }

    /**
     * Erstellt eine temporäre Datei für das zugeschnittene Bild
     *
     * @return
     */
    private Uri createTempFile() {
        try {
            tempFile = File.createTempFile("crop", ".png", Environment
                    .getExternalStorageDirectory());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Uri.fromFile(tempFile);
    }

    /**
     * Setzt die Listener für die Buttons.
     */
    private void setClickListener() {
        setVisibilityListener();

        mGroupDescriptionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder altertadd = new AlertDialog.Builder(GroupProfileActivity.this);
                altertadd.setMessage(group.getDescription());
                altertadd.show();
            }
        });

        mJoinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startJoinEvent();
            }
        });

        mLeaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkIsMember) {
                    if (getRights(ServiceProvider.getEmail()).contains(Right.FULLRIGHTS.name())) {
                        Toast.makeText(GroupProfileActivity.this, R.string.leaderTriesToLeave, Toast.LENGTH_LONG).show();
                    }
                    AlertDialog.Builder altertadd = new AlertDialog.Builder(GroupProfileActivity.this);
                    altertadd.setTitle(R.string.leaveGroupTitle);
                    altertadd.setMessage(R.string.leaveGroupMessage);
                    altertadd.setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setProgressBarIndeterminateVisibility(Boolean.TRUE);
                            GroupController.getInstance().leaveUserGroup(new ExtendedTaskDelegateAdapter<Void, Void>() {
                                @Override
                                public void taskDidFinish(ExtendedTask task, Void aVoid) {
                                    checkIsMember = false;
                                    mChangeVisibility.setVisibility(View.GONE);
                                    mAdapter.getGroupBlackBoardFragment().changeButtonVisibility(true);
                                    setUpProfile();

                                }

                                @Override
                                public void taskFailed(ExtendedTask task, String message) {
                                    Toast.makeText(GroupProfileActivity.this, message, Toast.LENGTH_LONG).show();
                                    setProgressBarIndeterminateVisibility(Boolean.FALSE);
                                }
                            }, groupName);
                            dialog.dismiss();
                        }
                    });
                    altertadd.setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    altertadd.show();
                }
            }
        });

        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder altertadd = new AlertDialog.Builder(GroupProfileActivity.this);
                altertadd.setTitle(R.string.deleteGroupTitle);
                altertadd.setMessage(R.string.areYouSure);
                altertadd.setPositiveButton(R.string.yesButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setProgressBarIndeterminateVisibility(Boolean.TRUE);
                        GroupController.getInstance().deleteUserGroup(new ExtendedTaskDelegateAdapter<Void, Void>() {
                            @Override
                            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                                setProgressBarIndeterminateVisibility(Boolean.TRUE);
                                finish();

                            }

                            @Override
                            public void taskFailed(ExtendedTask task, String message) {
                                Toast.makeText(GroupProfileActivity.this, message, Toast.LENGTH_LONG).show();
                                setProgressBarIndeterminateVisibility(Boolean.FALSE);
                            }
                        }, groupName);
                        dialog.dismiss();
                    }
                });
                altertadd.setNegativeButton(R.string.cancelButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                altertadd.show();
            }
        });
    }

    private void setVisibilityListener() {
        mChangeVisibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setProgressBarIndeterminateVisibility(Boolean.TRUE);
                GroupController.getInstance().changeMyVisibility(new ExtendedTaskDelegateAdapter<Void, UserGroupVisibleMembers>() {
                    @Override
                    public void taskDidFinish(ExtendedTask task, UserGroupVisibleMembers result) {
                        if (result.getVisibleMembers() != null) {
                            if (result.getVisibleMembers().contains(ServiceProvider.getEmail())) {
                                mChangeVisibility.setColorNormalResId(R.color.colorPrimaryLeave);
                                mChangeVisibility.setColorPressedResId(R.color.colorPressedBlackBoard);
                                mChangeVisibility.setImageResource(R.drawable.ic_eye_white_24dp);
                                checkVisibility = true;
                                Toast.makeText(GroupProfileActivity.this, R.string.youAreNowVisible, Toast.LENGTH_LONG).show();
                            }else {
                                mChangeVisibility.setColorNormalResId(R.color.colorPrimaryJoin);
                                mChangeVisibility.setColorPressedResId(R.color.colorPressedBlackBoard);
                                mChangeVisibility.setImageResource(R.drawable.ic_eye_off_white_24dp);
                                checkVisibility = false;
                                Toast.makeText(GroupProfileActivity.this, R.string.youAreNowInvisible, Toast.LENGTH_LONG).show();
                            }
                        } else {
                            mChangeVisibility.setColorNormalResId(R.color.colorPrimaryJoin);
                            mChangeVisibility.setColorPressedResId(R.color.colorPressedBlackBoard);
                            mChangeVisibility.setImageResource(R.drawable.ic_eye_off_white_24dp);
                            checkVisibility = false;
                            Toast.makeText(GroupProfileActivity.this, R.string.youAreNowInvisible, Toast.LENGTH_LONG).show();
                        }
                        setProgressBarIndeterminateVisibility(Boolean.FALSE);
                    }

                    @Override
                    public void taskFailed(ExtendedTask task, String message) {
                        Toast.makeText(GroupProfileActivity.this, message, Toast.LENGTH_LONG).show();
                        setProgressBarIndeterminateVisibility(Boolean.FALSE);
                    }
                }, groupName);
            }
        });
    }

    private void startJoinEvent() {
        if (!checkIsMember && !group.getPrivat()) {
            // Zeige einen Dialog ohne Passwort um eine öffentliche Gruppe zu joinen
            AlertDialog.Builder altertadd = new AlertDialog.Builder(GroupProfileActivity.this);
            altertadd.setMessage(R.string.title_alert_dialog_join_user_group);
            altertadd.setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setProgressBarIndeterminateVisibility(Boolean.TRUE);
                    GroupController.getInstance().joinUserGroup(new ExtendedTaskDelegateAdapter<Void, BooleanWrapper>() {
                        @Override
                        public void taskDidFinish(ExtendedTask task, BooleanWrapper booleanWrapper) {
                            if (booleanWrapper.getValue()) {
                                checkIsMember = true;
                                mAdapter.getGroupBlackBoardFragment().changeButtonVisibility(true);
                                setUpProfile();
                            }
                        }

                        @Override
                        public void taskFailed(ExtendedTask task, String message) {
                            Toast.makeText(GroupProfileActivity.this, message, Toast.LENGTH_LONG).show();
                            setProgressBarIndeterminateVisibility(Boolean.FALSE);
                        }
                    }, groupName);
                    dialog.dismiss();
                }
            });
            altertadd.setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            altertadd.show();
        } else if (!checkIsMember && group.getPrivat()) {
            // Zeige Dialog mit Passwort um eine private Gruppe zu joinen
            AlertDialog.Builder altertadd = new AlertDialog.Builder(GroupProfileActivity.this);
            LayoutInflater factory = LayoutInflater.from(GroupProfileActivity.this);
            final View passwordView = factory.inflate(R.layout.password_view, null);
            final EditText passwordEditText = (EditText) passwordView.findViewById(R.id.password_view);
            altertadd.setView(passwordView);
            altertadd.setMessage(R.string.title_alert_dialog_join_user_group);
            altertadd.setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!passwordEditText.getText().toString().isEmpty()) {
                        setProgressBarIndeterminateVisibility(Boolean.TRUE);
                        GroupController.getInstance().joinPrivateUserGroup(new ExtendedTaskDelegateAdapter<Void, BooleanWrapper>() {
                            @Override
                            public void taskDidFinish(ExtendedTask task, BooleanWrapper booleanWrapper) {
                                if (booleanWrapper.getValue()) {
                                    checkIsMember = true;
                                    mAdapter.getGroupBlackBoardFragment().changeButtonVisibility(true);
                                    setUpProfile();
                                } else {
                                    Toast.makeText(GroupProfileActivity.this, R.string.wrongPasswordMessage, Toast.LENGTH_LONG).show();
                                    setProgressBarIndeterminateVisibility(Boolean.FALSE);
                                }
                            }

                            @Override
                            public void taskFailed(ExtendedTask task, String message) {
                                Toast.makeText(GroupProfileActivity.this, message, Toast.LENGTH_LONG).show();
                                setProgressBarIndeterminateVisibility(Boolean.FALSE);
                            }
                        }, groupName, passwordEditText.getText().toString());
                        dialog.dismiss();
                    }
                }
            });
            altertadd.setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            altertadd.show();
        } else {
            Toast.makeText(GroupProfileActivity.this, R.string.alreadyInGroupString, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Gibt die Rechte des Benutzers zurück, die er in dieser Gruppe hat.
     * Falls er kein Mitglied ist, ist das Resultat null
     *
     * @return Die Rechte innerhalb dieser Gruppe oder null
     */
    public List<String> getRights() {
        return (ArrayList<String>) group.getMemberRights().get(ServiceProvider.getEmail());
    }

    /**
     * Gibt die Rechte der E-Mail zurück, die sie in dieser Gruppe hat.
     * Falls sie kein Mitglied ist, ist das Resultat null
     *
     * @param email
     * @return
     */
    public ArrayList<String> getRights(String email) {
        return (ArrayList<String>) group.getMemberRights().get(email);
    }

    /**
     * Erzeugt je nacht Öffentlichkeitseinstellung der Gruppe einen Dialog zum öffentlich
     * oder privat machen. Ist die Gruppe öffentlich und man möchte sie Privat machen, so wird
     * ein Dialog angezeigt der als Eingabe ein Passwort verlangt, welches man wiederholen muss.
     * Is die Gruppe nicht öffentlich, so wird man lediglich gefragt ob man sich sicher ist und
     * muss dies bestätigen. Das Passwort bleibt dabei erhalten, wird jedoch nicht mehr beachtet.
     */
    public void startChangePrivacyAction() {
        if (!group.getPrivat()) {
            // Zeige Dialog mit Passwort um die Gruppe öffentlich zu machen
            AlertDialog.Builder altertadd = new AlertDialog.Builder(GroupProfileActivity.this);
            LayoutInflater factory = LayoutInflater.from(GroupProfileActivity.this);
            final View makeGroupPrivateView = factory.inflate(R.layout.make_group_private, null);
            final EditText passwordEditText = (EditText) makeGroupPrivateView.findViewById(R.id.make_group_private_password);
            final EditText passwordEditTextAgain = (EditText) makeGroupPrivateView.findViewById(R.id.make_group_private_password_again);
            final TextView informView = (TextView) makeGroupPrivateView.findViewById(R.id.make_group_private_password_check_text_view);

            checkGroupPassword = false;
            //set listener for the EditTextViews
            // EditText für das Passwort
            passwordEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (passwordEditText.getText().toString().equals(passwordEditTextAgain.getText().toString())) {
                        informView.setText(R.string.checkPasswordPositive);
                        informView.setTextColor(GroupProfileActivity.this.getResources().getColor(R.color.check_group_name_positive));
                        checkGroupPassword = true;
                    } else {
                        informView.setText(R.string.checkPasswordNegative);
                        informView.setTextColor(GroupProfileActivity.this.getResources().getColor(R.color.check_group_name_negative));
                        checkGroupPassword = false;
                    }
                    informView.setVisibility(View.VISIBLE);
                }
            });

            // EditText für das wiederholte Passwort
            passwordEditTextAgain.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (passwordEditText.getText().toString().equals(passwordEditTextAgain.getText().toString())) {
                        informView.setText(R.string.checkPasswordPositive);
                        informView.setTextColor(GroupProfileActivity.this.getResources().getColor(R.color.check_group_name_positive));
                        checkGroupPassword = true;
                    } else {
                        informView.setText(R.string.checkPasswordNegative);
                        informView.setTextColor(GroupProfileActivity.this.getResources().getColor(R.color.check_group_name_negative));
                        checkGroupPassword = false;
                    }
                    informView.setVisibility(View.VISIBLE);
                }
            });
            altertadd.setView(makeGroupPrivateView);
            altertadd.setMessage(R.string.makeGroupPrivateString);
            altertadd.setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!passwordEditText.getText().toString().isEmpty() && checkGroupPassword) {
                        setProgressBarIndeterminateVisibility(Boolean.TRUE);
                        GroupController.getInstance().makeUserGroupPrivat(new ExtendedTaskDelegateAdapter<Void, BooleanWrapper>() {
                            @Override
                            public void taskDidFinish(ExtendedTask task, BooleanWrapper booleanWrapper) {
                                if (booleanWrapper.getValue()) {
                                    setUpProfile();
                                }
                            }

                            @Override
                            public void taskFailed(ExtendedTask task, String message) {
                                Toast.makeText(GroupProfileActivity.this, message, Toast.LENGTH_LONG).show();
                                setProgressBarIndeterminateVisibility(Boolean.FALSE);
                            }
                        }, groupName, passwordEditText.getText().toString());
                        dialog.dismiss();
                    }
                }
            });
            altertadd.setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            altertadd.show();
        } else {
            AlertDialog.Builder altertadd = new AlertDialog.Builder(GroupProfileActivity.this);
            altertadd.setMessage(R.string.makeGroupOpenString);
            altertadd.setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setProgressBarIndeterminateVisibility(Boolean.TRUE);
                    GroupController.getInstance().makeUserGroupOpen(new ExtendedTaskDelegateAdapter<Void, BooleanWrapper>() {
                        @Override
                        public void taskDidFinish(ExtendedTask task, BooleanWrapper booleanWrapper) {
                            if (booleanWrapper.getValue()) {
                                setUpProfile();
                            }
                        }

                        @Override
                        public void taskFailed(ExtendedTask task, String message) {
                            Toast.makeText(GroupProfileActivity.this, message, Toast.LENGTH_LONG).show();
                            setProgressBarIndeterminateVisibility(Boolean.FALSE);
                        }
                    }, groupName);
                    dialog.dismiss();
                }
            });
            altertadd.setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            altertadd.show();
        }
    }

    /**
     * Wenn die Gruppe eine Private Gruppe ist, dann wird ein AlertDialog erzeugt der drei
     * Textfelder hat. Das erste Textfeld muss mit dem momentanen Passwort befüllt werden.
     * Die nächsten zwei dienen zum ändern des Passwortes, diese beiden müssen übereinstimmen
     * damit eine Anfrage an den Server gesendet wird. Schlägt diese fehlt, so muss man es erneut
     * versuchen. Der Dialog bleibt jedoch bestehen mitsamt Eingabedaten.
     */
    public void startChangePasswordAction() {
        if (!group.getPrivat()) {
            Toast.makeText(this, R.string.groupOpenNoNeedForPw, Toast.LENGTH_LONG).show();
        } else {
            // Zeige Dialog mit Passwort um die Gruppe öffentlich zu machen
            AlertDialog.Builder altertadd = new AlertDialog.Builder(GroupProfileActivity.this);
            LayoutInflater factory = LayoutInflater.from(GroupProfileActivity.this);
            final View changePasswordView = factory.inflate(R.layout.change_password_action, null);
            final EditText currentPwEditText = (EditText) changePasswordView.findViewById(R.id.change_group_password_current_pw);
            final EditText passwordEditText = (EditText) changePasswordView.findViewById(R.id.change_group_password_new_pw);
            final EditText passwordEditTextAgain = (EditText) changePasswordView.findViewById(R.id.change_group_password_new_pw_again);
            final TextView informView = (TextView) changePasswordView.findViewById(R.id.change_group_password_info);

            checkGroupPassword = false;
            //set listener for the EditTextViews
            // EditText für das Passwort
            passwordEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (passwordEditText.getText().toString().equals(passwordEditTextAgain.getText().toString())) {
                        informView.setText(R.string.checkPasswordPositive);
                        informView.setTextColor(GroupProfileActivity.this.getResources().getColor(R.color.check_group_name_positive));
                        checkGroupPassword = true;
                    } else {
                        informView.setText(R.string.checkPasswordNegative);
                        informView.setTextColor(GroupProfileActivity.this.getResources().getColor(R.color.check_group_name_negative));
                        checkGroupPassword = false;
                    }
                    informView.setVisibility(View.VISIBLE);
                }
            });

            // EditText für das wiederholte Passwort
            passwordEditTextAgain.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (passwordEditText.getText().toString().equals(passwordEditTextAgain.getText().toString())) {
                        informView.setText(R.string.checkPasswordPositive);
                        informView.setTextColor(GroupProfileActivity.this.getResources().getColor(R.color.check_group_name_positive));
                        checkGroupPassword = true;
                    } else {
                        informView.setText(R.string.checkPasswordNegative);
                        informView.setTextColor(GroupProfileActivity.this.getResources().getColor(R.color.check_group_name_negative));
                        checkGroupPassword = false;
                    }
                    informView.setVisibility(View.VISIBLE);
                }
            });
            altertadd.setView(changePasswordView);
            altertadd.setMessage(R.string.groupChangePasswordTitle);
            altertadd.setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!currentPwEditText.getText().toString().isEmpty() && !passwordEditText.getText().toString().isEmpty() && checkGroupPassword) {
                        setProgressBarIndeterminateVisibility(Boolean.TRUE);
                        GroupController.getInstance().changeUserGroupPassword(new ExtendedTaskDelegateAdapter<Void, BooleanWrapper>() {
                            @Override
                            public void taskDidFinish(ExtendedTask task, BooleanWrapper booleanWrapper) {
                                if (booleanWrapper.getValue()) {
                                    setUpProfile();
                                }
                            }

                            @Override
                            public void taskFailed(ExtendedTask task, String message) {
                                Toast.makeText(GroupProfileActivity.this, message, Toast.LENGTH_LONG).show();
                                setProgressBarIndeterminateVisibility(Boolean.FALSE);
                            }
                        }, groupName, currentPwEditText.getText().toString(), passwordEditText.getText().toString());
                        dialog.dismiss();
                    }
                }
            });
            altertadd.setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            altertadd.show();
        }
    }

    /**
     * Öffnet einen Dialog mit einem Textfeld um dort eine globale Nachricht anzugeben.
     * Diese Nachricht wird dann über den GroupController an den Server geschickt und dort
     * wird jedem Mitglied der Gruppe eine Notification gesendet. Dabei darf die Message, die
     * man im Textfeld angibt nicht leer sein. Sie kann jedoch nur ein Zeichen umfassen.
     */
    public void startGlobalMessageAction() {
        AlertDialog.Builder altertadd = new AlertDialog.Builder(GroupProfileActivity.this);
        LayoutInflater factory = LayoutInflater.from(GroupProfileActivity.this);
        final View writeMessageView = factory.inflate(R.layout.post_message, null);
        altertadd.setView(writeMessageView);
        final EditText messageEditText = (EditText) writeMessageView.findViewById(R.id.post_black_board_edit_text);
        final TextView failureTextView = (TextView) writeMessageView.findViewById(R.id.failure_text_view);

        // Die TextView initialisieren, da die post Blackboard  Funktion diese auch verwendet und kein Text gesetzt ist.
        failureTextView.setText(R.string.globalMessageTooShort);
        failureTextView.setTextColor(GroupProfileActivity.this.getResources().getColor(R.color.check_group_name_negative));

        // EditText für die Global Message
        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (messageEditText.getText().toString().length() > 10) {
                    failureTextView.setVisibility(View.GONE);
                    checkGlobalMessageTextChecked = true;
                } else {
                    if (failureTextView.getVisibility() == View.GONE) {
                        failureTextView.setVisibility(View.VISIBLE);
                    }
                    checkGlobalMessageTextChecked = false;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        altertadd.setView(writeMessageView);
        altertadd.setMessage(R.string.globalMessageTitle);
        altertadd.setPositiveButton(R.string.sendButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (checkGlobalMessageTextChecked) {
                    setProgressBarIndeterminateVisibility(Boolean.TRUE);
                    GroupController.getInstance().sendGlobalMessage(new ExtendedTaskDelegateAdapter<Void, Void>() {
                        @Override
                        public void taskDidFinish(ExtendedTask task, Void aVoid) {
                            Toast.makeText(GroupProfileActivity.this, R.string.globalMessageSend, Toast.LENGTH_LONG);
                            setProgressBarIndeterminateVisibility(Boolean.FALSE);
                        }

                        @Override
                        public void taskFailed(ExtendedTask task, String message) {
                            Toast.makeText(GroupProfileActivity.this, message, Toast.LENGTH_LONG);
                            setProgressBarIndeterminateVisibility(Boolean.FALSE);
                        }
                    }, groupName, messageEditText.getText().toString());
                    dialog.dismiss();
                }
            }
        });
        altertadd.setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        altertadd.show();
    }

    /**
     * Wird aufgerufen, wenn ein Mitglied einer Gruppe mit dem Recht Mitglieder zu löschen den
     * Deletebutton in der Liste der Mitglieder betätigt. Es wird geprüft, ob versucht wird den
     * Leader der Gruppe zu löschen, falls ja bricht der Vorgang mit einem Toast ab. Wenn nicht
     * wird über den GroupController das ausgewählte Mitglied entfernt. Ja man kann sich selber
     * auch entfernen, das ist das selbe als wenn man eine Gruppe verlässt.
     *
     * @param email Die E-Mail des Endusers
     */
    public void startRemoveMemberAction(final String email) {
        if (getRights(email).contains(Right.FULLRIGHTS.name())) {
            Toast.makeText(GroupProfileActivity.this, R.string.deleteLeaderFailmessage, Toast.LENGTH_LONG).show();
        } else {
            AlertDialog.Builder altertadd = new AlertDialog.Builder(this);
            altertadd.setTitle(R.string.removeMemberTitle);
            altertadd.setMessage(email);
            altertadd.setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setProgressBarIndeterminateVisibility(Boolean.TRUE);
                    GroupController.getInstance().removeMember(new ExtendedTaskDelegateAdapter<Void, Void>() {
                        @Override
                        public void taskDidFinish(ExtendedTask task, Void aVoid) {
                            setUpProfile();
                        }

                        @Override
                        public void taskFailed(ExtendedTask task, String message) {
                            Toast.makeText(GroupProfileActivity.this, message, Toast.LENGTH_LONG).show();
                            setProgressBarIndeterminateVisibility(Boolean.FALSE);
                        }
                    }, groupName, email);
                    dialog.dismiss();
                }
            });
            altertadd.setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            altertadd.show();
        }
    }

    /**
     * Startet die DistributeRightsActivity und übergibt dabei die Mitglieder, sowie
     * den namen der Gruppe.
     */
    public void startDistributeRightsActoin() {
        Intent start_distribute_rights_intent = new Intent(this, DistributeRightsActivity.class);
        start_distribute_rights_intent.putExtra(EXTRA_GROUP_NAME, groupName);
        ArrayList<String> groupMembers = new ArrayList<>();
        for (String member : group.getMemberRights().keySet()) {
            groupMembers.add(member);
        }
        start_distribute_rights_intent.putExtra(EXTRA_GROUP_MEMBERS, groupMembers);
        startActivity(start_distribute_rights_intent);
    }

    public void startDistributeRightsToAction(final String email, final String firstName) {
        if (email == null || email.isEmpty()) {
            Toast.makeText(this, R.string.dontDistributeRightsToNothing, Toast.LENGTH_LONG).show();
            return;
        }
        AlertDialog.Builder altertadd = new AlertDialog.Builder(this);
        LayoutInflater factory = LayoutInflater.from(GroupProfileActivity.this);
        final View distributeView = factory.inflate(R.layout.distribute_rights_to_one, null);

        //Initialisiere die Switches in der View
        Switch distributeRightsSwitch = (Switch) distributeView.findViewById(R.id.switch_distribute_rights);
        Switch changeGroupPictureSwitch = (Switch) distributeView.findViewById(R.id.switch_change_group_picture);
        Switch globalMessageSwitch = (Switch) distributeView.findViewById(R.id.switch_global_message);
        Switch inviteGroupSwitch = (Switch) distributeView.findViewById(R.id.switch_invite_group);
        Switch deleteMemberSwitch = (Switch) distributeView.findViewById(R.id.switch_delete_member);
        Switch editBlacBoardSwitch = (Switch) distributeView.findViewById(R.id.switch_edit_black_board);

        //Initialisiere die Listen
        rightsToGive = new ArrayList<>();
        rightsToTake = new ArrayList<>();

        //Hole die Rechte
        final ArrayList<String> rights = getRights(email);
        if(rights.contains(Right.FULLRIGHTS.name())){
            Toast.makeText(GroupProfileActivity.this, R.string.cant_distribute_rights_to_leader, Toast.LENGTH_LONG).show();
            return;
        }

        for (String right : rights) {
            if (right.equals(Right.DISTRIBUTERIGHTS.name())) {
                distributeRightsSwitch.setChecked(true);
            }
            if (right.equals(Right.CHANGEGROUPPICTURE.name())) {
                changeGroupPictureSwitch.setChecked(true);
            }
            if (right.equals(Right.INVITEGROUP.name())) {
                inviteGroupSwitch.setChecked(true);
            }
            if (right.equals(Right.DELETEMEMBER.name())) {
                deleteMemberSwitch.setChecked(true);
            }
            if (right.equals(Right.GLOBALMESSAGE.name())) {
                globalMessageSwitch.setChecked(true);
            }
            if (right.equals(Right.EDITBLACKBOARD.name())) {
                editBlacBoardSwitch.setChecked(true);
            }
        }

        distributeRightsSwitch.setOncheckListener(new Switch.OnCheckListener() {
            @Override
            public void onCheck(Switch aSwitch, boolean checked) {
                if (checked) {
                    if(!rights.contains(Right.DISTRIBUTERIGHTS.name())){
                        rightsToGive.add(Right.DISTRIBUTERIGHTS.name());
                    }
                    rightsToTake.remove(Right.DISTRIBUTERIGHTS.name());
                } else {
                    rightsToGive.remove(Right.DISTRIBUTERIGHTS.name());
                    rightsToTake.add(Right.DISTRIBUTERIGHTS.name());
                }

            }
        });
        changeGroupPictureSwitch.setOncheckListener(new Switch.OnCheckListener() {
            @Override
            public void onCheck(Switch aSwitch, boolean checked) {
                if (checked) {
                    if(!rights.contains(Right.CHANGEGROUPPICTURE.name())){
                        rightsToGive.add(Right.CHANGEGROUPPICTURE.name());
                    }

                    rightsToTake.remove(Right.CHANGEGROUPPICTURE.name());
                } else {
                    rightsToGive.remove(Right.CHANGEGROUPPICTURE.name());
                    rightsToTake.add(Right.CHANGEGROUPPICTURE.name());
                }

            }
        });
        inviteGroupSwitch.setOncheckListener(new Switch.OnCheckListener() {
            @Override
            public void onCheck(Switch aSwitch, boolean checked) {
                if (checked) {
                    if(!rights.contains(Right.INVITEGROUP.name())){
                        rightsToGive.add(Right.INVITEGROUP.name());
                    }
                    rightsToTake.remove(Right.INVITEGROUP.name());
                } else {
                    rightsToGive.remove(Right.INVITEGROUP.name());
                    rightsToTake.add(Right.INVITEGROUP.name());
                }

            }
        });
        deleteMemberSwitch.setOncheckListener(new Switch.OnCheckListener() {
            @Override
            public void onCheck(Switch aSwitch, boolean checked) {
                if (checked) {
                    if(!rights.contains(Right.DELETEMEMBER.name())){
                        rightsToGive.add(Right.DELETEMEMBER.name());
                    }
                    rightsToTake.remove(Right.DELETEMEMBER.name());
                } else {
                    rightsToGive.remove(Right.DELETEMEMBER.name());
                    rightsToTake.add(Right.DELETEMEMBER.name());
                }

            }
        });
        globalMessageSwitch.setOncheckListener(new Switch.OnCheckListener() {
            @Override
            public void onCheck(Switch aSwitch, boolean checked) {
                if (checked) {
                    if(!rights.contains(Right.GLOBALMESSAGE.name())){
                        rightsToGive.add(Right.GLOBALMESSAGE.name());
                    }
                    rightsToTake.remove(Right.GLOBALMESSAGE.name());
                } else {
                    rightsToGive.remove(Right.GLOBALMESSAGE.name());
                    rightsToTake.add(Right.GLOBALMESSAGE.name());
                }

            }
        });
        editBlacBoardSwitch.setOncheckListener(new Switch.OnCheckListener() {
            @Override
            public void onCheck(Switch aSwitch, boolean checked) {
                if (checked) {
                    if(!rights.contains(Right.EDITBLACKBOARD.name())){
                        rightsToGive.add(Right.EDITBLACKBOARD.name());
                    }
                    rightsToTake.remove(Right.EDITBLACKBOARD.name());
                } else {
                    rightsToGive.remove(Right.EDITBLACKBOARD.name());
                    rightsToTake.add(Right.EDITBLACKBOARD.name());
                }

            }
        });

        altertadd.setTitle(R.string.distributeRightsToOne);
        altertadd.setMessage(firstName);
        altertadd.setView(distributeView);
        altertadd.setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (rightsToGive.size() > 0) {
                    setProgressBarIndeterminateVisibility(Boolean.TRUE);
                    RightController.getInstance().giveRightsToUser(new ExtendedTaskDelegateAdapter<Void, Void>() {
                        @Override
                        public void taskDidFinish(ExtendedTask task, Void aVoid) {
                            setUpProfile();
                        }

                        @Override
                        public void taskFailed(ExtendedTask task, String message) {
                            Toast.makeText(GroupProfileActivity.this, message, Toast.LENGTH_LONG).show();
                            setProgressBarIndeterminateVisibility(Boolean.FALSE);
                        }
                    }, groupName, email, rightsToGive);
                }
                if (rightsToTake.size() > 0) {
                    setProgressBarIndeterminateVisibility(Boolean.TRUE);
                    RightController.getInstance().takeRightsFromUser(new ExtendedTaskDelegateAdapter<Void, Void>() {
                        @Override
                        public void taskDidFinish(ExtendedTask task, Void aVoid) {
                            setUpProfile();
                        }

                        @Override
                        public void taskFailed(ExtendedTask task, String message) {
                            Toast.makeText(GroupProfileActivity.this, message, Toast.LENGTH_LONG).show();
                            setProgressBarIndeterminateVisibility(Boolean.FALSE);
                        }
                    }, groupName, email, rightsToTake);
                }
                dialog.dismiss();
            }
        });
        altertadd.setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        altertadd.show();
    }

    /**
     * Löscht den übergebenen Blackboard entry von der Gruppe.
     *
     * @param id Die id des Boardentries
     */
    public void startDeleteBoardEntry(final Long id) {
        if (id == 0 || id == null) {
            Toast.makeText(this, R.string.noEntryToDelete, Toast.LENGTH_LONG).show();
        } else {
            AlertDialog.Builder altertadd = new AlertDialog.Builder(this);
            altertadd.setTitle(R.string.deleteThisEntryQuestion);
            altertadd.setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setProgressBarIndeterminateVisibility(Boolean.TRUE);
                    GroupController.getInstance().deleteBoardMessage(new ExtendedTaskDelegateAdapter<Void, Void>() {
                        @Override
                        public void taskDidFinish(ExtendedTask task, Void aVoid) {
                            setUpProfile();
                        }

                        @Override
                        public void taskFailed(ExtendedTask task, String message) {
                            Toast.makeText(GroupProfileActivity.this, message, Toast.LENGTH_LONG).show();
                            setProgressBarIndeterminateVisibility(Boolean.FALSE);
                        }
                    }, groupName, id);
                    dialog.dismiss();
                }
            });
            altertadd.setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            altertadd.show();
        }
    }

    /**
     * Wird aufgerufen, wenn ein Benutzer eine Einladung erhalten hat und diese Notification
     * berührt hat, also diese geclickt hat.
     */
    public void checkIfInvitationIntent() {
        if (getIntent().getStringExtra(EXTRA_INVITE_GROUP) != null) {
            startJoinEvent();
        }
    }

    /**
     * Startet die InviteUsersActivity um neue Mitglieder für die Gruppe anzuwerben.
     */
    public void startInviteUsersToGroup() {
        Intent start_invite_users_intent = new Intent(this, InviteUsersToGroupActivity.class);
        start_invite_users_intent.putExtra(EXTRA_GROUP_NAME, groupName);
        ArrayList<String> members = new ArrayList<>();
        for (String member : group.getMemberRights().keySet()) {
            members.add(member);
        }
        start_invite_users_intent.putStringArrayListExtra(EXTRA_GROUP_MEMBERS, members);
        startActivity(start_invite_users_intent);
    }

    /**
     * Startet die CommentBlackBoardActivity und übergibt als Extra die id des zu
     * kommentierenden BoardEntries.
     *
     * @param id
     */
    public void startCommentMessage(Long id) {
        Intent comment_board_message_intent = new Intent(this, CommentBlackBoardActivity.class);
        comment_board_message_intent.putExtra(CommentBlackBoardActivity.EXTRA_BOARD_ID, id.toString());
        comment_board_message_intent.putExtra(EXTRA_GROUP_NAME, groupName);
        startActivity(comment_board_message_intent);
    }

    /**
     * Startet die CommentBlackBoardActivity und übergibt als Extra die id des zu
     * kommentierenden BoardEntries.
     *
     * @param id
     */
    public void startEditMessage(final Long id, String message) {
        AlertDialog.Builder altertadd = new AlertDialog.Builder(this);
        LayoutInflater factory = LayoutInflater.from(GroupProfileActivity.this);
        final View editView = factory.inflate(R.layout.post_message, null);
        final EditText newMessage = (EditText)editView.findViewById(R.id.post_black_board_edit_text);
        final TextView errorMessage= (TextView)editView.findViewById(R.id.failure_text_view);
        errorMessage.setText(R.string.textTooShort);
        newMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (newMessage.getText().length() < 2) {
                    errorMessage.setVisibility(View.VISIBLE);
                } else {
                    errorMessage.setVisibility(View.GONE);
                }
            }
        });
        newMessage.setText(message);
        altertadd.setView(editView);
        altertadd.setTitle(R.string.edit_this_entry);
        altertadd.setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (newMessage.getText().length() > 1) {
                    setProgressBarIndeterminateVisibility(Boolean.TRUE);
                    GroupController.getInstance().editBoardEntry(new ExtendedTaskDelegateAdapter<Void, Void>() {
                        @Override
                        public void taskDidFinish(ExtendedTask task, Void aVoid) {
                            setUpProfile();
                        }

                        @Override
                        public void taskFailed(ExtendedTask task, String message) {
                            Toast.makeText(GroupProfileActivity.this, message, Toast.LENGTH_LONG).show();
                            setProgressBarIndeterminateVisibility(Boolean.FALSE);
                        }
                    }, id, groupName, newMessage.getText().toString());
                    dialog.dismiss();
                }
            }
        });
        altertadd.setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        altertadd.show();
    }
}
