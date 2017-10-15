int main(int argc, char ** argv)
{
   try {
      std::cout << "All is well" << std::endl;
   }
   catch (SpecificException &e) { // +1
      std::cout << "Specific exception caught" << std::endl;
   }
   catch (BadException &e) {      // +1
      std::cout << "Bad exception caught" << std::endl;
   }
   catch (...) {                  // +1
      std::cout << "All other exceptions caught" << std::endl;
   }
}
