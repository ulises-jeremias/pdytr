#ifndef UTILS_H
#define UTILS_H

#include <stddef.h>

unsigned long djb2(char *str);
double dwalltime();
void ini_params(int argc, char *argv[], int *verbose_flag_ptr,
                char *host, char *src, char *dest,
                uint64_t *bytes, uint64_t *initial_pos);

#endif
