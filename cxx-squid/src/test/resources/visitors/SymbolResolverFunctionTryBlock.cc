void use(int value);

void forward(int value) try {
    use(value);
} catch (...) {
    throw;
}
