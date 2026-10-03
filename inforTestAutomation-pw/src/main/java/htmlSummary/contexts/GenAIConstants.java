package htmlSummary.contexts;

/**
 * Constants for ION API authentication and GenAI endpoints.
 */
public final class GenAIConstants {

    private GenAIConstants() {}

    // OAuth form keys
    public static final String CLIENT_ID_KEY = "client_id";
    public static final String CLIENT_SECRET_KEY = "client_secret";
    public static final String GRANT_TYPE_KEY = "grant_type";
    public static final String USERNAME_KEY = "username";
    public static final String PASSWORD_KEY = "password";
    public static final String GRANT_TYPE_PASSWORD = "password";
    public static final String APPLICATION_URLENCODED = "application/x-www-form-urlencoded";

    // HTTP headers
    public static final String AUTHORIZATION = "Authorization";
    public static final String ACCEPT = "accept";
    public static final String APPLICATION_JSON = "application/json";
    public static final String X_TENANTID_KEY = "X-Tenantid";
    public static final String X_CLIENTID_KEY = "X-Clientid";

    // Endpoints
    public static final String BEARER_RESOURCE_URL = "/as/token.oauth2";
    public static final String GET_MODEL_LIST = "/CQAICSHC02_TRN/GENAI/llmsvc/api/v1/models";
    public static final String GET_PROMPT = "/CQAICSHC02_TRN/GENAI/llmsvc/api/v1/prompt";
}
