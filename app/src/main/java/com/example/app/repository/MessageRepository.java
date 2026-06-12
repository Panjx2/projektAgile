package com.example.app.repository;

import com.example.app.data.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Modifying
    @Query("DELETE FROM Message m WHERE m.sender.id = :userId")
    void deleteBySenderId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Message m SET m.receiver = null WHERE m.receiver.id = :userId")
    void clearReceiver(@Param("userId") Long userId);

    @Query("""
            SELECT m FROM Message m
            WHERE (m.sender.id = :a AND m.receiver.id = :b)
               OR (m.sender.id = :b AND m.receiver.id = :a)
            ORDER BY m.createdAt ASC
            """)
    List<Message> findPrivateConversation(@Param("a") Long userA, @Param("b") Long userB);

    List<Message> findByProjectIdOrderByCreatedAtAsc(Long projectId);
}
