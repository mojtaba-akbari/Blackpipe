package org.services;

import org.ServiceModel.ElementHolder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

/*
Input IO Controller is task for reading and best algorithm of Input data
This Controller should implement more to cover more taking input data like Socket, REST, DB Read Driver...
// AVOID    use CSVReader Or Any Others Class Because Less Of Performance And High Usage Of Memory //
 */

public class InputIO implements Runnable {
    private String address;

    private ProxyService proxyService;

    private File fileInput;
    private FileReader fileReader;
    private BufferedReader bufferedReader;

    private ConcurrentLinkedQueue primaryStorage;

    public InputIO(String address, ProxyService proxyService) throws FileNotFoundException {
        this.address = address;
        this.fileInput = new File(this.address); // File Pointer
        this.fileReader = new FileReader(fileInput); // java.io.FileReader
        this.bufferedReader = new BufferedReader(fileReader); // java.io.BufferedReader
        this.proxyService = proxyService;

        // This Line Maybe Has smiled on your lip :) But there is much difference in performance with high concept of low-latency develop //
        this.primaryStorage = proxyService.getPrimaryStorage();
    }

    // Just Use This Method For Sampling From File //
    public Scanner initPointerToFile() throws FileNotFoundException {
        // Beware Scanner Use Regx //
        Scanner scanner = new Scanner(new File(address));
        return scanner;
    }

    // Get Extension Of Input //
    public String getExtension() {
        int indexOf = address.lastIndexOf(".");
        return address.substring(indexOf, address.length() - indexOf);
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        Stream<String> lines = bufferedReader.lines(); // Stream read all lines

        // For Remain Order Set Id With Counter //
        AtomicLong counter = new AtomicLong(0);

        lines.forEach(line -> {
            primaryStorage.add(new ElementHolder(line, counter.getAndIncrement()));
        });

        long endTime = System.currentTimeMillis();

        System.out.println("That took " + (endTime - startTime) / 1000 + " seconds and generate " + counter.get() + " ID");

        proxyService.setProxyServiceState(ProxyService.STATE.INPUT_END); // SEND EVENT
    }
}
