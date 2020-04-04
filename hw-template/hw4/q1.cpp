#include <iostream>
#include <vector>
#include <string>
#include <fstream>

using namespace std;

int main()
{
    vector<int> data;
    ifstream infile;
    infile.open("inp.text");

    if (infile.is_open())
    {
        while (infile.good()){
            char cNum[10];
            infile.getline(cNum, 256, ',');
            int num = atoi(cNum);
            data.push_back(num);
            // cout << num << " ";
        }
        infile.close();
    }
    else 
    {
        cout << "Error opening file";
    }

    for (int x = 0; x < data.size(); ++x) 
    {
        cout << data[x] << " ";
    }
}