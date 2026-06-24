package io.datasheild.rightsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.datasheild.rightsservice.entity.DPRRequest;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDPRRequest {

    private DPRRequest.DPRType requestType;

    private String channel;  // WEB, EMAIL, WHATSAPP, PHONE

    private String requestDetails;

    private String requestMetadata;  // JSON metadata

    public boolean isValid() {
        return requestType != null && !channel.isEmpty() && requestDetails != null;
    }
}
