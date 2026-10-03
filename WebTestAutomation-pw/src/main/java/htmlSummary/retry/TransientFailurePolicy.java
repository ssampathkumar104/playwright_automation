package htmlSummary.retry;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

/** Classifies failures as transient (429/5xx/timeouts → retry) or not (4xx → fail fast). */
public final class TransientFailurePolicy {

    private TransientFailurePolicy() {}

    /** True if the HTTP status is a transient (retryable) failure. */
    public static boolean isTransient(int httpStatus) {
        switch (httpStatus) {
            case 429: // Too Many Requests
            case 500: // Internal Server Error
            case 502: // Bad Gateway
            case 503: // Service Unavailable
            case 504: // Gateway Timeout
                return true;
            default:
                return false;
        }
    }

    /** True if the throwable is a transient failure (timeouts, connection resets, I/O issues). */
    public static boolean isTransient(Throwable t) {
        if (t instanceof SocketTimeoutException) return true;
        if (t instanceof ConnectException) return true;
        if (t instanceof IOException) {
            String msg = t.getMessage();
            if (msg != null && (msg.contains("Connection reset")
                    || msg.contains("Broken pipe")
                    || msg.contains("Connection timed out"))) {
                return true;
            }
        }
        return false;
    }
}
