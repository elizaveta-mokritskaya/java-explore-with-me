package ru.practicum.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.statdto.HitDto;
import ru.practicum.statsclient.StatisticsClient;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/events")
@Slf4j
public class PublicEventController {
    private final EventService service;
    private final StatisticsClient client;

    @GetMapping
    public List<EventShortDto> getEventByFilter(@RequestParam(required = false, defaultValue = "") String text,
                                                @RequestParam(required = false) List<Long> categories,
                                                @RequestParam(required = false) Boolean paid,
                                                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                                @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                                @RequestParam(defaultValue = "EVENT_DATE") String sort,
                                                @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                                @Positive @RequestParam(defaultValue = "10") Integer size,
                                                HttpServletRequest request) {


        List<EventShortDto> events = service.getEventByFilter(text, categories, paid,
                rangeStart,
                rangeEnd,
                onlyAvailable, sort,
                from / size, size);
        log.debug("!!!!!!!!!!!получаем ивенты!!!!!!!!!!!!!!!!!!!!!", events);
        HitDto hitDto = new HitDto("ewm-service", request.getRequestURI(), request.getRemoteAddr(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        client.addHit(hitDto);
        log.debug("!!!!!!!форматируем и добавляем HitDto!!!!!!!!!!!!!!!!!!!!!!!!!!", hitDto);
        return events;
    }

    @GetMapping("/{id}")
    public EventFullDto getPublicEventById(@PathVariable Long id, HttpServletRequest request) {
        EventFullDto dto = service.getPublicEventById(id);
        client.addHit(new HitDto("ewm-service", request.getRequestURI(), request.getRemoteAddr(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        return dto;
    }
}
