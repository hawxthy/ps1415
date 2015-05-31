package ws1415.ps1415.task;

/**
 * Diese Klasse bietet einen Adapter für den ExtendedTaskDelegate an, ähnlich wie es bei Java AWT-Events
 * gemacht wird. Durch diesen Adapter ist es nicht notwendig jede Callback-Methode zu implementieren,
 * sondern es reicht die gewünschten Methoden zu überschreiben.
 *
 * @author Richard Schulze
 */
public class ExtendedTaskDelegateAdapter<Progress, Result> implements ExtendedTaskDelegate<Progress, Result> {
    @Override
    public void taskDidFinish(ExtendedTask task, Result result) { }

    @Override
    public void taskDidProgress(ExtendedTask task, Progress... progress) { }

    @Override
    public void taskFailed(ExtendedTask task, String message) { }

}
