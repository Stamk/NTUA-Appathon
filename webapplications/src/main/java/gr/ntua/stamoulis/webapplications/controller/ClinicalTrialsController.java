package gr.ntua.stamoulis.webapplications.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.ntua.stamoulis.webapplications.model.ClinicalTrialsRequest;
import gr.ntua.stamoulis.webapplications.model.ClinicalTrialsResponse;
import gr.ntua.stamoulis.webapplications.service.ClinicalTrialsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
public class ClinicalTrialsController {

    @Autowired
    ClinicalTrialsService clinicalTrialsService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/getCountriesInfo")
    public ClinicalTrialsResponse getCountriesInfo(@RequestBody ClinicalTrialsRequest request) throws JsonProcessingException {
        /* Generate a unique requestId */
        String requestId = UUID.randomUUID().toString();

        log.debug("[{}] /getCountriesInfo received request: {}", requestId, objectMapper.writeValueAsString(request));
        ClinicalTrialsResponse response = clinicalTrialsService.computeCountriesInfo(request, requestId);

        return response;
    }
}
