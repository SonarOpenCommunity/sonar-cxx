// call_default_indexer_through_this_pointer.cpp
// compile with: /clr /c
value class Position {
public:
  Position(int x, int y) : position(gcnew array<int, 2>(100, 100)) {
    this->default[x, y] = 1;
  }

  property int default[int, int]{
    int get(int x, int y) {
    return position[x, y];
  }

  void set(int x, int y, int value) {}
  }

private:
  array<int, 2> ^ position;
};