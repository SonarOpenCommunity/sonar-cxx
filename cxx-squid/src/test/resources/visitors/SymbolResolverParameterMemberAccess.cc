struct DigestApi {
    int (*digest)(const char *data);
};

class Hasher {
public:
    int hash(const char *data) { return 0; }
};

void useParameters(DigestApi *api, Hasher &hasher, const Hasher &constHasher, struct DigestApi *taggedApi) {
    api->digest("a");
    hasher.hash("b");
    constHasher.hash("c");
    taggedApi->digest("d");
    struct DigestApi local;
    local.digest("e");
}
