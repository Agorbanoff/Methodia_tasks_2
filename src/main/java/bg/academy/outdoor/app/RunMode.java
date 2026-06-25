package bg.academy.outdoor.app;

public enum RunMode {
    ONCE,
    DRY_RUN,
    WATCH;

    public static RunMode fromArgs(String[] args) {
        if (args.length == 0 || "--once".equals(args[0])) {
            return ONCE;
        }

        if ("--dry-run".equals(args[0])) {
            return DRY_RUN;
        }

        if ("--watch".equals(args[0])) {
            return WATCH;
        }

        throw new IllegalArgumentException(
                "Unsupported mode: " + args[0] + ". Supported modes: --once, --dry-run, --watch"
        );
    }
}
