package com.thinkverge.lms.service;

import com.thinkverge.lms.dto.response.CertificateResponse;
import com.thinkverge.lms.model.Certificate;
import com.thinkverge.lms.model.User;
import com.thinkverge.lms.repository.CertificateRepository;
import com.thinkverge.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final UserRepository userRepository;

    // Student: get my certificates
    public List<CertificateResponse> myCertificates(String email) {
        User student = userRepository.findByEmail(email).orElseThrow();
        return certificateRepository.findByStudent(student)
                .stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

    private CertificateResponse map(Certificate c) {
        return CertificateResponse.builder()
                .id(c.getId())
                .courseTitle(c.getCourse().getTitle())
                .studentName(c.getStudent().getName())
                .certificateUrl(c.getCertificateUrl())
                .issuedAt(c.getIssuedAt())
                .build();
    }
}
