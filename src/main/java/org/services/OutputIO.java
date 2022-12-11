package org.services;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/*
Output Class Do All Thing About Controlling OutPut
1- Control output source channel , you inject any address output / linux file / linux socket ...
2- Control output buffer , size , watermark
3- Just write on channel and writing is task of OS
4- Thread priority is norm , but thread act is slow because of Channel theory no need act much
 */

public class OutputIO implements Runnable {
    private final int timerChecking = 1000;
    private final String modeChannel = "rw";
    public int highRiskRemainingIndicator = 61644800; // 50 MB High Risk Remaining - this indicator pointed from other source , so do not used getter() because low performance
    public int remainingIndicator; // this indicator pointed from other source , so do not used getter() because low performance
    private ProxyService proxyService;
    private boolean isShutdown = false;
    private int waterMark;
    private int outputBufferSize;
    private ByteBuffer outputBuffer;
    private FileChannel outputChannel;

    public OutputIO(String address, int outputBufferSize, int waterMark, ProxyService proxyService) throws FileNotFoundException {
        // Open OutPut Channel File //
        outputChannel = new RandomAccessFile(address, modeChannel).getChannel();
        this.outputBufferSize = outputBufferSize;
        outputBuffer = ByteBuffer.allocate(this.outputBufferSize); // Allocate 2G
        outputBuffer.clear();
        this.waterMark = waterMark;
        this.proxyService = proxyService;

        // Calculate Watermark Before Start
        this.remainingIndicator = outputBufferSize - waterMark;
    }

    public ByteBuffer getOutputBuffer() {
        return outputBuffer;
    }

    public FileChannel getOutputChannel() {
        return outputChannel;
    }

    public void setShutdown(boolean shutdown) {
        isShutdown = shutdown;
    }

    @Override
    public void run() {

        while (!isShutdown) {
            try {
                // outputIO No Need Use All Cyclone
                Thread.sleep(timerChecking);
                // WaterMark before Limit
                // Set Position =0 and write data to the channel
                // Wait Signal Notify All
                synchronized (outputBuffer) {
                    if (outputBuffer.remaining() <= remainingIndicator) {
                        outputChannel.write(ByteBuffer.wrap(outputBuffer.array(), 0, outputBuffer.position()));
                        outputBuffer.clear();
                        // Notify All Thread That Waited For Writing On Buffer //
                        outputBuffer.notifyAll();
                    }
                }

                System.out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            synchronized (outputBuffer) {
                outputChannel.write(ByteBuffer.wrap(outputBuffer.array(), 0, outputBuffer.position()));
                outputChannel.force(true); // If Finish SIG come, ALL remain Data Write
                outputBuffer.clear();
            }

            System.out.println("END Page Writing (Buffer Position: " + outputBuffer.position() + " Channel Size: " + outputChannel.size() + ")");
            // Close Output //
            outputChannel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
