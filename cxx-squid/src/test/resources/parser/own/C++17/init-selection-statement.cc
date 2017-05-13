// if samples
//
std::map<int, std::string> m;
std::mutex mx;
extern bool shared_flag; // guarded by mx
int demo() {
    if (auto it = m.find(10); it != m.end()) { return it->size(); }
    if (char buf[10]; std::fgets(buf, 10, stdin)) { m[0] += buf; }
    if (std::lock_guard lock(mx); shared_flag) { unsafe_ping(); shared_flag = false; }
    if (int s; int count = ReadBytesWithSignal(&s)) { publish(count); raise(s); }
    // todo
    //if (auto keywords = { "if", "for", "while" }; 
    //    std::any_of(keywords.begin(), keywords.end(),
    //    [&s](const char* kw) { return s == kw; })) {
    //    std::cerr << "Token must not be a keyword\n");
    //}
}


// switch samples
//
enum Result
{
    SUCCESS,
    DEVICE_FULL,
    ABORTED
};

std::pair<size_t /* bytes */, Result> writePacket()
{
    return{ 100, SUCCESS };
}

int main()
{
    switch (auto res = writePacket(); res.second)
    {
    case SUCCESS:
        std::cout << "successfully wrote " << res.first << " bytes\n";
        break;
    case DEVICE_FULL:
        std::cout << "insufficient space on device\n";
        break;
    case ABORTED:
        std::cout << "operation aborted before completion\n";
        break;
    }
    return 0;
}
