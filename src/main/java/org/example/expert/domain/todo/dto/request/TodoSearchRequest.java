package org.example.expert.domain.todo.dto.request;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TodoSearchRequest {
	private int page=1;
	private int size=10;
	private String weather;
	private LocalDate stDate;
	private LocalDate edDate;
}
