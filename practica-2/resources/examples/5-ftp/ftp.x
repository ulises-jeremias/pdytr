#define VERSION_NUMBER 1

%#define DATA_SIZE UINT_MAX

struct ftp_file {
    string name<PATH_MAX>;
	opaque data<>;
	unsigned long checksum;
};

struct ftp_req {
    string name<PATH_MAX>;
    uint64_t pos;
    uint64_t bytes;
};

program FTP_PROG {
    version FTP_VERSION {
        ftp_file READ(ftp_req) = 1;
        int WRITE(ftp_file) = 2;
    } = VERSION_NUMBER;
} = 555555555;

#define FTP_PROG 555555555