#include <iostream>
#include <vector>
#include <string>
#include <fstream>

using namespace std;

#define N 1024
#define BLOCK_SIZE 16

__global__ void arrMin(int *min, int *A, int *size)
{
	__shared__ int sharedMin;

	int tid = threadIdx.x;

	if (tid == 0)
	{
		sharedMin = 1000;
	}

	__syncthreads();

	int localMin = 1000;

	for (int i = blockIdx.x * blockDim.x + tid; i < *size; i += blockDim.x)
	{
		int val = A[i];

		if (localMin > val)
		{
			localMin = val;
		}
	}

	atomicMin(&sharedMin, localMin);
	__syncthreads();

	if (tid == 0)
	{
		min[blockIdx.x] = sharedMin;
	}
}

__global__ void makeB(int *A, int *B, int size)
{
	int i = blockIdx.x * blockDim.x + threadIdx.x;

	if (i < size)
	{
		B[i] = A[i] % 10;
	}
}

int main()
{
	vector<int> data;
	int *A, *B, *min;
	int *d_min, *d_A, *d_B, *d_size;
	int size;

	ifstream infile;
	infile.open("inp.txt");

	// Read file input and push to vector
	if (infile.is_open())
	{
		while (infile.good())
		{
			char cNum[10];
			infile.getline(cNum, 256, ',');
			int num = atoi(cNum);
			data.push_back(num);
		}
		size = data.size() * sizeof(int);
		infile.close();
	}
	else
	{
		cout << "Error opening file";
	}

	// Alloc space for host copies
	min = (int *)malloc(size);
	A = (int *)malloc(size);
	B = (int *)malloc(size);

	// Alloc space for device copies
	cudaMalloc((void **)&d_min, sizeof(int));
	cudaMalloc((void **)&d_A, size);
	cudaMalloc((void **)&d_B, size);
	cudaMalloc((void **)&d_size, sizeof(int));

	// Copy inputs to device
	cudaMemcpy(d_A, data.data(), size, cudaMemcpyHostToDevice);
	int temp = data.size();
	cudaMemcpy(d_size, &temp, sizeof(int), cudaMemcpyHostToDevice);

	arrMin<<<data.size() / BLOCK_SIZE + 1, BLOCK_SIZE>>>(d_min, d_A, d_size);
	makeB<<<data.size() / BLOCK_SIZE + 1, BLOCK_SIZE>>>(d_A, d_B, temp);

	cudaDeviceSynchronize();

	// Copy result back to host
	cudaMemcpy(min, d_min, sizeof(int), cudaMemcpyDeviceToHost);
	cudaMemcpy(B, d_B, size, cudaMemcpyDeviceToHost);

	ofstream outfile;
	outfile.open("q1a.txt");

	if (outfile.is_open())
	{

		outfile << "The min is " << min[0] << '\n';

		outfile.close();
	}
	else
	{
		cout << "Error opening file";
	}

	outfile.open("q1b.txt");
	if (outfile.is_open())
	{

		for (int i = 0; i < data.size(); ++i)
		{
			outfile << B[i] << ' ';
		}

		outfile.close();
	}
	else
	{
		cout << "Error opening file";
	}

	cout << '\n';

	// Cleanup
	free(min);
	free(A);
	cudaFree(d_min);
	cudaFree(d_A);

	return 0;
}
