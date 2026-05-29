package edu.oconnor.documentservice.service;

import edu.oconnor.documentservice.model.FailedDocument;
import edu.oconnor.documentservice.model.PagedResponse;
import edu.oconnor.documentservice.repository.EtlFileTrackerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FailedDocumentService {

    private final EtlFileTrackerRepository etlFileTrackerRepository;

    public PagedResponse<FailedDocument> getFailedDocuments(LocalDate from, LocalDate to, int page, int size) {
        OffsetDateTime fromDt = from != null ? from.atStartOfDay().atOffset(ZoneOffset.UTC) : null;
        OffsetDateTime toDt   = to   != null ? to.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC).minusNanos(1) : null;

        List<FailedDocument> content = etlFileTrackerRepository.findFailed(fromDt, toDt, page, size);
        long total = etlFileTrackerRepository.countFailed(fromDt, toDt);
        int totalPages = (int) Math.ceil((double) total / size);

        log.info("Retrieved {} failed document(s) (page={}, size={}, from={}, to={})", content.size(), page, size, from, to);
        return new PagedResponse<>(content, page, size, total, totalPages);
    }
}
