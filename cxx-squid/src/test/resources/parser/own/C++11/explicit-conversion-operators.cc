struct SomeType
{
    // implicit conversion
    operator int() const { return 7; }

    // explicit conversion
    explicit operator int*() const { return nullptr; }
};
