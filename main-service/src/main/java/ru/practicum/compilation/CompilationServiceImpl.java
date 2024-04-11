package ru.practicum.compilation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.CompilationMapper;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.event.Event;
import ru.practicum.event.EventRepository;
import ru.practicum.exception.DataNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository repository;
    private final EventRepository eventRepository;

    @Override
    public CompilationDto addCompilation(NewCompilationDto dto) {
        Compilation compilation = CompilationMapper.toCompilation(dto);
        List<Long> eventsId = dto.getEvents();
        if (eventsId != null) {
            List<Event> events = eventRepository.findAllById(eventsId);
            compilation.setEvents(events);
        }
        Compilation savedCompilation = repository.save(compilation);
        return CompilationMapper.toCompilationDto(savedCompilation);
    }

    @Override
    public void deleteCompilation(Long compId) {
        repository.deleteById(compId);
    }

    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest compilation) {
        Compilation compilationFromDb = repository.findById(compId).orElseThrow(
                () -> new DataNotFoundException("Подборка не найдена"));
        if (compilation.getEvents() != null && !compilation.getEvents().isEmpty()) {
            List<Long> eventIds = compilation.getEvents();
            List<Event> events = eventRepository.findAllById(eventIds);
            compilationFromDb.setEvents(events);
        }
        if (compilation.getPinned() != null) {
            compilationFromDb.setPinned(compilation.getPinned());
        }
        if (compilation.getTitle() != null) {
            compilationFromDb.setTitle(compilation.getTitle());
        }
        Compilation updated = repository.save(compilationFromDb);
        return CompilationMapper.toCompilationDto(updated);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        if (pinned == null) {
            return repository.findAll(PageRequest.of(from, size)).getContent().stream()
                    .map(CompilationMapper::toCompilationDto)
                    .collect(Collectors.toList());
        }

        return repository.findAllByPinned(pinned, PageRequest.of(from, size))
                .getContent().stream()
                .map(CompilationMapper::toCompilationDto)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilation(Long id) {
        Compilation compilation = repository.findById(id).orElseThrow(
                () -> new DataNotFoundException("Подборка не найдена"));
        return CompilationMapper.toCompilationDto(compilation);
    }
}
