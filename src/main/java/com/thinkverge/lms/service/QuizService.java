//package com.thinkverge.lms.service;
//
//import com.thinkverge.lms.dto.request.QuizRequest;
//import com.thinkverge.lms.dto.request.QuizSubmitRequest;
//import com.thinkverge.lms.dto.response.*;
//import com.thinkverge.lms.model.*;
//import com.thinkverge.lms.repository.*;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class QuizService {
//
//    private final QuizRepository quizRepository;
//    private final QuizQuestionRepository questionRepository;
//    private final QuizAttemptRepository attemptRepository;
//    private final CourseModuleRepository moduleRepository;
//    private final CourseRepository courseRepository;
//    private final UserRepository userRepository;
//    private final LessonRepository lessonRepository;
//    private final LessonProgressRepository progressRepository;
//    private final AssignmentRepository assignmentRepository;
//    private final SubmissionRepository submissionRepository;
//    private final CertificateRepository certificateRepository;
//    private final EmailService emailService;
//
//    // ─────────────────────────────────────────────────────────
//    // INSTRUCTOR: Create / replace a MODULE quiz
//    // ─────────────────────────────────────────────────────────
//    public QuizResponse createForModule(QuizRequest request) {
//        CourseModule module = moduleRepository.findById(request.getModuleId())
//                .orElseThrow(() -> new RuntimeException("Module not found"));
//
//        quizRepository.findByModule(module).ifPresent(existing -> {
//            questionRepository.deleteAll(questionRepository.findByQuiz(existing));
//            quizRepository.delete(existing);
//        });
//
//        Quiz quiz = Quiz.builder()
//                .module(module)
//                .course(module.getCourse())
//                .title(request.getTitle())
//                .passingScore(request.getPassingScore())
//                .isFinalQuiz(false)
//                .createdAt(LocalDateTime.now())
//                .build();
//        quiz = quizRepository.save(quiz);
//        saveQuestions(quiz, request.getQuestions());
//        return toResponse(quiz, true);
//    }
//
//    // ─────────────────────────────────────────────────────────
//    // INSTRUCTOR: Create / replace a FINAL (course-level) quiz
//    // ─────────────────────────────────────────────────────────
//    public QuizResponse createFinalQuiz(QuizRequest request) {
//        Course course = courseRepository.findById(request.getCourseId())
//                .orElseThrow(() -> new RuntimeException("Course not found"));
//
//        // Replace existing final quiz if any
//        quizRepository.findByCourseAndIsFinalQuizTrue(course).ifPresent(existing -> {
//            questionRepository.deleteAll(questionRepository.findByQuiz(existing));
//            quizRepository.delete(existing);
//        });
//
//        Quiz quiz = Quiz.builder()
//                .module(null)
//                .course(course)
//                .title(request.getTitle())
//                .passingScore(request.getPassingScore())
//                .isFinalQuiz(true)
//                .createdAt(LocalDateTime.now())
//                .build();
//        quiz = quizRepository.save(quiz);
//        saveQuestions(quiz, request.getQuestions());
//        return toResponse(quiz, true);
//    }
//
//    // ─────────────────────────────────────────────────────────
//    // INSTRUCTOR: Get module quiz (with correct answers)
//    // ─────────────────────────────────────────────────────────
//    public Optional<QuizResponse> getByModuleForInstructor(Long moduleId) {
//        CourseModule module = moduleRepository.findById(moduleId)
//                .orElseThrow(() -> new RuntimeException("Module not found"));
//        return quizRepository.findByModule(module).map(q -> toResponse(q, true));
//    }
//
//    // ─────────────────────────────────────────────────────────
//    // INSTRUCTOR: Get final quiz (with correct answers)
//    // ─────────────────────────────────────────────────────────
//    public Optional<QuizResponse> getFinalQuizForInstructor(Long courseId) {
//        Course course = courseRepository.findById(courseId)
//                .orElseThrow(() -> new RuntimeException("Course not found"));
//        return quizRepository.findByCourseAndIsFinalQuizTrue(course)
//                .map(q -> toResponse(q, true));
//    }
//
//    // ─────────────────────────────────────────────────────────
//    // STUDENT: Get module quiz (WITHOUT correct answers)
//    // ─────────────────────────────────────────────────────────
//    public Optional<QuizResponse> getByModuleForStudent(Long moduleId) {
//        CourseModule module = moduleRepository.findById(moduleId)
//                .orElseThrow(() -> new RuntimeException("Module not found"));
//        return quizRepository.findByModule(module).map(q -> toResponse(q, false));
//    }
//
//    // ─────────────────────────────────────────────────────────
//    // STUDENT: Get final quiz (WITHOUT correct answers)
//    //          Only available if all modules are cleared
//    // ─────────────────────────────────────────────────────────
//    public Optional<QuizResponse> getFinalQuizForStudent(Long courseId, String email) {
//        User student = userRepository.findByEmail(email).orElseThrow();
//        Course course = courseRepository.findById(courseId)
//                .orElseThrow(() -> new RuntimeException("Course not found"));
//
//        if (!allModulesCleared(course, student)) {
//            throw new RuntimeException("Complete all module quizzes before taking the final quiz");
//        }
//
//        return quizRepository.findByCourseAndIsFinalQuizTrue(course)
//                .map(q -> toResponse(q, false));
//    }
//
//    // ─────────────────────────────────────────────────────────
//    // STUDENT: Submit quiz attempt (module OR final)
//    // ─────────────────────────────────────────────────────────
//    public QuizAttemptResponse submitAttempt(QuizSubmitRequest request, String email) {
//        User student = userRepository.findByEmail(email).orElseThrow();
//        Quiz quiz = quizRepository.findById(request.getQuizId())
//                .orElseThrow(() -> new RuntimeException("Quiz not found"));
//
//        List<QuizQuestion> questions = questionRepository.findByQuiz(quiz);
//        int total = questions.size();
//        if (total == 0) throw new RuntimeException("Quiz has no questions");
//
//        int correct = 0;
//        for (QuizQuestion q : questions) {
//            String selected = request.getAnswers().get(q.getId());
//            if (q.getCorrectOption().equalsIgnoreCase(selected != null ? selected : "")) {
//                correct++;
//            }
//        }
//
//        int scorePercent = (int) Math.round((correct * 100.0) / total);
//        boolean passed   = scorePercent >= quiz.getPassingScore();
//
//        QuizAttempt attempt = QuizAttempt.builder()
//                .quiz(quiz)
//                .student(student)
//                .score(scorePercent)
//                .passed(passed)
//                .attemptedAt(LocalDateTime.now())
//                .build();
//        attemptRepository.save(attempt);
//
//        if (passed) {
//            tryIssueCertificate(student, quiz.getCourse());
//        }
//
//        return QuizAttemptResponse.builder()
//                .id(attempt.getId())
//                .quizId(quiz.getId())
//                .quizTitle(quiz.getTitle())
//                .moduleId(quiz.getModule() != null ? quiz.getModule().getId() : null)
//                .isFinalQuiz(Boolean.TRUE.equals(quiz.getIsFinalQuiz()))
//                .score(scorePercent)
//                .passingScore(quiz.getPassingScore())
//                .passed(passed)
//                .attemptedAt(attempt.getAttemptedAt())
//                .totalQuestions(total)
//                .correctAnswers(correct)
//                .build();
//    }
//
//    // ─────────────────────────────────────────────────────────
//    // STUDENT: My quiz attempts for a course
//    // ─────────────────────────────────────────────────────────
//    public List<QuizAttemptResponse> myAttemptsForCourse(Long courseId, String email) {
//        User student = userRepository.findByEmail(email).orElseThrow();
//        List<CourseModule> modules = moduleRepository.findByCourseIdOrderByOrderIndex(courseId);
//        Course course = courseRepository.findById(courseId).orElseThrow();
//
//        List<QuizAttemptResponse> result = new ArrayList<>();
//
//        // Module quizzes
//        for (CourseModule mod : modules) {
//            quizRepository.findByModule(mod).ifPresent(quiz ->
//                attemptRepository.findByQuizAndStudent(quiz, student)
//                    .forEach(a -> result.add(toAttemptResponse(a, quiz)))
//            );
//        }
//
//        // Final quiz attempts
//        quizRepository.findByCourseAndIsFinalQuizTrue(course).ifPresent(quiz ->
//            attemptRepository.findByQuizAndStudent(quiz, student)
//                .forEach(a -> result.add(toAttemptResponse(a, quiz)))
//        );
//
//        return result;
//    }
//
//    // ─────────────────────────────────────────────────────────
//    // MODULE STATUS — lock/unlock per module + final quiz status
//    // ─────────────────────────────────────────────────────────
//    public List<ModuleStatusResponse> getModuleStatuses(Long courseId, String email) {
//        User student = userRepository.findByEmail(email).orElseThrow();
//        Course course = courseRepository.findById(courseId).orElseThrow();
//        List<CourseModule> modules = moduleRepository.findByCourseIdOrderByOrderIndex(courseId);
//
//        // ── Final quiz info ──────────────────────────────────
//        Optional<Quiz> finalQuizOpt = quizRepository.findByCourseAndIsFinalQuizTrue(course);
//        boolean finalQuizExists = finalQuizOpt.isPresent();
//        boolean finalQuizPassed = false;
//        Integer finalQuizBestScore = null;
//        Integer finalQuizPassingScore = null;
//        Long finalQuizId = null;
//
//        if (finalQuizOpt.isPresent()) {
//            Quiz fq = finalQuizOpt.get();
//            finalQuizId = fq.getId();
//            finalQuizPassingScore = fq.getPassingScore();
//            finalQuizPassed = attemptRepository.existsByQuizAndStudentAndPassedTrue(fq, student);
//            finalQuizBestScore = attemptRepository.findByQuizAndStudent(fq, student)
//                    .stream().mapToInt(QuizAttempt::getScore).max().isPresent()
//                    ? attemptRepository.findByQuizAndStudent(fq, student)
//                        .stream().mapToInt(QuizAttempt::getScore).max().getAsInt()
//                    : null;
//        }
//
//        List<ModuleStatusResponse> statuses = new ArrayList<>();
//        boolean allModulesComplete = true; // will be set false if any module not cleared
//
//        for (int i = 0; i < modules.size(); i++) {
//            CourseModule mod = modules.get(i);
//
//            boolean unlocked = (i == 0) || isPreviousModuleCleared(modules.get(i - 1), student);
//
//            List<Lesson> lessons = lessonRepository.findByModuleOrderByOrderIndexAsc(mod);
//            boolean allLessonsComplete = !lessons.isEmpty() && lessons.stream().allMatch(l ->
//                    progressRepository.findByStudentAndLesson(student, l)
//                            .map(p -> Boolean.TRUE.equals(p.getCompleted()))
//                            .orElse(false));
//
//            Optional<Quiz> quizOpt = quizRepository.findByModule(mod);
//            boolean quizExists = quizOpt.isPresent();
//            boolean quizPassed = false;
//            Integer bestScore = null;
//            Integer passingScore = null;
//
//            if (quizOpt.isPresent()) {
//                Quiz quiz = quizOpt.get();
//                passingScore = quiz.getPassingScore();
//                quizPassed = attemptRepository.existsByQuizAndStudentAndPassedTrue(quiz, student);
//                bestScore = attemptRepository.findByQuizAndStudent(quiz, student)
//                        .stream().mapToInt(QuizAttempt::getScore).max().isPresent()
//                        ? attemptRepository.findByQuizAndStudent(quiz, student)
//                            .stream().mapToInt(QuizAttempt::getScore).max().getAsInt()
//                        : null;
//            }
//
//            // Module is cleared if: all lessons done AND (no quiz OR quiz passed)
//            boolean moduleCleared = allLessonsComplete && (!quizExists || quizPassed);
//            if (!moduleCleared) allModulesComplete = false;
//
//            final boolean fqExists    = finalQuizExists;
//            final boolean fqPassed    = finalQuizPassed;
//            final Integer fqBest      = finalQuizBestScore;
//            final Integer fqPassing   = finalQuizPassingScore;
//            final Long    fqId        = finalQuizId;
//
//            statuses.add(ModuleStatusResponse.builder()
//                    .moduleId(mod.getId())
//                    .moduleTitle(mod.getTitle())
//                    .orderIndex(mod.getOrderIndex())
//                    .lessonsCompleted(allLessonsComplete)
//                    .quizExists(quizExists)
//                    .quizPassed(quizPassed)
//                    .unlocked(unlocked)
//                    .quizBestScore(bestScore)
//                    .passingScore(passingScore)
//                    // final quiz info on every row so frontend can read from any
//                    .allModulesComplete(false) // set after loop
//                    .finalQuizExists(fqExists)
//                    .finalQuizPassed(fqPassed)
//                    .finalQuizBestScore(fqBest)
//                    .finalQuizPassingScore(fqPassing)
//                    .finalQuizId(fqId)
//                    .build());
//        }
//
//        // Now patch allModulesComplete on every entry
//        for (ModuleStatusResponse s : statuses) {
//            s.setAllModulesComplete(allModulesComplete);
//        }
//
//        return statuses;
//    }
//
//    // ─────────────────────────────────────────────────────────
//    // INSTRUCTOR: Delete any quiz (module or final)
//    // ─────────────────────────────────────────────────────────
//    public void deleteQuiz(Long quizId) {
//        Quiz quiz = quizRepository.findById(quizId)
//                .orElseThrow(() -> new RuntimeException("Quiz not found"));
//        questionRepository.deleteAll(questionRepository.findByQuiz(quiz));
//        attemptRepository.deleteAll(attemptRepository.findByQuiz(quiz));
//        quizRepository.delete(quiz);
//    }
//
//    // ─────────────────────────────────────────────────────────
//    // Called by SubmissionService after grading
//    // ─────────────────────────────────────────────────────────
//    public void checkCertificateAfterGrade(User student, Course course) {
//        tryIssueCertificate(student, course);
//    }
//
//    // ─────────────────────────────────────────────────────────
//    // CERTIFICATE: issue when ALL of:
//    //   1. All module lessons completed
//    //   2. All module quizzes passed (for modules that have quizzes)
//    //   3. Final quiz passed (if one exists)
//    //   4. All assignments graded
//    // ─────────────────────────────────────────────────────────
//    private void tryIssueCertificate(User student, Course course) {
//        if (certificateRepository.findByStudentAndCourse(student, course).isPresent()) return;
//
//        List<CourseModule> modules = moduleRepository.findByCourseIdOrderByOrderIndex(course.getId());
//
//        // 1 & 2: All modules cleared
//        for (CourseModule mod : modules) {
//            // All lessons
//            List<Lesson> lessons = lessonRepository.findByModuleOrderByOrderIndexAsc(mod);
//            boolean lessonsOk = lessons.isEmpty() || lessons.stream().allMatch(l ->
//                    progressRepository.findByStudentAndLesson(student, l)
//                            .map(p -> Boolean.TRUE.equals(p.getCompleted()))
//                            .orElse(false));
//            if (!lessonsOk) return;
//
//            // Module quiz
//            Optional<Quiz> quizOpt = quizRepository.findByModule(mod);
//            if (quizOpt.isPresent()) {
//                boolean passed = attemptRepository
//                        .existsByQuizAndStudentAndPassedTrue(quizOpt.get(), student);
//                if (!passed) return;
//            }
//        }
//
//        // 3: Final quiz must be passed (if it exists)
//        Optional<Quiz> finalQuizOpt = quizRepository.findByCourseAndIsFinalQuizTrue(course);
//        if (finalQuizOpt.isPresent()) {
//            boolean passed = attemptRepository
//                    .existsByQuizAndStudentAndPassedTrue(finalQuizOpt.get(), student);
//            if (!passed) return;
//        }
//
//        // 4: All assignments graded
//        List<Assignment> assignments = assignmentRepository.findByCourse(course);
//        for (Assignment a : assignments) {
//            Optional<com.thinkverge.lms.model.Submission> sub =
//                    submissionRepository.findByAssignmentAndStudent(a, student);
//            if (sub.isEmpty() || sub.get().getMarks() == null) return;
//        }
//
//        // ✅ Issue certificate
//        Certificate cert = Certificate.builder()
//                .student(student)
//                .course(course)
//                .issuedAt(LocalDateTime.now())
//                .build();
//        certificateRepository.save(cert);
//
//        emailService.send(
//                student.getEmail(),
//                "🎓 Certificate Issued – " + course.getTitle(),
//                "Congratulations " + student.getName() + "! You have successfully completed "
//                        + course.getTitle() + " and earned your certificate."
//        );
//    }
//
//    // ─────────────────────────────────────────────────────────
//    // Helper: all modules cleared = all lessons done + quiz passed (if exists)
//    // ─────────────────────────────────────────────────────────
//    private boolean allModulesCleared(Course course, User student) {
//        List<CourseModule> modules = moduleRepository.findByCourseIdOrderByOrderIndex(course.getId());
//        for (CourseModule mod : modules) {
//            if (!isPreviousModuleCleared(mod, student)) return false;
//        }
//        return true;
//    }
//
//    private boolean isPreviousModuleCleared(CourseModule mod, User student) {
//        List<Lesson> lessons = lessonRepository.findByModuleOrderByOrderIndexAsc(mod);
//        boolean lessonsOk = lessons.isEmpty() || lessons.stream().allMatch(l ->
//                progressRepository.findByStudentAndLesson(student, l)
//                        .map(p -> Boolean.TRUE.equals(p.getCompleted()))
//                        .orElse(false));
//        if (!lessonsOk) return false;
//
//        Optional<Quiz> quizOpt = quizRepository.findByModule(mod);
//        if (quizOpt.isPresent()) {
//            return attemptRepository.existsByQuizAndStudentAndPassedTrue(quizOpt.get(), student);
//        }
//        return true;
//    }
//
//    // ─────────────────────────────────────────────────────────
//    // Helpers: save questions, map to response
//    // ─────────────────────────────────────────────────────────
//    private void saveQuestions(Quiz quiz, List<com.thinkverge.lms.dto.request.QuizQuestionRequest> questionRequests) {
//        if (questionRequests == null) return;
//        List<QuizQuestion> questions = questionRequests.stream()
//                .map(q -> QuizQuestion.builder()
//                        .quiz(quiz)
//                        .question(q.getQuestion())
//                        .optionA(q.getOptionA())
//                        .optionB(q.getOptionB())
//                        .optionC(q.getOptionC())
//                        .optionD(q.getOptionD())
//                        .correctOption(q.getCorrectOption())
//                        .build())
//                .collect(Collectors.toList());
//        questionRepository.saveAll(questions);
//    }
//
//    private QuizResponse toResponse(Quiz quiz, boolean includeCorrect) {
//        List<QuizQuestion> questions = questionRepository.findByQuiz(quiz);
//        List<QuizQuestionResponse> qList = questions.stream().map(q ->
//                QuizQuestionResponse.builder()
//                        .id(q.getId())
//                        .question(q.getQuestion())
//                        .optionA(q.getOptionA())
//                        .optionB(q.getOptionB())
//                        .optionC(q.getOptionC())
//                        .optionD(q.getOptionD())
//                        .correctOption(includeCorrect ? q.getCorrectOption() : null)
//                        .build()
//        ).collect(Collectors.toList());
//
//        return QuizResponse.builder()
//                .id(quiz.getId())
//                .moduleId(quiz.getModule() != null ? quiz.getModule().getId() : null)
//                .courseId(quiz.getCourse() != null ? quiz.getCourse().getId() : null)
//                .title(quiz.getTitle())
//                .passingScore(quiz.getPassingScore())
//                .isFinalQuiz(Boolean.TRUE.equals(quiz.getIsFinalQuiz()))
//                .questions(qList)
//                .build();
//    }
//
//    private QuizAttemptResponse toAttemptResponse(QuizAttempt a, Quiz q) {
//        return QuizAttemptResponse.builder()
//                .id(a.getId())
//                .quizId(q.getId())
//                .quizTitle(q.getTitle())
//                .moduleId(q.getModule() != null ? q.getModule().getId() : null)
//                .isFinalQuiz(Boolean.TRUE.equals(q.getIsFinalQuiz()))
//                .score(a.getScore())
//                .passingScore(q.getPassingScore())
//                .passed(a.getPassed())
//                .attemptedAt(a.getAttemptedAt())
//                .build();
//    }
//}
package com.thinkverge.lms.service;

import com.thinkverge.lms.dto.request.QuizRequest;
import com.thinkverge.lms.dto.request.QuizSubmitRequest;
import com.thinkverge.lms.dto.response.*;
import com.thinkverge.lms.model.*;
import com.thinkverge.lms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository questionRepository;
    private final QuizAttemptRepository attemptRepository;
    private final CourseModuleRepository moduleRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final LessonProgressRepository progressRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final CertificateRepository certificateRepository;
    private final EmailService emailService;

    // ─────────────────────────────────────────────────────────
    // INSTRUCTOR: Create / replace a MODULE quiz
    // ─────────────────────────────────────────────────────────
    public QuizResponse createForModule(QuizRequest request) {
        CourseModule module = moduleRepository.findById(request.getModuleId())
                .orElseThrow(() -> new RuntimeException("Module not found"));

        quizRepository.findByModule(module).ifPresent(existing -> {
            questionRepository.deleteAll(questionRepository.findByQuiz(existing));
            quizRepository.delete(existing);
        });

        Quiz quiz = Quiz.builder()
                .module(module)
                .course(module.getCourse())
                .title(request.getTitle())
                .passingScore(request.getPassingScore())
                .isFinalQuiz(false)
                .createdAt(LocalDateTime.now())
                .build();
        quiz = quizRepository.save(quiz);
        saveQuestions(quiz, request.getQuestions());
        return toResponse(quiz, true);
    }

    // ─────────────────────────────────────────────────────────
    // INSTRUCTOR: Create / replace a FINAL (course-level) quiz
    // ─────────────────────────────────────────────────────────
    public QuizResponse createFinalQuiz(QuizRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Replace existing final quiz if any
        quizRepository.findByCourseAndIsFinalQuizTrue(course).ifPresent(existing -> {
            questionRepository.deleteAll(questionRepository.findByQuiz(existing));
            quizRepository.delete(existing);
        });

        Quiz quiz = Quiz.builder()
                .module(null)
                .course(course)
                .title(request.getTitle())
                .passingScore(request.getPassingScore())
                .isFinalQuiz(true)
                .createdAt(LocalDateTime.now())
                .build();
        quiz = quizRepository.save(quiz);
        saveQuestions(quiz, request.getQuestions());
        return toResponse(quiz, true);
    }

    // ─────────────────────────────────────────────────────────
    // INSTRUCTOR: Get module quiz (with correct answers)
    // ─────────────────────────────────────────────────────────
    public Optional<QuizResponse> getByModuleForInstructor(Long moduleId) {
        CourseModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));
        return quizRepository.findByModule(module).map(q -> toResponse(q, true));
    }

    // ─────────────────────────────────────────────────────────
    // INSTRUCTOR: Get final quiz (with correct answers)
    // ─────────────────────────────────────────────────────────
    public Optional<QuizResponse> getFinalQuizForInstructor(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        return quizRepository.findByCourseAndIsFinalQuizTrue(course)
                .map(q -> toResponse(q, true));
    }

    // ─────────────────────────────────────────────────────────
    // STUDENT: Get module quiz (WITHOUT correct answers)
    // ─────────────────────────────────────────────────────────
    public Optional<QuizResponse> getByModuleForStudent(Long moduleId) {
        CourseModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));
        return quizRepository.findByModule(module).map(q -> toResponse(q, false));
    }

    // ─────────────────────────────────────────────────────────
    // STUDENT: Get final quiz (WITHOUT correct answers)
    //          Only available if all modules are cleared
    // ─────────────────────────────────────────────────────────
    public Optional<QuizResponse> getFinalQuizForStudent(Long courseId, String email) {
        User student = userRepository.findByEmail(email).orElseThrow();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (!allModulesCleared(course, student)) {
            throw new RuntimeException("Complete all module quizzes before taking the final quiz");
        }

        return quizRepository.findByCourseAndIsFinalQuizTrue(course)
                .map(q -> toResponse(q, false));
    }

    // ─────────────────────────────────────────────────────────
    // STUDENT: Submit quiz attempt (module OR final)
    // ─────────────────────────────────────────────────────────
    public QuizAttemptResponse submitAttempt(QuizSubmitRequest request, String email) {
        User student = userRepository.findByEmail(email).orElseThrow();
        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        List<QuizQuestion> questions = questionRepository.findByQuiz(quiz);
        int total = questions.size();
        if (total == 0) throw new RuntimeException("Quiz has no questions");

        int correct = 0;
        for (QuizQuestion q : questions) {
            String selected = request.getAnswers().get(q.getId());
            if (q.getCorrectOption().equalsIgnoreCase(selected != null ? selected : "")) {
                correct++;
            }
        }

        int scorePercent = (int) Math.round((correct * 100.0) / total);
        boolean passed   = scorePercent >= quiz.getPassingScore();

        QuizAttempt attempt = QuizAttempt.builder()
                .quiz(quiz)
                .student(student)
                .score(scorePercent)
                .passed(passed)
                .attemptedAt(LocalDateTime.now())
                .build();
        attemptRepository.save(attempt);

        if (passed) {
            tryIssueCertificate(student, quiz.getCourse());
        }

        return QuizAttemptResponse.builder()
                .id(attempt.getId())
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .moduleId(quiz.getModule() != null ? quiz.getModule().getId() : null)
                .isFinalQuiz(Boolean.TRUE.equals(quiz.getIsFinalQuiz()))
                .score(scorePercent)
                .passingScore(quiz.getPassingScore())
                .passed(passed)
                .attemptedAt(attempt.getAttemptedAt())
                .totalQuestions(total)
                .correctAnswers(correct)
                .build();
    }

    // ─────────────────────────────────────────────────────────
    // STUDENT: My quiz attempts for a course
    // ─────────────────────────────────────────────────────────
    public List<QuizAttemptResponse> myAttemptsForCourse(Long courseId, String email) {
        User student = userRepository.findByEmail(email).orElseThrow();
        List<CourseModule> modules = moduleRepository.findByCourseIdOrderByOrderIndex(courseId);
        Course course = courseRepository.findById(courseId).orElseThrow();

        List<QuizAttemptResponse> result = new ArrayList<>();

        // Module quizzes
        for (CourseModule mod : modules) {
            quizRepository.findByModule(mod).ifPresent(quiz ->
                attemptRepository.findByQuizAndStudent(quiz, student)
                    .forEach(a -> result.add(toAttemptResponse(a, quiz)))
            );
        }

        // Final quiz attempts
        quizRepository.findByCourseAndIsFinalQuizTrue(course).ifPresent(quiz ->
            attemptRepository.findByQuizAndStudent(quiz, student)
                .forEach(a -> result.add(toAttemptResponse(a, quiz)))
        );

        return result;
    }

    // ─────────────────────────────────────────────────────────
    // MODULE STATUS — lock/unlock per module + final quiz status
    // ─────────────────────────────────────────────────────────
    public List<ModuleStatusResponse> getModuleStatuses(Long courseId, String email) {
        User student = userRepository.findByEmail(email).orElseThrow();
        Course course = courseRepository.findById(courseId).orElseThrow();
        List<CourseModule> modules = moduleRepository.findByCourseIdOrderByOrderIndex(courseId);

        // ── Final quiz info ──────────────────────────────────
        Optional<Quiz> finalQuizOpt = quizRepository.findByCourseAndIsFinalQuizTrue(course);
        boolean finalQuizExists = finalQuizOpt.isPresent();
        boolean finalQuizPassed = false;
        Integer finalQuizBestScore = null;
        Integer finalQuizPassingScore = null;
        Long finalQuizId = null;

        if (finalQuizOpt.isPresent()) {
            Quiz fq = finalQuizOpt.get();
            finalQuizId = fq.getId();
            finalQuizPassingScore = fq.getPassingScore();
            finalQuizPassed = attemptRepository.existsByQuizAndStudentAndPassedTrue(fq, student);
            finalQuizBestScore = attemptRepository.findByQuizAndStudent(fq, student)
                    .stream().mapToInt(QuizAttempt::getScore).max().isPresent()
                    ? attemptRepository.findByQuizAndStudent(fq, student)
                        .stream().mapToInt(QuizAttempt::getScore).max().getAsInt()
                    : null;
        }

        List<ModuleStatusResponse> statuses = new ArrayList<>();
        boolean allModulesComplete = true; // will be set false if any module not cleared

        for (int i = 0; i < modules.size(); i++) {
            CourseModule mod = modules.get(i);

            boolean unlocked = (i == 0) || isPreviousModuleCleared(modules.get(i - 1), student);

            List<Lesson> lessons = lessonRepository.findByModuleOrderByOrderIndexAsc(mod);
            boolean allLessonsComplete = !lessons.isEmpty() && lessons.stream().allMatch(l ->
                    progressRepository.findByStudentAndLesson(student, l)
                            .map(p -> Boolean.TRUE.equals(p.getCompleted()))
                            .orElse(false));

            Optional<Quiz> quizOpt = quizRepository.findByModule(mod);
            boolean quizExists = quizOpt.isPresent();
            boolean quizPassed = false;
            Integer bestScore = null;
            Integer passingScore = null;

            if (quizOpt.isPresent()) {
                Quiz quiz = quizOpt.get();
                passingScore = quiz.getPassingScore();
                quizPassed = attemptRepository.existsByQuizAndStudentAndPassedTrue(quiz, student);
                bestScore = attemptRepository.findByQuizAndStudent(quiz, student)
                        .stream().mapToInt(QuizAttempt::getScore).max().isPresent()
                        ? attemptRepository.findByQuizAndStudent(quiz, student)
                            .stream().mapToInt(QuizAttempt::getScore).max().getAsInt()
                        : null;
            }

            // Module is cleared if: all lessons done AND (no quiz OR quiz passed)
            boolean moduleCleared = allLessonsComplete && (!quizExists || quizPassed);
            if (!moduleCleared) allModulesComplete = false;

            final boolean fqExists    = finalQuizExists;
            final boolean fqPassed    = finalQuizPassed;
            final Integer fqBest      = finalQuizBestScore;
            final Integer fqPassing   = finalQuizPassingScore;
            final Long    fqId        = finalQuizId;

            statuses.add(ModuleStatusResponse.builder()
                    .moduleId(mod.getId())
                    .moduleTitle(mod.getTitle())
                    .orderIndex(mod.getOrderIndex())
                    .lessonsCompleted(allLessonsComplete)
                    .quizExists(quizExists)
                    .quizPassed(quizPassed)
                    .unlocked(unlocked)
                    .quizBestScore(bestScore)
                    .passingScore(passingScore)
                    // final quiz info on every row so frontend can read from any
                    .allModulesComplete(false) // set after loop
                    .finalQuizExists(fqExists)
                    .finalQuizPassed(fqPassed)
                    .finalQuizBestScore(fqBest)
                    .finalQuizPassingScore(fqPassing)
                    .finalQuizId(fqId)
                    .build());
        }

        // Now patch allModulesComplete on every entry
        for (ModuleStatusResponse s : statuses) {
            s.setAllModulesComplete(allModulesComplete);
        }

        return statuses;
    }

    // ─────────────────────────────────────────────────────────
    // INSTRUCTOR: Delete any quiz (module or final)
    // ─────────────────────────────────────────────────────────
    public void deleteQuiz(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        questionRepository.deleteAll(questionRepository.findByQuiz(quiz));
        attemptRepository.deleteAll(attemptRepository.findByQuiz(quiz));
        quizRepository.delete(quiz);
    }

    // ─────────────────────────────────────────────────────────
    // Called by SubmissionService after grading
    // ─────────────────────────────────────────────────────────
    public void checkCertificateAfterGrade(User student, Course course) {
        tryIssueCertificate(student, course);
    }

    // ─────────────────────────────────────────────────────────
    // CERTIFICATE: issue when ALL of:
    //   1. All module lessons completed
    //   2. All module quizzes passed (for modules that have quizzes)
    //   3. Final quiz passed (if one exists)
    //   4. All assignments graded
    // ─────────────────────────────────────────────────────────
    private void tryIssueCertificate(User student, Course course) {
        if (certificateRepository.findByStudentAndCourse(student, course).isPresent()) return;

        List<CourseModule> modules = moduleRepository.findByCourseIdOrderByOrderIndex(course.getId());

        // 1 & 2: All modules cleared
        for (CourseModule mod : modules) {
            // All lessons
            List<Lesson> lessons = lessonRepository.findByModuleOrderByOrderIndexAsc(mod);
            boolean lessonsOk = lessons.isEmpty() || lessons.stream().allMatch(l ->
                    progressRepository.findByStudentAndLesson(student, l)
                            .map(p -> Boolean.TRUE.equals(p.getCompleted()))
                            .orElse(false));
            if (!lessonsOk) return;

            // Module quiz
            Optional<Quiz> quizOpt = quizRepository.findByModule(mod);
            if (quizOpt.isPresent()) {
                boolean passed = attemptRepository
                        .existsByQuizAndStudentAndPassedTrue(quizOpt.get(), student);
                if (!passed) return;
            }
        }

        // 3: Final quiz must be passed (if it exists)
        Optional<Quiz> finalQuizOpt = quizRepository.findByCourseAndIsFinalQuizTrue(course);
        if (finalQuizOpt.isPresent()) {
            boolean passed = attemptRepository
                    .existsByQuizAndStudentAndPassedTrue(finalQuizOpt.get(), student);
            if (!passed) return;
        }

        // 4: All assignments graded
        List<Assignment> assignments = assignmentRepository.findByCourse(course);
        for (Assignment a : assignments) {
            Optional<com.thinkverge.lms.model.Submission> sub =
                    submissionRepository.findByAssignmentAndStudent(a, student);
            if (sub.isEmpty() || sub.get().getMarks() == null) return;
        }

        // ✅ Issue certificate
        Certificate cert = Certificate.builder()
                .student(student)
                .course(course)
                .issuedAt(LocalDateTime.now())
                .build();
        cert = certificateRepository.save(cert);

        // Set the URL to the frontend certificate view page
        cert.setCertificateUrl("/certificate/" + cert.getId());
        certificateRepository.save(cert);

        emailService.send(
                student.getEmail(),
                "🎓 Certificate Issued – " + course.getTitle(),
                "Congratulations " + student.getName() + "! You have successfully completed "
                        + course.getTitle() + " and earned your certificate."
        );
    }

    // ─────────────────────────────────────────────────────────
    // Helper: all modules cleared = all lessons done + quiz passed (if exists)
    // ─────────────────────────────────────────────────────────
    private boolean allModulesCleared(Course course, User student) {
        List<CourseModule> modules = moduleRepository.findByCourseIdOrderByOrderIndex(course.getId());
        for (CourseModule mod : modules) {
            if (!isPreviousModuleCleared(mod, student)) return false;
        }
        return true;
    }

    private boolean isPreviousModuleCleared(CourseModule mod, User student) {
        List<Lesson> lessons = lessonRepository.findByModuleOrderByOrderIndexAsc(mod);
        boolean lessonsOk = lessons.isEmpty() || lessons.stream().allMatch(l ->
                progressRepository.findByStudentAndLesson(student, l)
                        .map(p -> Boolean.TRUE.equals(p.getCompleted()))
                        .orElse(false));
        if (!lessonsOk) return false;

        Optional<Quiz> quizOpt = quizRepository.findByModule(mod);
        if (quizOpt.isPresent()) {
            return attemptRepository.existsByQuizAndStudentAndPassedTrue(quizOpt.get(), student);
        }
        return true;
    }

    // ─────────────────────────────────────────────────────────
    // Helpers: save questions, map to response
    // ─────────────────────────────────────────────────────────
    private void saveQuestions(Quiz quiz, List<com.thinkverge.lms.dto.request.QuizQuestionRequest> questionRequests) {
        if (questionRequests == null) return;
        List<QuizQuestion> questions = questionRequests.stream()
                .map(q -> QuizQuestion.builder()
                        .quiz(quiz)
                        .question(q.getQuestion())
                        .optionA(q.getOptionA())
                        .optionB(q.getOptionB())
                        .optionC(q.getOptionC())
                        .optionD(q.getOptionD())
                        .correctOption(q.getCorrectOption())
                        .build())
                .collect(Collectors.toList());
        questionRepository.saveAll(questions);
    }

    private QuizResponse toResponse(Quiz quiz, boolean includeCorrect) {
        List<QuizQuestion> questions = questionRepository.findByQuiz(quiz);
        List<QuizQuestionResponse> qList = questions.stream().map(q ->
                QuizQuestionResponse.builder()
                        .id(q.getId())
                        .question(q.getQuestion())
                        .optionA(q.getOptionA())
                        .optionB(q.getOptionB())
                        .optionC(q.getOptionC())
                        .optionD(q.getOptionD())
                        .correctOption(includeCorrect ? q.getCorrectOption() : null)
                        .build()
        ).collect(Collectors.toList());

        return QuizResponse.builder()
                .id(quiz.getId())
                .moduleId(quiz.getModule() != null ? quiz.getModule().getId() : null)
                .courseId(quiz.getCourse() != null ? quiz.getCourse().getId() : null)
                .title(quiz.getTitle())
                .passingScore(quiz.getPassingScore())
                .isFinalQuiz(Boolean.TRUE.equals(quiz.getIsFinalQuiz()))
                .questions(qList)
                .build();
    }

    private QuizAttemptResponse toAttemptResponse(QuizAttempt a, Quiz q) {
        return QuizAttemptResponse.builder()
                .id(a.getId())
                .quizId(q.getId())
                .quizTitle(q.getTitle())
                .moduleId(q.getModule() != null ? q.getModule().getId() : null)
                .isFinalQuiz(Boolean.TRUE.equals(q.getIsFinalQuiz()))
                .score(a.getScore())
                .passingScore(q.getPassingScore())
                .passed(a.getPassed())
                .attemptedAt(a.getAttemptedAt())
                .build();
    }
}
