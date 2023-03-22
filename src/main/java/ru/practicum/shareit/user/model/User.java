package ru.practicum.shareit.user.model;

import lombok.Data;

import javax.validation.constraints.Email;


@Data
public class User {
    private Long id;
    private String name;
    @Email
    private String email;
}
