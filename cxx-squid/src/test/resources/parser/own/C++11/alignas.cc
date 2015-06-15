// every object of type sse_t will be aligned to 16-byte boundary
struct alignas(16) sse_t
{
  float sse_data[4];
};

// the array "cacheline" will be aligned to 128-byte boundary
alignas(128) char cacheline[128];
