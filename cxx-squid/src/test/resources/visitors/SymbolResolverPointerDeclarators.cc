void several_declarators() {
  EVP_PKEY_CTX *first = EVP_PKEY_CTX_new_id(6, 0), *second = EVP_PKEY_CTX_new_id(408, 0);
  use(first);
  use(second);
}

void product_naming_a_declared_variable() {
  EVP_PKEY_CTX *ctx = EVP_PKEY_CTX_new_id(6, 0);
  UNKNOWN_FACTOR * ctx;
  use(ctx);
}
