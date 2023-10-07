package com.dmatik.orefalerts.service;

import com.dmatik.orefalerts.SSLUtil;
import com.dmatik.orefalerts.entity.CurrentAlert;
import com.dmatik.orefalerts.entity.CurrentAlertResponse;
import com.dmatik.orefalerts.entity.HistoryItem;
import com.dmatik.orefalerts.entity.HistoryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class OrefAlertsService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RestTemplate restTemplateHistory;

    static final String URL_CURRENT_ALERT = "https://www.oref.org.il/WarningMessages/alert/alerts.json";
    static final String URL_CURRENT_ALERT_MOCK = "http://10.0.0.30:48080/oref_alerts/alert";
    static final String URL_HISTORY = "https://www.oref.org.il//Shared/Ajax/GetAlarmsHistory.aspx?lang=he&mode=1";
    static final String URL_HISTORY_MOCK = "http://10.0.0.30:48080/oref_alerts/history";
    static final String URL_CURRENT_ALERT_MOCK_WRONG = "http://10.0.0.30:48080/gen_json";
    static final String URL_CURRENT_ALERT_MOCK_BAD_JSON = "http://10.0.0.30:48080/bad_json";

    static final String HEADER_USER_AGENT_KEY = "User-Agent";
    static final String HEADER_USER_AGENT_VALUE = "https://www.oref.org.il/";
    static final String HEADER_REFERER_KEY = "Referer";
    static final String HEADER_REFERER_VALUE = "https://www.oref.org.il//12481-he/Pakar.aspx";
    static final String HEADER_X_REQUESTED_WITH_KEY = "X-Requested-With";
    static final String HEADER_X_REQUESTED_WITH_VALUE = "XMLHttpRequest";

    public CurrentAlertResponse getCurrentAlert() throws URISyntaxException {

        CurrentAlertResponse response;

        // Check ENV VAR for Test Mode
        String currentAlertTestModeEnv = System.getenv("CURRENT_ALERT_TEST_MODE");
        if ( null != currentAlertTestModeEnv &&
                ( currentAlertTestModeEnv.equals("TRUE") || currentAlertTestModeEnv.equals("true") ) ) {

            // Test Mode data object
            String currentAlertTestModeLocationEnv = System.getenv("CURRENT_ALERT_TEST_MODE_LOC");
            String[] data = new String[1];
            data[0] = Objects.requireNonNullElse(currentAlertTestModeLocationEnv, "");

            // Test Mode response object
            response = new CurrentAlertResponse(true,
                    new CurrentAlert("133043790410000000",
                            "1",
                            "ירי רקטות וטילים",
                            data,
                            "היכנסו למרחב המוגן ושהו בו 10 דקות"));

            log.info("Current Alert: Running in Test Mode");
            log.info(response.toString());
            return response;
        }

        response = new CurrentAlertResponse(false, new CurrentAlert("","","", null,""));

        URI url = new URI(URL_CURRENT_ALERT);

        // Check ENV VAR for Mock
        String currentAlertMockEnv = System.getenv("CURRENT_ALERT_MOCK");
        if (null != currentAlertMockEnv) {
            if ( currentAlertMockEnv.equals("TRUE") || currentAlertMockEnv.equals("true") ) {
                url = new URI(URL_CURRENT_ALERT_MOCK);
                log.info("Current Alert: Calling positive MOCK service");
            }
            if ( currentAlertMockEnv.equals("WRONG") || currentAlertMockEnv.equals("wrong") ) {
                url = new URI(URL_CURRENT_ALERT_MOCK_WRONG);
                log.info("Current Alert: Calling negative MOCK service with wrong JSON structure");
            }
            if ( currentAlertMockEnv.equals("BAD") || currentAlertMockEnv.equals("bad") ) {
                url = new URI(URL_CURRENT_ALERT_MOCK_BAD_JSON);
                log.info("Current Alert: Calling negative MOCK service with bad JSON");
            }
        }

        // Setting Headers
        HttpHeaders headers = new HttpHeaders();
        headers.set(HEADER_USER_AGENT_KEY, HEADER_USER_AGENT_VALUE);
        headers.set(HEADER_REFERER_KEY, HEADER_REFERER_VALUE);
        headers.set(HEADER_X_REQUESTED_WITH_KEY, HEADER_X_REQUESTED_WITH_VALUE);
        HttpEntity<HttpHeaders> entity = new HttpEntity<>(headers);

        // Set custom error handler
        restTemplate.setErrorHandler(new ServiceErrorHandler());

        // Setting HTTP Message Converter
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));
        messageConverters.add(converter);
        restTemplate.setMessageConverters(messageConverters);

        // Adding ClientHttpRequestInterceptor
        restTemplate.setInterceptors( Collections.singletonList(new CurrentAlertHttpRequestInterceptor()) );

        // Pikud HaOref call
        ResponseEntity<CurrentAlert> orefResponse;
        CurrentAlert current;

        try {
            SSLUtil.turnOffSslChecking();
            orefResponse = restTemplate.exchange(url, HttpMethod.GET, entity, CurrentAlert.class);
            current = orefResponse.getBody();
        } catch (RestClientException e) {
            log.debug(e.getMessage());
            return response;
        } catch (Exception e) {
            log.error(e.getMessage());
            return response;
        }

        if (null != current) {
            if (null != current.getTitle() && current.getTitle().equals("EMPTY_RESPONSE")) {
                // Empty response
                log.debug("Empty response. No alert.");
            } else if ( null == current.getId() ) {
                // Wrong JSON structure
                log.error("Wrong JSON Response structure");
                log.info(response.toString());
            } else {
                // Correct JSON structure
                response.setAlert(true);
                response.setCurrent(current);
                log.debug(current.toString());
            }
        }

        if ( response.getAlert() ) {
            log.info(response.toString());
        }

        return response;
    }

    public HistoryResponse getHistory() throws URISyntaxException {

        HistoryResponse response;

        // Check ENV VAR for Test Mode
        String historyTestModeEnv = System.getenv("HISTORY_TEST_MODE");
        if ( null != historyTestModeEnv &&
                ( historyTestModeEnv.equals("TRUE") || historyTestModeEnv.equals("true") ) ) {

            // Test mode history object
            HistoryItem[] history = new HistoryItem[3];
            history[0] = new HistoryItem("תל אביב", "17.05.2021", "13:31", "2021-05-17T13:32:00");
            history[1] = new HistoryItem("ניו יורק", "17.05.2021", "13:31", "2021-05-17T13:32:00");
            history[2] = new HistoryItem("מוסקוה", "17.05.2021", "13:31", "2021-05-17T13:32:00");

            // Test mode response object
            response = new HistoryResponse(history);

            log.info("History: Running in Test Mode");
            log.info(response.toString());
            return response;
        }

        response = new HistoryResponse();

        // Empty history object
        response.setHistory(new HistoryItem[0]);

        URI url = new URI(URL_HISTORY);

        // Check ENV VAR for Mock
        String historyMockEnv = System.getenv("HISTORY_MOCK");
        if (null != historyMockEnv) {
            if (historyMockEnv.equals("TRUE") || historyMockEnv.equals("true")) {
                url = new URI(URL_HISTORY_MOCK);
                log.info("History: Calling positive MOCK service");
            }
        }

        // Setting Headers
        HttpHeaders headers = new HttpHeaders();
        headers.set(HEADER_USER_AGENT_KEY, HEADER_USER_AGENT_VALUE);
        headers.set(HEADER_REFERER_KEY, HEADER_REFERER_VALUE);
        headers.set(HEADER_X_REQUESTED_WITH_KEY, HEADER_X_REQUESTED_WITH_VALUE);
        HttpEntity<HttpHeaders> entity = new HttpEntity<>(headers);

        // Set custom error handler
        restTemplateHistory.setErrorHandler(new ServiceErrorHandler());

        // Adding ClientHttpRequestInterceptor
        restTemplateHistory.setInterceptors( Collections.singletonList( new HistoryHttpRequestInterceptor() ) );

        // Pikud HaOref call
        ResponseEntity<HistoryItem[]> orefResponse;
        try {
            SSLUtil.turnOffSslChecking();
            orefResponse = restTemplateHistory.exchange(url, HttpMethod.GET, entity, HistoryItem[].class);
        } catch (RestClientException e) {
            log.debug(e.getMessage());
            return response;
        } catch (Exception e) {
            log.error(e.getMessage());
            return response;
        }

        response.setHistory( orefResponse.getBody() );

        if( null != response.getHistory() && response.getHistory().length > 0) {
            log.info(response.toString());
        } else {
            log.debug(response.toString());
        }

        return response;
    }

}
