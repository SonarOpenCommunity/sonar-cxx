int main(int argc, char ** argv)
{
   try {
      std::cout << "All is well" << std::endl;
   }
   catch (...) {     // +1
      std::cout << "Exception!" << std::endl;
   }
}
