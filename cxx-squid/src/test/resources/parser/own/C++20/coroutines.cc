//
// Work in progress: This file is in progress of being updated to reflect the parts of
// Coroutines Technical Specification that were included in the working draft of C++20.
//

// uses the co_await operator to suspend execution until resumed 

task<> tcp_echo_server() {
  char data[1024];
  for (;;) {
    size_t n = co_await socket.async_read_some(buffer(data));
    co_await async_write(socket, buffer(data, n));
  }
}

// uses the keyword co_yield to suspend execution returning a value 

generator<int> iota(int n = 0) {
  while(true)
    co_yield n++;
}

// uses the keyword co_return to complete execution returning a value 

lazy<int> f() {
  co_return 7;
}
