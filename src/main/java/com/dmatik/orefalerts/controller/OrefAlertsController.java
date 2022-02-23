package com.dmatik.orefalerts.controller;

import com.dmatik.orefalerts.entity.CurrentAlertResponse;
import com.dmatik.orefalerts.entity.HistoryResponse;
import com.dmatik.orefalerts.service.OrefAlertsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URISyntaxException;

@RestController
@Slf4j
@RequestMapping()
public class OrefAlertsController {

    @Autowired
    private OrefAlertsService orefAlertsService;

    @GetMapping("/current")
    public CurrentAlertResponse getCurrentAlert() throws URISyntaxException, IOException {

        return orefAlertsService.getCurrentAlert();
    }

    @GetMapping("/history")
    public HistoryResponse getHistory() throws URISyntaxException {

        return orefAlertsService.getHistory();
    }
}
