package ws1415.ps1415.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.skatenight.skatenightAPI.model.Board;
import com.skatenight.skatenightAPI.model.UserGroupNewsBoardTransport;

import ws1415.ps1415.R;
import ws1415.ps1415.adapter.NewsBoardAdapter;
import ws1415.ps1415.controller.GroupController;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;

/**
 * Dieses Fragment dient zum Anzeigen von Newsboard Einträgen.
 *
 * @author Bernd Eissing
 */
public class GroupNewsBoardFragment extends Fragment {
    private NewsBoardAdapter mAdapter;
    private ListView mNewsBoardListView;

    private String groupName;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_group_news_board, container, false);

        mNewsBoardListView = (ListView) rootView.findViewById(R.id.group_profile_news_board_list_view);
        if(mAdapter != null) mNewsBoardListView.setAdapter(mAdapter);

        return rootView;
    }

    /**
     * Erstellt den Adapter für die ListView und fügt diesem die Liste der BoardEntries
     * hinzu, falls die ListView nicht null ist.
     *
     * @param newsBoard
     * @param contetx Die aufrufende View
     */
    public void setUp(Board newsBoard, String groupName, Context contetx) {
        this.groupName = groupName;
        if(newsBoard.getBoardEntries() != null){
            mAdapter = new NewsBoardAdapter(contetx, newsBoard.getBoardEntries());
            if (mNewsBoardListView != null) mNewsBoardListView.setAdapter(mAdapter);
        }
    }

    /**
     * Diese Methode dient als listAllGroups Option, da Aktivitäten in Gruppen häufig passieren wäre es nicht
     * klug bei jeder Aktivität die Liste zu aktualisieren. Aus diesem Grund soll die Aktualisierungs-
     * funktion manuel betätigt werden. Hier wird die Liste alles Newsboardentries geladen und ein
     * neuer Adapter erstellt.
     *
     * @param context
     */
    public void refresh(final Context context){
        GroupController.getInstance().getNewsBoard(new ExtendedTaskDelegateAdapter<Void, UserGroupNewsBoardTransport>(){
            @Override
            public void taskDidFinish(ExtendedTask task, UserGroupNewsBoardTransport userGroupNewsBoardTransport) {
                if(userGroupNewsBoardTransport.getBoardEntries() != null){
                    mAdapter = new NewsBoardAdapter(context, userGroupNewsBoardTransport.getBoardEntries());
                    if(mNewsBoardListView != null) mNewsBoardListView.setAdapter(mAdapter);
                }
            }
        }, groupName);
    }

}
