package com.dmatik.orefalerts;

import com.dmatik.orefalerts.controller.OrefAlertsController;
import com.dmatik.orefalerts.entity.HistoryItem;
import com.dmatik.orefalerts.entity.HistoryResponse;
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
public class HistoryTest {

    @Autowired
    private OrefAlertsService orefAlertsService;

    @Autowired
    private OrefAlertsController orefAlertsController;

    @Autowired
    private RestTemplate restTemplateHistory;

    private MockRestServiceServer mockServer;

    @Before
    public void init() {

        mockServer = MockRestServiceServer.createServer(restTemplateHistory);
    }

    @Test
    public void history_successfulFlow() throws URISyntaxException, IOException {

        // External REST URL to be mocked
        String url = "https://www.oref.org.il/warningMessages/alert/History/AlertsHistory.json";
        String mockPath = "src/test/mocks/history.json";

        // Create expected object
        HistoryItem[] history = new HistoryItem[2];
        history[0] = new HistoryItem("2024-07-03 17:26:52", "ירי רקטות וטילים", "בטחה", 1);
        history[1] = new HistoryItem("2024-07-03 17:26:52", "ירי רקטות וטילים", "גילת", 1);
        HistoryResponse historyResponseExpected = new HistoryResponse(history);

        // Read Mock from file
        Path path = ResourceUtils.getFile(mockPath).toPath();
        String historyMock = new String( Files.readAllBytes(path) );

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI(url)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(historyMock)
                );

        // Executing and asserting
        HistoryResponse historyResponse = orefAlertsService.getHistory();
        mockServer.verify();
        Assert.assertEquals(historyResponseExpected, historyResponse);
    }

    @Test
    public void history_emptyResponse() throws URISyntaxException {

        // External REST URL to be mocked
        String url = "https://www.oref.org.il/warningMessages/alert/History/AlertsHistory.json";

        // Create expected object
        HistoryResponse historyResponseExpected = new HistoryResponse();
        historyResponseExpected.setHistory(new HistoryItem[0]);

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI(url)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("")
                );

        // Executing and asserting
        HistoryResponse historyResponse = orefAlertsService.getHistory();
        mockServer.verify();
        Assert.assertEquals(historyResponseExpected, historyResponse);
    }

    @Test
    public void  history_httpNotFound() throws URISyntaxException {

        // External REST URL to be mocked
        String url = "https://www.oref.org.il/warningMessages/alert/History/AlertsHistory.json";

        // Create expected object
        HistoryResponse historyResponseExpected = new HistoryResponse();
        historyResponseExpected.setHistory(new HistoryItem[0]);

        this.mockServer
                .expect(ExpectedCount.once(), requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        // Executing and asserting
        HistoryResponse historyResponse = orefAlertsService.getHistory();
        mockServer.verify();
        Assert.assertEquals(historyResponseExpected, historyResponse);
    }

    @Test
    public void  history_httpServerError() throws URISyntaxException {

        // External REST URL to be mocked
        String url = "https://www.oref.org.il/warningMessages/alert/History/AlertsHistory.json";

        // Create expected object
        HistoryResponse historyResponseExpected = new HistoryResponse();
        historyResponseExpected.setHistory(new HistoryItem[0]);

        this.mockServer
                .expect(ExpectedCount.once(), requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        // Executing and asserting
        HistoryResponse historyResponse = orefAlertsService.getHistory();
        mockServer.verify();
        Assert.assertEquals(historyResponseExpected, historyResponse);
    }

    @Test
    public void history_controllerSuccessfulFlow() throws URISyntaxException, IOException {

        // External REST URL to be mocked
        String url = "https://www.oref.org.il/warningMessages/alert/History/AlertsHistory.json";
        String mockPath = "src/test/mocks/history.json";

        // Create expected object
        HistoryItem[] history = new HistoryItem[2];
        history[0] = new HistoryItem("2024-07-03 17:26:52", "ירי רקטות וטילים", "בטחה", 1);
        history[1] = new HistoryItem("2024-07-03 17:26:52", "ירי רקטות וטילים", "גילת", 1);
        HistoryResponse historyResponseExpected = new HistoryResponse(history);

        // Read Mock from file
        Path path = ResourceUtils.getFile(mockPath).toPath();
        String historyMock = new String( Files.readAllBytes(path) );

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI(url)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(historyMock)
                );

        // Executing and asserting
        HistoryResponse historyResponse = orefAlertsController.getHistory();
        mockServer.verify();
        Assert.assertEquals(historyResponseExpected, historyResponse);
    }
}
