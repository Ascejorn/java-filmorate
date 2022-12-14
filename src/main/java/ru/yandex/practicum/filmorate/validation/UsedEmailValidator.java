package ru.yandex.practicum.filmorate.validation;

import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UsedEmailValidator implements ConstraintValidator<UsedEmailValidation, String> {

    private final UserService service;

    @Autowired
    public UsedEmailValidator(UserService userService) {
        this.service = userService;
    }

    public boolean isValid(String email, ConstraintValidatorContext cxt) {
        return service.isNotExistEmail(email);
    }
}
