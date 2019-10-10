#include <stdio.h>
#include <stdlib.h>
#include <stddef.h>
#include <string.h>
#include <inttypes.h>

#include <include/command.h>
#include <include/util.h>
#include <ftp.h>

/* Wrapper function takes care of calling the RPC procedure */
int
ftp_read(CLIENT *clnt, ftp_param_t *param)
{
        char *src = param->src;
        char *dest = param->dest;
        uint64_t bytes = param->bytes;
        uint64_t initial_pos = param->initial_pos;
        int read_all = !param->bytes_flag;

        FILE* file;
        ftp_file *ftp_file_data;
        ftp_file_data = (ftp_file *) malloc(sizeof(ftp_file));
        
        ftp_file_data->continue_reading = 1;

        ftp_req req = {
                .name = src,
                .pos = initial_pos,
                .bytes = bytes,
        };

        file = fopen(dest, "w");

        if (file == NULL)
        {
                fprintf(stderr, "Error opening file %s\n", src);
                exit(1);
        }

        printf("Storing file %s...\n", dest);
        while(ftp_file_data->continue_reading && (bytes > 0 || read_all))
        {

                ftp_file_data = read_1(req, clnt);
                if (ftp_file_data == NULL)
                {
                        fprintf(stderr, "Trouble calling remote procedure\n");
                        exit(0);
                }

                fwrite(ftp_file_data->data.data_val, sizeof(char), ftp_file_data->data.data_len, file);
                req.pos = req.pos + ftp_file_data->data.data_len;
                
                if (!read_all)
                {
                        req.bytes = req.bytes - ftp_file_data->data.data_len;
                        bytes = req.bytes;
                }
        }
        printf("Done.\n");

        fclose(file);

        return 1;
}

/* Wrapper function takes care of calling the RPC procedure */
int
ftp_write(CLIENT *clnt, ftp_param_t *param)
{
        int verbose = param->verbose_flag;
        int bytes_flag = param->bytes_flag;
        char *src = param->src;
        char *dest = param->dest;
        uint64_t bytes = param->bytes;
        uint64_t initial_pos = param->initial_pos;

        if (verbose)
                printf("src: %s - dest: %s - bytes: %lu - pos: %lu\n", src, dest, bytes, initial_pos);

        FILE* file;
        int *result;

        file = fopen(src, "r");
        if (file == NULL)
        {
                fprintf(stderr, "Error opening file %s\n", src);
                exit(1);
        }

        uint64_t bytes_read, total_bytes_read = 0;
        char *buffer = (char *) malloc(1026);

        while ((bytes_read = fread(buffer, sizeof(char), 1024, file)) > 0 && (total_bytes_read <= bytes || !bytes_flag))
        {
                ftp_wfile ftp_file_data;

                ftp_file_data.data.data_val = (char *) malloc(bytes_read);

                /* Gather everything into a single data structure to send to the server */
                ftp_file_data.data.data_val = buffer;
                ftp_file_data.data.data_len = bytes_read;
                ftp_file_data.name = (char *) malloc(PATH_MAX);
                ftp_file_data.name = strcpy(ftp_file_data.name, dest);
                ftp_file_data.mode = total_bytes_read ? "a+" : "w";
                ftp_file_data.checksum = djb2(ftp_file_data.data.data_val);

                if (verbose)
                        printf("dest: %s\ndata: %s\nsize: %d\nchecksum: %" PRIu64 "\n",
                               ftp_file_data.name,
                               ftp_file_data.data.data_val,
                               ftp_file_data.data.data_len,
                               ftp_file_data.checksum);

                /* Call the client stub created by rpcgen */
                result = write_1(ftp_file_data, clnt);
                if (result == NULL)
                {
                        fprintf(stderr, "Trouble calling remote procedure\n");
                        fclose(file);
                        exit(0);
                }
                else if (*result == -1)
                {
                        fprintf(stderr, "Error creating file 'store/%s' in server\n", dest);
                }

                total_bytes_read += bytes_read;
        }

        fclose(file);

        printf("File stored at 'store/%s'\n", dest);

        return(*result);
}

/* Wrapper function takes care of calling the RPC procedure */
int
ftp_list(CLIENT *clnt, ftp_param_t *param)
{
        int all = param->all_flag;
        char *src = param->src;

        ftp_lreq req = {
                .name = src,
                .all = all,
        };

        char **paths;
        paths = list_1(req, clnt);

        printf("%s", paths[0]);

        return 1;
}
