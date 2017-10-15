int main(int argc, char ** argv)
{
   switch (argc)  // +1
   {
   case 1:
      std::cout << "No arguments" << std::endl;
      break;
   case 2:
      std::cout << "1 argument" << std::endl;
      break;
   default:
      std::cout << "Some arguments" << std::endl;
      break;
   }
}
