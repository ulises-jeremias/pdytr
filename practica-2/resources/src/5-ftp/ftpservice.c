/* Definition of the remote add and subtract procedure used by 
   simple RPC example 
   rpcgen will create a template for you that contains much of the code
   needed in this file is you give it the "-Ss" command line arg.
*/
#include <stddef.h>
#include <stdio.h>
#include <errno.h>
#include <string.h>
#include <inttypes.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <dirent.h>

#include <include/util.h>
#include <ftp.h>

/* Here is the actual remote procedure */
/* The return value of this procedure must be a pointer to int! */
/* we declare the variable result as static so we can return a 
   pointer to it */

int *
write_1_svc(ftp_wfile arg, struct svc_req *rqstp)
{
        char *name = arg.name;
        char *mode = arg.mode;
        int length = arg.data.data_len;
        uint64_t income_checksum = arg.checksum;

        char *buffer = (char *) malloc(1026);
        strncpy(buffer, arg.data.data_val, length);

        // Declare variables
	FILE *file;
        DIR *dir;
        static int result;
        char path[PATH_MAX];

        // Set path
        snprintf(path, PATH_MAX, "%s/%s", "store", name);
        
        printf("Path: %s\n\n", path);

        dir = opendir("store");
        if (dir)
        {
                closedir(dir);
        }
        else if (ENOENT == errno)
        {
                mkdir("store", 0777);
        }
        else
        {
                fprintf(stderr, "Error creating file '%s'\n", path);
                result = -1;
                return &result;
        }

        // Open file and check errors
        file = fopen(path, mode);
        if (file == NULL)
        {
                fprintf(stderr, "Error creating file '%s'\n", path);
                result = -1;
                return &result;
        }
    
        // Check checksum
        uint64_t checksum = djb2(buffer);
        if (income_checksum != checksum)
        {
                fprintf(stderr, "Error in checksum!!\nOriginal: %"PRIu64"\nOwn: %"PRIu64"\n", income_checksum, checksum);
        }

        // Write file
        result = fwrite(buffer, sizeof(char), length, file);

        fclose(file);
        printf("Storing %s...\n", path);

        return ((int*) &result);
}

ftp_file *
read_1_svc(ftp_req arg, struct svc_req *rqstp)
{
        char *name = arg.name;
	uint64_t pos = arg.pos;
	uint64_t bytes = arg.bytes;

        char path[PATH_MAX];

        // Set path
        snprintf(path, PATH_MAX, "%s/%s", "store", name);

        FILE *file;
        ftp_file *file_struct;

        file_struct = (ftp_file *) malloc(sizeof(ftp_file));
        
        if (bytes > 0 && bytes <= 1024)
        {
                file_struct->data.data_val = (char *) malloc(bytes);
        }
        else
        {
                file_struct->data.data_val = (char *) malloc(sizeof(char) * 1024);
                bytes = 1024;
        }

        file = fopen(path, "r");
        if (file == NULL)
        {
                fprintf(stderr, "Error opening file %s\n", path);
                file_struct->data.data_len = -1;
                return file_struct;
        }
        fseek(file, pos, SEEK_SET);
        
        file_struct->data.data_len = fread(file_struct->data.data_val, sizeof(char), bytes, file);
        file_struct->name = (char *) malloc(PATH_MAX);
        file_struct->name = strcpy(file_struct->name, name);
        file_struct->continue_reading = !feof(file);

        return file_struct;
}

char **
list_1_svc(ftp_lreq arg, struct svc_req *rqstp)
{
        DIR *dir;
        char **paths;
        paths = (char **) malloc(sizeof(char *));
        *paths = (char *) malloc(PATH_MAX);
        *paths = strcpy(*paths, "");
        struct dirent *dir_str;

        dir = opendir(arg.name);
        if (dir)
        {
                while ((dir_str = readdir(dir)) != NULL)
                {
                        if (strcmp(dir_str->d_name, ".") && strcmp(dir_str->d_name, ".."))
                        {
                                strcat(paths[0], dir_str->d_name);
                                strcat(*paths, "\t");
                                if (arg.all)
                                        strcat(*paths, "\n");
                        }
                }

                strcat(*paths, "\n");
                closedir(dir);
        }
        else
        {
                snprintf(*paths, PATH_MAX, "list: cannot access %s: No such directory", arg.name);
        }


        return paths;
}