package ws1415.ps1415.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.skatenight.skatenightAPI.model.Route;
import com.skatenight.skatenightAPI.model.ServerWaypoint;

import java.io.Serializable;
import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.adapter.RouteAdapter;
import ws1415.ps1415.controller.RouteController;
import ws1415.ps1415.dialog.AddRouteDialog;
import ws1415.ps1415.dialog.AddRouteDraftDialog;
import ws1415.ps1415.fragment.RouteListFragment;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;
import ws1415.ps1415.util.UniversalUtil;

/**
 * Zeigt eine Liste der auf dem Server existierenden Routen an und bietet Funktionen zur Verwaltung
 * der Routen.
 * @author Richard Schulze
 */
public class ManageRoutesActivity extends BaseActivity implements RouteListFragment.OnRouteClickListener {
    /**
     * Der Request-Code für das Hinzufügen einer Route.
     */
    private static final int ADD_ROUTE_REQUEST_CODE = 1;

    private RouteListFragment routeFragment;
    private RouteAdapter routeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Prüft ob der Benutzer eingeloggt ist
        if (!UniversalUtil.checkLogin(this)) {
            finish();
            return;
        }

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_manage_routes);

        // RouteFragment initialisieren
        routeFragment = (RouteListFragment) getFragmentManager().findFragmentById(R.id.routeFragment);
        setProgressBarIndeterminateVisibility(true);
        routeAdapter = new RouteAdapter(new ExtendedTaskDelegateAdapter<Void, List<Route>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<Route> routes) {
                setProgressBarIndeterminateVisibility(false);
            }
            @Override
            public void taskFailed(ExtendedTask task, String message) {
                setProgressBarIndeterminateVisibility(false);
            }
        });
        routeFragment.setListAdapter(routeAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_manage_routes, menu);
        return true;
    }

    /**
     * Händelt ActionBar Item clicks. Wenn man das action_add_route Item
     * auswählt, wird die RouteEditorActivity gestartet.
     *
     * @param item Das Item in der Action Bar
     * @return true
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_create_route) {
            Intent intent = new Intent(this, AddRouteDialog.class);
            startActivityForResult(intent, ADD_ROUTE_REQUEST_CODE);
            return true;
        } else if(id == R.id.action_refresh_routes) {
            refreshRoutes();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Aktualisiert die Liste der Routen, die in dieser Actiity angezeigt wird.
     */
    public void refreshRoutes() {
        setProgressBarIndeterminateVisibility(true);
        routeAdapter.refresh(new ExtendedTaskDelegateAdapter<Void, List<Route>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<Route> routes) {
                setProgressBarIndeterminateVisibility(false);
            }

            @Override
            public void taskFailed(ExtendedTask task, String message) {
                setProgressBarIndeterminateVisibility(false);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (ADD_ROUTE_REQUEST_CODE) {
            case ADD_ROUTE_REQUEST_CODE:
                refreshRoutes();
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRouteClick(ListView l, View v, int position, long id) {
        Route r = routeAdapter.getRoute(position);
        Intent intent = new Intent(this, ShowRouteActivity.class);
        intent.putExtra(ShowRouteActivity.EXTRA_TITLE, r.getName());
        intent.putExtra(ShowRouteActivity.EXTRA_ROUTE, r.getRouteData().getValue());
        List<ServerWaypoint> tmp = r.getWaypoints();
        intent.putExtra(OldShowRouteActivity.EXTRA_WAYPOINTS, (Serializable) tmp);
        startActivity(intent);
    }

    @Override
    public boolean onRouteLongClick(AdapterView<?> parent, View v, final int position, final long id) {
        final Route route = routeAdapter.getRoute(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(route.getName())
                .setItems(R.array.manage_routes_route_actions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent;
                        switch (which) {
                            case 0:
                                // Anzeigen
                                intent = new Intent(ManageRoutesActivity.this, ShowRouteActivity.class);
                                intent.putExtra(ShowRouteActivity.EXTRA_TITLE, route.getName());
                                intent.putExtra(ShowRouteActivity.EXTRA_ROUTE, route.getRouteData().getValue());
                                intent.putExtra(OldShowRouteActivity.EXTRA_WAYPOINTS, (Serializable) route.getWaypoints());
                                startActivity(intent);
                                break;
                            case 1:
                                // Als Vorlage nutzen
                                intent = new Intent(ManageRoutesActivity.this, AddRouteDraftDialog.class);
                                intent.putExtra(AddRouteDraftDialog.EXTRA_WAYPOINTS, (Serializable) route.getWaypoints());
                                startActivityForResult(intent, ADD_ROUTE_REQUEST_CODE);
                                break;
                            case 2:
                                // Löschen
                                AlertDialog.Builder builder = new AlertDialog.Builder(ManageRoutesActivity.this);
                                builder.setTitle(R.string.delete_route)
                                        .setMessage(R.string.delete_route_message)
                                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                RouteController.deleteRoute(new ExtendedTaskDelegateAdapter<Void, Void>() {
                                                    @Override
                                                    public void taskDidFinish(ExtendedTask task, Void aVoid) {
                                                        refreshRoutes();
                                                    }

                                                    @Override
                                                    public void taskFailed(ExtendedTask task, String message) {
                                                        Toast.makeText(ManageRoutesActivity.this, R.string.route_deletion_error, Toast.LENGTH_LONG).show();
                                                    }
                                                }, route.getId());
                                            }
                                        })
                                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        });
                                builder.create().show();
                                break;
                        }
                    }
                });
        builder.create().show();
        return true;
    }
}
