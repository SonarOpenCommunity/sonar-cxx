void pointers() {
  EVP_KDF *kdf = EVP_KDF_fetch(0, "HKDF", 0);
  EVP_KDF_CTX **ctx;
  use(kdf);
  use(ctx);
}

void product(int a, int b) {
  a * b;
}

struct Session { int id; };

void typed() {
  Session *session = open_session();
  use(session);
}
