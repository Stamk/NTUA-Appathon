package gr.ntua.stamoulis.webapplications.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL) /* In order to hide null elements from response */
public class ClinicalTrialsResponse {
    @JsonProperty("success")
    private boolean success;
    @JsonProperty("countriesInfo")
    private List<CountryInfo> countriesInfo;
}
