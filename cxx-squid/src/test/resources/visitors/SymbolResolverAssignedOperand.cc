void exercise() {
    int values[2];
    int index = 0;
    int *target = 0;
    int plain;
    int grouped;
    values[index] = 1;
    *target = 2;
    plain = 3;
    (grouped) = 4;
    use(values[0], index, *target, plain, grouped);
}

void use(int a, int b, int c, int d, int e);
