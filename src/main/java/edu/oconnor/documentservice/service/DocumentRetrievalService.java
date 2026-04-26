package edu.oconnor.documentservice.service;

import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class DocumentRetrievalService {

    private final RestTemplate restTemplate = new RestTemplate();
    private ConfidentialClientApplication msalApp;

    @Value("${sharepoint.tenant-id}")
    private String tenantId;

    @Value("${sharepoint.client-id}")
    private String clientId;

    @Value("${sharepoint.client-secret}")
    private String clientSecret;

    @Value("${sharepoint.host}")
    private String host;

    @Value("${sharepoint.site-name}")
    private String siteName;

    @PostConstruct
    public void init() throws Exception {
        msalApp = ConfidentialClientApplication
                .builder(clientId, ClientCredentialFactory.createFromSecret(clientSecret))
                .authority("https://login.microsoftonline.com/" + tenantId)
                .build();
        log.info("MSAL ConfidentialClientApplication initialized for tenant: {}", tenantId);
    }

    private String getAccessToken() {
        ClientCredentialParameters params = ClientCredentialParameters
                .builder(Set.of("https://graph.microsoft.com/.default"))
                .build();
        return msalApp.acquireToken(params).join().accessToken();
    }

    private String getSiteId(String token) {
        String url = "https://graph.microsoft.com/v1.0/sites/" + host + ":/sites/" + siteName;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Cannot connect to SharePoint site. Status: " + response.getStatusCode());
        }
        return (String) response.getBody().get("id");
    }

    private String getDriveId(String token, String siteId) {
        String url = "https://graph.microsoft.com/v1.0/sites/" + siteId + "/drive";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Cannot access SharePoint drive. Status: " + response.getStatusCode());
        }
        return (String) response.getBody().get("id");
    }

    public byte[] getDocument(String documentPath) {
        String token = getAccessToken();
        String siteId = getSiteId(token);
        String driveId = getDriveId(token, siteId);

        String path = documentPath.startsWith("/") ? documentPath : "/" + documentPath;
        String url = "https://graph.microsoft.com/v1.0/drives/" + driveId + "/root:" + path + ":/content";

        log.info("Fetching document from SharePoint: {}", documentPath);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_OCTET_STREAM));

        ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), byte[].class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to fetch document. Status: " + response.getStatusCode());
        }
        return response.getBody();
    }

    public String resolveContentType(String documentPath) {
        String lower = documentPath.toLowerCase();
        if (lower.endsWith(".pdf"))  return "application/pdf";
        if (lower.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lower.endsWith(".xlsx")) return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (lower.endsWith(".png"))  return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        return "application/octet-stream";
    }
}
