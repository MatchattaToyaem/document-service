package edu.oconnor.documentservice.repository;

import edu.oconnor.documentservice.model.FailedDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class EtlFileTrackerRepository {

    private final JdbcClient jdbcClient;

    public List<FailedDocument> findFailed(OffsetDateTime from, OffsetDateTime to, int page, int size) {
        return jdbcClient.sql("""
                SELECT id, file_name, sharepoint_path, sharepoint_id, etag,
                       last_modified, last_processed, created_at, updated_at
                FROM etl_file_tracker
                WHERE status = 'failed'
                  AND (:from::timestamptz IS NULL OR updated_at >= :from)
                  AND (:to::timestamptz   IS NULL OR updated_at <= :to)
                ORDER BY updated_at DESC
                LIMIT :limit OFFSET :offset
                """)
                .param("from", from)
                .param("to", to)
                .param("limit", size)
                .param("offset", (long) page * size)
                .query((rs, rowNum) -> new FailedDocument(
                        rs.getInt("id"),
                        rs.getString("file_name"),
                        rs.getString("sharepoint_path"),
                        rs.getString("sharepoint_id"),
                        rs.getString("etag"),
                        rs.getObject("last_modified", OffsetDateTime.class),
                        rs.getObject("last_processed", OffsetDateTime.class),
                        rs.getObject("created_at", OffsetDateTime.class),
                        rs.getObject("updated_at", OffsetDateTime.class)
                ))
                .list();
    }

    public long countFailed(OffsetDateTime from, OffsetDateTime to) {
        return jdbcClient.sql("""
                SELECT COUNT(*)
                FROM etl_file_tracker
                WHERE status = 'failed'
                  AND (:from::timestamptz IS NULL OR updated_at >= :from)
                  AND (:to::timestamptz   IS NULL OR updated_at <= :to)
                """)
                .param("from", from)
                .param("to", to)
                .query(Long.class)
                .single();
    }
}
