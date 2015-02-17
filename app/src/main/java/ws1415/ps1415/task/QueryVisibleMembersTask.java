package ws1415.ps1415.task;

import android.content.Context;

import com.skatenight.skatenightAPI.model.Member;
import com.skatenight.skatenightAPI.model.UserGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegate;
import ws1415.ps1415.R;
import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.util.PrefManager;

/**
 * Created by Bernd Eissing on 13.02.2015.
 */
public class QueryVisibleMembersTask extends ExtendedTask<Void, Void, HashMap<Member, String>> {
    private Context context;
    /**
     * Initialisiert den Task.
     *
     * @param delegate Klasse die Rückmeldungen zum Fortschritt des Task erhalten soll.
     */
    public QueryVisibleMembersTask(ExtendedTaskDelegate<Void, HashMap<Member, String>> delegate, Context context) {
        super(delegate);
        this.context = context;
    }

    /**
     * Override this method to perform a computation on a background thread. The
     * specified parameters are the parameters passed to {@link #execute}
     * by the caller of this task.
     * <p/>
     * This method can call {@link #publishProgress} to publish updates
     * on the UI thread.
     *
     * @param params The parameters of the task.
     * @return A result, defined by the subclass of this task.
     * @see #onPreExecute()
     * @see #onPostExecute
     * @see #publishProgress
     */
    @Override
    protected HashMap<Member, String> doInBackground(Void... params) {
        List<UserGroup> tmpGroups = null;
        List<Member> groupMembers = new ArrayList<Member>();
        HashMap<Member, String> farbenMap = new HashMap<Member, String> ();
        try{
            tmpGroups = (ArrayList)ServiceProvider.getService().skatenightServerEndpoint().fetchMyUserGroups().execute().getItems();
        }catch( IOException e){
            e.printStackTrace();
        }
        for(UserGroup userGroup: tmpGroups){
            if(PrefManager.getGroupVisibility(context, userGroup.getName())){
                try {
                    groupMembers = ServiceProvider.getService().skatenightServerEndpoint().fetchGroupMembers(userGroup.getName()).execute().getItems();
                }catch(IOException e){
                    e.printStackTrace();
                }
                for(Member m : groupMembers){
                    farbenMap.put(m, userGroup.getName());
                }
            }
        }
        return farbenMap;
    }
}
