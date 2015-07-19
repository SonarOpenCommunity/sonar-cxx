
[AttributeUsage(AttributeTargets::All)]
public ref class HelpAttribute : Attribute {
  String^ url;
public:
  HelpAttribute(String^ url) {
    this->url = url;
  }
  String^ Topic;
//  property String^ Url {
    String^ get() { return url; }
//  }
};
