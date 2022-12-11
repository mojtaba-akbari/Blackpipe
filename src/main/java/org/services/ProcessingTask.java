package org.services;

import org.ServiceModel.ElementHolder;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;

/*
   This Service Attaches To Thread Pool For Doing Task
   This Service Should Not Participate in The Inheritance
   For Better Performance Please Attach Any Data On Other Class To Pointer Specially Those That Need On Loop Performance
 */
public final class ProcessingTask implements Runnable {
    // Proxy Service
    ProxyService proxyService;

    // Sig Shutdown //
    private boolean shutdown = false;

    private Integer taskId = -1;

    private FilterApplicator filterApplicator;

    private ConcurrentLinkedQueue<ElementHolder> primaryStorage;

    private ByteBuffer outPutBuffer;

    private int highRiskIndicator;

    public ProcessingTask(ProxyService proxyService, Integer taskId) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, MalformedURLException, ClassNotFoundException, FileNotFoundException {
        this.proxyService = proxyService;

        this.primaryStorage = this.proxyService.getPrimaryStorage(); // take for push
        this.outPutBuffer = this.proxyService.getOutputIO().getOutputBuffer(); // take for write
        this.filterApplicator = this.proxyService.createApplicator(); // take for checking indicator algorithm
        this.highRiskIndicator = this.proxyService.getOutputIO().highRiskRemainingIndicator; // take high risk
        this.taskId = taskId;
    }

    public void setShutdown(boolean shutdown) {
        this.shutdown = shutdown;
    }

    public Integer getTaskId() {
        return taskId;
    }

    @Override
    public void run() {
        // Peek One Item From Storage //
        // Shutdown Thread Just When Queue is Empty And The Empty Signal Come //
        ElementHolder item = null;
        //
        while (!shutdown) {
            item = primaryStorage.poll();
            if (item != null) {
                try {
                    ElementHolder eh = filterApplicator.execute(item);

                    if (eh == null) continue; // Maybe Filter Pass Null Element For This Item //

                    byte[] buff = eh.message.getBytes(StandardCharsets.UTF_8);


                    synchronized (outPutBuffer) {
                        // Check Buffer Size , Because That is Maybe Need Cleared Before OverFlow Exception //
                        // Warn Output Controller On Mark //
                        if (outPutBuffer.remaining() <= highRiskIndicator) {
                            System.out.println("Wait On Buffer By Task: " + getTaskId());
                            outPutBuffer.wait();
                        }

                        outPutBuffer.put(ByteBuffer.wrap(
                                buff
                        )); // OUTPUT //
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        System.out.println("Task " + taskId + " Closed");
    }
}
