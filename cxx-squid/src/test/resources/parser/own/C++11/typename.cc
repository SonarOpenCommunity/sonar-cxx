template<typename T> class my_array {};

// two type template parameters and one template template parameter:
template<typename K, typename V, template<typename> typename C = my_array>
class Map
{
    C<K> key;
    C<V> value;
};

// class template with a template template parameter V
template<template<typename> class V> class C
{
    V<int> y; // uses the primary template
    V<int*> z; // uses the partial specialization
};

template<typename T1 = int, typename T2 = int> class A;

// template template parameter T has a parameter list, which 
// consists of one type template parameter with a default
template<template<typename = float> typename T> struct A
{
    void f();
    void g();
};

template<typename T>
struct X : B<T> // "B<T>" is dependent on T
{
    typename T::A* pa; // "T::A" is dependent on T
                       // (see below for the meaning of this use of "typename")
    void f(B<T>* pb) {
        static int i = B<T>::i; // "B<T>::i" is dependent on T
        pb->j++; // "pb->j" is dependent on T
    }
};

template <class BiIter>
bool operator<=(const sub_match<BiIter>& lhs,
    const typename iterator_traits<BiIter>::value_type& rhs);

template<typename ...Args>
bool f(Args ...args) {
    return (true && ... && args); // OK
}

// issue #1000
namespace Publishing {
    namespace Simulators {
        template<typename T> class ManagedMemoryVector
        {
        public:
            class iterator : public std::iterator<std::bidirectional_iterator_tag, T>
            {
            private:
                typename std::vector<TypedMemoryBlock<T> *>::iterator stditerator;
                typename std::vector<T>::size_type index;

            private:
                iterator(const typename std::vector<TypedMemoryBlock<T> *>::iterator &stditerator,
                    typename std::vector<T>::size_type index) : stditerator(stditerator), index(index)
                {
                }
            };
        };
    }
}

// issue #1000
class HydraulicSimulatorOutputEntry {
    HydraulicSimulatorOutputEntry(const typename PresetLocation presetLocation);
};

// issue #1000
class HydraulicSimulatorOutput {
    std::map<typename OutputEntryNames, std::string> m_outputEntryNameDictionary;
};
