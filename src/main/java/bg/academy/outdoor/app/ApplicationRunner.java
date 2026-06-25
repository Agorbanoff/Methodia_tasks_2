package bg.academy.outdoor.app;

public class ApplicationRunner {
    private final OnceRunner onceRunner;
    private final DryRunRunner dryRunRunner;
    private final WatchRunner watchRunner;

    public ApplicationRunner(OnceRunner onceRunner, DryRunRunner dryRunRunner, WatchRunner watchRunner) {
        this.onceRunner = onceRunner;
        this.dryRunRunner = dryRunRunner;
        this.watchRunner = watchRunner;
    }

    public void run(RunMode mode) throws Exception {
        switch (mode) {
            case ONCE -> onceRunner.run();
            case DRY_RUN -> dryRunRunner.run();
            case WATCH -> watchRunner.run();
        }
    }
}
