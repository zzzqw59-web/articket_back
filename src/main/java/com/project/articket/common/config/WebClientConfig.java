package com.project.articket.common.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.xml.JacksonXmlDecoder;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.dataformat.xml.XmlMapper;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {

        XmlMapper xmlMapper = XmlMapper.builder().build();

        return WebClient.builder()
                .codecs(congifurer -> {
                    congifurer.defaultCodecs()
                            .jacksonXmlDecoder(
                                    new JacksonXmlDecoder(xmlMapper)
                            );
                })
                .build();
    }
}
