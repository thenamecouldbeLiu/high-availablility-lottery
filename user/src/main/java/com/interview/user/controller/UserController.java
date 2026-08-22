package com.interview.user.controller;

import com.interview.common.response.Response;
import com.interview.user.controller.dto.CreateUserRequest;
import com.interview.user.controller.dto.UpdateUserRequest;
import com.interview.user.controller.dto.UserResponse;
import com.interview.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Response<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        return Response.success(service.create(request));
    }

    @GetMapping("/me")
    public Response<UserResponse> me() {
        return Response.success(service.getCurrent());
    }

    @GetMapping("/{id}")
    public Response<UserResponse> get(@PathVariable UUID id) {
        return Response.success(service.get(id));
    }

    @GetMapping
    public Response<Page<UserResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return Response.success(service.search(keyword,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @PutMapping("/{id}")
    public Response<UserResponse> update(
            @PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        return Response.success(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Response<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return Response.success();
    }
}
