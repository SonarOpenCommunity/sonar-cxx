// --- static operator() ---

// Overload Resolution
struct less {
	static constexpr auto operator()(int i, int j) -> bool {
		return i < j;
	}
	using P = bool( * )(int, int);
	operator P() const {
		return operator();
	}
};

void foo() {
	static_assert(less {}(1, 2));
}

// Lambdas
auto four = []() static {
	return 4;
};

//  Static lambdas with capture
auto under_lock = [lock = std::unique_lock(mtx)]() static {
	/* do something */ ;
};

// --- static operator[] ---

template < typename T, std::size_t S > struct array: std::array < T, S > {
	static constexpr inline std::size_t extent = []() -> std::size_t {
		if constexpr(_is_array < T > ) {
			return 1 + T::extent;
		}
		return 1;
	}();
	constexpr decltype(auto)
	operator[](std::size_t idx) {
		return * (this -> data() + idx);
	}
	constexpr decltype(auto)
	operator[](std::size_t idx, convertible_to < std::size_t > auto && ...args)
	requires(sizeof...(args) < extent) && (sizeof...(args) >= 1) {
		typename std::array < T, S > ::reference v = * (this -> data() + idx);
		return v.operator[](args...);
	}
	constexpr decltype(auto)
	operator[](std::size_t idx) const {
		return * (this -> data() + idx);
	}
	constexpr decltype(auto)
	operator[](std::size_t idx, convertible_to < std::size_t > auto && ...args) const requires(sizeof...(args) < extent) && (sizeof...(args) >= 1) {
		typename std::array < T, S > ::reference v = * (this -> data() + idx);
		return v.operator[](args...);
	}
};
