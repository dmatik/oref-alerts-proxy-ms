package com.dmatik.orefalerts;

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
import java.util.ArrayList;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = SpringTestConfig.class)
public class OrefAlertsServiceMockTest {

    @Autowired
    private OrefAlertsService orefAlertsService;

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
        String url = "https://www.oref.org.il/WarningMessages/alert/alerts.json";
        String mockPath = "src/test/mocks/alerts.json";

        // Create expected object
        CurrentAlertResponse currResponseExpected = new CurrentAlertResponse();
        currResponseExpected.setAlert(true);
        CurrentAlert currExpected = new CurrentAlert();
        currExpected.setId(1621242007417L);
        currExpected.setTitle("התרעות פיקוד העורף");
        ArrayList<String> data = new ArrayList<>();
        data.add("סעד");
        data.add("אשדוד - יא,יב,טו,יז,מרינה");
        currExpected.setData(data);
        currResponseExpected.setCurrent(currExpected);

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
        CurrentAlertResponse currentAlertResponse = orefAlertsService.getCurrentAlert();
        mockServer.verify();
        Assert.assertEquals(currResponseExpected, currentAlertResponse);
    }

    @Test
    public void currentAlert_emptyResponse() throws URISyntaxException {

        // External REST URL to be mocked
        String url = "https://www.oref.org.il/WarningMessages/alert/alerts.json";

        // Create expected object
        CurrentAlertResponse currResponseExpected = new CurrentAlertResponse();
        currResponseExpected.setAlert(false);
        CurrentAlert currExpected = new CurrentAlert();
        currExpected.setId(null);
        currExpected.setTitle("");
        currExpected.setData(null);
        currResponseExpected.setCurrent(currExpected);

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI(url)))
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
}
