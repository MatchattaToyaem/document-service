package edu.oconnor.documentservice.controller;

import edu.oconnor.documentservice.model.FailedDocument;
import edu.oconnor.documentservice.model.PagedResponse;
import edu.oconnor.documentservice.service.FailedDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class FailedDocumentController {

    private final FailedDocumentService failedDocumentService;

    @GetMapping("/failed")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PagedResponse<FailedDocument>> getFailedDocuments(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        log.info("Failed documents retrieval request: page={}, size={}, from={}, to={}", page, size, from, to);
        return ResponseEntity.ok(failedDocumentService.getFailedDocuments(from, to, page, size));
    }
}
