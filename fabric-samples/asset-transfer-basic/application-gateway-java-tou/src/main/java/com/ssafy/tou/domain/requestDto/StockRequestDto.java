package com.ssafy.tou.domain.requestDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockRequestDto {

    private String assetId;

    private String previousAssetId;
    private Long statementSeq;
    private Long branchSeq;
    private String branchLocation;
    private String branchName;
    private String branchContact;
    private String stockName;
    private Long stockQuantity;
    private String stockUnit;
    private String stockDate;
    private String inoutStatus;
    private String useStatus;
    private double latitude;
    private double longitude;


}
