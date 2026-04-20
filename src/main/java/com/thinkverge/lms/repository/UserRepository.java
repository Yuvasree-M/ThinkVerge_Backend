//package com.thinkverge.lms.repository;
//
//import com.thinkverge.lms.model.User;
//import com.thinkverge.lms.enums.Role;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.List;
//import java.util.Optional;
//
//public interface UserRepository extends JpaRepository<User, Long> {
//
//    Optional<User> findByEmail(String email);
//
//    List<User> findByRole(Role role);
//
//    boolean existsByEmail(String email);
//}

package com.thinkverge.lms.repository;

import com.thinkverge.lms.model.User;
import com.thinkverge.lms.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findByRole(Role role);

    boolean existsByEmail(String email);

    // ── Approval queries ──────────────────────────────────────
    List<User> findByApprovedFalse();

    List<User> findByRoleAndApprovedTrue(Role role);
}
