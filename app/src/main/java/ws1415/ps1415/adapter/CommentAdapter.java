package ws1415.ps1415.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.skatenight.skatenightAPI.model.CommentData;
import com.skatenight.skatenightAPI.model.CommentFilter;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import ws1415.ps1415.R;
import ws1415.ps1415.ServiceProvider;
import ws1415.ps1415.controller.CommentController;
import ws1415.ps1415.task.ExtendedTask;
import ws1415.ps1415.task.ExtendedTaskDelegateAdapter;

/**
 * Adapter für Kommentare. Erwartet einen CommentFilter als Parameter und fragt anschließend die
 * entsprechenden Kommentare beim Server an. Es kann außerdem angegeben werden, wieviele Kommentare
 * pro Serveraufruf abgerufen werden sollen. Wenn der Benutzer an das Ende der Liste scrollt, werden
 * automatisch weitere Kommentare abgerufen.
 * @author Richard Schulze
 */
public class CommentAdapter extends BaseAdapter {
    /**
     * View-Type für Event-Views.
     */
    private static final int COMMENT_VIEW_TYPE = 0;
    /**
     * View-Type für die View zum Anzeigen des Lade-Icons.
     */
    private static final int LOAD_VIEW_TYPE = 1;

    private Context context;
    private CommentFilter filter;
    private boolean canDelete;
    private List<CommentData> comments = new LinkedList<>();
    private int fetchDistance;
    private boolean keepFetching = true;

    /**
     * Speichert, ob der Adapter gerade Daten abruft.
     */
    private Boolean fetching = false;
    /**
     * Erstellt einen neuen CommentAdapter, der die übergebene View als ersten Eintrag anzeigt.
     * @param context       Der zu verwendende Context.
     * @param filter        Der anzuwendende Filter.
     * @param canDelete     true, wenn der aufrufende Benutzer Kommentare auch dann löschen kann,
     *                      wenn er sie nicht verfasst hat. Sonst false.
     */
    public CommentAdapter(Context context, CommentFilter filter, boolean canDelete) {
        if (filter == null) {
            throw new NullPointerException("no filter submitted");
        }
        if (context == null) {
            throw new NullPointerException("no context submitted");
        }
        this.context = context;
        this.filter = filter;
        this.canDelete = canDelete;
        fetchDistance = (int) Math.max(filter.getLimit() * 0.4, 1);
        fetchData(true);
    }

    @Override
    public int getCount() {
        return comments.size() + (fetching ? 1 : 0);
    }

    @Override
    public CommentData getItem(int position) {
        if (fetching && position == comments.size()) {
            return null;
        }
        if (position >= 0) {
            return comments.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        if (position >= 0 && position < comments.size()) {
            return comments.get(position).getId();
        } else {
            return -1;
        }
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View view;
        if (position == comments.size()) {
            // Ladeanzeige
            if (convertView != null && getItemViewType(position) == LOAD_VIEW_TYPE) {
                view = convertView;
            } else {
                view = View.inflate(parent.getContext(), R.layout.listitem_fetching, null);


            }
        } else {
            CommentData comment = getItem(position);
            if (convertView != null && getItemViewType(position) == COMMENT_VIEW_TYPE) {
                view = convertView;
            } else {
                view = View.inflate(parent.getContext(), R.layout.listitem_comment, null);
            }
            TextView commentView = (TextView) view.findViewById(R.id.comment);
            commentView.setText(comment.getComment());

            final View finalView = view;
            ImageButton editComment = (ImageButton) view.findViewById(R.id.edit_comment);
            editComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editComment(getItem(position));
                }
            });
            ImageButton deleteComment = (ImageButton) view.findViewById(R.id.delete_comment);
            deleteComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteComment(finalView, position);
                }
            });
            if (comment.getAuthor().equals(ServiceProvider.getEmail())) {
                editComment.setVisibility(View.VISIBLE);
            } else {
                editComment.setVisibility(View.GONE);
            }
            if (comment.getAuthor().equals(ServiceProvider.getEmail()) || canDelete) {
                deleteComment.setVisibility(View.VISIBLE);
            } else {
                deleteComment.setVisibility(View.GONE);
            }
            TextView author = (TextView) view.findViewById(R.id.author);
            author.setText(comment.getVisibleAuthor());
            TextView date = (TextView) view.findViewById(R.id.date);
            Date dateValue = new Date(comment.getDate().getValue());
            date.setText(DateFormat.getMediumDateFormat(context).format(dateValue) + " " + DateFormat.getTimeFormat(context).format(dateValue));
        }

        if (comments.size() - position - 1 < fetchDistance) {
            fetchData(false);
        }

        return view;
    }

    private void editComment(final CommentData comment) {
        View editView = View.inflate(context, R.layout.dialog_comment_editing, null);
        TextView author = (TextView) editView.findViewById(R.id.author);
        author.setText(comment.getVisibleAuthor());
        TextView date = (TextView) editView.findViewById(R.id.date);
        Date dateValue = new Date(comment.getDate().getValue());
        date.setText(DateFormat.getMediumDateFormat(context).format(dateValue) + " " + DateFormat.getTimeFormat(context).format(dateValue));
        final EditText newComment = (EditText) editView.findViewById(R.id.comment);
        newComment.setText(comment.getComment());
        final ProgressBar progressBar = (ProgressBar) editView.findViewById(R.id.commentLoading);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(editView)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newComment.getText().toString().isEmpty()) {
                    Toast.makeText(context, R.string.error_no_content, Toast.LENGTH_SHORT).show();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                CommentController.editComment(new ExtendedTaskDelegateAdapter<Void, Void>() {
                    @Override
                    public void taskDidFinish(ExtendedTask task, Void aVoid) {
                        comment.setComment(newComment.getText().toString());
                        dialog.dismiss();
                        notifyDataSetChanged();
                    }
                    @Override
                    public void taskFailed(ExtendedTask task, String message) {
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    }
                }, comment.getId(), newComment.getText().toString());
            }
        });
    }

    /**
     * Zeigt einen Dialog zum Löschen des Kommentars an.
     * @param view        Das View-Objekt des Listitems.
     * @param position    Die Position des zu löschenden Kommentars.
     */
    private void deleteComment(final View view, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.delete_comment)
                .setMessage(R.string.delete_comment_message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CommentController.deleteComment(new ExtendedTaskDelegateAdapter<Void, Void>() {
                            @Override
                            public void taskDidFinish(ExtendedTask task, Void aVoid) {
                                comments.remove(position);
                                notifyDataSetChanged();
                            }
                            @Override
                            public void taskFailed(ExtendedTask task, String message) {
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                            }
                        }, getItemId(position));
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.create().show();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < comments.size()) {
            return COMMENT_VIEW_TYPE;
        } else {
            return LOAD_VIEW_TYPE;
        }
    }

    /**
     * Ruft weitere Events vom Server ab.
     * @param refresh    Falls true, so wird die Liste der Events aktualisiert, d.h. die Liste wird
     *                   neu vom Server abgerufen und nicht erweitert.
     */
    private void fetchData(boolean refresh) {
        if (!keepFetching || fetching) {
            return;
        }
        synchronized (fetching) {
            if (fetching) {
                return;
            } else {
                fetching = true;
            }
        }

        // Lade-Icon anzeigen lassen
        notifyDataSetChanged();

        if (refresh) {
            filter.setCursorString(null);
        }

        CommentController.listComments(new ExtendedTaskDelegateAdapter<Void, List<CommentData>>() {
            @Override
            public void taskDidFinish(ExtendedTask task, List<CommentData> newComments) {
                if (newComments != null) {
                    comments.addAll(newComments);
                } else {
                    keepFetching = false;
                }
                finish();
            }
            @Override
            public void taskFailed(ExtendedTask task, String message) {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                finish();
            }
            /**
             * Beendet das Abrufen der Daten unabhängig davon, ob das Abrufen erfolgreich war oder
             * fehlgeschlagen ist.
             */
            private void finish() {
                fetching = false;
                CommentAdapter.this.notifyDataSetChanged();
            }
        }, filter);
    }

    /**
     * Veranlasst den Adapter die Kommentarliste neu herunterzuladen. Es wird dabei an den Anfang der
     * Liste gescrollt.
     */
    public void refresh() {
        comments.clear();
        keepFetching = true;
        fetchData(true);
    }

    /**
     * Fügt den angegebenen Kommentar vorne an den Adapter an.
     * @param comment    Der hinzuzufügende Kommentar.
     */
    public void addComment(CommentData comment) {
        if (comment != null) {
            comments.add(0, comment);
            notifyDataSetChanged();
        }
    }
}
