/* A simple server in the internet domain using TCP
   The port number is passed as an argument */
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <getopt.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>


void set_verbose_mode(int argc, char *argv[], int *verbose_flag_ptr);

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
        static int verbose_flag;

        int sockfd, newsockfd, portno, clilen;
        char buffer[BUFFER_SIZE];
        struct sockaddr_in serv_addr, cli_addr;
        int n;

        if (argc < 2)
        {
                fprintf(stderr, "ERROR, no port provided\n");
                exit(1);
        }

        /* CREA EL FILE DESCRIPTOR DEL SOCKET PARA LA CONEXION */
        sockfd = socket(AF_INET, SOCK_STREAM, 0);
        /* AF_INET - FAMILIA DEL PROTOCOLO - IPV4 PROTOCOLS INTERNET */
        /* SOCK_STREAM - TIPO DE SOCKET */

        if (sockfd < 0)
                error("ERROR opening socket");

        bzero((char *) &serv_addr, sizeof(serv_addr));
        /* ASIGNA EL PUERTO PASADO POR ARGUMENTO */
        /* ASIGNA LA IP EN DONDE ESCUCHA (SU PROPIA IP) */
        portno = atoi(argv[1]);
        serv_addr.sin_family = AF_INET;
        serv_addr.sin_addr.s_addr = INADDR_ANY;
        serv_addr.sin_port = htons(portno);

        set_verbose_mode(argc, argv, &verbose_flag);

        /* Instead of reporting ‘--verbose’
        and ‘--brief’ as they are encountered,
        we report the final status resulting from them. */
        if (verbose_flag)
                puts("verbose flag is set");

        /* VINCULA EL FILE DESCRIPTOR CON LA DIRECCION Y EL PUERTO */
        if (bind(sockfd, (struct sockaddr *) &serv_addr, sizeof(serv_addr)) < 0)
                error("ERROR on binding");

        /* SETEA LA CANTIDAD QUE PUEDEN ESPERAR MIENTRAS SE MANEJA UNA CONEXION */
        listen(sockfd, 5);

        /* SE BLOQUEA A ESPERAR UNA CONEXION */
        clilen = sizeof(cli_addr);
        newsockfd = accept(sockfd,
                           (struct sockaddr *) &cli_addr,
                           &clilen);

        /* DEVUELVE UN NUEVO DESCRIPTOR POR EL CUAL SE VAN A REALIZAR LAS COMUNICACIONES */
        if (newsockfd < 0)
                error("ERROR on accept");

        bzero(buffer, BUFFER_SIZE);

        /* LEE EL MENSAJE DEL CLIENTE */
        n = read(newsockfd, buffer, BUFFER_SIZE - 1);
        if (n < 0)
                error("ERROR reading from socket");
        
        printf("Receibed message with %d characters\n", n);

        if (verbose_flag) {
                printf("Message content:\n%s\n", buffer);
        }


        /* RESPONDE AL CLIENTE */
        n = write(newsockfd, "I got your message", 18);
        if (n < 0) error("ERROR writing to socket");

        return 0;
}


void
set_verbose_mode(int argc, char *argv[], int *verbose_flag_ptr)
{
        static int verbose_flag;
        int c;

        while (1)
        {
                static struct option long_options[] =
                {
                        {"verbose", no_argument, &verbose_flag, 1},
                        {"brief", no_argument, &verbose_flag, 0},
                        {0, 0, 0, 0}
                };
                /* getopt_long stores the option index here. */
                int option_index = 0;

                c = getopt_long(argc, argv, "vb",
                                long_options, &option_index);

                /* Detect the end of the options. */
                if (c == -1)
                        break;

                switch (c)
                {
                case 0:
                        /* If this option set a flag, do nothing else now. */
                        if (long_options[option_index].flag != 0)
                                break;
                        printf("option %s", long_options[option_index].name);
                        if (optarg)
                                printf(" with arg %s", optarg);
                        printf("\n");
                        break;
                case 'v':
                        verbose_flag = 1;
                        break;
                case 'b':
                        verbose_flag = 0;
                        break;
                case '?':
                        /* getopt_long already printed an error message. */
                        break;
                default:
                        exit(0);
                }
        }

        *verbose_flag_ptr = verbose_flag;
}
