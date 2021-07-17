package com.dmatik.orefalerts.service;

import com.dmatik.orefalerts.entity.CurrentAlert;
import com.dmatik.orefalerts.entity.CurrentAlertResponse;
import com.dmatik.orefalerts.entity.HistoryItem;
import com.dmatik.orefalerts.entity.HistoryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

@Service
@Slf4j
public class OrefAlertsService {

    @Autowired
    private RestTemplate restTemplate;

    static final String URL_CURRENT_ALERT = "https://www.oref.org.il/WarningMessages/alert/alerts.json";
    static final String URL_HISTORY = "https://www.oref.org.il//Shared/Ajax/GetAlarmsHistory.aspx?lang=he&mode=1";

    static final String HEADER_USER_AGENT_KEY = "User-Agent";
    static final String HEADER_USER_AGENT_VALUE = "https://www.oref.org.il/";
    static final String HEADER_REFERER_KEY = "Referer";
    static final String HEADER_REFERER_VALUE = "https://www.oref.org.il//12481-he/Pakar.aspx";
    static final String HEADER_X_REQUESTED_WITH_KEY = "X-Requested-With";
    static final String HEADER_X_REQUESTED_WITH_VALUE = "XMLHttpRequest";

    public CurrentAlertResponse getCurrentAlert() throws URISyntaxException {

        log.info("executing Current Alert Service");
        CurrentAlertResponse response = new CurrentAlertResponse();

        // Empty alert object
        CurrentAlert emptyAlert = new CurrentAlert();
        emptyAlert.setId(null);
        emptyAlert.setTitle("");
        emptyAlert.setData(null);
        response.setAlert(false);
        response.setCurrent(emptyAlert);

        URI url = new URI(URL_CURRENT_ALERT);

        // Setting Headers
        HttpHeaders headers = new HttpHeaders();
        headers.set(HEADER_USER_AGENT_KEY, HEADER_USER_AGENT_VALUE);
        headers.set(HEADER_REFERER_KEY, HEADER_REFERER_VALUE);
        headers.set(HEADER_X_REQUESTED_WITH_KEY, HEADER_X_REQUESTED_WITH_VALUE);
        HttpEntity<HttpHeaders> entity = new HttpEntity<>(headers);

        // Set custom error handler
        restTemplate.setErrorHandler(new ServiceErrorHandler());

        // Pikud HaOref call
        ResponseEntity<CurrentAlert> orefResponse;
        try {
            orefResponse = restTemplate.exchange(url, HttpMethod.GET, entity, CurrentAlert.class);
        } catch (Exception e) {
            log.error("External service error");
            return response;
        }

        CurrentAlert current = orefResponse.getBody();

        if (null != current) {
            response.setAlert(true);
            response.setCurrent(current);
        }

        log.info("Current Alert Service finished");
        return response;
    }

    public HistoryResponse getHistory() throws URISyntaxException {

        log.info("Executing History Service");
        HistoryResponse response = new HistoryResponse();

        // Empty history object
        response.setHistory(new HistoryItem[0]);

        URI url = new URI(URL_HISTORY);

        // Setting Headers
        HttpHeaders headers = new HttpHeaders();
        headers.set(HEADER_USER_AGENT_KEY, HEADER_USER_AGENT_VALUE);
        headers.set(HEADER_REFERER_KEY, HEADER_REFERER_VALUE);
        headers.set(HEADER_X_REQUESTED_WITH_KEY, HEADER_X_REQUESTED_WITH_VALUE);
        HttpEntity<HttpHeaders> entity = new HttpEntity<>(headers);

        // Set custom error handler
        restTemplate.setErrorHandler(new ServiceErrorHandler());

        // Pikud HaOref call
        ResponseEntity<HistoryItem[]> orefResponse;
        try {
            orefResponse = restTemplate.exchange(url, HttpMethod.GET, entity, HistoryItem[].class);
        } catch (Exception e) {
            log.error("External service error");
            return response;
        }

        HistoryItem[] history = orefResponse.getBody();
        response.setHistory(history);

        log.info("History Service finished");
        return response;
    }
}
