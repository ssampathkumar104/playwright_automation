package htmlSummary.contexts;

/**
 * Request body for the Infor GenAI /prompt endpoint.
 */
public class GenAIPromptContext {

    public String  model;
    public String  version;
    public boolean useConverseApi;
    public boolean safetyGuardrail;
    public String  prompt;

    public static GenAIPromptContext build(String model, String version, String prompt) {
        GenAIPromptContext ctx = new GenAIPromptContext();
        ctx.model           = model;
        ctx.version         = version;
        ctx.useConverseApi  = true;
        ctx.safetyGuardrail = false;
        ctx.prompt          = prompt;
        return ctx;
    }

    /** Serialises to the JSON body expected by the GenAI /prompt endpoint. */
    public String toJson() {
        String escapedPrompt = prompt
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");

        return "{\n"
                + "  \"model\": \""         + model          + "\",\n"
                + "  \"version\": \""       + version        + "\",\n"
                + "  \"useConverseApi\": "  + useConverseApi + ",\n"
                + "  \"safetyGuardrail\": " + safetyGuardrail + ",\n"
                + "  \"prompt\": \""        + escapedPrompt  + "\"\n"
                + "}";
    }
}
