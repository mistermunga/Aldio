package com.viberato.aldio.repository;

import com.viberato.aldio.entity.UserStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserStatsRepository extends JpaRepository<UserStats, Long> {

}
