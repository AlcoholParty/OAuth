package com.springsecurity.oauth.repository;

import com.springsecurity.oauth.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Object> {
    Member findByEmailId(String emailId);
}
