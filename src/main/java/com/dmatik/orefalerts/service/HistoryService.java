package com.dmatik.orefalerts.service;

import com.dmatik.orefalerts.entity.HistoryItem;
import com.dmatik.orefalerts.entity.HistoryResponse;
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
public class HistoryService {

    @Autowired
    private RestTemplate restTemplate;

    final String URL = "https://www.oref.org.il//Shared/Ajax/GetAlarmsHistory.aspx?lang=he&mode=1";

    public HistoryResponse getHistory() {

        log.info("Executing History Service");
        HistoryResponse response = new HistoryResponse();

        URI url;
        try {
            url = new URI(URL);
        } catch (URISyntaxException e) {
            log.error("Wrong URL string");
            log.debug(String.valueOf(e.getStackTrace()));
            return response;
        }

        // Setting Headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "https://www.oref.org.il/");
        headers.set("Referer", "https://www.oref.org.il//12481-he/Pakar.aspx");
        headers.set("X-Requested-With", "XMLHttpRequest");
        HttpEntity entity = new HttpEntity(headers);

        // Set custom error handler
        restTemplate.setErrorHandler(new ServiceErrorHandler());

        // Pikud HaOref call
        ResponseEntity<HistoryItem[]> orefResponse = null;
        try {
            orefResponse = restTemplate.exchange(url, HttpMethod.GET, entity, HistoryItem[].class);
        } catch (Exception e) {
            log.error("Calling external service error");
            //log.debug(String.valueOf(e.getStackTrace()));
        }

        response.setHistory(new HistoryItem[0]);

        if (null != orefResponse) {

            HistoryItem[] history = orefResponse.getBody();
            response.setHistory(history);
        }

        return response;
    }
}
