package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.ArrayList;
import java.util.List;

import static ru.practicum.shareit.user.UserMapper.toUserDto;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Override
    public List<UserDto> getAll() {
        List<UserDto> users = new ArrayList<>();
        for (User user : userStorage.getAll()) {
            users.add(toUserDto(user));
        }
        return users;
    }

    @Override
    public UserDto getById(Long id) {
        User user = userStorage.getById(id).orElseThrow(() ->
            new NotFoundException("Не найден пользователь с id: " + id)
        );
        return toUserDto(user);
    }

    public void validateUser(Long id) {
        if (getById(id) == null) {
            throw new NotFoundException("Такого пользователя нет");
        }
    }

    private void validateUserEmail(User user) {
        if (user.getEmail() == null) {
            throw new ValidationException("Отсутствует email");
        }
        for (User currentUser : userStorage.getAll()) {
            if (user.getEmail().equals(currentUser.getEmail())) {
                throw new ConflictException("Пользователь с таким email уже зарегистрирован");
            }
        }
    }


    @Override
    public UserDto create(User user) {
        validateUserEmail(user);
        return toUserDto(userStorage.create(user));
    }

    @Override
    public UserDto update(User user, Long id) {
        validateUser(id);
        User currentUser = userStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Не найден пользователь с id: " + id));
        if (user.getEmail() != null && !currentUser.getEmail().equals(user.getEmail())) {
            validateUserEmail(user);
            currentUser.setEmail(user.getEmail());
        }
        if (user.getName() != null) {
            currentUser.setName(user.getName());
        }

        return toUserDto(userStorage.update(currentUser));
    }

    @Override
    public void delete(Long id) {
        validateUser(id);
        getById(id);
        userStorage.delete(id);
    }


}
