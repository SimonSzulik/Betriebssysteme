#/******************************/#
#/*** Simon Szulik     1474315 */#
#/*** Betriebssysteme WS 24/25 */#
#/******************************/#
import zmq
import threading
import multiprocessing
import time
import numpy as np
import matplotlib.pyplot as plt

def zmq_publisher(context, port, messages, timings, index):
    socket = context.socket(zmq.PUB)
    socket.bind(f"tcp://127.0.0.1:{port}")
    time.sleep(0.1)

    start_time = time.perf_counter()
    socket.send_string(messages[index])
    timings[index] = time.perf_counter() - start_time
    socket.close()

def zmq_subscriber(context, port, messages, timings, index):
    socket = context.socket(zmq.SUB)
    socket.connect(f"tcp://127.0.0.1:{port}")
    socket.setsockopt_string(zmq.SUBSCRIBE, "")

    start_time = time.perf_counter()
    _ = socket.recv_string()
    timings[index] += time.perf_counter() - start_time
    socket.close()

def run_zmq_in_process(runs=50):
    context = zmq.Context()
    port = 5555
    timings = np.zeros(runs)
    messages = [f"Message {i}" for i in range(runs)]

    for i in range(runs):
        publisher_thread = threading.Thread(target=zmq_publisher, args=(context, port, messages, timings, i))
        subscriber_thread = threading.Thread(target=zmq_subscriber, args=(context, port, messages, timings, i))

        publisher_thread.start()
        subscriber_thread.start()

        publisher_thread.join()
        subscriber_thread.join()

    context.term()
    return timings

def zmq_publisher_process(port, messages, index, timing_queue):
    context = zmq.Context()
    socket = context.socket(zmq.PUB)
    socket.bind(f"tcp://127.0.0.1:{port}")
    time.sleep(0.1)

    start_time = time.perf_counter()
    socket.send_string(messages[index])
    timing_queue.put(time.perf_counter() - start_time)
    socket.close()
    context.term()

def zmq_subscriber_process(port, timing_queue):
    context = zmq.Context()
    socket = context.socket(zmq.SUB)
    socket.connect(f"tcp://127.0.0.1:{port}")
    socket.setsockopt_string(zmq.SUBSCRIBE, "")

    start_time = time.perf_counter()
    _ = socket.recv_string()
    timing_queue.put(time.perf_counter() - start_time)
    socket.close()
    context.term()

def run_zmq_between_processes(runs=50):
    port = 5556
    timings = []

    for i in range(runs):
        messages = [f"Message {i}" for i in range(runs)]
        timing_queue = multiprocessing.Queue()

        pub_proc = multiprocessing.Process(target=zmq_publisher_process, args=(port, messages, i, timing_queue))
        sub_proc = multiprocessing.Process(target=zmq_subscriber_process, args=(port, timing_queue))

        pub_proc.start()
        sub_proc.start()

        pub_proc.join()
        sub_proc.join()

        timings.append(sum([timing_queue.get() for _ in range(2)]))

    return np.array(timings)

def analyze_and_plot(results, title):
    mean_latency = np.mean(results)
    std_dev_latency = np.std(results)
    conf_interval = 1.96 * std_dev_latency / np.sqrt(len(results))

    print(f"{title}")
    print(f"Durchschnittliche Latenz: {mean_latency:.6f} Sekunden")
    print(f"Standardabweichung: {std_dev_latency:.6f} Sekunden")
    print(f"95%-Konfidenzintervall: ±{conf_interval:.6f} Sekunden")

    plt.hist(results, bins=10, alpha=0.7, color='blue', edgecolor='black')
    plt.title(title)
    plt.xlabel("Latenz (Sekunden)")
    plt.ylabel("Häufigkeit")
    plt.axvline(mean_latency, color='red', linestyle='dashed', linewidth=1, label=f"Durchschnitt: {mean_latency:.6f}")
    plt.legend()
    plt.grid(True)
    plt.show()

results_in_process = run_zmq_in_process(50)
results_between_processes = run_zmq_between_processes(50)

analyze_and_plot(results_in_process, "ZeroMQ: Innerhalb eines Prozesses")
analyze_and_plot(results_between_processes, "ZeroMQ: Zwischen Prozessen")
