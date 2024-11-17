/******************************/
/*** Simon Szulik     1474315 */
/*** Betriebssysteme WS 24/25 */
/******************************/
#include <stdio.h>
#include <fcntl.h>
#include <unistd.h>
#include <time.h>
#include <limits.h>

#define ITERATIONS 1000000

int main() {
    struct timespec start, end;
    long latencies[ITERATIONS];
    long min_latency_read = LONG_MAX;
    long min_latency_write = LONG_MAX;
    long min_latency_pid = LONG_MAX;
    long total_latency_read = 0;
    long total_latency_write = 0;
    long total_latency_pid = 0;
    int fd_read = open("/dev/zero", O_RDONLY);
    int fd_write = open("/dev/null", O_WRONLY);
    char buffer[1];

    if (fd_read < 0 || fd_write < 0) {
        perror("Error opening file");
        return 1;
    }

    // Latenz für read messen
    for (int i = 0; i < ITERATIONS; i++) {
        clock_gettime(CLOCK_MONOTONIC, &start);
        read(fd_read, buffer, 1);
        clock_gettime(CLOCK_MONOTONIC, &end);

        long latency_ns = (end.tv_sec - start.tv_sec) * 1e9 + (end.tv_nsec - start.tv_nsec);
        latencies[i] = latency_ns;
        total_latency_read += latency_ns;

        if (latency_ns < min_latency_read) {
            min_latency_read = latency_ns;
        }
    }

    long avg_latency_read = total_latency_read / ITERATIONS;
    printf("Minimum read latency: %ld ns\n", min_latency_read);
    printf("Average read latency: %ld ns\n", avg_latency_read);

    // Latenz für write messen
    for (int i = 0; i < ITERATIONS; i++) {
        clock_gettime(CLOCK_MONOTONIC, &start);
        write(fd_write, buffer, 1);
        clock_gettime(CLOCK_MONOTONIC, &end);

        long latency_ns = (end.tv_sec - start.tv_sec) * 1e9 + (end.tv_nsec - start.tv_nsec);
        latencies[i] = latency_ns;
        total_latency_write += latency_ns;

        if (latency_ns < min_latency_write) {
            min_latency_write = latency_ns;
        }
    }

    long avg_latency_write = total_latency_write / ITERATIONS;
    printf("Minimum write latency: %ld ns\n", min_latency_write);
    printf("Average write latency: %ld ns\n", avg_latency_write);

    // Latenz für getpid() messen
    for (int i = 0; i < ITERATIONS; i++) {
        clock_gettime(CLOCK_MONOTONIC, &start);
        getpid();  // Messung der Latenz von getpid()
        clock_gettime(CLOCK_MONOTONIC, &end);

        long latency_ns = (end.tv_sec - start.tv_sec) * 1e9 + (end.tv_nsec - start.tv_nsec);
        total_latency_pid += latency_ns;

        if (latency_ns < min_latency_pid) {
            min_latency_pid = latency_ns;
        }
    }

    long avg_latency_pid = total_latency_pid / ITERATIONS;
    printf("Minimum getpid latency: %ld ns\n", min_latency_pid);
    printf("Average getpid latency: %ld ns\n", avg_latency_pid);

    close(fd_read);
    close(fd_write);

    return 0;
}
