package com.dmatik.orefalerts;

import com.dmatik.orefalerts.controller.OrefAlertsController;
import com.dmatik.orefalerts.entity.CurrentAlert;
import com.dmatik.orefalerts.entity.CurrentAlertResponse;
import com.dmatik.orefalerts.service.OrefAlertsService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = SpringTestConfig.class)
public class CurrentAlertTest {

    @Autowired
    private OrefAlertsService orefAlertsService;

    @Autowired
    private OrefAlertsController orefAlertsController;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @Before
    public void init() {

        mockServer = MockRestServiceServer.createServer(restTemplate);
    }


    @Test
    public void currentAlert_successfulFlow() throws URISyntaxException, IOException {

        // External REST URL to be mocked
        //String url = "https://www.oref.org.il/WarningMessages/alert/alerts.json";
        String url = "https://8bd02e38-21e7-4516-9f12-4f124fd9ce1e.mock.pstmn.io/redalert";
        String mockPath = "src/test/mocks/alerts.json";

        // Create expected object
        String[] data = new String[2];
        data[0] = "סעד";
        data[1] = "אשדוד - יא,יב,טו,יז,מרינה";
        CurrentAlertResponse currResponseExpected =
                new CurrentAlertResponse(true, new CurrentAlert(1621242007417L, "התרעות פיקוד העורף", data));


        // Read Mock from file
        Path path = ResourceUtils.getFile(mockPath).toPath();
        String currMock = new String( Files.readAllBytes(path) );

        mockServer.expect(ExpectedCount.once(),
                requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(currMock)
                );

        // Executing and asserting
        CurrentAlertResponse currentAlertResponse = orefAlertsService.getCurrentAlert();
        mockServer.verify();
        Assert.assertEquals(currResponseExpected, currentAlertResponse);
    }

    @Test
    public void currentAlert_emptyResponse() throws URISyntaxException {

        // External REST URL to be mocked
        //String url = "https://www.oref.org.il/WarningMessages/alert/alerts.json";
        String url = "https://8bd02e38-21e7-4516-9f12-4f124fd9ce1e.mock.pstmn.io/redalert";

        // Create expected object
        CurrentAlertResponse currResponseExpected = new CurrentAlertResponse(false, new CurrentAlert(null, "", null));

        mockServer.expect(ExpectedCount.once(),
                requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("")
                );

        // Executing and asserting
        CurrentAlertResponse currentAlertResponse = orefAlertsService.getCurrentAlert();
        mockServer.verify();
        Assert.assertEquals(currResponseExpected, currentAlertResponse);
    }

    @Test
    public void  currentAlert_httpNotFound() throws URISyntaxException {

        // External REST URL to be mocked
        //String url = "https://www.oref.org.il/WarningMessages/alert/alerts.json";
        String url = "https://8bd02e38-21e7-4516-9f12-4f124fd9ce1e.mock.pstmn.io/redalert";

        // Create expected object
        CurrentAlertResponse currResponseExpected = new CurrentAlertResponse(false, new CurrentAlert(null, "", null));

        this.mockServer
                .expect(ExpectedCount.once(), requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        // Executing and asserting
        CurrentAlertResponse currentAlertResponse = orefAlertsService.getCurrentAlert();
        this.mockServer.verify();
        Assert.assertEquals(currResponseExpected, currentAlertResponse);
    }

    @Test
    public void  currentAlert_httpServerError() throws URISyntaxException {

        // External REST URL to be mocked
        //String url = "https://www.oref.org.il/WarningMessages/alert/alerts.json";
        String url = "https://8bd02e38-21e7-4516-9f12-4f124fd9ce1e.mock.pstmn.io/redalert";

        // Create expected object
        CurrentAlertResponse currResponseExpected = new CurrentAlertResponse(false, new CurrentAlert(null, "", null));

        this.mockServer
                .expect(ExpectedCount.once(), requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        // Executing and asserting
        CurrentAlertResponse currentAlertResponse = orefAlertsService.getCurrentAlert();
        //this.mockServer.verify();
        Assert.assertEquals(currResponseExpected, currentAlertResponse);
    }

    @Test
    public void currentAlert_controllerSuccessfulFlow() throws URISyntaxException, IOException {

        // External REST URL to be mocked
        //String url = "https://www.oref.org.il/WarningMessages/alert/alerts.json";
        String url = "https://8bd02e38-21e7-4516-9f12-4f124fd9ce1e.mock.pstmn.io/redalert";
        String mockPath = "src/test/mocks/alerts.json";

        // Create expected object
        String[] data = new String[2];
        data[0] = "סעד";
        data[1] = "אשדוד - יא,יב,טו,יז,מרינה";
        CurrentAlertResponse currResponseExpected =
                new CurrentAlertResponse(true, new CurrentAlert(1621242007417L, "התרעות פיקוד העורף", data));


        // Read Mock from file
        Path path = ResourceUtils.getFile(mockPath).toPath();
        String currMock = new String( Files.readAllBytes(path) );

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI(url)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(currMock)
                );

        // Executing and asserting
        CurrentAlertResponse currentAlertResponse = orefAlertsController.getCurrentAlert();
        //mockServer.verify();
        Assert.assertEquals(currResponseExpected, currentAlertResponse);
    }
}
