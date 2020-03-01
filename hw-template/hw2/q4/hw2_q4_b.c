#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <omp.h>
#include <math.h>

int Sieve(int N, int threads)
{
    char* nums;
    nums = (char*) malloc((N + 1) * sizeof(char));

    omp_set_num_threads(threads);

    #pragma omp parallel for
    for (int i = 0; i <= N; i++)
    {
        nums[i] = 0;
    }

    double start = omp_get_wtime();
    #pragma openmp parallel for
    for (int i = 2; i <= sqrt(N); i++)
    {

        if (nums[i] == 0)
        {
            for (int j = i; j <= N / i; j += 1)
            {
                nums[i * j] = -1;
            }
        }
    }

    double end = omp_get_wtime();

    #pragma openmp parallel for
    int numPrimes = 0;
    for (int i = 2; i <= N; i++)
    {
        if (nums[i] == 0)
        {
            numPrimes++;
        }
    }

    //printf("%d threads took %f\n", threads, end - start);

    free(nums);
    return numPrimes;
}

void main(void)
{

    int num_primes;
    int num_threads = 8;

    num_primes = Sieve(10000000, num_threads);
    //printf("%d", num_primes);
}
