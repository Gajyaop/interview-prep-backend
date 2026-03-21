package com.harsh.backend.repository;

import com.harsh.backend.entity.Question;
import com.harsh.backend.entity.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByTopic(String topic);

    List<Question> findByDifficulty(String difficulty);

    List<Question> findByTopicAndDifficulty(String topic, String difficulty);

    List<Question> findByType(QuestionType type);

    @Query("SELECT DISTINCT q.topic FROM Question q ORDER BY q.topic")
    List<String> findDistinctTopics();

    @Query(value = """
            SELECT * FROM questions
            WHERE topic = :topic
            AND difficulty = :difficulty
            AND type = :#{#type.name()}
            ORDER BY RANDOM()
            LIMIT :count
            """, nativeQuery = true)
    List<Question> findTopByFilters(
            @Param("topic") String topic,
            @Param("difficulty") String difficulty,
            @Param("type") QuestionType type,
            @Param("count") int count
    );
}