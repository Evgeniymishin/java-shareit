package ru.practicum.shareit.booking;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.shareit.enums.Status;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDate;


@Data
public class Booking {
    Long id;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate start;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate end;
    private Item item;
    private Booking booker;
    private Status status;
}
