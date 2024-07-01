package care.smith.fts.cda;

import care.smith.fts.api.*;
import care.smith.fts.api.cda.BundleSender;
import care.smith.fts.api.cda.CohortSelector;
import care.smith.fts.api.cda.DataSelector;
import care.smith.fts.api.cda.DeidentificationProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class DefaultTransferProcessRunner implements TransferProcessRunner {

  private final Map<String, Run> runs = new HashMap<>();

  @Override
  public String run(TransferProcess process) {
    var id = "processId";
    Run run = new Run(process);
    run.execute();
    runs.put(id, run);
    return id;
  }

  @Override
  public Mono<State> state(String id) {
    Run run = runs.get(id);
    if (run != null) {
      return Mono.just(run.state());
    } else {
      return Mono.error(new IllegalArgumentException());
    }
  }

  public static class Run {

    private final CohortSelector cohortSelector;
    private final DataSelector dataSelector;
    private final DeidentificationProvider deidentificationProvider;
    private final AtomicLong skippedPatients;
    private final BundleSender bundleSender;
    private final AtomicLong sentBundles;

    public Run(TransferProcess process) {
      cohortSelector = process.cohortSelector();
      dataSelector = process.dataSelector();
      deidentificationProvider = process.deidentificationProvider();
      bundleSender = process.bundleSender();

      skippedPatients = new AtomicLong();
      sentBundles = new AtomicLong();
    }

    public void execute() {
      cohortSelector
          .selectCohort()
          .flatMap(
              patient -> {
                Flux<ConsentedPatientBundle> data =
                    dataSelector.select(patient).map(b -> new ConsentedPatientBundle(b, patient));
                Flux<TransportBundle> transportBundleFlux =
                    deidentificationProvider.deidentify(data);
                return bundleSender
                    .send(transportBundleFlux)
                    .doOnNext(r -> sentBundles.getAndAdd(r.bundleCount()));
              })
          .doOnError(e -> skippedPatients.incrementAndGet())
          .onErrorContinue((err, o) -> log.debug("Skipping patient: {}", err.getMessage()))
          .collectList()
          .subscribe();
    }

    public State state() {
      return new State("", true, sentBundles.get(), skippedPatients.get());
    }
  }
}
