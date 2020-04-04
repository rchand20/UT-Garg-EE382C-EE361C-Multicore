#include <iostream>
#include <vector>
#include <string>
#include <fstream>

using namespace std;

int main()
{
    vector<string> msg = {"Hello!", "C++", "World", "from", "VS Code", "and the C++ extension!"};

    for (const string& word : msg)
    {
        cout << word << " ";
    }
    cout << endl;

    vector<int> data;

    ifstream infile;
    infile.open("inp.text");
    if(!infile.is_open()) std::cout << "ERROR: File Open" << '\n';

    while (infile.good()){
        char cNum[10];
        infile.getline(cNum, 256, ',');
        cout << atoi(cNum) << " ";
    }
    infile.close();
}