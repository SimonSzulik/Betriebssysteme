# /******************************/#
# /*** Simon Szulik     1474315 */#
# /*** Betriebssysteme WS 24/25 */#
# /******************************/#
import socket
import time
import numpy as np
import matplotlib.pyplot as plt

# Server-Setup
HOST = "0.0.0.0"
PORT = 12345
latencies = []

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as server_socket:
    server_socket.bind((HOST, PORT))
    server_socket.listen(1)
    print("Server läuft und wartet auf Verbindungen...")

    conn, addr = server_socket.accept()
    with conn:
        print(f"Verbindung von {addr} hergestellt.")
        for _ in range(50):
            data = conn.recv(1024).decode()
            if not data:
                break

            send_time = float(data)
            recv_time = time.time()
            latency = recv_time - send_time
            latencies.append(latency)
            print(f"Latenz: {latency:.6f} Sekunden")

np_results = np.array(latencies)
mean_latency = np.mean(np_results)
std_dev_latency = np.std(np_results)
conf_interval = 1.96 * std_dev_latency / np.sqrt(len(np_results))

print(f"\nDurchschnittliche Latenz: {mean_latency:.6f} Sekunden")
print(f"Standardabweichung: {std_dev_latency:.6f} Sekunden")
print(f"95%-Konfidenzintervall: ±{conf_interval:.6f} Sekunden")

plt.switch_backend('Agg')

plt.hist(np_results, bins=10, alpha=0.7, color='blue', edgecolor='black')
plt.title("Verteilung der Latenzen")
plt.xlabel("Latenz (Sekunden)")
plt.ylabel("Häufigkeit")
plt.axvline(mean_latency, color='red', linestyle='dashed', linewidth=1, label=f"Durchschnitt: {mean_latency:.6f}")
plt.legend()
plt.grid(True)

plt.savefig('/app/Docker.png')