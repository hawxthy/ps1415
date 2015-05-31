package ws1415.ps1415.task;

/**
 * Interface das Updates vom ExtendedTask ermöglicht.
 * Created by Pascal Otto on 26.11.14.
 */
public interface ExtendedTaskDelegate<Progress, Result> {
    /**
     * Wird vom Task aufgerufen wenn er erfolgreich beendet wurde.
     * @param task Der beendete Task.
     * @param result Das Ergebnis des Task.
     */
    public void taskDidFinish(ExtendedTask task, Result result);

    /**
     * Wird vom Task aufgerufen wenn dieser {@link ExtendedTask#publishProgress}
     * aufruft.
     * @param task Der Task.
     * @param progress Der aktuelle Fortschritt.
     */
    public void taskDidProgress(ExtendedTask task, Progress... progress);

    /**
     * Wird vom Task aufgerufen wenn er fehlgeschlagen ist.
     * @param task Der fehlgeschlagene Task.
     * @param message Der Grund.
     */
    public void taskFailed(ExtendedTask task, String message);
}
