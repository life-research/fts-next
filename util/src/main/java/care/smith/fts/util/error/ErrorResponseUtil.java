package care.smith.fts.util.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface ErrorResponseUtil {

  private static <T> Mono<ResponseEntity<T>> onError(Throwable e, HttpStatus httpStatus) {
    return Mono.just(
        ResponseEntity.of(ProblemDetail.forStatusAndDetail(httpStatus, e.getMessage())).build());
  }

  static <T> Mono<ResponseEntity<T>> badRequest(Throwable e) {
    return onError(e, HttpStatus.BAD_REQUEST);
  }

  static <T> Mono<ResponseEntity<T>> notFound(Throwable e) {
    return onError(e, HttpStatus.NOT_FOUND);
  }

  static <T> Mono<ResponseEntity<T>> internalServerError(Throwable e) {
    return onError(e, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
