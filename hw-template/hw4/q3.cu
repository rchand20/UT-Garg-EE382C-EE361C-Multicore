#include <iostream>
#include <vector>
#include <string>
#include <fstream>
#include <cuda.h>

using namespace std;

__global__ void mark(int *arr, int *predicates, int length)
{
  int idx = blockDim.x * blockIdx.x + threadIdx.x;
  if (idx >= length)
    return;
  predicates[idx] = arr[idx] % 2 == 0 ? 0 : 1;
}

__global__ void block_scan(int *output, int *input, int *sums, int n)
{
  int bid = blockIdx.x;
  int tid = threadIdx.x;
  int blockOff = bid * n;

  extern __shared__ int buffer[];

  buffer[2 * tid] = input[blockOff + (2 * tid)];
  buffer[2 * tid + 1] = input[blockOff + (2 * tid) + 1];

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

  output[blockOff + (2 * tid)] = buffer[2 * tid];
  output[blockOff + (2 * tid) + 1] = buffer[2 * tid + 1];
}

__global__ void sum_scan(int *output, int *input, int n, int power)
{
  extern __shared__ int temp[];

  int tid = threadIdx.x;

  if (tid < n)
  {
    temp[2 * tid] = input[2 * tid];
    temp[2 * tid + 1] = input[2 * tid + 1];
  }
  else
  {
    temp[2 * tid] = 0;
    temp[2 * tid + 1] = 0;
  }

  int offset = 1;
  for (int d = power >> 1; d > 0; d >>= 1)
  {
    __syncthreads();
    if (tid < d)
    {
      int a = offset * (2 * tid + 1) - 1;
      int b = offset * (2 * tid + 2) - 1;
      temp[b] += temp[a];
    }
    offset *= 2;
  }

  if (tid == 0)
  {
    temp[power - 1] = 0;
  }

  for (int d = 1; d < power; d *= 2)
  {
    offset >>= 1;
    __syncthreads();
    if (tid < d)
    {
      int a = offset * (2 * tid + 1) - 1;
      int b = offset * (2 * tid + 2) - 1;
      int t = temp[a];
      temp[a] = temp[b];
      temp[b] += t;
    }
  }

  __syncthreads();

  if (tid < n)
  {
    output[2 * tid] = temp[2 * tid];
    output[2 * tid + 1] = temp[2 * tid + 1];
  }
}

__global__ void add(int *output, int length, int *n)
{
  int blockId = blockIdx.x;
  int tid = threadIdx.x;
  int blockOffset = blockId * length;

  output[blockOffset + tid] += n[blockId];
}

__global__ void compact(int *result, int *input, int *predicates, int *output, int length)
{
  int idx = blockDim.x * blockIdx.x + threadIdx.x;
  if (idx >= length)
  {
    return;
  }

  if (predicates[idx] == 1)
  {
    result[output[idx]] = input[idx];
  }
}

int nextPowerOfTwo(int x)
{
  int power = 1;
  while (power < x)
  {
    power *= 2;
  }
  return power;
}

int main()
{
  vector<int> data;
  ifstream infile;
  infile.open("inp.text");

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

  int *output = (int *)malloc(size1);

  cudaMalloc((void **)&d_output, size1);
  cudaMalloc((void **)&d_input, size1);
  cudaMalloc((void **)&d_predicates, size1);
  cudaMalloc((void **)&d_result, size1);

  cudaMemcpy(d_input, data.data(), size1, cudaMemcpyHostToDevice);
  mark<<<size / 1024 + 1, 1024>>>(d_input, d_predicates, size);
  cudaDeviceSynchronize();

  int *d_sums;
  int *d_inc;

  int blocks = size / 1024;
  blocks += 1;
  int power = nextPowerOfTwo(blocks);
  const int sharedSize = 2 * 1024 * sizeof(int);
  cudaMalloc((void **)&d_sums, blocks * sizeof(int));
  cudaMalloc((void **)&d_inc, blocks * sizeof(int));

  block_scan<<<blocks, 512, sharedSize>>>(d_output, d_predicates, d_sums, 1024);
  sum_scan<<<1, blocks / 2, 2 * power * sizeof(int)>>>(d_inc, d_sums, blocks, power);
  add<<<blocks, 1024>>>(d_output, 1024, d_inc);
  compact<<<size / 1024 + 1, 1024>>>(d_result, d_input, d_predicates, d_output, size);
  cudaMemcpy(output, d_result, size1, cudaMemcpyDeviceToHost);
  for (int i = 0; i < size; i++)
  {
    cout << output[i] << ", ";
  }
  cudaDeviceSynchronize();

  cudaFree(d_output);
  cudaFree(d_input);
  cudaFree(d_sums);
  cudaFree(d_inc);
  cudaFree(d_predicates);

  free(output);
}