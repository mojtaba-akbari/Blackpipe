package org.services;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MonitorTask implements Runnable{
    private boolean isShutdown = false;
    private ConcurrentLinkedQueue<String> storage;

    private ByteBuffer outputBuffer;
    private FileChannel outputChannel;
    public MonitorTask(ConcurrentLinkedQueue<String> storage, ByteBuffer outputBuffer, FileChannel outputChannel) {
        this.storage = storage;
        this.outputBuffer=outputBuffer;
        this.outputChannel=outputChannel;
    }

    public void setShutdown(boolean shutdown) {
        isShutdown = shutdown;
    }

    @Override
    public void run() {
        while(!isShutdown){
            try {
                // Monitor No Need Use All Cyclone
                Thread.sleep(1000);
                // WaterMark On 200 MB before Limit
                // Set Position =0 and write data to the channel
                // Wait Signal Under 50 MB
                synchronized (outputBuffer) {
                    if (outputBuffer.remaining() <= (Integer.MAX_VALUE - 2 - 1932735283.2))
                    {
                        outputChannel.write(ByteBuffer.wrap(outputBuffer.array(), 0, outputBuffer.position()));
                        outputBuffer.clear();
                        // Notify All Thread That Waited For Writing On Buffer //
                        outputBuffer.notifyAll();
                    }
                }

                //System.out.println("Storage Size: "+storage.size()+" Buffer Position: "+outputBuffer.position()+" Channel Size: "+outputChannel.size());
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            synchronized (outputBuffer) {
                outputChannel.write(ByteBuffer.wrap(outputBuffer.array(),0,outputBuffer.position()));
                outputChannel.force(true); // If Finish SIG come, ALL remain Data Write
                outputBuffer.clear();
            }

            System.out.println("END Page Writing (Storage Size: "+storage.size()+" Buffer Position: "+outputBuffer.position()+" Channel Size: "+outputChannel.size()+")");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
