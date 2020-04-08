#include <iostream>
#include <vector>
#include <string>
#include <fstream>
#include <cuda.h>

using namespace std;

__global__ void odd_count(int *arr, unsigned int *d_count, int length)
{
  int idx = blockDim.x * blockIdx.x + threadIdx.x;
  if (idx >= length)
  {
    return;
  }

  if (arr[idx] % 2 != 0)
  {
    atomicAdd(d_count, 1);
  }
}

__global__ void mark(int *arr, int *predicates, int length)
{
  int idx = blockDim.x * blockIdx.x + threadIdx.x;
  if (idx >= length)
  {
    return;
  }
  predicates[idx] = arr[idx] % 2 ? 1 : 0;
}

__global__ void scan(int *output, int *predicates, int *sums, int n)
{
  int bid = blockIdx.x;
  int tid = threadIdx.x;
  int block = bid * n;

  extern __shared__ int buffer[];

  buffer[2 * tid] = predicates[block + (2 * tid)];
  buffer[2 * tid + 1] = predicates[block + (2 * tid) + 1];

  int offset = 1;
  for (int d = n >> 1; d > 0; d >>= 1)
  {
    __syncthreads();
    if (tid < d)
    {
      int a = offset * (2 * tid + 1) - 1;
      int b = offset * (2 * tid + 2) - 1;
      buffer[b] += buffer[a];
    }

    offset *= 2;
  }

  __syncthreads();

  if (tid == 0)
  {
    sums[bid] = buffer[n - 1];
    buffer[n - 1] = 0;
  }

  for (int d = 1; d < n; d *= 2)
  {
    offset >>= 1;
    __syncthreads();
    if (tid < d)
    {
      int a = offset * (2 * tid + 1) - 1;
      int b = offset * (2 * tid + 2) - 1;
      int t = buffer[a];
      buffer[a] = buffer[b];
      buffer[b] += t;
    }
  }

  __syncthreads();

  output[block + (2 * tid)] = buffer[2 * tid];
  output[block + (2 * tid) + 1] = buffer[2 * tid + 1];
}

__global__ void add(int *output, int length, int *n)
{
  int blockId = blockIdx.x;
  int tid = threadIdx.x;
  int block = blockId * length;

  output[block + tid] += n[blockId];
}

__global__ void compact(int *result, int *input, int *predicates, int *scanned, int length)
{
  int idx = blockDim.x * blockIdx.x + threadIdx.x;
  if (idx >= length)
  {
    return;
  }

  if (predicates[idx] == 1)
  {
    int address = scanned[idx];
    result[address] = input[idx];
    if (result[address] == 5031)
    {
      printf("this index is fucking up: %d. related scanned[idx] is %d. the input at this index is %d\n", idx, scanned[idx], input[idx]);
    }
  }
}

int main()
{
  vector<int> data;
  ifstream infile;
  infile.open("inp.txt");

  if (infile.is_open())
  {
    while (infile.good())
    {
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

  int size = data.size();
  int size1 = size * sizeof(int);

  int *d_output;
  int *d_input;
  int *d_predicates;
  int *d_result;
  int *d_dummy_blocks_sums;
  int *d_sums;
  int *d_inc;
  int *input_copy;
  unsigned int *d_count;

  int *output = (int *)malloc(size1);
  unsigned int *count = (unsigned int *)malloc(sizeof(unsigned int));

  int blocks = size / 1024;
  if (size % 1024 != 0)
  {
    blocks += 1;
  }
  const int sharedSize = 2 * 1024 * sizeof(int);

  cudaMalloc((void **)&d_sums, blocks * sizeof(int));
  cudaMalloc((void **)&d_count, sizeof(unsigned int));
  cudaMalloc((void **)&d_inc, blocks * sizeof(int));
  cudaMalloc((void **)&d_dummy_blocks_sums, blocks * sizeof(int));
  cudaMalloc((void **)&d_output, size1);
  cudaMalloc((void **)&d_input, size1);
  cudaMalloc((void **)&input_copy, size1);
  cudaMalloc((void **)&d_predicates, size1);
  cudaMalloc((void **)&d_result, size1);

  cudaMemcpy(d_input, data.data(), size1, cudaMemcpyHostToDevice);
  cudaMemcpy(d_count, 0, sizeof(unsigned int), cudaMemcpyHostToDevice);
  cudaMemcpy(input_copy, data.data(), size1, cudaMemcpyHostToDevice);

  odd_count<<<blocks, 1024>>>(input_copy, d_count, size);
  mark<<<blocks, 1024>>>(d_input, d_predicates, size);
  scan<<<blocks, 512, sharedSize>>>(d_output, d_predicates, d_sums, 1024);
  scan<<<1, (blocks + 1) / 2, sharedSize>>>(d_inc, d_sums, d_dummy_blocks_sums, 1024);
  add<<<blocks, 1024>>>(d_output, 1024, d_inc);
  compact<<<size, 1024>>>(d_result, input_copy, d_predicates, d_output, size);

  cudaMemcpy(count, d_count, sizeof(unsigned int), cudaMemcpyDeviceToHost);
  cudaMemcpy(output, d_result, *count * sizeof(int), cudaMemcpyDeviceToHost);

  ofstream outfile;
  outfile.open("q3.txt");

  if (outfile.is_open())
  {

    for (int i = 0; i < *count; i++)
    {
      outfile << output[i] << ", ";
    }

    outfile.close();
  }
  else
  {
    cout << "Error opening file";
  }

  cudaDeviceSynchronize();

  cudaFree(d_output);
  cudaFree(d_input);
  cudaFree(d_sums);
  cudaFree(d_count);
  cudaFree(d_inc);
  cudaFree(d_predicates);
  cudaFree(d_dummy_blocks_sums);

  free(output);
  free(count);

  return 0;
}