package ru.practicum.statsclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.statdto.HitDto;
import ru.practicum.statdto.HitOutcomeDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class StatisticsClient {

    private final RestTemplate rest;
    private static String statServerUrl;

    @Autowired
    public StatisticsClient(RestTemplateBuilder builder) {
        statServerUrl = "http://localhost:9090";
        rest = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(statServerUrl))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build();
    }

    public void addHit(HitDto hitDto) {
        rest.postForLocation(statServerUrl + "/hit", hitDto);
    }

    public List<HitOutcomeDto> getStat(LocalDateTime start,
                                       LocalDateTime end,
                                       List<String> uris,
                                       boolean unique
    ) {
        String startStr = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String endStr = end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        log.info("!!!!!!!!!!!!!!!!!!{}\n", startStr);
        log.info("!!!!!!!!!!!!!!!!!!{}\n", endStr);
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
                startStr, endStr, urisToSend.toString(), unique);

        return response.getBody();
    }
}
