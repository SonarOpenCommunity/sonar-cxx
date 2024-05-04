struct DeleteSample { // = 0
    DeleteSample(const DeleteSample&) = delete;
    DeleteSample& operator=(const DeleteSample&) = delete;
    DeleteSample(DeleteSample&&) = delete;
    DeleteSample& operator=(DeleteSample&&) = delete;
    ~DeleteSample() = default;
};

struct DefaultSample { // = 0
    DefaultSample(const DefaultSample&) = default;
    DefaultSample& operator=(const DefaultSample&) = default;
    DefaultSample(DefaultSample&&) = default;
    DefaultSample& operator=(DefaultSample&&) = default;
    ~DefaultSample() = default;
};

struct Sample { // = 5
    Sample(const Sample&) {
        if (true) {
            std::cout << "+1" << std::endl;
        }
    }
    Sample& operator=(const Sample&) {
        if (true) {
            std::cout << "+1" << std::endl;
        }
    }
    Sample(Sample&&) {
        if (true) {
            std::cout << "+1" << std::endl;
        }
    }
    Sample& operator=(Sample&&) {
        if (true) {
            std::cout << "+1" << std::endl;
        }
    }
    ~Sample() {
        if (true) {
            std::cout << "+1" << std::endl;
        }
    }
};
