// compile with: /clr /c
#using <System.Data.dll>

namespace MyNamespace {
   
   [SerializableAttribute] 
   [AttributeUsageAttribute(AttributeTargets::Class|AttributeTargets::Struct|AttributeTargets::Constructor|AttributeTargets::Method|AttributeTargets::Property, Inherited=false)] 
   [ComVisibleAttribute(true)] 
   public ref class NewDataSet : public ::System::Data::DataSet {
        
      public: [System::Diagnostics::DebuggerNonUserCodeAttribute]
         [System::CodeDom::Compiler::GeneratedCodeAttribute(L"System.Data.Design.TypedDataSetGenerator", L"4.0.0.0")]
         NewDataSet();
        
      public: [System::Diagnostics::DebuggerNonUserCodeAttribute]
         [System::CodeDom::Compiler::GeneratedCodeAttribute(L"System.Data.Design.TypedDataSetGenerator", L"4.0.0.0")]
         Sample();
   };
}
