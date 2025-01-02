# /******************************/#
# /*** Simon Szulik     1474315 */#
# /*** Betriebssysteme WS 24/25 */#
# /******************************/#
import socket
import time

HOST = "server-instance"
PORT = 12345

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as client_socket:
    client_socket.connect((HOST, PORT))
    print("Verbindung zum Server hergestellt.")

    for _ in range(50):
        send_time = time.time()
        client_socket.sendall(str(send_time).encode())
        time.sleep(0.1)
