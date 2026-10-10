class Hasher {
  public:
    Hasher(const char *name);
};

class Config {};

void construct() {
    const char *name = "SHA256";
    Hasher hasher(name);
    Hasher make(Config);
}
