package com.dmatik.orefalerts.controller;

import com.dmatik.orefalerts.entity.HistoryResponse;
import com.dmatik.orefalerts.service.HistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/history")
public class HistoryController {

    @Autowired
    private HistoryService historyService;

    @GetMapping
    public HistoryResponse getHistory() {

        return historyService.getHistory();
    }
}
