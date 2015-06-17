package ws1415.ps1415.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.melnykov.fab.FloatingActionButton;
import com.skatenight.skatenightAPI.model.Board;
import com.skatenight.skatenightAPI.model.UserGroup;
import com.skatenight.skatenightAPI.model.UserGroupBlackBoardTransport;

import java.util.ArrayList;

import ws1415.ps1415.R;
import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.activity.GroupProfileActivity;
import ws1415.ps1415.activity.PostBlackBoardActivity;
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
    private static final int SELECT_PHOTO = 1;
    private static final int PICTURE_CROP = 2;
    private ListView mBlackBoardListView;
    private BlackBoardListAdapter mAdapter;
    private FloatingActionButton mAddMessageButton;
    private Context context;
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
                Intent post_black_board_intent = new Intent(context, PostBlackBoardActivity.class);
                post_black_board_intent.putExtra(GroupProfileActivity.EXTRA_GROUP_NAME, group.getName());
                context.startActivity(post_black_board_intent);
            }
        });

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
        this.context = contetx;
        this.group = group;
        if (blackBoard != null) {
            if (!group.getMemberRights().keySet().contains(ServiceProvider.getEmail())) {
                mAddMessageButton.setVisibility(View.GONE);
            }
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

    /**
     * Ändert die Sichtbarkeit des Buttons vür das Schreiben von Nachrichten auf dem BlackBoard
     *
     * @param visibility
     */
    public void changeButtonVisibility(boolean visibility){
        if(visibility){
            mAddMessageButton.setVisibility(View.VISIBLE);
        }else{
            mAddMessageButton.setVisibility(View.GONE);
        }
    }
}
