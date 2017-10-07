int main()
{
    int myints[7] = {1, 3, 7, 2, 0, 4, -1};
    std::vector<int> myvector(myints, myints + (sizeof(myints) / sizeof(myints[0])));

    std::sort(myvector.begin(),
              myvector.end(),
              [](int a, int b) {
                  return a < b;
              });

    std::for_each(myvector.begin(),
                  myvector.end(),
                  [](int &n) {
                      if (n < 0) return; // +2 (nesting = 1)
                      std::cout << n << std::endl;
                  });
}
