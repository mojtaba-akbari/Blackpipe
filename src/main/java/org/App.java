package org;

import org.services.ProxyService;


/**
 * BlackBase :) :| :)
 * Surly I am BEsT ;)
 */
public class App {

    private static ProxyService proxyService;

    public static void main(String[] args) throws
            ClassNotFoundException {

        if (args[0].equals("help")) {
            System.out.println("[RepositoryClass(Can be Empty)] [filterModel] [dataModel] [inputFileAddress] [outputFileAddress] [threadPoolSize] [bufferSize(byte)] [waterMark(byte)]");

            System.exit(0);
        }

        if (args.length < 8) {
            System.out.println("Please Provide All Args");
            System.exit(1);
        }

        String repositoryAddressArg = args[0];
        String filterModelArg = args[1];
        String dataModelArg = args[2];
        String inputAddressArg = args[3];
        String outputAddressArg = args[4];
        int poolSizeArg = Integer.valueOf(args[5]);
        int bufferSizeArg = Integer.valueOf(args[6]);
        int waterMarkArg = Integer.valueOf(args[7]);


        proxyService = new ProxyService(repositoryAddressArg, filterModelArg, dataModelArg, inputAddressArg, outputAddressArg, bufferSizeArg, waterMarkArg, poolSizeArg);
        Thread proxyServiceThread = new Thread(proxyService);
        proxyServiceThread.start();

        // SEND INIT EVENT //
        proxyService.setProxyServiceState(ProxyService.STATE.INIT);
    }
}
