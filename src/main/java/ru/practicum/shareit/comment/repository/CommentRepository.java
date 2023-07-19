package ru.practicum.shareit.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.comment.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query(value = "SELECT * FROM comments " +
            "WHERE item_id IN (:itemId)",
            nativeQuery = true)
    List<Comment> findCommentsByItemId(@Param("itemId") List<Long> itemId);
}
