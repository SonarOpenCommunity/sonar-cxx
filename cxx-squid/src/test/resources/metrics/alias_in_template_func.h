template<typename Second>
struct A {
    template<typename Third>
    void foo(){
        using type = void;
    }
};

template<typename Fourth>
void bar(){
    using type = void;
}

/*!
 * \brief Commented
 */
template<typename Fourth>
void foobar(){
    /*!
     * \brief Commented (Not a doxygen comment)
     */
    using type = void;

    using second_type = double;
}
