package ru.practicum.compilation.dto;

import ru.practicum.compilation.Compilation;
import ru.practicum.event.Event;
import ru.practicum.event.dto.EventMapper;
import ru.practicum.event.dto.EventShortDto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CompilationMapper {
    public static Compilation toCompilation(NewCompilationDto dto) {
        return Compilation.builder()
                .pinned(dto.getPinned() != null && dto.getPinned())
                .title(dto.getTitle())
                .build();
    }

    public static CompilationDto toCompilationDto(Compilation compilation) {
        List<Event> events = compilation.getEvents();
        List<EventShortDto> eventShortDtos = new ArrayList<>();
        if (events != null) {
            eventShortDtos = events.stream().map(EventMapper::toEventShortDto)
                    .collect(Collectors.toList());
        }
        return new CompilationDto(
                compilation.getId(),
                compilation.getPinned(),
                compilation.getTitle(),
                eventShortDtos
        );
    }
}
