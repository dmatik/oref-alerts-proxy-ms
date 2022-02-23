package com.dmatik.orefalerts.service;

import com.dmatik.orefalerts.entity.CurrentAlert;
import com.dmatik.orefalerts.entity.CurrentAlertResponse;
import com.dmatik.orefalerts.entity.HistoryItem;
import com.dmatik.orefalerts.entity.HistoryResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class OrefAlertsService {

    @Autowired
    private RestTemplate restTemplate;

    static final String URL_CURRENT_ALERT = "https://www.oref.org.il/WarningMessages/alert/alerts.json";
    static final String URL_CURRENT_ALERT_MOCK = "https://8bd02e38-21e7-4516-9f12-4f124fd9ce1e.mock.pstmn.io/redalert";
    static final String URL_HISTORY = "https://www.oref.org.il//Shared/Ajax/GetAlarmsHistory.aspx?lang=he&mode=1";

    static final String HEADER_USER_AGENT_KEY = "User-Agent";
    static final String HEADER_USER_AGENT_VALUE = "https://www.oref.org.il/";
    static final String HEADER_REFERER_KEY = "Referer";
    static final String HEADER_REFERER_VALUE = "https://www.oref.org.il//12481-he/Pakar.aspx";
    static final String HEADER_X_REQUESTED_WITH_KEY = "X-Requested-With";
    static final String HEADER_X_REQUESTED_WITH_VALUE = "XMLHttpRequest";

    public CurrentAlertResponse getCurrentAlert() throws URISyntaxException, IOException {

        CurrentAlertResponse response =
                new CurrentAlertResponse(false, new CurrentAlert(null, "", null));

        URI url = new URI(URL_CURRENT_ALERT_MOCK);

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
//        List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
//        if (CollectionUtils.isEmpty(interceptors)) {
//            interceptors = new ArrayList<>();
//        }
//        interceptors.add(new MyClientHttpRequestInterceptor());
//        restTemplate.setInterceptors(interceptors);

        // Pikud HaOref call
        ResponseEntity<CurrentAlert> orefResponse;
        try {
            orefResponse = restTemplate.exchange(url, HttpMethod.GET, entity, CurrentAlert.class);
        } catch (RestClientException e) {
            return response;
        } catch (Exception e) {
            log.error(e.getMessage());
            return response;
        }

        CurrentAlert current = orefResponse.getBody();

        if (null != current) {
            response.setAlert(true);
            response.setCurrent(current);

            // Java object to JSON file
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
            LocalDateTime now = LocalDateTime.now();
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(new File("alerts\\" + dtf.format(now) +".json"), current);
        }

        return response;
    }

    public HistoryResponse getHistory() throws URISyntaxException {

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
            log.error(e.getMessage());
            return response;
        }

        response.setHistory( orefResponse.getBody() );

        return response;
    }

//    private void writeAlertsToFile(String content) throws IOException {
//        File file = new ClassPathResource("alerts.log").getFile();
//
//
//        if (!file.exists()) {
//            file.createNewFile();
//        }
//
//        FileWriter fw = new FileWriter(file.getAbsoluteFile());
//        BufferedWriter bw = new BufferedWriter(fw);
//        bw.write(content);
//        bw.close();
//    }
}
