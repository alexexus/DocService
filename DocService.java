import com.google.common.util.concurrent.RateLimiter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;

@Service
@RequiredArgsConstructor
public class DocService {

    private final DocRepository repository;

    public long createDoc(NewDoc newDoc, String pg) {
        if (!pg.isBlank()) {
            newDoc.setProductGroup(pg);
        }
        return repository.save(newDoc).getId();
    }

    @Validated
    @RestController
    @RequiredArgsConstructor
    public static class DocController {

        private final DocService service;
        private final RateLimiter rateLimiter = RateLimiter.create(10);

        @PostMapping("/api/v3/lk/documents/create")
        public long createDoc(@RequestBody NewDoc newDoc,
                              @RequestParam String pg) {
            boolean okToGo = rateLimiter.tryAcquire();
            if (okToGo) {
                return service.createDoc(newDoc, pg);
            } else {
                throw new TooManyRequestsException("Too many requests");
            }
        }
    }

    @Repository
    public interface DocRepository extends JpaRepository<NewDoc, Long> {
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NewDoc {

        private long id;
        @NotBlank
        private String documentFormat;
        @NotBlank
        private String productDocument;
        private String productGroup;
        @NotBlank
        private String signature;
        @NotBlank
        private String type;
    }

    public static class TooManyRequestsException extends RuntimeException {
        public TooManyRequestsException(String message) {
            super(message);
        }
    }
}

/*
Maven
<dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.data</groupId>
            <artifactId>spring-data-jpa</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <dependency>
            <groupId>com.google.guava</groupId>
            <artifactId>guava</artifactId>
            <version>32.1.2-jre</version>
        </dependency>
    </dependencies>
 */
