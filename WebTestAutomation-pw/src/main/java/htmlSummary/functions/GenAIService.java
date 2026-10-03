package htmlSummary.functions;

import static io.restassured.RestAssured.given;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import htmlSummary.config.SummaryConfigLoader;
import htmlSummary.contexts.GenAIConstants;
import htmlSummary.contexts.GenAIModelContext;
import htmlSummary.contexts.GenAIPromptContext;
import htmlSummary.contexts.IONAPIContext;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/** GenAI service for authentication, model selection, and prompt calls. */
public class GenAIService {

    private static final Logger log = LogManager.getLogger(GenAIService.class);
    private final IONAPIContext apiContext;

    /** Private constructor — use authenticate() instead. */
    private GenAIService(IONAPIContext apiContext) {
        this.apiContext = apiContext;
    }

    /** Authenticates with ION Gateway and returns a ready-to-use service. */
    public static GenAIService authenticate() {
        String bearerBaseURI = SummaryConfigLoader.getProperty("BEARER_BASE_URI");
        String clientId      = SummaryConfigLoader.getProperty("CLIENT_ID");
        String clientSecret  = SummaryConfigLoader.getProperty("CLIENT_SECRET_ID");
        String username      = SummaryConfigLoader.getProperty("TOKEN_GENERATION_USERNAME");
        String password      = SummaryConfigLoader.getProperty("TOKEN_GENERATION_PASSWORD");
        String tenantId      = SummaryConfigLoader.getProperty("XTENANT_ID");

        Response response = RestAssured.given()
                .formParam(GenAIConstants.CLIENT_ID_KEY, clientId)
                .formParam(GenAIConstants.CLIENT_SECRET_KEY, clientSecret)
                .formParam(GenAIConstants.GRANT_TYPE_KEY, GenAIConstants.GRANT_TYPE_PASSWORD)
                .formParam(GenAIConstants.USERNAME_KEY, username)
                .formParam(GenAIConstants.PASSWORD_KEY, password)
                .contentType(GenAIConstants.APPLICATION_URLENCODED)
                .header(GenAIConstants.X_TENANTID_KEY, tenantId)
                .header(GenAIConstants.X_CLIENTID_KEY, clientId)
                .when()
                .post(bearerBaseURI + GenAIConstants.BEARER_RESOURCE_URL)
                .then()
                .extract().response();

        log.info("====== [GenAI] Bearer Token: " + response.getStatusCode() + " ======");

        IONAPIContext ctx = new IONAPIContext();
        ctx.resource_base_url = SummaryConfigLoader.getProperty("RESOURCE_BASE_URL");
        ctx.returnBearerToken = "Bearer " + response.jsonPath().get("access_token");

        return new GenAIService(ctx);
    }

    /** Selects the best model: prefers Claude Sonnet 4.5, else the first enabled reasoning model. */
    @SuppressWarnings("unchecked")
    public GenAIModelContext selectBestModel() {
        Response response = given()
                .spec(buildRequest())
                .when()
                .get(GenAIConstants.GET_MODEL_LIST)
                .then()
                .assertThat().statusCode(200)
                .extract().response();

        String selectedModel = null;
        String selectedVersion = null;

        List<Map<String, Object>> families = response.jsonPath().getList("");
        for (Map<String, Object> family : families) {
            List<Map<String, Object>> versions = (List<Map<String, Object>>) family.get("versions");
            for (Map<String, Object> version : versions) {
                boolean reasoning = Boolean.parseBoolean(version.get("reasoning").toString());
                boolean enabled = Boolean.parseBoolean(version.get("enabled").toString());
                Object eol = version.get("eol");

                if (enabled && reasoning && eol == null) {
                    String modelName = family.get("name").toString();
                    String versionId = version.get("id").toString();

                    if ("claude-sonnet-4-5-20250929-v1:0".equals(versionId)) {
                        return new GenAIModelContext(modelName, versionId);
                    }
                    if (selectedModel == null) {
                        selectedModel = modelName;
                        selectedVersion = versionId;
                    }
                }
            }
        }

        return new GenAIModelContext(selectedModel, selectedVersion);
    }

    /** POSTs a prompt to the GenAI /prompt endpoint and returns the raw response. */
    public Response postPrompt(GenAIPromptContext promptCtx) {
        return given()
                .spec(buildRequest())
                .contentType(GenAIConstants.APPLICATION_JSON)
                .header("x-infor-logicalidprefix", "lid://infor.genai")
                .body(promptCtx.toJson())
                .when()
                .post(GenAIConstants.GET_PROMPT)
                .then()
                .extract().response();
    }

    /** Builds a request spec with base URI and auth headers, reused by the API calls. */
    private RequestSpecification buildRequest() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(GenAIConstants.AUTHORIZATION, apiContext.returnBearerToken);
        headers.put(GenAIConstants.ACCEPT, GenAIConstants.APPLICATION_JSON);

        return new RequestSpecBuilder()
                .setBaseUri(apiContext.resource_base_url)
                .addHeaders(headers)
                .build();
    }
}
