//package com.project.articket.common.config;
//
//import io.swagger.v3.oas.models.OpenAPI;
//import io.swagger.v3.oas.models.info.Info;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class ApiDocumentationConfig {
//    // API 문서화를 위한 Swagger 설정
//    // @Configuration은 해당 클래스를 스프링 설정 클래스로 지정하고, @Bean은 메서드가 반환하는 객체를
//    // 스프링 컨테이너에 빈으로 등록. OpenAPI 객체를 빈으로 등록하여 Swagger API 문서의 제목, 버전, 설명
//    // 등의 정보를 설정한다.
//    // http://localhost:8080/swagger-ui/index.html 에 접속하면 API문서를 확인하고 직접 테스트가 가능하다.
//    @Bean
//    public OpenAPI apiDocumentation() {
//        return new OpenAPI().info(
//                new Info()
//                        .title("TODO List API입니다.")
//                        .version("1.0")
//                        .description("Spring Boot를 이용한 TODO List API 문서 입니다.")
//        );
//    }
//}
