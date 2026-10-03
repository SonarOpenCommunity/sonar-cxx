enum Level {
    LOW,
    HIGH
};

enum class Mode {
    STRICT = 1,
    LENIENT = 2
};

void use(int value);

void configure() {
    use(LOW);
}
