namespace Graphics {
  /// <remarks>
  /// Class <c>Point</c> models a point in a two-dimensional plane.
  /// </remarks>
  public ref class Point {
  public:
    /// <value>
    /// The Point's x-coordinate.
    /// </value>
    property int X;
    /// <value>
    /// The Points' y-coordinate.
    /// </value>
    property int Y;
    /// <summary>
    /// This constructor initializes the new Point to (0,0).
    /// </summary>
    Point() {
      X = 0;
      Y = 0;
    }
    /// <summary>
    /// This constructor initializes the new Point to
    /// (<paramref name="xord"/>,<paramref name="yord"/>).
    /// </summary>
    /// <param name="xord">
    /// <c>xord</c> is the new Point's x-coordinate.
    /// </param>
    /// <param name="yord">
    /// <c>yord</c> is the new Point's y-coordinate.
    /// </param>
    Point(int xord, int yord) {
      X = xord;
      Y = yord;
    }
    /// <summary>
    /// This function changes the point's location to the given
    /// coordinates.
    /// </summary>
    /// <param name="xord">
    /// <c>xord</c> is the new x-coordinate.
    /// </param>
    /// <param name="yord">
    /// <c>yord</c> is the new y-coordinate.
    /// </param>
    /// <seealso cref="Translate"/>
    void Move(int xord, int yord) {
      X = xord;
      Y = yord;
    }
    /// <summary>
    /// This function changes the point's location by the given
    /// x- and y-offsets.
    /// </summary>
    /// <example>
    /// The following code:
    /// <code>
    /// Point p(3,5);
    /// p.Translate(-1,3);
    /// </code>
    /// results in <c>p</c>'s having the value (2,8).
    /// </example>
    /// <param name="xord">
    /// <c>xord</c> is the relative x-offset.
/// /// </param>
/// <param name="yord">
/// <c>yord</c> is the relative y-offset.
/// </param>
/// <seealso cref="Move"/>
void Translate(int xord, int yord) {
  X += xord;
  Y += yord;
}
/// <summary>
/// This function determines whether two Points have the same
/// location.
/// </summary>
/// <param name="o">
/// <c>o</c> is the object to be compared to the current object.
/// </param>
/// <returns>
/// True if the Points have the same location; otherwise, false.
/// </returns>
/// <seealso cref="operator =="/>
/// <seealso cref="operator !="/>
bool Equals(Object^ o) override {
  Point^ p = dynamic_cast<Point^>(o);
  if (!p) return false;
  return (X == p->X) && (Y == p->Y);
}
/// <summary>
/// Computes the hash code for a Point.
/// </summary>
/// <returns>
/// A hash code computed from the x and y coordinates.
/// </returns>
int GetHashCode() override {
  return X ^ Y;
}
/// <summary>
/// Report a point's location as a string.
/// </summary>
/// <returns>
/// A string representing a point's location, in the form (x,y),
/// without any leading, training, or embedded whitespace.
/// </returns>
String^ ToString() override {
  return String::Format("({0},{1})", X, Y);
}
/// <summary>
/// This operator determines whether two Points have the same
/// location.
/// </summary>
/// <param name="p1">The first Point to be compared.</param>
/// <param name="p2">The second Point to be compared.</param>
/// <returns>
/// True if the Points have the same location; otherwise, false.
/// </returns>
/// <seealso cref="Equals"/>
/// <seealso cref="operator !="/>
static bool operator==(Point^ p1, Point^ p2) {
  if ((Object^)p1 == nullptr || (Object^)p2 == nullptr)
    return false;
  return (p1->X == p2->X) && (p1->Y == p2->Y);
}
/// <summary>
/// This operator determines whether two Points have the same
/// location.
/// /// </summary>
/// <param name="p1">The first Point to be compared.</param>
/// <param name="p2">The second Point to be compared.</param>
/// <returns>
/// True if the Points do not have the same location;
/// otherwise, false.
/// </returns>
/// <seealso cref="Equals"/>
/// <seealso cref="operator =="/>
static bool operator!=(Point^ p1, Point^ p2) {
  return !(p1 == p2);
}
};
}