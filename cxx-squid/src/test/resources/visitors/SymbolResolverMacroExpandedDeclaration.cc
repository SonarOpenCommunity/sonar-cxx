typedef struct { int socket; int flags; } uv_udp_t;

#define SOCKOPT_SETTER(name, option4, option6, validate)                      \
  int uv_udp_set_##name(uv_udp_t* handle, int value) {                        \
    int optval = value;                                                      \
                                                                              \
    if (!(validate(value))) {                                                 \
      return -1;                                                              \
    }                                                                         \
                                                                              \
    if (handle->socket == 0)                                                  \
      return -2;                                                              \
                                                                              \
    return optval;                                                            \
  }

#define VALIDATE_TTL(value) ((value) >= 1 && (value) <= 255)

SOCKOPT_SETTER(ttl,
               1,
               2,
               VALIDATE_TTL)

#undef SOCKOPT_SETTER
#undef VALIDATE_TTL
