package ws1415.ps1415.task;

import android.os.AsyncTask;

/**
 * Erweiterung des {@link android.os.AsyncTask} die Rückmeldungen zum Fortschritt des Task
 * ermöglicht.
 * Created by Pascal Otto on 26.11.14.
 */
public abstract class ExtendedTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    protected ExtendedTaskDelegate<Progress, Result> delegate;
    protected String message;
    protected boolean error;

    /**
     * Initialisiert den Task.
     * @param delegate Klasse die Rückmeldungen zum Fortschritt des Task erhalten soll.
     */
    public ExtendedTask(ExtendedTaskDelegate<Progress, Result> delegate) {
        this.delegate = delegate;
        this.message = null;
        this.error = false;
    }

    /**
     * Wird in der {@link ExtendedTask#doInBackground} Methode aufgerufen, falls
     * ein Fehler aufgetreten ist. Das Result der Methode wird daraufhin ignoriert und in der
     * delegate wird die {@link ExtendedTaskDelegate#taskFailed} Methode
     * aufgerufen.
     * @param message Grund des Fehlers.
     */
    protected void publishError(String message) {
        this.message = message;
        this.error = true;
    }

    /**
     * Wird aufgerufen wenn in der {@link ExtendedTask#doInBackground} Methode
     * die {@link ExtendedTask#publishProgress} Methode aufgerufen wird.
     * Unterklassen müssen beim Überschreiben dieser Methode super aufrufen, da die delegate sonst
     * nicht über den Fortschritt benachrichtigt wird.
     * @param values
     */
    @Override
    protected void onProgressUpdate(Progress... values) {
        super.onProgressUpdate(values);
        if (delegate != null) delegate.taskDidProgress(this, values);
    }

    /**
     * Wird aufgerufen wenn die {@link ExtendedTask#doInBackground} Methode
     * beendet wurde. Unterklassen müssen beim Überschreiben dieser Methode super aufrufen, da die delegate sonst
     * nicht über den erfolgreichen Abschluss bzw. den Fehler benachrichtigt wird.
     * @param result
     */
    @Override
    protected void onPostExecute(Result result) {
        super.onPostExecute(result);
        if (!this.error) {
            if (delegate != null) delegate.taskDidFinish(this, result);
        }
        else {
            if (delegate != null) delegate.taskFailed(this, this.message);
        }
    }
}
