#include <stdlib.h>
#include <stddef.h>
#include <stdio.h>
#include <string.h>
#include <ctype.h>
#include <getopt.h>

#include <include/command.h>
#include <include/util.h>
#include <ftp.h>

int
main(int argc, char *argv[])
{
        static int verbose_flag;
        static int all_flag;
        static int bytes_flag = 0;

        uint64_t bytes = 0, initial_pos = 0;

        CLIENT *clnt;
        int i;

        // Variables for ftp command
        char src[PATH_MAX];
        char dest[PATH_MAX];
        char host[PATH_MAX];

        strcpy(src, ""); strcpy(dest, ""); strcpy(host, "");

        static command_t commands[] = {
                {
                        .name="write",
                        .description="Add a file from --src to --dest",
                        .handle=&ftp_write,
                },
                {
                        .name="read",
                        .description="Store a file from --src to --dest",
                        .handle=&ftp_read,
                },
                {
                        .name="list",
                        .description="List all files from --src",
                        .handle=&ftp_list,
                }
        };

        if (argc < 2)
        {
                fprintf(stderr,"Usage: %s\n", argv[0]);
                for (i = 0; i < sizeof(commands)/sizeof(command_t); i++)
                {
                        fprintf(stderr, "\t- %s: %s\n", commands[i].name, commands[i].description);
                }
                exit(0);
        }

        int command = -1;
        for (i = 0; i < sizeof(commands)/sizeof(command_t); i++)
        {
                if (!strcmp(commands[i].name, argv[1]))
                {
                        command = i;
                        break;
                }
        }

        if (command == -1)
        {
                fprintf(stderr, "Invalid command");
                exit(1);
        }

        ini_params(argc, argv, &verbose_flag, &all_flag, &bytes_flag, host, src, dest, &bytes, &initial_pos);

        /* Instead of reporting ‘--verbose’
        and ‘--brief’ as they are encountered,
        we report the final status resulting from them. */
        if (verbose_flag)
                puts("verbose flag is set");

        if (!strlen(src))
        {
                fprintf(stderr, "Specify a --src path\n");
                exit(1);
        }

        if (!strlen(dest))
        {
                fprintf(stderr, "--dest setted to tmp1\n");
                strcpy(dest, "tmp1");
        }

        // Config RPC
        /* Create a CLIENT data structure that reference the RPC
        procedure FTP_PROG, version FTP_VERSION running on the
        host specified by the 1st command line arg. */
        if(!strlen(host))
        {
                strcpy(host, "localhost");
        }
        clnt = clnt_create(host, FTP_PROG, FTP_VERSION, "tcp");

        /* Make sure the create worked */
        if (clnt == (CLIENT *) NULL)
        {
                clnt_pcreateerror(argv[1]);
                exit(1);
        }

        if (verbose_flag)
                printf("Connecting to server with host %s\n", host);

        ftp_param_t param = {
                .verbose_flag = verbose_flag,
                .all_flag = all_flag,
                .bytes_flag = bytes_flag,
                .src = src,
                .dest = dest,
                .bytes = bytes,
                .initial_pos = initial_pos
        };

        commands[command].handle(clnt, &param);

        return 0;
}