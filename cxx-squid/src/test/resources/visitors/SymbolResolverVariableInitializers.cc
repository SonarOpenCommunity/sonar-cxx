struct Config {
    const char *name = "field-default";
};

const char *global_sigalgs = "global-value";

void configure() {
    const char *local_sigalgs = "local-value";
    use(local_sigalgs);
}

void use(const char *value);
