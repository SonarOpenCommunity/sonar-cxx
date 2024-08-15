template<typename T>
class host : public std::enable_shared_from_this<host<T>> {
public:
  template<typename... U>
  explicit host(U&&... args);             // should ignore &&
  host() = delete;
  host(const host&) = delete;
  host(host&&)      = default;            // should ignore && and default
  host& operator=(const host&) = delete;
  host& operator=(host&&)      = default; // should ignore && and default
};
