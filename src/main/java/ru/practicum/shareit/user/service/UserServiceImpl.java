package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ru.practicum.shareit.user.UserMapper.toUserDto;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> getAll() {
        List<UserDto> users = new ArrayList<>();
        for (User user : userRepository.findAll()) {
            users.add(toUserDto(user));
        }
        return users;
    }

    @Transactional(readOnly = true)
    @Override
    public UserDto getById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() ->
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
        for (User currentUser : userRepository.findAll()) {
            if (user.getEmail().equals(currentUser.getEmail())) {
                throw new ConflictException("Пользователь с таким email уже зарегистрирован");
            }
        }
    }

    @Transactional
    @Override
    public UserDto create(User user) {
        return toUserDto(userRepository.save(user));
    }

    @Transactional
    @Override
    public UserDto update(User user, Long id) {
        User currentUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Не найден пользователь с id: " + id));
        Optional.ofNullable(user.getEmail()).ifPresent(currentUser::setEmail);
        Optional.ofNullable(user.getName()).ifPresent(currentUser::setName);

        return toUserDto(userRepository.save(currentUser));
    }

    @Transactional
    @Override
    public void delete(Long id) {
        validateUser(id);
        getById(id);
        userRepository.deleteById(id);
    }


}
