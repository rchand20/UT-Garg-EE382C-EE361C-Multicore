#include <iostream>
#include <vector>
#include <string>
#include <fstream>

using namespace std;

#define BLOCK_SIZE 1024

__global__ void countEntriesPartA(int *A, int *B, int size)
{
	int i = blockIdx.x * blockDim.x + threadIdx.x;

	if (i < size)
	{
		int range = A[i] / 100;
		atomicAdd(&B[range], 1);
	}

	__syncthreads();
}

__global__ void countEntriesPartB(int *A, int *B2, int size)
{
	__shared__ int localB[10];
	int tid = threadIdx.x;
	int i = blockIdx.x * blockDim.x + threadIdx.x;

	if (tid == 0)
	{
		for (int j = 0; j < 10; j++)
			localB[j] = 0;
	}

	if (i < size)
	{
		int range = A[i] / 100;
		atomicAdd(&localB[range], 1);
	}

	__syncthreads();

	if (tid == 0)
	{
		for (int j = 0; j < 10; ++j)
		{
			atomicAdd(&B2[j], localB[j]);
			//B2[j] += localB[j];
		}
	}

	__syncthreads();
}

__global__ void countEntriesPartC(int *B, int *C, int size)
{
	extern __shared__ int smem[];
	int tid = threadIdx.x;

	if (tid < 10)
	{
		smem[2 * tid] = B[2 * tid];
		smem[2 * tid + 1] = B[2 * tid + 1];
	}

	else
	{
		smem[2 * tid] = 0;
		smem[2 * tid + 1] = 0;
	}

	int offset = 1;
	for (int d = size >> 1; d > 0; d >>= 1)
	{
		__syncthreads();

		if (tid < d)
		{
			int ai = offset * (2 * tid + 1) - 1;
			int bi = offset * (2 * tid + 2) - 1;
			smem[bi] += smem[ai];
		}
		offset *= 2;
	}

	if (tid == 0)
	{
		smem[size - 1] = 0;
	}

	for (int d = 1; d < size; d *= 2)
	{
		offset >>= 1;
		__syncthreads();
		if (tid < d)
		{
			int ai = offset * (2 * tid + 1) - 1;
			int bi = offset * (2 * tid + 2) - 1;
			int temp = smem[ai];
			smem[ai] = smem[bi];
			smem[bi] += temp;
		}
	}

	__syncthreads();

	C[2 * tid] = smem[2 * tid + 1];
	C[2 * tid + 1] = smem[2 * tid + 2];
	if ((2 * tid + 1) == 9)
	{
		C[2 * tid + 1] = smem[2 * tid + 1] + B[9];
	}
}

int main()
{
	vector<int> data;
	int *A, *B, *C;
	int *B2;
	int *d_A, *d_B, *d_B2, *d_C;
	int size;
	int Bsize = 10 * sizeof(int);

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
	A = (int *)malloc(size);
	B = (int *)calloc(10, sizeof(int));
	B2 = (int *)calloc(10, sizeof(int));
	C = (int *)calloc(10, sizeof(int));

	// Alloc space for device copies
	cudaMalloc((void **)&d_A, size);
	cudaMalloc((void **)&d_B, Bsize);
	cudaMalloc((void **)&d_B2, Bsize);
	cudaMalloc((void **)&d_C, Bsize);

	// Copy inputs to device
	cudaMemcpy(d_A, data.data(), size, cudaMemcpyHostToDevice);

	countEntriesPartA<<<data.size() / BLOCK_SIZE + 1, BLOCK_SIZE>>>(d_A, d_B, data.size());
	countEntriesPartB<<<data.size() / BLOCK_SIZE + 1, BLOCK_SIZE>>>(d_A, d_B2, data.size());
	countEntriesPartC<<<1, 5, 2 * 16 * sizeof(int)>>>(d_B, d_C, 16);
	// Copy result back to host
	cudaMemcpy(B, d_B, Bsize, cudaMemcpyDeviceToHost);
	cudaMemcpy(B2, d_B2, Bsize, cudaMemcpyDeviceToHost);
	cudaMemcpy(C, d_C, Bsize, cudaMemcpyDeviceToHost);

	// for (int i = 0; i < 10; ++i)
	// {
	// 	cout << B[i] << ' ';
	// }
	// cout << '\n';

	// for (int i = 0; i < 10; ++i)
	// {
	// 	cout << B2[i] << ' ';
	// }
	// cout << '\n';

	// for (int i = 0; i < 10; ++i)
	// {
	// 	cout << C[i] << ' ';
	// }
	// cout << '\n';

	ofstream outfile;
	outfile.open("q2a.txt");

	if (outfile.is_open())
	{

		for (int i = 0; i < 10; i++)
		{
			outfile << 	B[i] << ", ";
		}

		outfile.close();
	}
	else
	{
		cout << "Error opening file";
	}

	outfile.open("q2b.txt");

	if (outfile.is_open())
	{

		for (int i = 0; i < 10; i++)
		{
			outfile << 	B2[i] << ", ";
		}

		outfile.close();
	}
	else
	{
		cout << "Error opening file";
	}


	outfile.open("q2c.txt");

	if (outfile.is_open())
	{

		for (int i = 0; i < 10; i++)
		{
			outfile << 	C[i] << ", ";
		}

		outfile.close();
	}
	else
	{
		cout << "Error opening file";
	}

	// Cleanup
	free(A);
	free(B);
	free(B2);
	free(C);
	cudaFree(d_A);
	cudaFree(d_B);
	cudaFree(d_B2);
	cudaFree(d_C);

	return 0;
}
