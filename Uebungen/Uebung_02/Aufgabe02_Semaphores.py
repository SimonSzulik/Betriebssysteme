#/******************************/#
#/*** Simon Szulik     1474315 */#
#/*** Betriebssysteme WS 24/25 */#
#/******************************/#
import multiprocessing
import time
import ctypes
import numpy as np
import matplotlib.pyplot as plt

def thread_a(semaphore, shared_mem, start_event, end_event, timings, index):
    start_event.wait()
    start_time = time.perf_counter()
    shared_mem.value = 42
    semaphore.release()
    end_event.wait()
    end_time = time.perf_counter()
    timings[index] = end_time - start_time

def thread_b(semaphore, shared_mem, start_event, end_event):
    start_event.wait()
    semaphore.acquire()
    _ = shared_mem.value
    end_event.set()

def run_experiment_semaphore(runs=50):
    timings = multiprocessing.Array(ctypes.c_double, runs)

    for i in range(runs):
        semaphore = multiprocessing.Semaphore(0)
        shared_mem = multiprocessing.Value(ctypes.c_int, 0)
        start_event = multiprocessing.Event()
        end_event = multiprocessing.Event()

        proc_a = multiprocessing.Process(target=thread_a, args=(semaphore, shared_mem, start_event, end_event, timings, i))
        proc_b = multiprocessing.Process(target=thread_b, args=(semaphore, shared_mem, start_event, end_event))

        proc_a.start()
        proc_b.start()

        start_event.set()
        proc_a.join()
        proc_b.join()

    return list(timings)

results_semaphore = run_experiment_semaphore(50)

np_results_semaphore = np.array(results_semaphore)
mean_latency = np.mean(np_results_semaphore)
std_dev_latency = np.std(np_results_semaphore)
conf_interval = 1.96 * std_dev_latency / np.sqrt(len(np_results_semaphore))

print(f"Durchschnittliche Latenz (Semaphore): {mean_latency:.6f} Sekunden")
print(f"Standardabweichung: {std_dev_latency:.6f} Sekunden")
print(f"95%-Konfidenzintervall: ±{conf_interval:.6f} Sekunden")

plt.hist(np_results_semaphore, bins=10, alpha=0.7, color='green', edgecolor='black')
plt.title("Verteilung der Latenzen (Semaphore)")
plt.xlabel("Latenz (Sekunden)")
plt.ylabel("Häufigkeit")
plt.axvline(mean_latency, color='red', linestyle='dashed', linewidth=1, label=f"Durchschnitt: {mean_latency:.6f}")
plt.legend()
plt.grid(True)
plt.show()
