package bg.academy.outdoor.config;

public record EmailConfig(
        boolean enabled,
        String to
) {
}
