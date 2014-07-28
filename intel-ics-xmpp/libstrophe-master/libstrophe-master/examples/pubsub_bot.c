/* bot.c
** libstrophe XMPP client library -- basic usage example
**
** Copyright (C) 2005-2009 Collecta, Inc. 
**
**  This software is provided AS-IS with no warranty, either express
**  or implied.
**
**  This software is distributed under license and may not be copied,
**  modified or distributed except as expressly authorized under the
**  terms of the license contained in the file LICENSE.txt in this
**  distribution.
*/

/* simple bot example
**  
** This example was provided by Matthew Wild <mwild1@gmail.com>.
**
** This bot responds to basic messages and iq version requests.
*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

#include <strophe.h>
#include "../src/common.h"

char xmpp_pub_msg[20];
char xmpp_sub_msg[10]="1,d,3";

int xmpp_debug_mode = 1;

int pubsub_handler(xmpp_conn_t * const conn, xmpp_stanza_t * const stanza, void * const userdata)
{
	xmpp_stanza_t *item;
	int i;
	char *buf;
	char *attr_buf[20];
	int attr_cnt;
	xmpp_ctx_t *ctx = (xmpp_ctx_t*)userdata;
    
    printf( "\n\nXMPP PUBSUB handler!\n" );

	item=stanza;
    
    do{
        buf=xmpp_stanza_get_name(item);
        if(xmpp_debug_mode) printf( "stanza name: %s\n", buf);
        attr_cnt=xmpp_stanza_get_attribute_count(item);
        if(xmpp_debug_mode) printf( "  stanza attribute cnt=%d\n",attr_cnt );
        if(attr_cnt>0)
            xmpp_stanza_get_attributes(item, &attr_buf, 20 );
    
        for(i=0; i<(attr_cnt*2); i+=2)
        {
            if(xmpp_debug_mode) printf( "  Attribute: %s ",attr_buf[i] );
            if(xmpp_debug_mode) printf( "  Value: %s\n",attr_buf[i+1] );
        }
        if(xmpp_debug_mode) printf("efefe:%s\n", item);
        if(strcmp(xmpp_stanza_get_name(item),"(null)")==0){
            return 0;
        }
        else{
            if( strcmp(xmpp_stanza_get_name(item),"UUID")==0 ) break;
            item=xmpp_stanza_get_children(item);

        }
    } while (item != NULL );
	
    if(xmpp_debug_mode) printf( "  %s: %s \n",xmpp_stanza_get_name(item), xmpp_stanza_get_text(item));
    if((item=xmpp_stanza_get_next(item)) == NULL){
        return 0;
    }

    if(xmpp_debug_mode) printf( "%s: %s \n",xmpp_stanza_get_name(item), xmpp_stanza_get_text(item) );
    if((item=xmpp_stanza_get_next(item)) == NULL){
        return 0;
    }
    
    if(xmpp_debug_mode) printf( "%s: %s \n",xmpp_stanza_get_name(item), xmpp_stanza_get_text(item));
    strcpy(xmpp_sub_msg, xmpp_stanza_get_text(item));
    if((item=xmpp_stanza_get_next(item)) == NULL){
        return 0;
    }
    
    if(xmpp_debug_mode) printf( "%s: %s \n",xmpp_stanza_get_name(item), xmpp_stanza_get_text(item));
    if((item=xmpp_stanza_get_next(item)) == NULL){
        return 0;
    }
    
    if(xmpp_debug_mode) printf( "%s: %s \n",xmpp_stanza_get_name(item), xmpp_stanza_get_text(item));
    if((item=xmpp_stanza_get_next(item)) == NULL){
        return 0;
    }
    
    if(xmpp_debug_mode) printf( "%s: %s \n",xmpp_stanza_get_name(item), xmpp_stanza_get_text(item));
    if((item=xmpp_stanza_get_next(item)) == NULL){
        return 0;
    }
    if(xmpp_debug_mode) printf( "%s: %s \n",xmpp_stanza_get_name(item), xmpp_stanza_get_text(item));
    
    if(xmpp_debug_mode) printf( "done.\n" );

    return 1;
}


void publishMessage(xmpp_conn_t * const conn, int id, int uuid, const char *type, int value, const char *loc, float confid)
{
    time_t nowtime;
    struct timeval curtv;
    struct tm *nowtm;
    char tmbuf[64];
    char to_string[64];
    char from_string[64];
    
    strcpy(to_string, xmpp_conn_get_pass(conn));
    strcpy(from_string, xmpp_conn_get_pass(conn));
    strcat(from_string, "@wukong.ccc.ntu.edu.tw");
    strcat(to_string, "@pubsub.wukong.ccc.ntu.edu.tw");
    if(xmpp_debug_mode) printf("1from_string: %s\n", from_string);
    if(xmpp_debug_mode) printf("1to_string: %s\n", to_string);
    
    gettimeofday(&curtv, NULL);
    nowtime = curtv.tv_sec;
    nowtm = localtime(&nowtime);
    strftime(tmbuf, sizeof tmbuf, "%Y-%m-%d %H:%M:%S", nowtm);
    
    xmpp_send_raw_string(conn,
        "<iq "     \
        "type=\"set\" "			\
        "from=\"%s\" "			\
        "to=\"%s\">"			\
        "<pubsub xmlns=\"http://jabber.org/protocol/pubsub\">"	\
        "<publish node=\"/Device/type/profile/IR\">"				\
        "<item id=\"%d\">"   \
        "<message xmlns=\"pubsub:test:message\">"   \
        "<body>"    \
        "<context>"    \
        "<UUID>%d</UUID>"   \
        "<type>%s</type>"  \
        "<value>%s</value>"    \
        "<location>%s</location>"  \
        "<confidence>%0.2f</confidence>"  \
        "<timestamp>%s</timestamp>"    \
        "<reference>%s</reference>" \
        "</context>"    \
        "</body></message></item></publish></pubsub></iq>", from_string, to_string, id, uuid, type, xmpp_pub_msg, loc, confid, tmbuf, "wukong");

    
}

void sendSubscribe(xmpp_conn_t * const conn, xmpp_ctx_t *ctx){
    xmpp_stanza_t *iq, *pubsub, *subscribeStanza;
    char to_string[64];
    char from_string[64];
    
    strcpy(to_string, xmpp_conn_get_pass(conn));
    strcat(to_string, "@pubsub.wukong.ccc.ntu.edu.tw");
    if(xmpp_debug_mode) printf("2to_string: %s\n", to_string);
    strcpy(from_string, xmpp_conn_get_pass(conn));
    strcat(from_string, "@wukong.ccc.ntu.edu.tw");
    if(xmpp_debug_mode) printf("2from_string: %s\n", from_string);
    
    /* create iq stanza for request */
    iq = xmpp_stanza_new(ctx);
    xmpp_stanza_set_name(iq, "iq");
    xmpp_stanza_set_type(iq, "set");
    xmpp_stanza_set_attribute(iq, "to", to_string);
    
    pubsub = xmpp_stanza_new(ctx);
    xmpp_stanza_set_name(pubsub, "pubsub");
    
    xmpp_stanza_set_ns(pubsub, "http://jabber.org/protocol/pubsub");
    
    subscribeStanza = xmpp_stanza_new(ctx);
    xmpp_stanza_set_name(subscribeStanza, "subscribe");
    xmpp_stanza_set_attribute(subscribeStanza, "node", "/Device/type/profile/feedback");
    xmpp_stanza_set_attribute(subscribeStanza, "jid", from_string);
    
    xmpp_stanza_add_child(pubsub, subscribeStanza);
    xmpp_stanza_add_child(iq, pubsub);
    
    /* we can release the stanza since it belongs to iq now */
    xmpp_stanza_release(subscribeStanza);
    xmpp_stanza_release(pubsub);
    
    /* set up reply handler */
    xmpp_handler_add(conn,pubsub_handler, NULL,"message", NULL, ctx);
    
    /* send out the stanza */
    xmpp_send(conn, iq);
    
    /* release the stanza */
    xmpp_stanza_release(iq);

}

/* define a handler for connection events */
void conn_handler(xmpp_conn_t * const conn, const xmpp_conn_event_t status,
		  const int error, xmpp_stream_error_t * const stream_error,
		  void * const userdata)
{
    xmpp_ctx_t *ctx = (xmpp_ctx_t *)userdata;
    
    if (status == XMPP_CONN_CONNECT) {
        xmpp_stanza_t* pres;
        fprintf(stderr, "DEBUG: connected....\n");
        xmpp_handler_add(conn,pubsub_handler, NULL,"message", NULL, ctx);
        
        /* Send initial <presence/> so that we appear online to contacts */
        pres = xmpp_stanza_new(ctx);
        xmpp_stanza_set_name(pres, "presence");
        xmpp_send(conn, pres);
        xmpp_stanza_release(pres);
        
        publishMessage(conn,22, 99, "LED_LIB", 12, "kitchen", 0.99);

        sendSubscribe(conn, ctx);
    }
    else {
        fprintf(stderr, "DEBUG: disconnected\n");
        xmpp_stop(ctx);
    }
}

long timevaldiff(struct timeval *starttime, struct timeval *finishtime)
{
    long msec;
    msec=(finishtime->tv_sec-starttime->tv_sec)*1000;
    msec+=(finishtime->tv_usec-starttime->tv_usec)/1000;
    return msec;
}

int main(int argc, char **argv)
{
    xmpp_ctx_t *ctx;
    xmpp_conn_t *conn;
    xmpp_log_t *log;
    char *jid, *pass;
    char expire_time_char[10];
    struct timeval start, finish;
    long msec=0, expire_time = 1000;
    FILE *xmpp_pub_file, *xmpp_sub_file;
    int iChar=0;
    
    //////////////////////////////
    //add these to the function doing setup stuffs
    //jid = "sub@192.168.0.103";
    //pass = "sub";
    if(argc < 4)
        return 0;
    jid = argv[1];
    pass = argv[2];
    strcpy(expire_time_char,argv[4]);
    //host = "140.112.170.26";
    
    if(argc == 6){
        xmpp_debug_mode = argv[5][0]  -'0';
        printf("xmpp_debug_mode: %d\n", xmpp_debug_mode);
    }
    
    if(argc >= 5){
        expire_time = 0;
        for(iChar=0;iChar<10;iChar++){
            if(expire_time_char[iChar]>='0' && expire_time_char[iChar]<='9'){
                expire_time = expire_time*10 + (expire_time_char[iChar] - '0');
            }
            else{
                break;
            }
        }
        if(xmpp_debug_mode) printf("expire_time: %d\n", expire_time);
    }
    
    
    
    // read the sensor input
    //printf("read file\n");
    xmpp_pub_file = fopen("/galileo/xmpp_pub.txt","r");
    
    if(xmpp_pub_file != NULL){
        fread(xmpp_pub_msg, sizeof(char), 19, xmpp_pub_file);
        fclose(xmpp_pub_file);
        printf("pub: %s\n",xmpp_pub_msg);
    }
    else{
        printf("no /galileo/xmpp_pub.txt\n");
        return 0;
    }
    
    /* init library */
    //printf("init library\n");
    xmpp_initialize();
    
    /* create a context */
    //log = xmpp_get_default_logger(XMPP_LEVEL_ERROR); /* pass NULL instead to silence output */
    if(xmpp_debug_mode)
        log = xmpp_get_default_logger(XMPP_LEVEL_DEBUG);
    else
        log = xmpp_get_default_logger(XMPP_LEVEL_ERROR);
    ctx = xmpp_ctx_new(NULL, log);
    
    /* create a connection */
    //printf("create a connection\n");
    conn = xmpp_conn_new(ctx);
    
    /* setup authentication information */
    //printf("setup authentication information\n");
    xmpp_conn_set_jid(conn, jid);
    xmpp_conn_set_pass(conn, pass);
    
    //printf("auth_handle_open\n");
    auth_handle_open(conn);

    /* initiate connection */
    //printf("initiate connection\n");
    xmpp_connect_client(conn, argv[3], 5222, conn_handler, ctx);
    
    xmpp_conn_disable_tls(conn);
    /* enter the event loop -
       our connect handler will trigger an exit */
    //xmpp_run(ctx);
    ctx->loop_status = XMPP_LOOP_RUNNING;
    //End: the function doing setup stuffs
    //////////////////////////////
    
    printf("jid: %s, ip: %s, password: %s, expire_time: %d, debug_mode: %d\n", jid, argv[3], pass, expire_time, xmpp_debug_mode);
    
    //////////////////////////////
    // add these to the update function
    // change 20000millisecond to a shorter time
    gettimeofday(&start, NULL);
    while (1) {
     //printf("xxxxxxxxeee2");
        xmpp_run_once(ctx, 1);
        gettimeofday(&finish, NULL);
        msec = timevaldiff(&start, &finish);
        if (msec > expire_time) {
            break;
        }
        fflush(stdout);
    }
    //End:  the update function
    //////////////////////////////
    
    // write the xmpp sub msg
    xmpp_sub_file = fopen("/galileo/xmpp_sub.txt","wb+");
    if(xmpp_sub_file != NULL){
        fwrite(xmpp_sub_msg, sizeof(char), 6, xmpp_sub_file);
        fclose(xmpp_sub_file);
        printf("sub: %s\n",xmpp_sub_msg);
    }
    else{
        printf("no /galileo/xmpp_sub.txt\n");
    }
    /* release our connection and context */
    xmpp_conn_release(conn);
    xmpp_ctx_free(ctx);

    /* final shutdown of the library */
    xmpp_shutdown();

    return 1;
}
