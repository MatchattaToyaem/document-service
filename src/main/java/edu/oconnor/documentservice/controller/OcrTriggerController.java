package edu.oconnor.documentservice.controller;


import edu.oconnor.documentservice.service.OcrTriggerService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OcrTriggerController {
    private final OcrTriggerService ocrTriggerService;

    public OcrTriggerController(OcrTriggerService ocrTriggerService) {
        this.ocrTriggerService = ocrTriggerService;
    }

    @GetMapping("/ocr-trigger")
    @PreAuthorize("isAuthenticated()")
    public String ocrTrigger() {
        return ocrTriggerService.triggerService();
    }
}
