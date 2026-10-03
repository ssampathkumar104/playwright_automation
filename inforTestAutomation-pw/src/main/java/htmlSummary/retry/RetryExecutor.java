package htmlSummary.retry;

import java.util.concurrent.Callable;

/**
 * Retries a Callable on transient API failures (used for GenAI prompt calls).
 * Up to 4 attempts with 0/2/4/8s backoff; retries 429/5xx/timeouts, not 4xx client errors.
 */
public final class RetryExecutor {

    private static final long[] DELAYS_MS = { 0L, 2_000L, 4_000L, 8_000L };
    public static final int MAX_ATTEMPTS = DELAYS_MS.length;

    private RetryExecutor() {}

    /** Runs the operation, retrying transient failures with backoff; throws on final/non-transient failure. */
    public static <T> T withRetries(Callable<T> op) throws Exception {
        if (op == null) {
            throw new IllegalArgumentException("op must not be null");
        }

        Exception lastFailure = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            long delayMs = DELAYS_MS[attempt - 1];
            if (delayMs > 0L) {
                Thread.sleep(delayMs);
            }

            try {
                return op.call();
            } catch (Exception e) {
                lastFailure = e;
                if (!shouldRetry(e)) {
                    throw e;
                }
                if (attempt == MAX_ATTEMPTS) {
                    throw e;
                }
            }
        }

        throw lastFailure != null
                ? lastFailure
                : new IllegalStateException("RetryExecutor exited loop without a result");
    }

    /** True if the exception is transient (retryable), per TransientFailurePolicy. */
    private static boolean shouldRetry(Exception e) {
        if (e instanceof HttpStatusCarrier) {
            return TransientFailurePolicy.isTransient(((HttpStatusCarrier) e).getHttpStatus());
        }
        return TransientFailurePolicy.isTransient(e);
    }

    /** Marker interface for exceptions that carry an HTTP status code. */
    public interface HttpStatusCarrier {
        int getHttpStatus();
    }

    /** Exception carrying an HTTP status code for retry classification. */
    public static class HttpStatusException extends Exception implements HttpStatusCarrier {
        private static final long serialVersionUID = 1L;
        private final int httpStatus;

        public HttpStatusException(int httpStatus, String message) {
            super(message);
            this.httpStatus = httpStatus;
        }

        public HttpStatusException(int httpStatus, String message, Throwable cause) {
            super(message, cause);
            this.httpStatus = httpStatus;
        }

        @Override
        public int getHttpStatus() {
            return httpStatus;
        }
    }
}
