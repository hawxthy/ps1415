package ws1415.ps1415.activity;

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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.skatenight.skatenightAPI.model.BoardEntry;
import com.skatenight.skatenightAPI.model.BooleanWrapper;

import ws1415.ps1415.R;
import ws1415.ps1415.adapter.CommentBoardAdapter;
import ws1415.ps1415.controller.GroupController;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;

public class CommentBlackBoardActivity extends Activity {
    public static final String EXTRA_BOARD_ID = "entryId";
    private BoardEntry be;
    private Long id;

    //Viewelemente
    private ListView mComments;
    private CommentBoardAdapter mAdapter;
    private FloatingActionButton commentButton;

    // Bedingungsvariable
    private boolean textOkay = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_comment_black_board);
        setProgressBarIndeterminateVisibility(Boolean.FALSE);

        id = Long.parseLong(getIntent().getStringExtra(EXTRA_BOARD_ID));
        if(id== null){
            Toast.makeText(this, R.string.noLongIdSubmitted, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialisiere die View Elemente
        mComments = (ListView)findViewById(R.id.comments_list_view);
        commentButton = (FloatingActionButton)findViewById(R.id.add_comment_button);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_comment_black_board, menu);
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

        setUp();
    }

    /**
     * Ruft den Boardentry vom Server ab und setzt die Kommentare als Items in die Listview
     */
    private void setUp(){
        setProgressBarIndeterminateVisibility(Boolean.TRUE);
        GroupController.getInstance().getBoardEntry(new ExtendedTaskDelegateAdapter<Void, BoardEntry>() {
            @Override
            public void taskDidFinish(ExtendedTask task, BoardEntry boardEntry) {
                if (boardEntry != null) {
                    mAdapter = new CommentBoardAdapter(CommentBlackBoardActivity.this, boardEntry.getId(), boardEntry.getComments());
                    mComments.setAdapter(mAdapter);
                }
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
            }

            @Override
            public void taskFailed(ExtendedTask task, String message) {
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
                Toast.makeText(CommentBlackBoardActivity.this, message, Toast.LENGTH_LONG).show();
            }
        }, id);

        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder altertadd = new AlertDialog.Builder(CommentBlackBoardActivity.this);
                LayoutInflater factory = LayoutInflater.from(CommentBlackBoardActivity.this);
                final View commentView = factory.inflate(R.layout.post_black_board, null);
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
                        if(messageEditText.getText().length() < 3){
                            failureTextView.setText(R.string.textTooShort);
                            failureTextView.setVisibility(View.VISIBLE);
                            textOkay = false;
                        }else{
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
                                        be= entry;
                                        setUp();
                                    }
                                    setProgressBarIndeterminateVisibility(Boolean.FALSE);
                                }

                                @Override
                                public void taskFailed(ExtendedTask task, String message) {
                                    Toast.makeText(CommentBlackBoardActivity.this, message, Toast.LENGTH_LONG).show();
                                    setProgressBarIndeterminateVisibility(Boolean.FALSE);
                                }
                            },id, messageEditText.getText().toString());
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
        });
    }
}
