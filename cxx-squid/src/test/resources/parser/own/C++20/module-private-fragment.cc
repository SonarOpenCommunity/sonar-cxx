export module A;

export struct X;
export X *factory();

module :private;

struct X {};

X* factory() {
   return new X();
}
