package ru.yandex.practicum.filmorate.model;

import lombok.*;
import ru.yandex.practicum.filmorate.validation.Create;
import ru.yandex.practicum.filmorate.validation.IdValidation;
import ru.yandex.practicum.filmorate.validation.LocalDateMinValidation;
import ru.yandex.practicum.filmorate.validation.Update;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@IdValidation(value = "film", groups = {Update.class})
public class Film extends Id {

    @NotBlank(message = "Имя не должно быть пустым.", groups = {Create.class})
    String name;

    @NotNull(message = "Описание не должно быть пустым.", groups = {Create.class})
    @Size(
            max = 200,
            message = "Описание должно быть меньше чем {max} символов.",
            groups = {Create.class, Update.class})

    String description;

    @NotNull(message = "Дата релиза не должна быть пустой.", groups = {Create.class})
    @LocalDateMinValidation(value = "1895-12-28", groups = {Create.class, Update.class})
    LocalDate releaseDate;

    @NotNull(message = "Длительность не должна быть пустой.", groups = {Create.class})
    @Positive(message = "Длительность должна быть положительной.", groups = {Create.class, Update.class})
    Long duration;
}