package ws1415.ps1415.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.skatenight.skatenightAPI.model.Board;
import com.skatenight.skatenightAPI.model.UserGroup;
import com.skatenight.skatenightAPI.model.UserGroupBlackBoardTransport;

import ws1415.ps1415.R;
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
    ListView mBlackBoardListView;
    BlackBoardListAdapter mAdapter;
    FloatingActionButton mAddMessageButton;
    String groupName;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_group_black_board, container, false);

        // Die ListView und die Button für das Posten von Einträgen initialisieren
        mBlackBoardListView = (ListView) rootView.findViewById(R.id.group_black_board_list_view);
        mAddMessageButton = (FloatingActionButton) rootView.findViewById(R.id.group_black_board_add_message_button);

        // Clicklistener setzen für das Posten von Einträgen
        mAddMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder altertadd = new AlertDialog.Builder(rootView.getContext());
                LayoutInflater factory = LayoutInflater.from(rootView.getContext());
                final View postView = factory.inflate(R.layout.post_black_board, null);
                final EditText messageEditText = (EditText) postView.findViewById(R.id.post_black_board_edit_text);
                altertadd.setView(postView);
                altertadd.setMessage(R.string.postMessageTitle);
                altertadd.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!messageEditText.getText().toString().isEmpty()){
                            GroupController.getInstance().postBlackBoard(new ExtendedTaskDelegateAdapter<Void, Void>() {
                                @Override
                                public void taskDidFinish(ExtendedTask task, Void aVoid) {
                                    getNewBlackBoard(container.getContext());
                                }

                                @Override
                                public void taskFailed(ExtendedTask task, String message) {
                                    Toast.makeText(rootView.getContext(), message, Toast.LENGTH_LONG).show();
                                }
                            }, groupName, messageEditText.getText().toString());
                            dialog.dismiss();
                        }
                        dialog.dismiss();
                    }
                });
                altertadd.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
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
     * @param groupName Der Name der Gruppe
     * @param contetx Die View von der aus diese Methode aufgerufen wird
     */
    public void setUp(Board blackBoard, String groupName, Context contetx) {
        this.groupName = groupName;
        if(blackBoard != null){
            if(blackBoard.getBoardEntries() != null){
                mAdapter = new BlackBoardListAdapter(contetx, blackBoard.getBoardEntries());
                if (mBlackBoardListView != null) mBlackBoardListView.setAdapter(mAdapter);
            }
        }
    }

    /**
     * Diese Methode wird aufgerufen, wenn eine neue Nachricht auf dem Blackboard der Gruppe
     * gepostet wurde. Hier wird nur das Blackboard der Gruppe abgerufen und ein neuer Adapter
     * dazu gesetzt.
     *
     * @param context
     */
    private void getNewBlackBoard(final Context context){
        GroupController.getInstance().getBlackBoard(new ExtendedTaskDelegateAdapter<Void, UserGroupBlackBoardTransport>(){
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroupBlackBoardTransport userGroupBlackBoardTransport) {
                if(userGroupBlackBoardTransport.getBoardEntries() != null){
                    mAdapter = new BlackBoardListAdapter(context, userGroupBlackBoardTransport.getBoardEntries());
                    if(mBlackBoardListView !=null) mBlackBoardListView.setAdapter(mAdapter);
                }
            }
        }, groupName);
    }
}
