package com.thinkverge.lms.controller;

import com.thinkverge.lms.dto.response.CourseResponse;
import com.thinkverge.lms.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /** --- Get all approved courses --- */
    @GetMapping
    public List<CourseResponse> getCourses() {
        return courseService.getApprovedCourses();
    }

    /** --- Get all courses (Admin) --- */
    @GetMapping("/admin/all")
    public List<CourseResponse> getAllCoursesForAdmin() {
        return courseService.getAllCourses();
    }

    /** --- Get single course --- */
    @GetMapping("/{id}")
    public CourseResponse getCourse(@PathVariable Long id) {
        return courseService.getCourse(id);
    }

    /** --- Create course (Instructor) --- */
    @PostMapping("/instructor")
    public CourseResponse create(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String category,
            @RequestParam Integer durationHours,
            @RequestParam(required = false) MultipartFile thumbnail,
            Authentication auth
    ) {
        return courseService.createCourse(
                title, description, category, durationHours, thumbnail, auth.getName()
        );
    }

    /** --- Update course (Instructor) --- */
    @PutMapping("/instructor/{id}")
    public CourseResponse update(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String category,
            @RequestParam Integer durationHours,
            @RequestParam(required = false) MultipartFile thumbnail,
            Authentication auth
    ) {
        return courseService.updateCourse(
                id, title, description, category, durationHours, thumbnail, auth.getName()
        );
    }

    /** --- Delete course (Instructor) --- */
    @DeleteMapping("/instructor/{id}")
    public void delete(@PathVariable Long id, Authentication auth) {
        courseService.deleteCourse(id, auth.getName());
    }

    /** --- Get my courses (Instructor) --- */
    @GetMapping("/instructor/my")
    public List<CourseResponse> myCourses(Authentication auth) {
        return courseService.getInstructorCourses(auth.getName());
    }

    /** --- Approve course (Admin) --- */
    @PutMapping("/admin/{id}/approve")
    public void approve(@PathVariable Long id) {
        courseService.approveCourse(id);
    }

    /** --- Reject course (Admin) --- */
    @PutMapping("/admin/{id}/reject")
    public void reject(@PathVariable Long id) {
        courseService.rejectCourse(id);
    }
}