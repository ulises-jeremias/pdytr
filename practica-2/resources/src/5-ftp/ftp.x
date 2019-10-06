#define VERSION_NUMBER 1

%#define DATA_SIZE UINT_MAX

struct ftp_file {
    string name<PATH_MAX>;
	opaque data<>;
    int continue_reading;
};

struct ftp_wfile {
    string name<PATH_MAX>;
    string mode<>;
	opaque data<>;
	uint64_t checksum;
};

struct ftp_lreq {
    string name<PATH_MAX>;
    int all;
};

struct ftp_req {
    string name<PATH_MAX>;
    uint64_t pos;
    uint64_t bytes;
};

program FTP_PROG {
    version FTP_VERSION {
        ftp_file READ(ftp_req) = 1;
        int WRITE(ftp_wfile) = 2;
        string LIST(ftp_lreq) = 3;
    } = VERSION_NUMBER;
} = 555555555;

#define FTP_PROG 555555555
