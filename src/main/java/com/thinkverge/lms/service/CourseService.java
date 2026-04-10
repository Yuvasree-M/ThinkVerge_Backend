package com.thinkverge.lms.service;

import com.thinkverge.lms.dto.response.CourseResponse;
import com.thinkverge.lms.enums.CourseStatus;
import com.thinkverge.lms.model.Course;
import com.thinkverge.lms.model.User;
import com.thinkverge.lms.repository.CourseRepository;
import com.thinkverge.lms.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;
    private final EmailService emailService;

    /** --- Helper: Convert Course → CourseResponse --- */
    private CourseResponse toDto(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .thumbnail(course.getThumbnail())
                .instructorName(course.getInstructor().getName())
                .category(course.getCategory())
                .durationHours(course.getDurationHours())
                .status(course.getStatus().name())
                .build();
    }

    /** --- Create Course --- */
    public CourseResponse createCourse(
            String title,
            String description,
            String category,
            Integer durationHours,
            MultipartFile thumbnail,
            String instructorEmail
    ) {
        User instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        String thumbnailUrl = null;
        if (thumbnail != null && !thumbnail.isEmpty()) {
            thumbnailUrl = fileUploadService.uploadFile(thumbnail);
        }

        Course course = Course.builder()
                .title(title)
                .description(description)
                .category(category)
                .durationHours(durationHours)
                .thumbnail(thumbnailUrl)
                .status(CourseStatus.PENDING)
                .instructor(instructor)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Course saved = courseRepository.save(course);

        // Notify admin
        String adminEmail = "sreeyuva368@gmail.com";
        emailService.sendNewCourse(adminEmail, saved.getTitle(), instructor.getName());

        return toDto(saved);
    }

    /** --- Get all approved courses --- */
    public List<CourseResponse> getApprovedCourses() {
        return courseRepository.findByStatus(CourseStatus.APPROVED)
            .stream()
            .map(course -> CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .thumbnail(course.getThumbnail())
                .instructorName(course.getInstructor().getName())
                .category(course.getCategory())
                .durationHours(course.getDurationHours())
                .status(course.getStatus().name())
                .build())
            .toList();
    }
    /** --- Get single course --- */
    public CourseResponse getCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        return toDto(course);
    }

    /** --- Get instructor courses --- */
    public List<CourseResponse> getInstructorCourses(String email) {
        return courseRepository.findByInstructorEmail(email)
                .stream()
                .map(this::toDto)
                .toList();
    }

    /** --- Approve course --- */
    public void approveCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        course.setStatus(CourseStatus.APPROVED);
        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);

        emailService.sendCourseApproved(course.getInstructor().getEmail(), course.getTitle());
    }

    /** --- Reject course --- */
    public void rejectCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        course.setStatus(CourseStatus.REJECTED);
        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);

        emailService.sendCourseRejected(course.getInstructor().getEmail(), course.getTitle());
    }
    
 // CourseService.java
    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }
}