int main(int argc, char* argv[])
{
   if ((argc == 1)               // +1
       && (argv[0] != NULL)) {   // +1
      std::cout << "This program is called " << argv[0] << std::endl;
   }
}
