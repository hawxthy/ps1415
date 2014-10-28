package ws1415.SkatenightBackend;

/**
 * Wrapper, damit boolean-Werte über den Google Endpoint übertragen werden können.
 */
public class BooleanWrapper {
    public boolean value;

    public BooleanWrapper() {
        this(false);
    }

    public BooleanWrapper(boolean initialValue) {
        this.value = initialValue;
    }

}
