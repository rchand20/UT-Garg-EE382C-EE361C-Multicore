#include <stdio.h>
#include <omp.h>
#include <stdlib.h>
#include <string.h>

void MatrixMult(char file1[], char file2[], int T)
{
  FILE *f1;
  FILE *f2;
  int rows1, columns1;
  int rows2, columns2;

  f1 = fopen(file1, "r");
  if (f1 == NULL)
    exit(EXIT_FAILURE);

  fscanf(f1, "%d%d", &rows1, &columns1);
  double m1[rows1][columns1];

  for (int i = 0; i < rows1; i++)
  {
    for (int j = 0; j < columns1; j++)
    {
      if (fscanf(f1, "%lf", &m1[i][j]) != 1)
      {
        exit(1);
      }
    }
  }

  fclose(f1);

  f2 = fopen(file2, "r");
  if (f2 == NULL)
    exit(EXIT_FAILURE);

  fscanf(f2, "%d%d", &rows2, &columns2);
  double m2[rows2][columns2];

  for (int i = 0; i < rows2; i++)
  {
    for (int j = 0; j < columns2; j++)
    {
      if (fscanf(f2, "%lf", &m2[i][j]) != 1)
      {
        exit(1);
      }
    }
  }

  fclose(f2);

  double result[rows1][columns2];

  omp_set_num_threads(T);

  double start = omp_get_wtime();
  #pragma omp parallel for shared(m1, m2, result)
  for(int i = 0; i < rows1; i++) {
    for(int j = 0; j < columns2; j++) {
      for(int k = 0; k < columns1; k++) {
        result[i][j] += m1[i][k] * m2[k][j];
      }
    }
  }


  double end = omp_get_wtime();

    for (int i= 0; i< rows1; i++)
    {
        for (int j= 0; j< columns2; j++)
        {
            printf("%lf\t",result[i][j]);
        }
        printf("\n");
    }


  printf("execution time for %d threads is %f\n", T, end-start);

}

void main(int argc, char *argv[])
{
  char *file1, *file2;
  file1 = argv[1];
  file2 = argv[2];
  int T = atoi(argv[3]);
  MatrixMult(file1, file2, T);
}
