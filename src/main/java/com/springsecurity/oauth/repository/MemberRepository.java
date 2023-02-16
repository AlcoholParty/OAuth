package com.springsecurity.oauth.repository;

import com.springsecurity.oauth.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Object> {
    Member findByEmailId(String emailId);

    @Query("SELECT m FROM Member m WHERE m.emailId = :emailId AND m.platform = :platform")
    Member findBySocialMember(@Param("emailId") String emailId, @Param("platform") String platform);

    @Query("SELECT m FROM Member m WHERE m.name = :name AND m.phoneNumber = :phoneNumber")
    Member findByJoinMember(@Param("name") String name, @Param("phoneNumber") String phoneNumber);

    @Query("SELECT m FROM Member m WHERE m.emailId = :emailId AND m.platform = :platform")
    Member findByNaverMember(@Param("emailId") String emailId, @Param("platform") String platform);
}
