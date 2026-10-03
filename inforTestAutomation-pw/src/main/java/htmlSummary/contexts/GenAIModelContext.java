package htmlSummary.contexts;

/**
 * Holds the selected GenAI model name and version.
 */
public class GenAIModelContext {

    public String modelName;
    public String modelVersion;

    public GenAIModelContext(String modelName, String modelVersion) {
        this.modelName = modelName;
        this.modelVersion = modelVersion;
    }

    @Override
    public String toString() {
        return "[model=" + modelName + ", version=" + modelVersion + "]";
    }
}
