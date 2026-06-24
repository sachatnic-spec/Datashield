package com.datasheild.discovery.dto;

import com.datasheild.discovery.entity.PIIScan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanRequest {
    private String scanName;
    private PIIScan.ScanType scanType;
    private String targetDatabase;
    private String targetTable;
    private String scanQuery;
}
