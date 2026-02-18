package com.example.jichini.member.repository;

import com.example.jichini.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
     Optional<Member> findByUserId(String userId);
}

