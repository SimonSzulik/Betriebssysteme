/******************************/
/*** Simon Szulik     1474315 */
/*** Betriebssysteme WS 24/25 */
/******************************/
#include <stdio.h>
#include <pthread.h>
#include <time.h>

#define NUM_THREADS 2
#define NUM_ITERATIONS 1000000

long context_switches = 0;  // Variable zum Zählen der Kontextwechsel

// Mutex zum Schutz der globalen Variable
pthread_mutex_t context_switch_mutex = PTHREAD_MUTEX_INITIALIZER;

void *thread_func(void *arg) {
    long i;
    for (i = 0; i < NUM_ITERATIONS; i++) {
        sched_yield();  // Erzwungener Kontextwechsel

        // Kontextwechsel zählen, threadsicher
        pthread_mutex_lock(&context_switch_mutex);
        context_switches++;
        pthread_mutex_unlock(&context_switch_mutex);
    }
    return NULL;
}

int main() {
    pthread_t threads[NUM_THREADS];
    struct timespec start, end;

    // Zeitmessung starten
    clock_gettime(CLOCK_MONOTONIC, &start);

    // Threads erstellen
    for (int i = 0; i < NUM_THREADS; i++) {
        pthread_create(&threads[i], NULL, thread_func, NULL);
    }

    // Warten auf Threads
    for (int i = 0; i < NUM_THREADS; i++) {
        pthread_join(threads[i], NULL);
    }

    // Zeitmessung beenden
    clock_gettime(CLOCK_MONOTONIC, &end);

    // Berechnung der Zeitdifferenz in Nanosekunden
    long elapsed_time = (end.tv_sec - start.tv_sec) * 1000000000 + (end.tv_nsec - start.tv_nsec);
    printf("Kontextwechsel-Dauer: %ld ns\n", elapsed_time);

    // Ausgabe der geschätzten Anzahl der Kontextwechsel
    printf("Geschätzte Anzahl der Kontextwechsel: %ld\n", context_switches);

    return 0;
}
