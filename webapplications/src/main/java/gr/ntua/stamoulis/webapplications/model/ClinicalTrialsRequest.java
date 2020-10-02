package gr.ntua.stamoulis.webapplications.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ClinicalTrialsRequest {
    @JsonProperty("condition")
    private String condition;
}
