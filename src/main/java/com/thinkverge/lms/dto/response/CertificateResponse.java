package com.thinkverge.lms.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CertificateResponse {

    private String courseTitle;
    private String certificateUrl;

}