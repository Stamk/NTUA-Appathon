package gr.ntua.stamoulis.webapplications.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.ntua.stamoulis.webapplications.model.ClinicalTrialsRequest;
import gr.ntua.stamoulis.webapplications.model.ClinicalTrialsResponse;
import gr.ntua.stamoulis.webapplications.model.CountryInfo;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Slf4j
@Service
public class ClinicalTrialsService {

    private ObjectMapper objectMapper = new ObjectMapper();

    public ClinicalTrialsResponse computeCountriesInfo(ClinicalTrialsRequest request, String requestId) {
        ClinicalTrialsResponse response = new ClinicalTrialsResponse();

        response.setCountriesInfo(getClinicalTrials(request.getCondition(), requestId));
        response.setSuccess(true);

        return response;
    }


    public List<CountryInfo> getClinicalTrials(String condition, String requestId) {
        log.info("[{}] ClinicalTrialsService.getClinicalTrials Invoked", requestId);
        List<CountryInfo> countriesInfo = new ArrayList<>();
        HashMap<String, Integer> capitalCities = new HashMap<>();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("https://clinicaltrials.gov/ct2/results/download_fields?cond=" + condition + "&down_count=1&down_fmt=xml&down_chunk=0");

        /* RestTemplate init */
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectTimeout(6000); //6 sec connection timeout
        httpRequestFactory.setReadTimeout(1200000); //2 min read timeout
        RestTemplate rt = new RestTemplate(httpRequestFactory);

        /* HTTP headers */
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<String>(requestHeaders);

        //Make the first call in order to calculate the chunk size
        try {
            ResponseEntity<String> response = rt.exchange(builder.build().encode().toUri(), HttpMethod.GET, requestEntity, String.class);
            String xmlResponse = response.getBody();
            JSONObject xmlJSONObj = XML.toJSONObject(xmlResponse);
            //calculate chunk size from count field
            int count = (int) ((JSONObject) xmlJSONObj.get("search_results")).get("count");
            int chuncks = (int) (Math.ceil((double) count / 10000));
            log.debug("[{}] ClinicalTrialsService.getClinicalTrials calculated chunks: {}, for count:{}", requestId, chuncks, count);
            for (int j = 0; j < chuncks; j++) {
                log.debug("[{}] ClinicalTrialsService.getClinicalTrials sending request for chunk: {}", requestId, j + 1);
                builder = UriComponentsBuilder.fromUriString("https://clinicaltrials.gov/ct2/results/download_fields?cond=" + condition + "&down_count=10000&down_fmt=xml&down_chunk=" + j);
                response = rt.exchange(builder.build().encode().toUri(), HttpMethod.GET, requestEntity, String.class);
                log.debug("[{}] ClinicalTrialsService.getClinicalTrials received response for chunk: {}", requestId, j + 1);
                //iterating results
                for (int i = 0; i < ((JSONArray) ((JSONObject) xmlJSONObj.get("search_results")).get("study")).length(); i++) {
                    JSONObject studyObject = ((JSONObject) ((JSONArray) ((JSONObject) xmlJSONObj.get("search_results")).get("study")).get(i));
                    if (studyObject.get("locations") instanceof String) {
//                        if(!((String) studyObject.get("locations")).trim().isEmpty()) {
//                            log.debug("[{}] {}", requestId, objectMapper.writeValueAsString(studyObject.get("locations")));
//                        }
                        continue;
                    }
                    JSONObject locationsObject = (JSONObject) ((JSONObject) ((JSONArray) ((JSONObject) xmlJSONObj.get("search_results")).get("study")).get(i)).get("locations");
                    Object locationInfoObject = (((JSONObject) locationsObject).get("location"));
                    if (locationInfoObject instanceof JSONArray) {
                        for (Object locationInfo : ((JSONArray) locationInfoObject)) {
                            String[] splits = locationInfo.toString().split(",");
                            String country = splits[splits.length - 1].trim();
                            capitalCities.merge(country, 1, Integer::sum);
                        }
                    } else {
                        String[] splits = locationInfoObject.toString().split(",");
                        String country = splits[splits.length - 1].trim();
                        capitalCities.merge(country, 1, Integer::sum);
                    }
                }
                //transform hashmap to a list
                for (Map.Entry<String, Integer> entry : capitalCities.entrySet()) {
                    String countryName = entry.getKey();
                    int countValue = entry.getValue();
                    //check if country already exists in the list
                    Optional<CountryInfo> optionalCountryInfo = countriesInfo.stream().filter(countryInfo ->
                        countryInfo.getCountry().equals(countryName)
                    ).findAny();
                    //if it exists just increase its count value
                    if(optionalCountryInfo.isPresent()){
                        optionalCountryInfo.get().setCount(optionalCountryInfo.get().getCount() + countValue);
                    } else {
                        //else add it as a new country
                        CountryInfo countryInfo = new CountryInfo();
                        countryInfo.setCountry(countryName);
                        countryInfo.setCount(countValue);
                        countriesInfo.add(countryInfo);
                    }
                }
                log.debug("[{}] ClinicalTrialsService.getClinicalTrials finished iteration: {} out of: {}", requestId, j + 1, chuncks);
            }
            //sort results based on count field
            countriesInfo.sort(Comparator.comparing(CountryInfo::getCount).reversed());
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("[{}] ClinicalTrialsService.getClinicalTrials UnSuccessful Http Status Code for Login: {}", requestId, response.getStatusCode());
                return null;
            }
        } catch (HttpClientErrorException e) {
            log.error("[{}] ClinicalTrialsService.getClinicalTrials HttpClientErrorException occurred: {}", requestId, e.toString());
            e.printStackTrace();
        } catch (Exception e) {
            log.error("[{}] ClinicalTrialsService.getClinicalTrials an exception occurred: {}", requestId, e.toString());
            e.printStackTrace();
        } finally {
            log.info("[{}] ClinicalTrialsService.getClinicalTrials Ended", requestId);
        }
        return countriesInfo;
    }
}
