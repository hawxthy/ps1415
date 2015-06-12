package ws1415.ps1415.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.skatenight.skatenightAPI.model.BoardEntry;
import com.skatenight.skatenightAPI.model.BooleanWrapper;

import org.w3c.dom.Text;

import ws1415.ps1415.R;
import ws1415.ps1415.adapter.CommentBoardAdapter;
import ws1415.ps1415.controller.GroupController;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.util.DateUtil;
import ws1415.ps1415.util.GroupImageLoader;

public class CommentBlackBoardActivity extends Activity {
    public static final String EXTRA_BOARD_ID = "entryId";
    private BoardEntry be;
    private Long id;
    private String groupName;

    //Viewelemente
    private TextView mWriterView;
    private TextView mContentView;
    private TextView mDateView;
    private ListView mComments;
    private ImageView mMessageImage;
    private CommentBoardAdapter mAdapter;
    private FloatingActionButton commentButton;
    private ActionBar  mActionBar;

    // Bedingungsvariable
    private boolean textOkay = false;
    private boolean textSet = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_comment_black_board);
        setProgressBarIndeterminateVisibility(Boolean.FALSE);

        id = Long.parseLong(getIntent().getStringExtra(EXTRA_BOARD_ID));
        if (id == null) {
            Toast.makeText(this, R.string.noLongIdSubmitted, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        groupName = getIntent().getStringExtra(GroupProfileActivity.EXTRA_GROUP_NAME);
        if(groupName == null){
            Toast.makeText(this, R.string.noGoupNameSubmitted, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        mActionBar = getActionBar();

        mActionBar.setHomeButtonEnabled(false);
        mActionBar.setDisplayHomeAsUpEnabled(true);

        // Initialisiere die View Elemente
        mComments = (ListView) findViewById(R.id.comments_list_view);
        commentButton = (FloatingActionButton) findViewById(R.id.add_comment_button);
        mWriterView = (TextView) findViewById(R.id.list_view_item_black_board_creator_text_view);
        mContentView = (TextView) findViewById(R.id.list_view_item_black_board_content_edit_text);
        mDateView = (TextView) findViewById(R.id.list_view_item_black_board_date_text_view);
        mMessageImage = (ImageView) findViewById(R.id.black_board_message_image);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_comment_black_board, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Ruft die setUp() Methode auf umd die Kommentare neu zu laden
     */
    @Override
    protected void onResume() {
        super.onResume();
        setUp();
    }

    /**
     * Ruft den Boardentry vom Server ab und setzt die Kommentare als Items in die Listview
     */
    private void setUp() {
        setProgressBarIndeterminateVisibility(Boolean.TRUE);
        GroupController.getInstance().getBoardEntry(new ExtendedTaskDelegateAdapter<Void, BoardEntry>() {
            @Override
            public void taskDidFinish(ExtendedTask task, BoardEntry boardEntry) {
                if (boardEntry != null) {
                    mAdapter = new CommentBoardAdapter(CommentBlackBoardActivity.this, boardEntry.getId(), boardEntry.getComments());
                    mComments.setAdapter(mAdapter);
                    if (!textSet) {
                        mWriterView.setText(boardEntry.getWriter());
                        mContentView.setText(boardEntry.getMessage());
                        mDateView.setText(DateUtil.getInstance().formatMyDate(boardEntry.getDate().getValue()));
                        if (boardEntry.getBlobKey() != null) {
                            GroupImageLoader.getInstance().setBoardImageToImageView(CommentBlackBoardActivity.this, boardEntry.getBlobKey(), mMessageImage);
                            mMessageImage.setVisibility(View.VISIBLE);
                        }
                        textSet = true;
                    }
                } else {
                    Toast.makeText(CommentBlackBoardActivity.this, R.string.no_board_entry_found, Toast.LENGTH_SHORT).show();
                }
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
            }

            @Override
            public void taskFailed(ExtendedTask task, String message) {
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
                Toast.makeText(CommentBlackBoardActivity.this, message, Toast.LENGTH_LONG).show();
            }
        }, id);

        /**
         * Startet einen AlertDialog mit einem Textfeld. Dort kann man seine Nachricht eingeben
         * und beim eingeben wird überprüft, ob die Nachricht mindestens 3 Zeichen lang ist.
         * Ist dies der Fall so kann die Nachricht gesendet werden. Dabei wird über den
         * GroupController die commentBlackBoard Methode aufgerufen, welche den BoardEntry Kommentiert
         * den Kommentar speichert und diesen wieder zurück an die App sendet.
         */
        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder altertadd = new AlertDialog.Builder(CommentBlackBoardActivity.this);
                LayoutInflater factory = LayoutInflater.from(CommentBlackBoardActivity.this);
                final View commentView = factory.inflate(R.layout.post_message, null);
                final EditText messageEditText = (EditText) commentView.findViewById(R.id.post_black_board_edit_text);
                final TextView failureTextView = (TextView) commentView.findViewById(R.id.failure_text_view);

                messageEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        if (messageEditText.getText().length() < 3) {
                            failureTextView.setText(R.string.textTooShort);
                            failureTextView.setVisibility(View.VISIBLE);
                            textOkay = false;
                        } else {
                            failureTextView.setVisibility(View.GONE);
                            textOkay = true;
                        }
                    }
                });
                altertadd.setView(commentView);
                altertadd.setMessage(R.string.titleWriteComment);
                altertadd.setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (textOkay) {
                            setProgressBarIndeterminateVisibility(Boolean.TRUE);
                            GroupController.getInstance().commentBlackBoard(new ExtendedTaskDelegateAdapter<Void, BoardEntry>() {
                                @Override
                                public void taskDidFinish(ExtendedTask task, BoardEntry entry) {
                                    if (entry != null) {
                                        be = entry;
                                        setUp();
                                    }
                                    setProgressBarIndeterminateVisibility(Boolean.FALSE);
                                }

                                @Override
                                public void taskFailed(ExtendedTask task, String message) {
                                    Toast.makeText(CommentBlackBoardActivity.this, message, Toast.LENGTH_LONG).show();
                                    setProgressBarIndeterminateVisibility(Boolean.FALSE);
                                }
                            }, id, groupName, messageEditText.getText().toString());
                            dialog.dismiss();
                        } else {
                            Toast.makeText(CommentBlackBoardActivity.this, R.string.textTooShort, Toast.LENGTH_SHORT).show();
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
        });
    }

    /**
     * Methode zum löschen eines Kommentars.
     *
     * @param commentId die id des Kommentars
     * @param entryId die id des BoardEntry
     */
    public void startDeleteComment(long commentId, Long entryId){
        setProgressBarIndeterminateVisibility(Boolean.TRUE);
        GroupController.getInstance().deleteComment(new ExtendedTaskDelegateAdapter<Void, Void>(){
            @Override
            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                setUp();
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
            }

            @Override
            public void taskFailed(ExtendedTask task, String message) {
                Toast.makeText(CommentBlackBoardActivity.this, message, Toast.LENGTH_LONG).show();
            }
        }, groupName, entryId, commentId);
    }
}
