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
    // INSTRUCTOR: Create quiz for a module (with questions inline)
    // ─────────────────────────────────────────────────────────
    public QuizResponse createForModule(QuizRequest request) {
        CourseModule module = moduleRepository.findById(request.getModuleId())
                .orElseThrow(() -> new RuntimeException("Module not found"));

        // Delete existing quiz for this module if any (replace)
        quizRepository.findByModule(module).ifPresent(existing -> {
            questionRepository.deleteAll(questionRepository.findByQuiz(existing));
            quizRepository.delete(existing);
        });

        Quiz quiz = Quiz.builder()
                .module(module)
                .course(module.getCourse())
                .title(request.getTitle())
                .passingScore(request.getPassingScore())
                .createdAt(LocalDateTime.now())
                .build();
        quiz = quizRepository.save(quiz);

        // Save questions
        if (request.getQuestions() != null) {
            final Quiz savedQuiz = quiz;
            List<QuizQuestion> questions = request.getQuestions().stream()
                    .map(q -> QuizQuestion.builder()
                            .quiz(savedQuiz)
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

        return toResponse(quiz, true);
    }

    // ─────────────────────────────────────────────────────────
    // INSTRUCTOR: Get quiz for a module (with correct answers)
    // ─────────────────────────────────────────────────────────
    public Optional<QuizResponse> getByModuleForInstructor(Long moduleId) {
        CourseModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));
        return quizRepository.findByModule(module)
                .map(q -> toResponse(q, true));
    }

    // ─────────────────────────────────────────────────────────
    // STUDENT: Get quiz for a module (WITHOUT correct answers)
    // ─────────────────────────────────────────────────────────
    public Optional<QuizResponse> getByModuleForStudent(Long moduleId) {
        CourseModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));
        return quizRepository.findByModule(module)
                .map(q -> toResponse(q, false));  // hide correctOption
    }

    // ─────────────────────────────────────────────────────────
    // STUDENT: Submit quiz attempt
    // ─────────────────────────────────────────────────────────
    public QuizAttemptResponse submitAttempt(QuizSubmitRequest request, String email) {
        User student = userRepository.findByEmail(email).orElseThrow();
        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        List<QuizQuestion> questions = questionRepository.findByQuiz(quiz);
        int total = questions.size();
        if (total == 0) throw new RuntimeException("Quiz has no questions");

        // Grade answers
        int correct = 0;
        for (QuizQuestion q : questions) {
            String selected = request.getAnswers().get(q.getId());
            if (q.getCorrectOption().equalsIgnoreCase(selected != null ? selected : "")) {
                correct++;
            }
        }

        int scorePercent = (int) Math.round((correct * 100.0) / total);
        boolean passed = scorePercent >= quiz.getPassingScore();

        QuizAttempt attempt = QuizAttempt.builder()
                .quiz(quiz)
                .student(student)
                .score(scorePercent)
                .passed(passed)
                .attemptedAt(LocalDateTime.now())
                .build();
        attemptRepository.save(attempt);

        // ✅ If passed, check if certificate can be issued
        if (passed) {
            tryIssueCertificate(student, quiz.getCourse());
        }

        return QuizAttemptResponse.builder()
                .id(attempt.getId())
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .moduleId(quiz.getModule().getId())
                .score(scorePercent)
                .passingScore(quiz.getPassingScore())
                .passed(passed)
                .attemptedAt(attempt.getAttemptedAt())
                .totalQuestions(total)
                .correctAnswers(correct)
                .build();
    }

    // ─────────────────────────────────────────────────────────
    // STUDENT: Get my quiz attempts for a course
    // ─────────────────────────────────────────────────────────
    public List<QuizAttemptResponse> myAttemptsForCourse(Long courseId, String email) {
        User student = userRepository.findByEmail(email).orElseThrow();
        List<Quiz> quizzes = quizRepository.findByCourseId(courseId);
        List<QuizAttemptResponse> result = new ArrayList<>();
        for (Quiz q : quizzes) {
            List<QuizAttempt> attempts = attemptRepository.findByQuizAndStudent(q, student);
            for (QuizAttempt a : attempts) {
                result.add(toAttemptResponse(a, q));
            }
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────
    // MODULE STATUS — tells frontend lock/unlock state per module
    // ─────────────────────────────────────────────────────────
    public List<ModuleStatusResponse> getModuleStatuses(Long courseId, String email) {
        User student = userRepository.findByEmail(email).orElseThrow();
        List<CourseModule> modules = moduleRepository.findByCourseIdOrderByOrderIndex(courseId);
        List<ModuleStatusResponse> statuses = new ArrayList<>();

        for (int i = 0; i < modules.size(); i++) {
            CourseModule mod = modules.get(i);

            // First module always unlocked
            boolean unlocked;
            if (i == 0) {
                unlocked = true;
            } else {
                // Unlock if previous module's quiz is passed (or has no quiz)
                CourseModule prevMod = modules.get(i - 1);
                unlocked = isPreviousModuleCleared(prevMod, student);
            }

            // Lessons completed?
            List<Lesson> lessons = lessonRepository.findByModuleOrderByOrderIndexAsc(mod);
            boolean allLessonsComplete = !lessons.isEmpty() && lessons.stream().allMatch(l ->
                    progressRepository.findByStudentAndLesson(student, l)
                            .map(p -> Boolean.TRUE.equals(p.getCompleted()))
                            .orElse(false)
            );

            // Quiz info
            Optional<Quiz> quizOpt = quizRepository.findByModule(mod);
            boolean quizExists = quizOpt.isPresent();
            boolean quizPassed = false;
            Integer bestScore = null;
            Integer passingScore = null;

            if (quizOpt.isPresent()) {
                Quiz quiz = quizOpt.get();
                passingScore = quiz.getPassingScore();
                boolean passed = attemptRepository
                        .existsByQuizAndStudentAndPassedTrue(quiz, student);
                quizPassed = passed;
                bestScore = attemptRepository
                        .findByQuizAndStudent(quiz, student)
                        .stream()
                        .mapToInt(QuizAttempt::getScore)
                        .max()
                        .isPresent()
                        ? attemptRepository.findByQuizAndStudent(quiz, student)
                            .stream().mapToInt(QuizAttempt::getScore).max().getAsInt()
                        : null;
            }

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
                    .build());
        }
        return statuses;
    }

    // ─────────────────────────────────────────────────────────
    // CERTIFICATE: Auto-issue when all modules quizzes passed
    //              AND all course assignments graded
    // ─────────────────────────────────────────────────────────
    private void tryIssueCertificate(User student, Course course) {
        // Already has certificate?
        if (certificateRepository.findByStudentAndCourse(student, course).isPresent()) return;

        List<CourseModule> modules = moduleRepository.findByCourseIdOrderByOrderIndex(course.getId());

        // 1. All module quizzes must be passed
        for (CourseModule mod : modules) {
            Optional<Quiz> quizOpt = quizRepository.findByModule(mod);
            if (quizOpt.isPresent()) {
                boolean passed = attemptRepository
                        .existsByQuizAndStudentAndPassedTrue(quizOpt.get(), student);
                if (!passed) return; // not done yet
            }
        }

        // 2. All assignments must be graded
        List<Assignment> assignments = assignmentRepository.findByCourse(course);
        for (Assignment a : assignments) {
            Optional<com.thinkverge.lms.model.Submission> sub =
                    submissionRepository.findByAssignmentAndStudent(a, student);
            if (sub.isEmpty() || sub.get().getMarks() == null) return; // not graded yet
        }

        // ✅ Issue certificate
        Certificate cert = Certificate.builder()
                .student(student)
                .course(course)
                .issuedAt(LocalDateTime.now())
                .build();
        certificateRepository.save(cert);

        emailService.send(
                student.getEmail(),
                "🎓 Certificate Issued – " + course.getTitle(),
                "Congratulations " + student.getName() + "! You have successfully completed "
                        + course.getTitle() + " and earned your certificate."
        );
    }

    // ─────────────────────────────────────────────────────────
    // Called by SubmissionService after grading — check certificate
    // ─────────────────────────────────────────────────────────
    public void checkCertificateAfterGrade(User student, Course course) {
        tryIssueCertificate(student, course);
    }

    // ─────────────────────────────────────────────────────────
    // Helper: previous module cleared = all lessons done + quiz passed (if exists)
    // ─────────────────────────────────────────────────────────
    private boolean isPreviousModuleCleared(CourseModule mod, User student) {
        // Lessons
        List<Lesson> lessons = lessonRepository.findByModuleOrderByOrderIndexAsc(mod);
        boolean lessonsOk = lessons.isEmpty() || lessons.stream().allMatch(l ->
                progressRepository.findByStudentAndLesson(student, l)
                        .map(p -> Boolean.TRUE.equals(p.getCompleted()))
                        .orElse(false));

        if (!lessonsOk) return false;

        // Quiz
        Optional<Quiz> quizOpt = quizRepository.findByModule(mod);
        if (quizOpt.isPresent()) {
            return attemptRepository.existsByQuizAndStudentAndPassedTrue(quizOpt.get(), student);
        }
        return true; // no quiz = auto-unlocked after lessons
    }

    // ─────────────────────────────────────────────────────────
    // INSTRUCTOR: Delete quiz
    // ─────────────────────────────────────────────────────────
    public void deleteQuiz(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        questionRepository.deleteAll(questionRepository.findByQuiz(quiz));
        attemptRepository.deleteAll(attemptRepository.findByQuiz(quiz));
        quizRepository.delete(quiz);
    }

    // ─────────────────────────────────────────────────────────
    // Mappers
    // ─────────────────────────────────────────────────────────
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
                .questions(qList)
                .build();
    }

    private QuizAttemptResponse toAttemptResponse(QuizAttempt a, Quiz q) {
        return QuizAttemptResponse.builder()
                .id(a.getId())
                .quizId(q.getId())
                .quizTitle(q.getTitle())
                .moduleId(q.getModule() != null ? q.getModule().getId() : null)
                .score(a.getScore())
                .passingScore(q.getPassingScore())
                .passed(a.getPassed())
                .attemptedAt(a.getAttemptedAt())
                .build();
    }
}
