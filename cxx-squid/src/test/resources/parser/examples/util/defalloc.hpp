/* The following code example is taken from the book
 * "The C++ Standard Library - A Tutorial and Reference"
 * by Nicolai M. Josuttis, Addison-Wesley, 1999
 *
 * (C) Copyright Nicolai M. Josuttis 1999.
 * Permission to copy, use, modify, sell and distribute this software
 * is granted provided this copyright notice appears in all copies.
 * This software is provided "as is" without express or implied
 * warranty, and with no claim as to its suitability for any purpose.
 */
namespace std {
   template <class T>
   class allocator {
     public:
       // type definitions
       typedef size_t    size_type;
       typedef ptrdiff_t difference_type;
       typedef T*        pointer;
       typedef const T*  const_pointer;
       typedef T&        reference;
       typedef const T&  const_reference;
       typedef T         value_type;

       // rebind allocator to type U
       template <class U>
       struct rebind {
           typedef allocator<U> other;
       };

       // return address of values
       pointer address (reference value) const {
           return &value;
       }
       const_pointer address (const_reference value) const {
           return &value;
       }

       /* constructors and destructor
        * - nothing to do because the allocator has no state
        */
       allocator() throw() {
       }
       allocator(const allocator&) throw() {
       }
       template <class U>
         allocator (const allocator<U>&) throw() {
       }
       ~allocator() throw() {
       }

       // return maximum number of elements that can be allocated
       size_type max_size () const throw() {
           return numeric_limits<size_t>::max() / sizeof(T);
       }

       // allocate but don't initialize num elements of type T
       pointer allocate (size_type num,
                         allocator<void>::const_pointer hint = 0) {
           // allocate memory with global new
           return (pointer)(::operator new(num*sizeof(T)));
       }

       // initialize elements of allocated storage p with value value
       void construct (pointer p, const T& value) {
           // initialize memory with placement new
           new((void*)p)T(value);
       }

       // destroy elements of initialized storage p
       void destroy (pointer p) {
           // destroy objects by calling their destructor
           p->~T();
       }

       // deallocate storage p of deleted elements
       void deallocate (pointer p, size_type num) {
           // deallocate memory with global delete
           ::operator delete((void*)p);
       }
   };

   // return that all specializations of this allocator are interchangeable
   template <class T1, class T2>
   bool operator== (const allocator<T1>&,
                    const allocator<T2>&) throw() {
       return true;
   }
   template <class T1, class T2>
   bool operator!= (const allocator<T1>&,
                    const allocator<T2>&) throw() {
       return false;
   }
}

