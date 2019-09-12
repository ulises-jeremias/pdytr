#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>

/*
 * En tiempo de compilación se puede definir esta macro con un valor numérico >= 0
 *
 */
#ifndef BUFFER_SIZE
#define BUFFER_SIZE 1000
#endif

/*
 * Error
 * -- Muestra un mensaje de error y finaliza la ejecución del programa.
 *
 * @param char * msg, Mensaje a mostrar
 *
 * @return void
 *
 */
void
error(char *msg)
{
        perror(msg);
        exit(0);
}

int
main(int argc, char *argv[])
{
        int sockfd, portno, n;
        struct sockaddr_in serv_addr;
        struct hostent *server;

        char buffer[BUFFER_SIZE];

        if (argc < 3)
        {
                fprintf(stderr, "usage %s hostname port\n", argv[0]);
                exit(0);
        }

        /* TOMA EL NUMERO DE PUERTO DE LOS ARGUMENTOS */
        portno = atoi(argv[2]);

        /* CREA EL FILE DESCRIPTOR DEL SOCKET PARA LA CONEXION */
        sockfd = socket(AF_INET, SOCK_STREAM, 0);
        /* AF_INET - FAMILIA DEL PROTOCOLO - IPV4 PROTOCOLS INTERNET */
        /* SOCK_STREAM - TIPO DE SOCKET */

        if (sockfd < 0)
                error("ERROR opening socket");

        /* TOMA LA DIRECCION DEL SERVER DE LOS ARGUMENTOS */
        server = gethostbyname(argv[1]);
        if (server == NULL)
        {
                fprintf(stderr, "ERROR, no such host\n");
                exit(0);
        }

        bzero((char *) &serv_addr, sizeof(serv_addr));
        serv_addr.sin_family = AF_INET;

        /* COPIA LA DIRECCION IP Y EL PUERTO DEL SERVIDOR A LA ESTRUCTURA DEL SOCKET */
        bcopy((char *) server->h_addr,
              (char *) &serv_addr.sin_addr.s_addr,
              server->h_length);
        serv_addr.sin_port = htons(portno);

        /* DESCRIPTOR - DIRECCION - TAMAÑO DIRECCION */
        if (connect(sockfd, (const struct sockaddr *) &serv_addr, sizeof(serv_addr)) < 0)
                error("ERROR connecting");

        *buffer = 'y';
        memset((buffer + 1), 'e', BUFFER_SIZE - 2);

        /* ENVIA UN MENSAJE AL SOCKET */
        n = write(sockfd, buffer, strlen(buffer));
        if (n < 0)
                error("ERROR writing to socket");
        bzero(buffer, BUFFER_SIZE);

        /* ESPERA RECIBIR UNA RESPUESTA */
        n = read(sockfd, buffer, BUFFER_SIZE - 1);
        if (n < 0)
                error("ERROR reading from socket");

        printf("%s\n", buffer);

        return 0;
}
