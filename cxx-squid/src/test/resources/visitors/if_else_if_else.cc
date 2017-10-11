int main(int argc, char ** argv)
{
   if (argc == 1) {        // +1
      std::cout << "No arguments" << std::endl;
   }
   else if (argc == 2) {   // +1
      std::cout << "1 argument" << std::endl;
   }
   else {                  // +1
      std::cout << "Some arguments" << std::endl;
   }
}
