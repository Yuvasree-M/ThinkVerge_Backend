package com.thinkverge.lms.controller;

import com.thinkverge.lms.dto.response.CertificateResponse;
import com.thinkverge.lms.service.CertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    // Student: get my certificates
    @GetMapping("/my")
    public List<CertificateResponse> my(Authentication auth) {
        return certificateService.myCertificates(auth.getName());
    }

    // Public: view a single certificate by ID (used by the certificate page)
    @GetMapping("/{id}")
    public CertificateResponse getById(@PathVariable Long id) {
        return certificateService.getById(id);
    }
}
