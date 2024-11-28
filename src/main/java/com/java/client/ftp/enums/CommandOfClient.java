package com.java.client.ftp.enums;

public enum CommandOfClient {
    //1. Standard FTP of CMD

    // file
    PUT,
    MPUT,
    GET,
    MGET,
    DELETE,
    MDELETE,
    SEND,
    RECV,
    APPEND,

    // folder
    MKDIR,
    RMDIR,
    CD,
    PWD,

    // file and folder
    DIR,
    MDIR,
    LS,
    MLS,
    RENAME,

    // local command
    ACTIVE,
    PASSIVE,
    LCD,
    DEBUG,
    LITERAL,
    PROMPT,
    GLOB,
    TRACE,
    VERBOSE,
    STATUS,
    HASH,
    HELP,

    // connection
    OPEN,
    CLOSE,
    DISCONNECT,
    BYE,
    QUIT,


    // transfer data
    ASCII,
    BINARY,
    PORT,
    EPRT,
    EPSV,

    // extend
    QUOTE,

    //help
    REMOTEHELP,

    //2. Extended FTP
    FTP,
    LOGIN
    ;
}
