package org.example.expert.domain.todo.service;

import java.time.LocalDate;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.request.TodoSearchRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.QTodo;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQueryFactory;

@Service
@RequiredArgsConstructor
@Transactional
public class TodoService {

    private final TodoRepository todoRepository;
    private final WeatherClient weatherClient;

    public TodoSaveResponse saveTodo(AuthUser authUser, TodoSaveRequest todoSaveRequest) {
        User user = User.fromAuthUser(authUser);

        String weather = weatherClient.getTodayWeather();

        Todo newTodo = new Todo(
                todoSaveRequest.getTitle(),
                todoSaveRequest.getContents(),
                weather,
                user
        );
        Todo savedTodo = todoRepository.save(newTodo);

        return new TodoSaveResponse(
                savedTodo.getId(),
                savedTodo.getTitle(),
                savedTodo.getContents(),
                weather,
                new UserResponse(user.getId(), user.getEmail())
        );
    }

    public Page<TodoResponse> getTodos(TodoSearchRequest searchRequest) {
        Pageable pageable = PageRequest.of((searchRequest.getPage() - 1), searchRequest.getSize());
        Page<Todo> todos=null;
        String weather = searchRequest.getWeather();
        LocalDate std = searchRequest.getStDate();
        LocalDate edd = searchRequest.getEdDate();
        if(searchRequest.getWeather().isEmpty()&&searchRequest.getStDate()==null&&searchRequest.getEdDate()==null){
            todos = todoRepository.findAllByOrderByModifiedAtDesc(pageable);
        }
        if(searchRequest.getWeather().isEmpty()){
        todos = todoRepository.findAllByOrderByModifiedAtDescWithDate(pageable, std, edd);
        }
        if(searchRequest.getStDate()==null&&searchRequest.getEdDate()==null){
        todos = todoRepository.findAllByOrderByModifiedAtDescWithWeather(pageable, weather);
        }
        if(!searchRequest.getWeather().isEmpty()&&searchRequest.getStDate()==null&&searchRequest.getEdDate()==null){
        todos = todoRepository.findAllByOrderByModifiedAtDescWithDateAndWeather(pageable, weather, std, edd);
        }

        if(todos==null){
            return new ResponseEntity<Page<TodoResponse>>(HttpStatus.NOT_FOUND).getBody();
        }
        return todos.map(todo -> new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                new UserResponse(todo.getUser().getId(), todo.getUser().getEmail()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        ));
    }

    @PersistenceContext
    EntityManager em;

    public TodoResponse getTodo(long todoId) {

        JPAQueryFactory jqf = new JPAQueryFactory(em);

        QTodo todo = QTodo.todo;

        Optional<Todo> todoOptional= Optional.ofNullable(jqf.selectFrom(todo)
			.where(todo.id.eq(todoId))
			.fetchOne());

        Todo todoResult = todoOptional.orElseThrow(() -> new InvalidRequestException("Todo not found"));

		return new TodoResponse(
			todoResult.getId(),
			todoResult.getTitle(),
			todoResult.getContents(),
			todoResult.getWeather(),
			new UserResponse(todoResult.getUser().getId(), todoResult.getUser().getEmail()),
			todoResult.getCreatedAt(),
			todoResult.getModifiedAt());

    }
}
