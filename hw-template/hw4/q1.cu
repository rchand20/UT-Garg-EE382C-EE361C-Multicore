#include <iostream>
#include <vector>
#include <string>
#include <fstream>

using namespace std;

#define N 1024	
#define BLOCK_SIZE 16

__global__ void arrMin2(int *min, int *A, int *size) {
	__shared__ int sharedMin;

	int tid = threadIdx.x;

	if(tid == 0) {
		sharedMin = 1000;
	}

	__syncthreads();

	int localMin = 1000;

	for(int i = blockIdx.x * blockDim.x + tid; i < *size; i += blockDim.x) {
		int val = A[i];

		if(localMin > val) {
			localMin = val;
		}
	}

	atomicMin(&sharedMin, localMin);
	__syncthreads();

	if(tid == 0) {
		min[blockIdx.x] = sharedMin;
	}
}

int main() {
    vector<int> data;
	int *A, *min;
	int *d_min, *d_A, *d_size;
	int size; 

    ifstream infile;
    infile.open("inp2.text");
	
	// Read file input and push to vector
    if (infile.is_open()) {
        while (infile.good()){
            char cNum[10];
            infile.getline(cNum, 256, ',');
            int num = atoi(cNum);
            data.push_back(num);
        }
		size = data.size() * sizeof(int);
        infile.close();
    }
    else {
        cout << "Error opening file";
    }
	
	// Alloc space for host copies 
	min = (int *)malloc(size);   
	A = (int *)malloc(size);

	// Alloc space for device copies
	cudaMalloc((void **) &d_min, sizeof(int));
	cudaMalloc((void **) &d_A, data.size() * sizeof(int));
	cudaMalloc((void **) &d_size, sizeof(int));

	// Copy inputs to device
	cudaMemcpy(d_A, data.data(), size, cudaMemcpyHostToDevice);
	int temp = data.size();
	cudaMemcpy(d_size, &temp, sizeof(int), cudaMemcpyHostToDevice);

	arrMin2<<<N/BLOCK_SIZE, BLOCK_SIZE >>>(d_min, d_A, d_size);	
	
	// Copy result back to host
	cudaMemcpy(min, d_min, sizeof(int), cudaMemcpyDeviceToHost);

	cout << "The min is " << min[0] << '\n';

	// Cleanup
	free(min); free(A);
	cudaFree(d_min); cudaFree(d_A);

	return 0;
}
