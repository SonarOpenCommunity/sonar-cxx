auto rethrower = [][[noreturn]]() { throw; };

auto lm = [][[nodiscard]]()->int { return 42; };

auto lm = [][[nodiscard, vendor::attr]]()->int { return 42; };
