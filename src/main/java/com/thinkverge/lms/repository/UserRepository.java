package com.thinkverge.lms.repository;

import com.thinkverge.lms.model.User;
import com.thinkverge.lms.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findByRole(Role role);

    boolean existsByEmail(String email);

    List<User> findByApprovedFalse();

    List<User> findByRoleAndApprovedTrue(Role role);
    
    @Query(
    	    value = """
    	        SELECT u.id, u.name, u.email, u.profile_image,
    	               COUNT(DISTINCT c.id)  AS courseCount,
    	               COUNT(DISTINCT e.id)  AS studentCount,
    	               MIN(c.category)       AS specialty
    	        FROM users u
    	        LEFT JOIN courses c ON c.instructor_id = u.id
    	        LEFT JOIN enrollments e ON e.course_id = c.id
    	        WHERE u.role = 'INSTRUCTOR'
    	        GROUP BY u.id, u.name, u.email, u.profile_image
    	        """,
    	    nativeQuery = true
    	)
    	List<Object[]> findInstructorStats();
}
