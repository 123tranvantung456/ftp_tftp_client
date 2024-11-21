package com.javaweb.client.system.enums;

public enum CommandOfClient {
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
    LCD,
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
    REMOTEHELP;
}
