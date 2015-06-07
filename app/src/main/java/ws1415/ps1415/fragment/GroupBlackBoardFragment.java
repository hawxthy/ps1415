package ws1415.ps1415.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.skatenight.skatenightAPI.model.Board;
import com.skatenight.skatenightAPI.model.UserGroup;
import com.skatenight.skatenightAPI.model.UserGroupBlackBoardTransport;

import java.util.ArrayList;

import ws1415.ps1415.R;
import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.adapter.BlackBoardListAdapter;
import ws1415.ps1415.controller.GroupController;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;

/**
 * Dieses Fragment dient zum Anzeigen von Blackboard Einträgen.
 *
 * @author Bernd Eissing
 */
public class GroupBlackBoardFragment extends Fragment {
    private ListView mBlackBoardListView;
    private BlackBoardListAdapter mAdapter;
    private FloatingActionButton mAddMessageButton;
    private UserGroup group;

    // Attribute zum Testen von Bedingungen
    private boolean checkBoardMesageTextChecked;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_group_black_board, container, false);

        // Die ListView und die Button für das Posten von Einträgen initialisieren
        mBlackBoardListView = (ListView) rootView.findViewById(R.id.group_black_board_list_view);
        mAddMessageButton = (FloatingActionButton) rootView.findViewById(R.id.group_black_board_add_message_button);

        checkBoardMesageTextChecked = false;

        // Clicklistener setzen für das Posten von Einträgen
        mAddMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder altertadd = new AlertDialog.Builder(rootView.getContext());
                LayoutInflater factory = LayoutInflater.from(rootView.getContext());
                final View postView = factory.inflate(R.layout.post_black_board, null);
                final EditText messageEditText = (EditText) postView.findViewById(R.id.post_black_board_edit_text);
                final TextView failureTextView = (TextView) postView.findViewById(R.id.failure_text_view);

                // Die TextView initialisieren, da die send global messge Funktion diese auch verwendet und kein Text gesetzt ist.
                failureTextView.setText(R.string.boardMessageTooShort);
                failureTextView.setTextColor(rootView.getContext().getResources().getColor(R.color.check_group_name_negative));

                // EditText für die Blackboard Message
                messageEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        if (messageEditText.getText().toString().length() > 1) {
                            failureTextView.setVisibility(View.GONE);
                            checkBoardMesageTextChecked = true;
                        } else {
                            if (failureTextView.getVisibility() == View.GONE) {
                                failureTextView.setVisibility(View.VISIBLE);
                            }
                            checkBoardMesageTextChecked = false;
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                    }
                });

                altertadd.setView(postView);
                altertadd.setMessage(R.string.postMessageTitle);
                altertadd.setPositiveButton(R.string.sendButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (messageEditText.getText().toString().length() > 1) {
                            GroupController.getInstance().postBlackBoard(new ExtendedTaskDelegateAdapter<Void, Void>() {
                                @Override
                                public void taskDidFinish(ExtendedTask task, Void aVoid) {
                                    getNewBlackBoard(container.getContext());
                                }

                                @Override
                                public void taskFailed(ExtendedTask task, String message) {
                                    Toast.makeText(rootView.getContext(), message, Toast.LENGTH_LONG).show();
                                }
                            }, group.getName(), messageEditText.getText().toString());
                            dialog.dismiss();
                        }
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
        if (mAdapter != null) mBlackBoardListView.setAdapter(mAdapter);

        return rootView;
    }

    /**
     * Falls das Blackboard der Gruppe Einträge hat, so wird diese Liste dem
     * BlackBoardListAdapter übergeben.
     *
     * @param blackBoard Das Blackboard der Gruppe
     * @param group      Die Gruppe
     * @param contetx    Die View von der aus diese Methode aufgerufen wird
     */
    public void setUp(Board blackBoard, UserGroup group, Context contetx) {
        this.group = group;
        if (blackBoard != null) {
            mAdapter = new BlackBoardListAdapter(contetx, blackBoard.getBoardEntries(), (ArrayList<String>) group.getMemberRights().get(ServiceProvider.getEmail()));
            if (mBlackBoardListView != null) mBlackBoardListView.setAdapter(mAdapter);
        }
    }

    /**
     * Diese Methode wird aufgerufen, wenn eine neue Nachricht auf dem Blackboard der Gruppe
     * gepostet wurde. Hier wird nur das Blackboard der Gruppe abgerufen und ein neuer Adapter
     * dazu gesetzt.
     *
     * @param context
     */
    private void getNewBlackBoard(final Context context) {
        GroupController.getInstance().getBlackBoard(new ExtendedTaskDelegateAdapter<Void, UserGroupBlackBoardTransport>() {
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroupBlackBoardTransport userGroupBlackBoardTransport) {
                if (userGroupBlackBoardTransport.getBoardEntries() != null) {
                    mAdapter = new BlackBoardListAdapter(context, userGroupBlackBoardTransport.getBoardEntries(), (ArrayList<String>) group.getMemberRights().get(ServiceProvider.getEmail()));
                    if (mBlackBoardListView != null) mBlackBoardListView.setAdapter(mAdapter);
                }
            }
        }, group.getName());
    }
}
