package gr.ntua.stamoulis.webapplications.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data

public class CountryInfo {
    @JsonProperty("country")
    private String country;
    @JsonProperty("count")
    private int count;
}
