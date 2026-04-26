package edu.oconnor.documentservice.controller;

import edu.oconnor.documentservice.service.DocumentRetrievalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentRetrievalController {

    private final DocumentRetrievalService documentRetrievalService;

    @GetMapping("/file")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> getDocument(@RequestParam String path) {
        log.info("Document retrieval request: {}", path);

        byte[] content = documentRetrievalService.getDocument(path);
        String contentType = documentRetrievalService.resolveContentType(path);

        String filename = path.contains("/")
                ? path.substring(path.lastIndexOf('/') + 1)
                : path;

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(content);
    }
}
