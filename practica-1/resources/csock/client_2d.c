#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <getopt.h>
#include <sys/time.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>

double dwalltime(void);
unsigned long djb2(unsigned char *str);
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

        set_verbose_mode(argc, argv, &verbose_flag);

        /* Instead of reporting ‘--verbose’
        and ‘--brief’ as they are encountered,
        we report the final status resulting from them. */
        if (verbose_flag)
                puts("verbose flag is set");

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

        double timetick = dwalltime();

        /* ENVIA UN MENSAJE AL SOCKET */
        n = write(sockfd, buffer, strlen(buffer));
        if (n < 0)
                error("ERROR writing to socket");

        //Envia el tamaño del dato
        size_t data_length = strlen(buffer);
        n = write(sockfd, &data_length, sizeof(data_length));

        //Envia el checksum
        unsigned long buffer_hash = djb2(buffer);
        n = write(sockfd, &buffer_hash, sizeof(buffer_hash));

        bzero(buffer, BUFFER_SIZE);
        
        /* ESPERA RECIBIR UNA RESPUESTA */
        n = read(sockfd, buffer, BUFFER_SIZE - 1);
        if (n < 0)
                error("ERROR reading from socket");

        if (verbose_flag) {
                printf("%s\n", buffer);
                printf("Time in seconds: %fs\n", dwalltime() - timetick);
        } else {
                printf("%f\n", dwalltime() - timetick);
        }

        return 0;
}


unsigned long
djb2(unsigned char *str)
{
        unsigned long hash = 5381;
        int c;

        while (c = *str++)
                hash = ((hash << 5) + hash) + c; /* hash * 33 + c */

        return hash;
}


double
dwalltime(void)
{
        double sec;
        struct timeval tv;

        gettimeofday(&tv, NULL);
        sec = tv.tv_sec + tv.tv_usec/1000000.0;
        return sec;
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
