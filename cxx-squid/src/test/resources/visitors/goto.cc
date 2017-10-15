int main(int argc, char ** argv)
{
   std::cout << "Start" << std::endl;

   goto later;  // +1

   std::cout << "Middle" << std::endl;

later:
   std::cout << "End" << std::endl;
}
