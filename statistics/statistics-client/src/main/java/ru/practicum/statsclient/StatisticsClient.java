package ru.practicum.statsclient;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.practicum.statdto.HitDto;
import ru.practicum.statdto.HitOutcomeDto;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class StatisticsClient {
    private final RestTemplate rest;
    @Value("${explore-with-me-server.url}")
    private static String statServerUrl;

    public void addHit(HitDto hitDto) {
        rest.postForLocation(statServerUrl + "/hit", hitDto);
    }

    public List<HitOutcomeDto> getStat(LocalDateTime start,
                                       LocalDateTime end,
                                       List<String> uris,
                                       boolean unique
    ) {
        StringBuilder urisToSend = new StringBuilder();
        for (String uri : uris) {
            urisToSend.append(uri).append(",");
        }
        ResponseEntity<List<HitOutcomeDto>> response = rest.exchange(
                statServerUrl + "/stats?start={start}&end={end}&uris={uris}&unique={unique}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                },
                start, end, urisToSend.toString(), unique);

        return response.getBody();
    }
}
