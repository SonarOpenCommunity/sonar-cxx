namespace demo {

enum Color { RED, GREEN, BLUE };
typedef unsigned char byte_t;

struct Point {
    int x;
    int y;

    int sum() {
        int total = x + y;
        return total;
    }
};

int globalCounter = 0;

void tick(int amount) {
    globalCounter = globalCounter + amount;
}

}
