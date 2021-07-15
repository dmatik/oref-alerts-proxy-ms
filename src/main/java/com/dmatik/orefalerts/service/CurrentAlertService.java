package com.dmatik.orefalerts.service;

import com.dmatik.orefalerts.entity.CurrentAlert;
import com.dmatik.orefalerts.entity.CurrentAlertResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

@Service
@Slf4j
public class CurrentAlertService {

    @Autowired
    private RestTemplate restTemplate;

    private String URL = "https://www.oref.org.il/WarningMessages/alert/alerts.json";

    public CurrentAlertResponse getCurrentAlert() {

        log.info("Executing Current Alert Service");
        CurrentAlertResponse response = new CurrentAlertResponse();

        URI url = null;
        try {
            url = new URI(URL);
        } catch (URISyntaxException e) {
            log.info("Wrong URL string");
            log.debug(e.getStackTrace().toString());
            return response;
        }

        // Setting Headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "https://www.oref.org.il/");
        headers.set("Referer", "https://www.oref.org.il//12481-he/Pakar.aspx");
        headers.set("X-Requested-With", "XMLHttpRequest");
        HttpEntity entity = new HttpEntity(headers);

        // Pikud HaOref call
        ResponseEntity<CurrentAlert> orefResponse = null;
        try {
            orefResponse = restTemplate.exchange(url, HttpMethod.GET, entity, CurrentAlert.class);
        } catch (Exception e) {
            log.info("Unable to call " + URL + " service.");
            log.debug(e.getStackTrace().toString());
            return response;
        }
        CurrentAlert current = orefResponse.getBody();

        // Setting empty alert object in case of empty response.
        if(null != current) {
            response.setAlert(true);
            response.setCurrent(current);
        } else {
            // Empty alert object
            CurrentAlert emptyAlert = new CurrentAlert();
            emptyAlert.setId(null);
            emptyAlert.setTitle("");
            emptyAlert.setData(null);
            response.setAlert(false);
            response.setCurrent(emptyAlert);
        }

        return response;
    }
}
