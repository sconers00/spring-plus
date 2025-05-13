package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    @Query("SELECT t FROM Todo t LEFT JOIN FETCH t.user u ORDER BY t.modifiedAt DESC")
    Page<Todo> findAllByOrderByModifiedAtDesc(Pageable pageable);

    @Query("Select t From Todo t Left Join Fetch t.user u Where t.createdAt>:std and t.modifiedAt<:edd ORDER BY t.modifiedAt DESC")
    Page<Todo> findAllByOrderByModifiedAtDescWithDate(Pageable pageable, LocalDate std, LocalDate edd);

    @Query("Select t From Todo t Left Join Fetch t.user u Where t.weather=:weather Order By t.modifiedAt Desc")
    Page<Todo> findAllByOrderByModifiedAtDescWithWeather(Pageable pageable, String weather);

    @Query("Select t From Todo t Left Join Fetch t.user u Where t.weather=:weather and t.createdAt>:std and t.modifiedAt<:edd Order By t.modifiedAt Desc")
    Page<Todo> findAllByOrderByModifiedAtDescWithDateAndWeather(Pageable pageable, String weather, LocalDate std, LocalDate edd);
}
