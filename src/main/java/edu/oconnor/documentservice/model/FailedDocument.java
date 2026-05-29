package edu.oconnor.documentservice.model;

import java.time.OffsetDateTime;

public record FailedDocument(
        Integer id,
        String fileName,
        String sharepointPath,
        String sharepointId,
        String etag,
        OffsetDateTime lastModified,
        OffsetDateTime lastProcessed,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
