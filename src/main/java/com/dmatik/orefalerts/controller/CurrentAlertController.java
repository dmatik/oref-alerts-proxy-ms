package com.dmatik.orefalerts.controller;

import com.dmatik.orefalerts.entity.CurrentAlertResponse;
import com.dmatik.orefalerts.service.CurrentAlertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URISyntaxException;

@RestController
@Slf4j
@RequestMapping("/current")
public class CurrentAlertController {

    @Autowired
    private CurrentAlertService currentAlertService;

    @GetMapping
    public CurrentAlertResponse getCurrentAlert() {

        return currentAlertService.getCurrentAlert();
    }
}
