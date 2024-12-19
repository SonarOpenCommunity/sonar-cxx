void func1()
{
    int input[256]{};
    int table[256]{};
    int output[256]{};
    int inputPos{};
    int outputPos{};

    output[outputPos++] = table[((input[inputPos + 1] & 0x0f) << 2) | (input[inputPos + 2] >> 6)];
}
